package org.cytoscape.app.communitydetection.rest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.junit.Test;
import static org.junit.Assert.*;



/**
 *
 * @author churas
 */
public class HttpClientFactoryImplTest {
    
    @Test
    public void testGetClient(){
	HttpClientFactoryImpl fac = new HttpClientFactoryImpl();
	HttpClient client = fac.getHttpClient(null);
	assertNotNull("client is null", client);
	
	RequestConfig config = RequestConfig.custom().setConnectTimeout(45).build();
	client = fac.getHttpClient(config);
	assertNotNull("client is null", client);
    }
}
