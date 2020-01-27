package org.cytoscape.app.communitydetection;

import java.util.Properties;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


/**
 *
 * @author churas
 */
public class PropertiesReaderTest {
	
	@Test
	public void testHandleEventMatchingSource(){
		CyProperty mockProp = mock(CyProperty.class);
		when(mockProp.getName()).thenReturn("foo");
		Properties props = new Properties();
		props.setProperty(AppUtils.PROP_HTTP_SOCKET_TIMEOUT, "5");
		when(mockProp.getProperties()).thenReturn(props);
		PropertyUpdatedEvent propUpdatedEvent = new PropertyUpdatedEvent(mockProp);
		
		PropertiesHelper ph = PropertiesHelper.getInstance();
		ph.setHttpSocketTimeoutMillis(25);
		PropertiesReader pr = new PropertiesReader("foo", "xxx");
		pr.handleEvent(propUpdatedEvent);
		assertEquals(5, ph.getHttpSocketTimeoutMillis());
		
	}
	
	@Test
	public void testHandleEventNonMatchingSource(){
		CyProperty mockProp = mock(CyProperty.class);
		when(mockProp.getName()).thenReturn("foo");
		Properties props = new Properties();
		props.setProperty(AppUtils.PROP_HTTP_SOCKET_TIMEOUT, "5");
		when(mockProp.getProperties()).thenReturn(props);
		PropertyUpdatedEvent propUpdatedEvent = new PropertyUpdatedEvent(mockProp);
		
		PropertiesHelper ph = PropertiesHelper.getInstance();
		ph.setHttpSocketTimeoutMillis(25);
		PropertiesReader pr = new PropertiesReader("bar", "xxx");
		pr.handleEvent(propUpdatedEvent);
		assertEquals(25, ph.getHttpSocketTimeoutMillis());
		
	}
}
