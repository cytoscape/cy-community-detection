package org.cytoscape.app.communitydetection.edgelist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class WriterTaskTest {
	private NetworkTestSupport _nts = new NetworkTestSupport();
	
	
	@Test
	public void testCancelNullStream() {
		WriterTask wt = new WriterTask(null, null, null);
		wt.cancel();
	}

	@Test
	public void testCancel() throws Exception {
		OutputStream mockStream = mock(OutputStream.class);
		WriterTask wt = new WriterTask(mockStream, null, null);
		wt.cancel();
		verify(mockStream, times(1)).close();
	}
	
	@Test
	public void testCancelOutStreamCloseThrowsException() throws Exception {
		OutputStream mockStream = mock(OutputStream.class);
		doThrow(new IOException("hi")).when(mockStream).close();
		
		WriterTask wt = new WriterTask(mockStream, null, null);
		wt.cancel();
		verify(mockStream, times(1)).close();
	}

	@Test
	public void testRunOnEmptyNetwork() throws Exception{
		CyNetwork network = _nts.getNetwork();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		WriterTask wt = new WriterTask(bos, network, null);
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		wt.run(mockMonitor);
		verifyNoInteractions(mockMonitor);
		assertEquals(0, bos.size());
	}
	
	@Test
	public void testRunOnNetworkWithCoupleNodesAndOneEdge() throws Exception {
		CyNetwork network = _nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		CyEdge edge = network.addEdge(nodeOne, nodeTwo, true);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		WriterTask wt = new WriterTask(bos, network, null);
		wt.run(mockMonitor);
		verifyNoInteractions(mockMonitor);
		assertEquals(nodeOne.getSUID().toString()
				+ "\t" + nodeTwo.getSUID().toString() + "\n", bos.toString());
	}
	
	@Test
	public void testRunOnNetworkWithCoupleNodesAndOneEdgeWithWeight() throws Exception {
		CyNetwork network = _nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		CyEdge edge = network.addEdge(nodeOne, nodeTwo, true);
		network.getDefaultEdgeTable().createColumn("foo", Integer.class, true, 0);
		network.getRow(edge).set("foo", 5);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		WriterTask wt = new WriterTask(bos, network, "foo");
		wt.run(mockMonitor);
		verifyNoInteractions(mockMonitor);
		assertEquals(nodeOne.getSUID().toString()
				+ "\t" + nodeTwo.getSUID().toString() + "\t5\n", bos.toString());
	}
	
	@Test
	public void testRunOnNetworkWithCoupleNodesAndOneEdgeWithNullWeight() throws Exception {
		CyNetwork network = _nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		CyEdge edge = network.addEdge(nodeOne, nodeTwo, true);
		network.getDefaultEdgeTable().createColumn("foo", Integer.class, false);
		network.getRow(edge).set("foo", null);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		WriterTask wt = new WriterTask(bos, network, "foo");
		try {
			wt.run(mockMonitor);
		} catch(Exception e){
			assertTrue(e.getMessage().contains("foo does not have a value for row with SUID: "));
		}
		verifyNoInteractions(mockMonitor);
		assertEquals(0, bos.size());
	}
	
	@Test
	public void testRunOnNetworkWithCoupleNodesAndOneEdgeWithNegativeWeight() throws Exception {
		CyNetwork network = _nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		CyEdge edge = network.addEdge(nodeOne, nodeTwo, true);
		network.getDefaultEdgeTable().createColumn("foo", Integer.class, false);
		network.getRow(edge).set("foo", -1);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		WriterTask wt = new WriterTask(bos, network, "foo");
		try {
			wt.run(mockMonitor);
		} catch(Exception e){
			assertTrue(e.getMessage().contains("foo contains negative values."));
		}
		verifyNoInteractions(mockMonitor);
		assertEquals(0, bos.size());
	}
	
	
}
