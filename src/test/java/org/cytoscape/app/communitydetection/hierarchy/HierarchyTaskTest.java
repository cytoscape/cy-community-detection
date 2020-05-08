package org.cytoscape.app.communitydetection.hierarchy;

import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactory;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

/**
 *
 * @author churas
 */
public class HierarchyTaskTest {
	
	@Test
	public void testRunAlgorithmIsNull() throws Exception {
		HierarchyTask task = new HierarchyTask(null, null, null, null,
				null, null, null, null);
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
		HierarchyTask task = new HierarchyTask(null, null, null, null, null, cda , null, null);
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
	public void testRunTaskIsCanceledAfterGet() throws Exception {
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		HierarchyTask task = new HierarchyTask(null, null, null, null, null, cda , null, null);	
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn("resultuuid");
		when(mockRestClient.getCDResult(eq("resultuuid"), eq(mockMonitor), eq(0.1f), eq(0.8f), 
				eq(PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis()))).thenAnswer(new Answer() {
         public Object answer(InvocationOnMock invocation) {
			 task.cancel();
             return new CommunityDetectionResult();
         }
 });
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
								
		task.setAlternateWriterTaskFactory(mockWriterFac);
		task.setAlternateCDRestClient(mockRestClient);
		task.run(mockMonitor);
		verify(mockMonitor).setProgress(0.1);
		verify(mockMonitor).setTitle("Community Detection: Creating Hierarchy Network");
		verify(mockMonitor).setStatusMessage("Exporting the network");
		verify(mockMonitor).setStatusMessage("Network exported, retrieving the hierarchy");
		verify(mockRestClient).setTaskCanceled(false);
	}
	
	@Test
	public void testRunNetworkFactoryReturnsNull() throws Exception {
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn("taskid");
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		when(mockRestClient.getCDResult(eq("resultuuid"), eq(mockMonitor), eq(0.1f), eq(0.8f), 
				eq(PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis()))).thenReturn(cdRes);
		CyNetwork mockNetwork = mock(CyNetwork.class);		
		when(mockRestClient.getCDResult(anyString(), eq(mockMonitor), anyFloat(), anyFloat(),
				anyInt())).thenReturn(cdRes);
		
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		HierarchyNetworkFactory mockNetworkFactory = mock(HierarchyNetworkFactory.class);
		HierarchyNetworkViewFactory mockNetworkViewFactory = mock(HierarchyNetworkViewFactory.class);
		VisualStyleFactory mockStyleFactory = mock(VisualStyleFactory.class);
		LayoutFactory mockLayoutFactory = mock(LayoutFactory.class);
		HierarchyTask task = new HierarchyTask(mockNetworkFactory,
				mockNetworkViewFactory, mockStyleFactory, mockLayoutFactory, mockNetwork, cda , null, "weight");
		task.setAlternateWriterTaskFactory(mockWriterFac);
		task.setAlternateCDRestClient(mockRestClient);
		
		try {
			task.run(mockMonitor);
		} catch(Exception e){
			assertEquals("Error creating hierarchy from result", e.getMessage());
		}
		
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Double> progressCaptor = ArgumentCaptor.forClass(Double.class);
		verify(mockMonitor, times(2)).setProgress(progressCaptor.capture());
		List<Double> progressValues = progressCaptor.getAllValues();
		assertEquals(0.1, progressValues.get(0), 0.01);
		assertEquals(0.9, progressValues.get(1), 0.01);
		verify(mockMonitor, times(3)).setStatusMessage(messageCaptor.capture());
		List<String> messageValues = messageCaptor.getAllValues();
		assertEquals("Exporting the network", messageValues.get(0));
		assertEquals("Network exported, retrieving the hierarchy", messageValues.get(1));
		assertTrue(messageValues.get(2).contains("Received hierarchy in "));
		
		
		verify(mockMonitor).setTitle("Community Detection: Creating Hierarchy Network");
	}
	
	@Test
	public void testRunNetworkViewFactoryIsNull() throws Exception {
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn("taskid");
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		when(mockRestClient.getCDResult(eq("resultuuid"), eq(mockMonitor), eq(0.1f), eq(0.8f), 
				eq(PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis()))).thenReturn(cdRes);
		CyNetwork mockNetwork = mock(CyNetwork.class);		
		when(mockRestClient.getCDResult(anyString(), eq(mockMonitor), anyFloat(), anyFloat(),
				anyInt())).thenReturn(cdRes);
		
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		CyNetwork mockHierarchyNetwork = mock(CyNetwork.class);
		HierarchyNetworkFactory mockNetworkFactory = mock(HierarchyNetworkFactory.class);
		when(mockNetworkFactory.getHierarchyNetwork(eq(mockNetwork), eq(cdRes), eq("weight"), eq(cda), eq(null))).thenReturn(mockHierarchyNetwork);
		VisualStyleFactory mockStyleFactory = mock(VisualStyleFactory.class);
		LayoutFactory mockLayoutFactory = mock(LayoutFactory.class);
		HierarchyTask task = new HierarchyTask(mockNetworkFactory,
				null, mockStyleFactory, mockLayoutFactory, mockNetwork, cda , null, "weight");
		task.setAlternateWriterTaskFactory(mockWriterFac);
		task.setAlternateCDRestClient(mockRestClient);
		try {
			task.run(mockMonitor);
		} catch(Exception e){
			assertEquals("networkViewFactory is null", e.getMessage());
		}
	}
	
	@Test
	public void testRunStyleFactoryIsNull() throws Exception {
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn("taskid");
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		when(mockRestClient.getCDResult(eq("resultuuid"), eq(mockMonitor), eq(0.1f), eq(0.8f), 
				eq(PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis()))).thenReturn(cdRes);
		CyNetwork mockNetwork = mock(CyNetwork.class);		
		when(mockRestClient.getCDResult(anyString(), eq(mockMonitor), anyFloat(), anyFloat(),
				anyInt())).thenReturn(cdRes);
		
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		CyNetwork mockHierarchyNetwork = mock(CyNetwork.class);
		HierarchyNetworkFactory mockNetworkFactory = mock(HierarchyNetworkFactory.class);
		HierarchyNetworkViewFactory mockNetworkViewFactory = mock(HierarchyNetworkViewFactory.class);
		when(mockNetworkFactory.getHierarchyNetwork(eq(mockNetwork), eq(cdRes), eq("weight"), eq(cda), eq(null))).thenReturn(mockHierarchyNetwork);
		LayoutFactory mockLayoutFactory = mock(LayoutFactory.class);
		HierarchyTask task = new HierarchyTask(mockNetworkFactory,
				mockNetworkViewFactory, null, mockLayoutFactory, mockNetwork, cda , null, "weight");
		task.setAlternateWriterTaskFactory(mockWriterFac);
		task.setAlternateCDRestClient(mockRestClient);
		try {
			task.run(mockMonitor);
		} catch(Exception e){
			assertEquals("styleFactory is null", e.getMessage());
		}
	}
	
	@Test
	public void testRunLayoutFactoryIsNull() throws Exception {
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn("taskid");
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		when(mockRestClient.getCDResult(eq("resultuuid"), eq(mockMonitor), eq(0.1f), eq(0.8f), 
				eq(PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis()))).thenReturn(cdRes);
		CyNetwork mockNetwork = mock(CyNetwork.class);		
		when(mockRestClient.getCDResult(anyString(), eq(mockMonitor), anyFloat(), anyFloat(),
				anyInt())).thenReturn(cdRes);
		
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		CyNetwork mockHierarchyNetwork = mock(CyNetwork.class);
		HierarchyNetworkFactory mockNetworkFactory = mock(HierarchyNetworkFactory.class);
		HierarchyNetworkViewFactory mockNetworkViewFactory = mock(HierarchyNetworkViewFactory.class);
		
		when(mockNetworkFactory.getHierarchyNetwork(eq(mockNetwork), eq(cdRes), eq("weight"), eq(cda), eq(null))).thenReturn(mockHierarchyNetwork);
		VisualStyleFactory mockStyleFactory = mock(VisualStyleFactory.class);
		HierarchyTask task = new HierarchyTask(mockNetworkFactory,
				mockNetworkViewFactory, mockStyleFactory, null, mockNetwork, cda , null, "weight");
		task.setAlternateWriterTaskFactory(mockWriterFac);
		task.setAlternateCDRestClient(mockRestClient);
		try {
			task.run(mockMonitor);
		} catch(Exception e){
			assertEquals("layoutFactory is null", e.getMessage());
		}
	}
	
	@Test
	public void testRunSuccess() throws Exception {
		CDRestClient mockRestClient = mock(CDRestClient.class);
		when(mockRestClient.postCDData(any(), any(), any())).thenReturn("taskid");
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		when(mockRestClient.getCDResult(eq("resultuuid"), eq(mockMonitor), eq(0.1f), eq(0.8f), 
				eq(PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis()))).thenReturn(cdRes);
		CyNetwork mockNetwork = mock(CyNetwork.class);		
		when(mockRestClient.getCDResult(anyString(), eq(mockMonitor), anyFloat(), anyFloat(),
				anyInt())).thenReturn(cdRes);
		
		CyWriter mockCyWriter = mock(CyWriter.class);
		WriterTaskFactory mockWriterFac = mock(WriterTaskFactory.class);
		when(mockWriterFac.createWriter(any(),any(), any())).thenReturn(mockCyWriter);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		CyNetwork mockHierarchyNetwork = mock(CyNetwork.class);
		HierarchyNetworkFactory mockNetworkFactory = mock(HierarchyNetworkFactory.class);
		when(mockNetworkFactory.getHierarchyNetwork(eq(mockNetwork), eq(cdRes), eq("weight"), eq(cda), eq(null))).thenReturn(mockHierarchyNetwork);
		HierarchyNetworkViewFactory mockNetworkViewFactory = mock(HierarchyNetworkViewFactory.class);
		VisualStyleFactory mockStyleFactory = mock(VisualStyleFactory.class);
		LayoutFactory mockLayoutFactory = mock(LayoutFactory.class);
		HierarchyTask task = new HierarchyTask(mockNetworkFactory,
				mockNetworkViewFactory, mockStyleFactory, mockLayoutFactory, mockNetwork, cda , null, "weight");
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
