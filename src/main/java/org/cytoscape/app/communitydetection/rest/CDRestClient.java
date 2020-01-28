package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithms;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;
import org.ndexbio.communitydetection.rest.model.Task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API client for CD service. Implements GET, POST and DELETE.
 *
 */
public class CDRestClient {

	private final static Logger LOGGER = LoggerFactory.getLogger(CDRestClient.class);
	private final ObjectMapper mapper;
	private boolean isTaskCanceled;
	private HttpClientFactory _httpClientFactory;
	
	/**
	 * Cached result of calling {@link #getAlgorithms(boolean)}
	 */
	private CommunityDetectionAlgorithms _cachedCommunityDetectionAlgorithms;
	/**
	 * For failed tasks this variable sets the number of characters
	 * to get from
	 * {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionResult#getResult()}
	 */
	protected final static int TRUNCATE_ERROR_MESSAGE_RESULT_LEN = 50;

	private CDRestClient() {
		mapper = new ObjectMapper();
		isTaskCanceled = false;
		_httpClientFactory = new HttpClientFactoryImpl();
	}

	private static class SingletonHelper {
		private static final CDRestClient INSTANCE = new CDRestClient();
	}

	public static CDRestClient getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	/**
	 * Sets alternate {@link org.apache.http.client.HttpClient}
	 * to be used by this object. To use the default set this
	 * to {@code null}
	 * @param client Alternate client to use
	 */
	protected void setAlternateHttpClientFactory(HttpClientFactory clientFactory){
	    _httpClientFactory = clientFactory;
	}
	
	/**
	 * 
	 * @param algorithm name of the algorithm to run as set in 
	 *                  {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm#getName()}
	 * @param customParameters Map of custom parameters with set to name of parameter and value set to the value of parameter
	 * @param data Data in String format to pass to CD REST Service. This 
	 * @return
	 * @throws CDRestClientException higher level error with CD REST Service
	 * @throws IOException low level http error
	 * @throws IllegalArgumentException if algorithm is null
	 */
	public String postCDData(final String algorithm,
			Map<String,String> customParameters,
			final String data) throws CDRestClientException, IOException {

		if (algorithm == null){
			throw new IllegalArgumentException("algorithm cannot be null");
		}
		CommunityDetectionRequest request = new CommunityDetectionRequest();
		request.setAlgorithm(algorithm);
		request.setData(new TextNode(data));
		if (customParameters != null){
		    request.setCustomParameters(customParameters);
		}
		StringEntity body = new StringEntity(mapper.writeValueAsString(request));

		HttpClient client = getClient();
		HttpPost postRequest = new HttpPost(getBaseurl());
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.setEntity(body);

		HttpResponse httpPostResponse = null;
		int statusCode = 0;
		LOGGER.debug(data);
		for (int count = 0; count < PropertiesHelper.getInstance().getSubmitRetryCount(); count++) {
			if (isTaskCanceled == true){
				return null;
			}
			httpPostResponse = client.execute(postRequest);
			
			statusCode = httpPostResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_ACCEPTED) {
				break;
			} else {
				LOGGER.debug("Try # " + Integer.toString(count+1) +
						" received HTTP CODE: " + Integer.toString(statusCode) +
						" when attempting to submit " + algorithm + " task");
			}
		}
		if (statusCode != HttpStatus.SC_ACCEPTED) {
		    throw new CDRestClientException("POST call to " + getBaseurl() +
					" failed. Error code: "
					+ statusCode,
					this.getErrorResponse(httpPostResponse));
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpPostResponse.getEntity().getContent()));
		Task serviceTask = mapper.readValue(reader, Task.class);
		LOGGER.debug("Task ID: " + serviceTask.getId());
		return serviceTask.getId();
	}

	/**
	 * Delete task with id passed in by making delete request to CD Service. 
	 * If status code is not {@link org.apache.http.HttpStatus.SC_OK} this method
	 * will log the code returned, but will not complain
	 * 
	 * @param taskId id of task to delete
	 * @throws IllegalArgumentException if taskId is null
	 * @throws Exception If the http client raises an exception
	 */
	public void deleteTask(String taskId) throws IllegalArgumentException, IOException {
		if (taskId == null){
			throw new IllegalArgumentException("task id cannot be null");
		}
		HttpClient client = getClient();
		HttpDelete hdel = new HttpDelete(getBaseurl() + "/" + taskId);
		HttpResponse deleteResponse = client.execute(hdel);
		if (deleteResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			LOGGER.info("Could not delete task: " + taskId);
		}
	}
	
	public CommunityDetectionResult getCDResult(final String taskId, int totalRuntime) throws CDRestClientException, IOException {
		return getCDResult(taskId, null, 0.0f, 0.0f, totalRuntime);
	}

	/**
	 * Polls CD Service checking for completion of task specified by {@code taskId}
	 * using {@link org.cytoscape.app.communitydetection.PropertiesHelper#getPollingIntervalTimeMillis()} 
	 * @param taskId
	 * @param taskMonitor
	 * @param currentProgress
	 * @param totalProgress
	 * @param totalRuntime Maximum time in milliseconds to wait for completion of task
	 * @return
	 * @throws IllegalArgumentException if taskId is null
	 * @throws CDRestClientException if task fails or if there is a high level error from CD Service
	 * @throws IOException if there is a low level http error
	 */
	public CommunityDetectionResult getCDResult(String taskId, TaskMonitor taskMonitor, float currentProgress,
			float totalProgress, int totalRuntime) throws CDRestClientException, IOException {

		if (taskId == null){
			throw new IllegalArgumentException("task id cannot be null");
		}
		CommunityDetectionResult cdResult = null;
		int waitTime = PropertiesHelper.getInstance().getPollingIntervalTimeMillis();
		long retryLimit = -1;
		
		if (totalRuntime == 0){
			retryLimit = 1;
		}else if (totalRuntime > 0){
			retryLimit = Math.round((double)totalRuntime /(double)waitTime);
		}
		
		HttpClient client = getClient();
		int retryCount = 0;
		
		float progressScaler = 0.0f;
		if (taskMonitor != null){
			progressScaler = (totalProgress - currentProgress)/100.0f;
		}
		LOGGER.debug("For Task: " + taskId + " RetryCount => " + Integer.toString(retryCount) + 
				     " RetryLimit => " + Long.toString(retryLimit));
		while(retryLimit < 0 || retryCount < retryLimit){
			this.threadSleep(waitTime);
			if (isTaskCanceled) {
				LOGGER.debug("User canceled task: " + taskId);
				silentlyDeleteTask(taskId);
				return null;
			}
			cdResult = this.getCDResult(client, taskId);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS)) {
				silentlyDeleteTask(taskId);
				return cdResult;
			}

			if (taskMonitor != null){
				taskMonitor.setProgress(currentProgress + ((float)cdResult.getProgress()*progressScaler));	
			}
			retryCount++;
		}
		silentlyDeleteTask(taskId);
		throw new CDRestClientException("Request timed out. This could be due to "
				+ "heavy server load or too large of a task to run", cdResult);
	}

	public CommunityDetectionAlgorithms getAlgorithms(boolean refreshCache) throws CDRestClientException, IOException {
		if (this._cachedCommunityDetectionAlgorithms != null && refreshCache == false){
			return this._cachedCommunityDetectionAlgorithms;
		}
		HttpClient client = getClient();
		HttpResponse httpGetResponse = client.execute(new HttpGet(getBaseurl() + "/" + "algorithms"));
		int statuscode = httpGetResponse.getStatusLine().getStatusCode();
		if (statuscode != HttpStatus.SC_OK){
			throw new CDRestClientException("Unable to get list of "
					+ "algorithms from CD Service (HTTP Status Code: " +
					Integer.toString(statuscode) + ")",
					this.getErrorResponse(httpGetResponse));
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
		this._cachedCommunityDetectionAlgorithms = mapper.readValue(reader, CommunityDetectionAlgorithms.class);
		return this._cachedCommunityDetectionAlgorithms;
	}

	public boolean getIsTaskCanceled() {
		return isTaskCanceled;
	}

	public void setTaskCanceled(boolean isCanceled) {
		isTaskCanceled = isCanceled;
	}

	/**
	 * Uses {@link #_httpClientFactory} to create {@link HttpClient} with
	 * configuration passed in
	 * @param timeout - timeout in seconds used for connection, connection request,
	 *                  and socket timeout
	 * @return 
	 */
	private HttpClient getClient() {
		PropertiesHelper pHelper = PropertiesHelper.getInstance();
	    RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(pHelper.getHttpConnectTimeoutMillis())
			    .setConnectionRequestTimeout(pHelper.getHttpConnectionRequestTimeoutMillis())
				.setSocketTimeout(pHelper.getHttpSocketTimeoutMillis()).build();
		if (_httpClientFactory == null){
			throw new NullPointerException("HttpClientFactory is null");
		}
	    return _httpClientFactory.getHttpClient(config);
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
	
	/**
	 * Given an http response that is assumed to have failed
	 * attempt to extract an
	 * {@link org.ndexbio.communitydetection.rest.model.ErrorResponse}
	 * from the JSON in the content
	 * @param response
	 * @return Error Response or null if not found
	 */
	protected ErrorResponse getErrorResponse(HttpResponse response){
		if (response == null){
			LOGGER.info("null response passed in for analysis");
			return null;
		}
		try {
			HttpEntity entity = response.getEntity();
			if (entity == null){
				LOGGER.info("response entity is null, very weird");
				return null;
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			return mapper.readValue(reader, ErrorResponse.class);
		} catch(IOException ex){
			LOGGER.info("caught exception trying to parse error response from server", ex);
		}
		return null;
	}
	
	/**
	 * Tells CD Service to delete task, but if there is any error just log it
	 * at warning level and return
	 * @param taskId 
	 */
	private void silentlyDeleteTask(final String taskId){
		try {
			deleteTask(taskId);
		} catch(IllegalArgumentException iae){
			LOGGER.warn("Received invalid taskid error trying to delete task", iae);
		} catch(IOException io){
			LOGGER.warn("Received error trying to delete task: " + taskId, io);
		}
	}

	/**
	 * Tell this thread to sleep for {@code sleepTimeInMillis}. This 
	 * method catches {@link java.lang.InterruptedException} and ignores
	 * it
	 * @param sleepTimeInMillis 
	 */
	private void threadSleep(long sleepTimeInMillis){
		try {
			Thread.sleep(sleepTimeInMillis);
		} catch(InterruptedException ie){
				
		}
	}
	
	/**
	 * Low level call that makes a get request to obtain {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionResult}

     * @param client HTTP Client already configured
	 * @param taskId id of task
	 * @return {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionResult} from CD Service
	 * @throws CDRestClientException If CD Service said the request failed or other higher level error
	 * @throws IOException If there was a lower level HTTP error
	 */
	private CommunityDetectionResult getCDResult(HttpClient client, final String taskId) throws CDRestClientException, IOException{
		HttpResponse httpGetResponse = client.execute(new HttpGet(getBaseurl() + "/" + taskId));
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
		CommunityDetectionResult cdResult = mapper.readValue(reader, CommunityDetectionResult.class);
		if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
			throw new CDRestClientException("Error fetching the result:" +
				getErrorMessageFromResult(cdResult), cdResult);
		}
		return cdResult;
	}
	
	private String getBaseurl() {
		String baseURL = PropertiesHelper.getInstance().getBaseurl();
		if (baseURL == null){
			throw new NullPointerException("REST Service URL is null");
		}
		return baseURL;
	}
}
