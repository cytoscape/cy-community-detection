/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.app.communitydetection.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;

/**
 *
 * @author churas
 */
public interface HttpClientFactory {
    
    public HttpClient getHttpClient(RequestConfig config);
}
