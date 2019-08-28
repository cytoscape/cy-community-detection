package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

public class CDRestClient {

	private final static int RETRY_COUNT = 100;

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

	public String postCDData(String algorithm, Boolean graphDirected, String data) throws Exception {

		CommunityDetectionRequest request = new CommunityDetectionRequest();
		request.setAlgorithm(algorithm);
		request.setData(new TextNode(data));
		request.setGraphdirected(graphDirected);
		request.setIpAddress(InetAddress.getLocalHost().getHostAddress());
		StringEntity body = new StringEntity(mapper.writeValueAsString(request));

		int timeout = 10;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		HttpPost postRequest = new HttpPost("http://ddot-stage.ucsd.edu/cd/communitydetection/v1");
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

	public CommunityDetectionResult getCDResult(String resultURI, Integer totalRuntime) throws Exception {
		CommunityDetectionResult cdResult = null;
		int timeout = totalRuntime;
		int waitTime = totalRuntime * 1000 / RETRY_COUNT;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		for (int count = 0; count < RETRY_COUNT; count++) {
			Thread.sleep(waitTime);
			HttpResponse httpGetResponse = client.execute(new HttpGet(resultURI));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = mapper.readValue(reader, CommunityDetectionResult.class);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS) || isTaskCanceled) {
				break;
			}
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
				throw new Exception(cdResult.getMessage());
			}
		}
		if (!cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS)) {
			throw new Exception(cdResult.getMessage());
		}
		return cdResult;
	}

	public CommunityDetectionResult getCDResult(String resultURI, TaskMonitor taskMonitor, Float currentProgress,
			Float totalProgress, Integer totalRuntime) throws Exception {
		CommunityDetectionResult cdResult = null;
		int timeout = totalRuntime;
		int waitTime = totalRuntime * 1000 / RETRY_COUNT;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		for (int count = 0; count < RETRY_COUNT; count++) {
			Thread.sleep(waitTime);
			HttpResponse httpGetResponse = client.execute(new HttpGet(resultURI));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = mapper.readValue(reader, CommunityDetectionResult.class);
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.COMPLETE_STATUS) || isTaskCanceled) {
				break;
			}
			if (cdResult.getStatus().equals(CommunityDetectionResultStatus.FAILED_STATUS)) {
				throw new Exception(cdResult.getMessage());
			}
			float progressRatio = RETRY_COUNT;
			taskMonitor.setProgress(currentProgress + (totalProgress * (float) (count + 1)) / progressRatio);
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
