package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.JsonObject;

public class CDRestClient {

	HttpClient client;

	private CDRestClient() {
		client = HttpClientBuilder.create().build();
	}

	private static class SingletonHelper {
		private static final CDRestClient INSTANCE = new CDRestClient();
	}

	public static CDRestClient getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public void postEdgeList(String edgeList) throws ClientProtocolException, IOException {

		JsonObject jsonInput = new JsonObject();
		jsonInput.addProperty("algorithm", "infomap");
		jsonInput.addProperty("type", "undirected");
		jsonInput.addProperty("edge_list", edgeList);
		StringEntity se = new StringEntity(jsonInput.toString());
		HttpPost postRequest = new HttpPost("http://127.0.0.1:5000/cd");
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.setEntity(se);

		HttpResponse httpResponse = client.execute(postRequest);
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
		StringBuffer response = new StringBuffer();
		String temp;
		while ((temp = reader.readLine()) != null) {

			response.append(temp);
		}
		FileOutputStream outStream = new FileOutputStream(
				"C:\\Workspace\\Cytoscape\\cy-community-detection\\test\\edge_list.txt");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
		List<String> responseEdgeList = Arrays.asList(response.toString().replace('"', ' ').trim().split(";"));
		for (String edge : responseEdgeList) {
			writer.write(edge.replace(',', '\t'));
			writer.newLine();
		}
		writer.close();
	}

}
