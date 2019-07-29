package org.cytoscape.app.communitydetection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.JsonObject;

public class TestMain {

	public static void main(String[] args) throws Exception {
		JsonObject jsonInput = new JsonObject();
		jsonInput.addProperty("algorithm", "infomap");
		jsonInput.addProperty("type", "undirected");
		jsonInput.addProperty("edge_list", "1\t2\n1\t3\n2\t3");
		StringEntity se = new StringEntity(jsonInput.toString());
		HttpPost postRequest = new HttpPost("http://127.0.0.1:5000/cd");
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.setEntity(se);

		HttpClient client = HttpClientBuilder.create().build();
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
