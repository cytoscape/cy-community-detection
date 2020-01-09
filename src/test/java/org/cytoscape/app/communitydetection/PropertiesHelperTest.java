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
	ph.setBaseurl("url");
	ph.setThreadcount("thread");
	assertEquals("url", ph.getBaseurl());
	assertEquals("thread", ph.getThreadcount());
    }
}
