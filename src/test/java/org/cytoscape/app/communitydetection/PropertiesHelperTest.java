package org.cytoscape.app.communitydetection;

import java.util.Properties;
import org.cytoscape.app.communitydetection.hierarchy.LauncherDialog;
import org.cytoscape.app.communitydetection.util.AppUtils;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class PropertiesHelperTest {
    
	@Test
	public void testGetPropertyAsIntNullProperties(){
		PropertiesHelper ph = PropertiesHelper.getInstance();

		assertEquals(25, ph.getPropertyAsInt(null, "foo", 25));
	}
	@Test
	public void testGetPropertyAsIntEmptyString(){
		PropertiesHelper ph = PropertiesHelper.getInstance();
		Properties props = new Properties();
		props.setProperty("foo","   ");
		assertEquals(25, ph.getPropertyAsInt(props, "foo", 25));
	}
    @Test
    public void testGettersAndSetters(){
		PropertiesHelper ph = PropertiesHelper.getInstance();

		ph.setAppName("appy");
		ph.setAppVersion("version");
		ph.setBaseurl("url");
		ph.setThreadcount("thread");
		ph.setHttpSocketTimeoutMillis(1);
		ph.setHttpConnectTimeoutMillis(2);
		ph.setHttpConnectionRequestTimeoutMillis(3);
		ph.setSubmitRetryCount(5);
		ph.setCommunityDetectionTimeoutMillis(6);
		ph.setFunctionalEnrichmentTimeoutMillis(7);
		assertEquals("appy", ph.getAppName());
		assertEquals("version", ph.getAppVersion());
		assertEquals("url", ph.getBaseurl());
		assertEquals("url", ph.getBaseurlHostNameOnly());
		
		assertEquals("thread", ph.getThreadcount());
		assertEquals(1, ph.getHttpSocketTimeoutMillis());
		assertEquals(2, ph.getHttpConnectTimeoutMillis());
		assertEquals(3, ph.getHttpConnectionRequestTimeoutMillis());
		assertEquals(5, ph.getSubmitRetryCount());
		assertEquals(6, ph.getCommunityDetectionTimeoutMillis());
		assertEquals(7, ph.getFunctionalEnrichmentTimeoutMillis());
		assertEquals(true, LauncherDialog.ALGORITHM_ENDPOINT_UPDATED);
		
		ph.setBaseurl(null);
		assertEquals("", ph.getBaseurlHostNameOnly());
		ph.setBaseurl("https://hi.com");
		assertEquals("hi.com", ph.getBaseurlHostNameOnly());
		ph.setBaseurl("hi.com/foo");
		assertEquals("hi.com", ph.getBaseurlHostNameOnly());

	}
	
	@Test
	public void testUpdateViaPropertiesWithNullProperties(){
		PropertiesHelper ph = PropertiesHelper.getInstance();
		ph.getInstance().updateViaProperties(null);
		assertEquals("http://cdservice.cytoscape.org/cd/communitydetection/v1",
				ph.getBaseurl());
		assertEquals("cdservice.cytoscape.org", ph.getBaseurlHostNameOnly());
		assertEquals("4", ph.getThreadcount());
		assertEquals(10000, ph.getHttpSocketTimeoutMillis());
		assertEquals(10000, ph.getHttpConnectTimeoutMillis());
		assertEquals(10000, ph.getHttpConnectionRequestTimeoutMillis());
		assertEquals(2, ph.getSubmitRetryCount());
		assertEquals(1800000, ph.getCommunityDetectionTimeoutMillis());
		assertEquals(1800000, ph.getFunctionalEnrichmentTimeoutMillis());
	}
	
	@Test
	public void testUpdateViaPropertiesWithNonNumericProperty(){
		
		Properties props = new Properties();
		props.setProperty(AppUtils.PROP_HTTP_CONNECT_TIMEOUT, "xxxx");
		props.setProperty(AppUtils.PROP_HTTP_SOCKET_TIMEOUT, "5");
		PropertiesHelper ph = PropertiesHelper.getInstance();
		
		ph.getInstance().updateViaProperties(props);
		assertEquals(5, ph.getHttpSocketTimeoutMillis());
		assertEquals(10000, ph.getHttpConnectTimeoutMillis());
	}
	
	@Test
	public void testUpdateViaPropertiesWithValidProperties(){
		Properties props = new Properties();
		props.setProperty(AppUtils.PROP_APP_BASEURL, "baseurl");
		props.setProperty(AppUtils.PROP_APP_THREADCOUNT, "1");
		props.setProperty(AppUtils.PROP_CD_TASK_TIMEOUT, "2");
		props.setProperty(AppUtils.PROP_FE_TASK_TIMEOUT, "3");
		props.setProperty(AppUtils.PROP_HTTP_CONNECTION_REQUEST_TIMEOUT, "4");
		props.setProperty(AppUtils.PROP_HTTP_CONNECT_TIMEOUT, "5");
		props.setProperty(AppUtils.PROP_HTTP_SOCKET_TIMEOUT, "6");
		props.setProperty(AppUtils.PROP_POLL_INTERVAL_TIME, "7");
		props.setProperty(AppUtils.PROP_SUBMIT_RETRY_COUNT, "8");

		PropertiesHelper ph = PropertiesHelper.getInstance();
		
		ph.getInstance().updateViaProperties(props);
		assertEquals("baseurl", ph.getBaseurl());
		assertEquals("1", ph.getThreadcount());
		assertEquals(6, ph.getHttpSocketTimeoutMillis());
		assertEquals(5, ph.getHttpConnectTimeoutMillis());
		assertEquals(4, ph.getHttpConnectionRequestTimeoutMillis());
		assertEquals(7, ph.getPollingIntervalTimeMillis());
		assertEquals(8, ph.getSubmitRetryCount());
		assertEquals(2, ph.getCommunityDetectionTimeoutMillis());
		assertEquals(3, ph.getFunctionalEnrichmentTimeoutMillis());
	}
}
