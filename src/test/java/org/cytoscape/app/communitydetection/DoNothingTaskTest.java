package org.cytoscape.app.communitydetection.iquery;

import org.cytoscape.work.TaskMonitor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class IQueryTaskTest {
	
	@Test
	public void testRun(){
		IQueryTask task = new IQueryTask();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		try {
			task.run(mockMonitor);
		} catch(Exception ex){
			fail("Unexpected Exception: " + ex.getMessage());
		}
	}
	
}
