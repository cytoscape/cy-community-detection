package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.work.TaskMonitor;

import com.google.gson.Gson;

public class CDRestClient {

	private CDRestClient() {
	}

	private static class SingletonHelper {
		private static final CDRestClient INSTANCE = new CDRestClient();
	}

	public static CDRestClient getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public String postEdgeList(String algorithm, String graphDirected, ByteArrayOutputStream outStream)
			throws Exception {

		File tmpFile = File.createTempFile("edgeList", ".txt");
		outStream.writeTo(new FileOutputStream(tmpFile));
		FileBody sbFile = new FileBody(tmpFile, ContentType.TEXT_PLAIN);
		StringBody sbAlgorithm = new StringBody(algorithm, ContentType.TEXT_PLAIN);
		StringBody sbDirected = new StringBody(graphDirected, ContentType.TEXT_PLAIN);
		StringBody sbRoot = new StringBody("Root", ContentType.TEXT_PLAIN);

		HttpEntity multiPartBody = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addPart("algorithm", sbAlgorithm).addPart("edgefile", sbFile).addPart("graphdirected", sbDirected)
				.addPart("rootnetwork", sbRoot).build();

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost("http://ddot-stage.ucsd.edu/communitydetection/cd/v1");
		postRequest.setEntity(multiPartBody);
		HttpResponse httpPostResponse = null;
		for (int count = 0; count < 3; count++) {
			httpPostResponse = client.execute(postRequest);
			if (202 == httpPostResponse.getStatusLine().getStatusCode()) {
				break;
			}
		}
		tmpFile.delete();
		if (202 != httpPostResponse.getStatusLine().getStatusCode()) {
			throw new Exception(httpPostResponse.getStatusLine().getReasonPhrase());
		}
		return httpPostResponse.getFirstHeader("Location").getValue();
	}

	public Result getEdgeList(String resultURI, TaskMonitor taskMonitor) throws Exception {
		Result cdResult = null;
		int waitTime = 1000;
		int retryCount = 100;
		HttpClient client = HttpClientBuilder.create().build();
		for (int count = 0; count < retryCount; count++) {
			Thread.sleep(waitTime);
			HttpResponse httpGetResponse = client.execute(new HttpGet(resultURI));
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpGetResponse.getEntity().getContent()));
			cdResult = new Gson().fromJson(reader, Result.class);
			if (cdResult.getStatus().equals("done")) {
				break;
			}
			if (cdResult.getStatus().equals("error")) {
				throw new Exception(cdResult.getEdgeList());
			}
			float progressRatio = retryCount;
			taskMonitor.setProgress(0.1 + (0.8 * (float) (count + 1)) / progressRatio);
		}
		if (!cdResult.getStatus().equals("done")) {
			throw new Exception(cdResult.getEdgeList());
		}
		return cdResult;
	}
}
