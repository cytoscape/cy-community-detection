package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;

import com.google.gson.Gson;

public class CDRestClient_Java {

	private final Gson gson;
	private boolean isTaskCanceled;

	private CDRestClient_Java() {
		gson = new Gson();
		isTaskCanceled = false;
	}

	private static class SingletonHelper {
		private static final CDRestClient_Java INSTANCE = new CDRestClient_Java();
	}

	public static CDRestClient_Java getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public String postEdgeList(String algorithm, Boolean graphDirected, String edgeList) throws Exception {

		CommunityDetectionRequest request = new CommunityDetectionRequest();
		request.setAlgorithm(algorithm);
		request.setEdgeList(edgeList);
		request.setGraphdirected(graphDirected);
		request.setIpAddress(InetAddress.getLocalHost().getHostAddress());
		StringEntity body = new StringEntity(gson.toJson(request));

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost("http://ddot.ucsd.edu/cd/communitydetection/v1");
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
			throw new Exception(httpPostResponse.getStatusLine().getReasonPhrase());
		}
		return httpPostResponse.getFirstHeader("Location").getValue();
	}

	public CommunityDetectionResult getEdgeList(String resultURI, TaskMonitor taskMonitor) throws Exception {
		CommunityDetectionResult cdResult = null;
		int waitTime = 1000;
		int retryCount = 100;
		HttpClient client = HttpClientBuilder.create().build();
		for (int count = 0; count < retryCount; count++) {
			Thread.sleep(waitTime);
			HttpResponse httpGetResponse = client.execute(new HttpGet(resultURI));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = new Gson().fromJson(reader, CommunityDetectionResult.class);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS) || isTaskCanceled) {
				break;
			}
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
				throw new Exception(cdResult.getMessage());
			}
			float progressRatio = retryCount;
			taskMonitor.setProgress(0.1 + (0.8 * (float) (count + 1)) / progressRatio);
		}
		if (!cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS)) {
			throw new Exception(cdResult.getMessage());
		}
		return cdResult;
	}

	public void setTaskCanceled(boolean isCanceled) {
		isTaskCanceled = isCanceled;
	}
}
