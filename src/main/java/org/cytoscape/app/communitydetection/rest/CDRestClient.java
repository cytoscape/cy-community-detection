package org.cytoscape.app.communitydetection.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

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

	@SuppressWarnings("unchecked")
	public void postEdgeList(String edgeList) throws ClientProtocolException, IOException {

		JSONObject jsonInput = new JSONObject();
		jsonInput.put("algorithm", "infomap");
		jsonInput.put("undirected", "undirected");
		jsonInput.put("edge_list", edgeList);
		StringEntity se = new StringEntity(jsonInput.toJSONString());

		HttpPost postRequest = new HttpPost("http://127.0.0.1:5000/cd");
		postRequest.addHeader("accept", "application/json");
		postRequest.setEntity(se);

		HttpResponse httpResponse = client.execute(postRequest);
		BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
		StringBuffer response = new StringBuffer();
		String temp;
		while ((temp = rd.readLine()) != null) {
			response.append(temp);
		}
		System.out.println(response.toString());
	}

}
