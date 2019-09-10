package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;
import org.ndexbio.communitydetection.rest.model.Task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

public class CDRestClient {

	private final static String URI = "http://ddot-stage.ucsd.edu/cd/communitydetection/v1";

	private final ObjectMapper mapper;
	private boolean isTaskCanceled;

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

	private CloseableHttpClient getClient(int timeout) {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}

	public String postCDData(String algorithm, Boolean graphDirected, String data) throws Exception {

		CommunityDetectionRequest request = new CommunityDetectionRequest();
		request.setAlgorithm(algorithm);
		request.setData(new TextNode(data));
		request.setGraphdirected(graphDirected);
		request.setIpAddress(InetAddress.getLocalHost().getHostAddress());
		StringEntity body = new StringEntity(mapper.writeValueAsString(request));

		CloseableHttpClient client = getClient(10);
		HttpPost postRequest = new HttpPost(URI);
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
			throw new Exception("Error: " + httpPostResponse.getStatusLine().getStatusCode());
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpPostResponse.getEntity().getContent()));
		Task serviceTask = mapper.readValue(reader, Task.class);
		System.out.println("Task ID: " + serviceTask.getId());
		return serviceTask.getId();
	}

	public void deleteTask(String taskId) throws Exception {
		CloseableHttpClient client = getClient(10);
		HttpResponse deleteResponse = client.execute(new HttpDelete(URI + "/" + taskId));
		if (200 != deleteResponse.getStatusLine().getStatusCode()) {
			System.out.println("Could not delete task: " + taskId);
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
			HttpResponse httpGetResponse = client.execute(new HttpGet(URI + "/" + taskId));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = mapper.readValue(reader, CommunityDetectionResult.class);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS)) {
				break;
			}
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
				throw new Exception("Error fetching the result!");
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
			HttpResponse httpGetResponse = client.execute(new HttpGet(URI + "/" + taskId));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = mapper.readValue(reader, CommunityDetectionResult.class);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS)) {
				break;
			}
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
				throw new Exception("Error fetching the result!");
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

	public void setTaskCanceled(boolean isCanceled) {
		isTaskCanceled = isCanceled;
	}
}
