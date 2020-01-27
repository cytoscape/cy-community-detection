package org.cytoscape.app.communitydetection;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class PropertiesHelperTest {
    
    @Test
    public void testGettersAndSetters(){
	PropertiesHelper ph = PropertiesHelper.getInstance();
	assertNull(ph.getBaseurl());
	assertNull(ph.getThreadcount());
	assertEquals(0, ph.getHttpSocketTimeoutMillis());
	assertEquals(0, ph.getHttpConnectTimeoutMillis());
	assertEquals(0, ph.getHttpConnectionRequestTimeoutMillis());
	assertEquals(0, ph.getPollingMaxRetryCount());
	assertEquals(0, ph.getSubmitRetryCount());
	assertEquals(0, ph.getCommunityDetectionTimeoutMillis());
	assertEquals(0, ph.getFunctionalEnrichmentTimeoutMillis());
			
	ph.setBaseurl("url");
	ph.setThreadcount("thread");
	ph.setHttpSocketTimeoutMillis(1);
	ph.setHttpConnectTimeoutMillis(2);
	ph.setHttpConnectionRequestTimeoutMillis(3);
	ph.setPollingMaxRetryCount(4);
	ph.setSubmitRetryCount(5);
	ph.setCommunityDetectionTimeoutMillis(6);
	ph.setFunctionalEnrichmentTimeoutMillis(7);
	assertEquals("url", ph.getBaseurl());
	assertEquals("thread", ph.getThreadcount());
	assertEquals(1, ph.getHttpSocketTimeoutMillis());
	assertEquals(2, ph.getHttpConnectTimeoutMillis());
	assertEquals(3, ph.getHttpConnectionRequestTimeoutMillis());
	assertEquals(4, ph.getPollingMaxRetryCount());
	assertEquals(5, ph.getSubmitRetryCount());
	assertEquals(6, ph.getCommunityDetectionTimeoutMillis());
	assertEquals(7, ph.getFunctionalEnrichmentTimeoutMillis());
    }
}
