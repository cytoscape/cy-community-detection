package org.cytoscape.app.communitydetection.hierarchy;

import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import org.cytoscape.app.communitydetection.edgelist.ReaderTask;
import org.cytoscape.app.communitydetection.edgelist.ReaderTaskFactory;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactory;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

/**
 *
 * @author churas
 */
public class HierarchyTaskTest {
	
	@Test
	public void testRunAlgorithmIsNull() throws Exception {
		HierarchyTask task = null;//new HierarchyTask(null, null, null, null, null);
		task.run(null);
	}
	
	@Test
	public void testRunTaskIsCanceledAfterPost() throws Exception {
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn(null);
		
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		HierarchyTask task = null;//new HierarchyTask(null, null,cda , null, null);
		task.setAlternateWriterTaskFactory(mockWriterFac);
		task.setAlternateCDRestClient(mockRestClient);
		task.cancel();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		task.run(mockMonitor);
		verify(mockMonitor).setTitle("Community Detection: Creating Hierarchy Network");
		verify(mockMonitor).setStatusMessage("Exporting the network");
		verify(mockRestClient).setTaskCanceled(false);
	}
	
	@Test
	public void testRunSuccess() throws Exception {
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn("taskid");
		
		CyNetwork[] cyNetworkList = new CyNetwork[1];
		CyNetwork mockNetwork = mock(CyNetwork.class);
		when(mockNetwork.getSUID()).thenReturn(12345L);
		cyNetworkList[0] = mockNetwork;
		ReaderTask mockReaderTask = mock(ReaderTask.class);
		when(mockReaderTask.getNetworks()).thenReturn(cyNetworkList);
		org.cytoscape.work.Task[] taskList = new org.cytoscape.work.Task[1];
		taskList[0] = mockReaderTask;
		TaskIterator taskIterator = new TaskIterator(1, taskList);
		ReaderTaskFactory mockReaderTaskFac = mock(ReaderTaskFactory.class);
		when(mockReaderTaskFac.createTaskIterator(any(), any(), eq(12345L))).thenReturn(taskIterator);
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		cdRes.setResult(new TextNode("1\t2;3\t4\n"));
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		
		when(mockRestClient.getCDResult(anyString(), eq(mockMonitor), anyFloat(), anyFloat(),
				anyInt())).thenReturn(cdRes);
		
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		HierarchyTask task = null;//new HierarchyTask(mockReaderTaskFac, mockNetwork,cda , null, null);
		task.setAlternateWriterTaskFactory(mockWriterFac);
		task.setAlternateCDRestClient(mockRestClient);
		
		task.run(mockMonitor);
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Double> progressCaptor = ArgumentCaptor.forClass(Double.class);
		verify(mockMonitor, times(4)).setProgress(progressCaptor.capture());
		List<Double> progressValues = progressCaptor.getAllValues();
		assertEquals(0.1, progressValues.get(0), 0.01);
		assertEquals(0.9, progressValues.get(1), 0.01);
		assertEquals(0.95, progressValues.get(2), 0.01);
		assertEquals(1.0, progressValues.get(3), 0.01);
		verify(mockMonitor, times(6)).setStatusMessage(messageCaptor.capture());
		List<String> messageValues = messageCaptor.getAllValues();
		assertEquals("Exporting the network", messageValues.get(0));
		assertEquals("Network exported, retrieving the hierarchy", messageValues.get(1));
		assertTrue(messageValues.get(2).contains("Received hierarchy in "));
		assertTrue(messageValues.get(3).contains("Network created in "));
		assertEquals("Creating a view for the network", messageValues.get(4));
		
		assertTrue(messageValues.get(5).contains("Total time "));
		
		verify(mockMonitor).setTitle("Community Detection: Creating Hierarchy Network");
	}
}
