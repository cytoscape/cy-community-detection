package org.cytoscape.app.communitydetection.event;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class BaseurlUpdatedEventTest {
	
	@Test
	public void testGetters(){
		BaseurlUpdatedEvent event = new BaseurlUpdatedEvent("old", "new");
		assertEquals("old", event.getOldURL());
		assertEquals("new", event.getNewURL());
	}
}
