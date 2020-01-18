package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithms;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;
import org.ndexbio.communitydetection.rest.model.Task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API client for CD service. Implements GET, POST and DELETE.
 *
 */
public class CDRestClient {

	private final static Logger _logger = LoggerFactory.getLogger(CDRestClient.class);
	private final ObjectMapper mapper;
	private boolean isTaskCanceled;
	
	/**
	 * For failed tasks this variable sets the number of characters
	 * to get from
	 * {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionResult#getResult()}
	 */
	protected final static int TRUNCATE_ERROR_MESSAGE_RESULT_LEN = 50;
	
	private List<CommunityDetectionAlgorithm> algorithms;

	private CDRestClient() {
		mapper = new ObjectMapper();
		isTaskCanceled = false;
	}

	private static class SingletonHelper {
		private static final CDRestClient INSTANCE = new CDRestClient();
	}

	public static CDRestClient getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private String getBaseurl() {
		return PropertiesHelper.getInstance().getBaseurl();
	}

	public String postCDData(String algorithm, Map<String,String> customParameters, String data) throws Exception {
		CommunityDetectionRequest request = null;

		request = new CommunityDetectionRequest();
		request.setAlgorithm(algorithm);
		request.setData(new TextNode(data));
		if (customParameters != null){
		    request.setCustomParameters(customParameters);
		}
		StringEntity body = new StringEntity(mapper.writeValueAsString(request));

		CloseableHttpClient client = getClient(10);
		HttpPost postRequest = new HttpPost(getBaseurl());
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.setEntity(body);

		HttpResponse httpPostResponse = null;
		for (int count = 0; count < 2; count++) {
			httpPostResponse = client.execute(postRequest);
			if (202 == httpPostResponse.getStatusLine().getStatusCode() || isTaskCanceled) {
				break;
			}
		}
		if (202 != httpPostResponse.getStatusLine().getStatusCode()) {
		    String additionalInfo = "";
		    try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpPostResponse.getEntity().getContent()));
			ErrorResponse eObj = mapper.readValue(reader, ErrorResponse.class);
			additionalInfo = " : " + eObj.getMessage() + " (" + eObj.getDescription() + ")";
		    } catch(Exception subex){
			
		    }
		    throw new Exception("POST call to " + getBaseurl() + " failed. Error code: "
					+ httpPostResponse.getStatusLine().getStatusCode() + additionalInfo);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpPostResponse.getEntity().getContent()));
		Task serviceTask = mapper.readValue(reader, Task.class);
		_logger.debug("Task ID: " + serviceTask.getId());
		return serviceTask.getId();
	}

	public void deleteTask(String taskId) throws Exception {
		CloseableHttpClient client = getClient(10);
		HttpResponse deleteResponse = client.execute(new HttpDelete(getBaseurl() + "/" + taskId));
		if (200 != deleteResponse.getStatusLine().getStatusCode()) {
			_logger.info("Could not delete task: " + taskId);
		}
	}

	public CommunityDetectionResult getCDResult(String taskId, Integer totalRuntime) throws Exception {
		CommunityDetectionResult cdResult = null;
		int waitTime = 1000;
		CloseableHttpClient client = getClient(totalRuntime);
		for (int count = 0; count < totalRuntime; count++) {
			Thread.sleep(waitTime);
			if (isTaskCanceled) {
				deleteTask(taskId);
				break;
			}
			HttpResponse httpGetResponse = client.execute(new HttpGet(getBaseurl() + "/" + taskId));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = mapper.readValue(reader, CommunityDetectionResult.class);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS)) {
				break;
			}
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
				throw new Exception("Error fetching the result" +
					getErrorMessageFromResult(cdResult));
			}
		}
		if (cdResult != null
				&& !(cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS) || isTaskCanceled)) {
			throw new Exception("Request timed out!");
		}
		return cdResult;
	}

	public CommunityDetectionResult getCDResult(String taskId, TaskMonitor taskMonitor, Float currentProgress,
			Float totalProgress, Integer totalRuntime) throws Exception {
		CommunityDetectionResult cdResult = null;
		int waitTime = 1000;
		CloseableHttpClient client = getClient(totalRuntime);
		for (int count = 0; count < totalRuntime; count++) {
			Thread.sleep(waitTime);
			if (isTaskCanceled) {
				deleteTask(taskId);
				break;
			}
			HttpResponse httpGetResponse = client.execute(new HttpGet(getBaseurl() + "/" + taskId));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = mapper.readValue(reader, CommunityDetectionResult.class);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS)) {
				break;
			}
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
				throw new Exception("Error fetching the result" +
					getErrorMessageFromResult(cdResult));
			}
			float progressRatio = totalRuntime;
			taskMonitor.setProgress(currentProgress + (totalProgress * (float) (count + 1)) / progressRatio);
		}
		if (cdResult != null
				&& !(cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS) || isTaskCanceled)) {
			throw new Exception("Request timed out!");
		}
		return cdResult;
	}

	public List<CommunityDetectionAlgorithm> getAlgorithms() throws Exception {
		if (algorithms != null) {
			return algorithms;
		}
		algorithms = new ArrayList<>();
		CloseableHttpClient client = getClient(10);
		HttpResponse httpGetResponse = client.execute(new HttpGet(getBaseurl() + "/" + "algorithms"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
		CommunityDetectionAlgorithms algos = mapper.readValue(reader, CommunityDetectionAlgorithms.class);
		for (CommunityDetectionAlgorithm algo : algos.getAlgorithms().values()) {
			algorithms.add(algo);
		}
		return algorithms;
	}

	public List<CommunityDetectionAlgorithm> getAlgorithmsByType(String inputType) throws Exception {
		List<CommunityDetectionAlgorithm> algos = new ArrayList<>();
		for (CommunityDetectionAlgorithm algo : getAlgorithms()) {
			if (algo.getInputDataFormat().equalsIgnoreCase(inputType)) {
				algos.add(algo);
			}
		}
		return algos;
	}

	public boolean getIsTaskCanceled() {
		return isTaskCanceled;
	}

	public void setTaskCanceled(boolean isCanceled) {
		isTaskCanceled = isCanceled;
	}

	private CloseableHttpClient getClient(int timeout) {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}
	
	/**
	 * Gets the first 50 characters from result if its not null and appends
	 * any text from 
	 * {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionResult#getMessage()}
	 * @param cdResult
	 * @return 
	 */
	protected String getErrorMessageFromResult(CommunityDetectionResult cdResult){
	    if (cdResult == null){
		return "";
	    }
	    String errMsg = " : ";
	    if (cdResult.getResult() != null){
		if (cdResult.getResult().isTextual()){
		    if (cdResult.getResult().asText().length() > TRUNCATE_ERROR_MESSAGE_RESULT_LEN){
			errMsg = errMsg + cdResult.getResult().asText().substring(0,
			        TRUNCATE_ERROR_MESSAGE_RESULT_LEN);
		    } else {
			errMsg = errMsg + cdResult.getResult().asText();
		    }
		}
	    }
	    if (cdResult.getMessage() != null){
		errMsg = errMsg + " : " + cdResult.getMessage();
	    }
	    return errMsg;
	}
}
