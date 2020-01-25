/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.app.communitydetection.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author churas
 */
public class HttpClientFactoryImpl implements HttpClientFactory {

    @Override
    public HttpClient getHttpClient(RequestConfig config) {
	if (config == null){
	    return HttpClientBuilder.create().build();
	}
	return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }
}
