package org.cytoscape.app.communitydetection.subnetwork;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.DoNothingTask;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class SubNetworkTaskFactoryImplTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();

	@Test
	public void testIsReadyNoSelectedNodes(){
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		when(mockNetworkView.getModel()).thenReturn(net, net);
		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(null, null, null, null, null, null,
		                                            null, null, null, null, null, null);
		assertFalse(factory.isReady(mockNetworkView));
	}
	
	@Test
	public void testIsReadyOneSelectedNodeButMissingOriginalNetworkAttribute(){
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		CyNode node = net.addNode();
		net.getRow(node).set("selected", true);
		when(mockNetworkView.getModel()).thenReturn(net, net);
		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(null, null, null, null, null, null,
		                                            null, null, null, null, null, null);
		assertFalse(factory.isReady(mockNetworkView));
	}
	
	@Test
	public void testIsReadyAlt(){
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		CyNode node = net.addNode();
		net.getRow(node).set("selected", true);
		net.getDefaultNetworkTable().createColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class, false,1L);
		when(mockNetworkView.getModel()).thenReturn(net, net);
		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(null, null, null, null, null, null,
		                                            null, null, null, null, null, null);
		assertTrue(factory.isReady(null, mockNetworkView));
	}
	
	@Test
	public void testCreateTaskIteratorSingleMatchingParentNetworkFound() throws Exception{
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		when(mockNetworkView.getModel()).thenReturn(net);
		CyNetworkManager mockNetManager = mock(CyNetworkManager.class);
		Set<CyNetwork> allNets = new HashSet<>();
		when(mockNetManager.getNetworkSet()).thenReturn(allNets);
		ParentNetworkFinder mockFinder = mock(ParentNetworkFinder.class);
		List<CyNetwork> netList = new ArrayList<>();
		CyNetwork resNet = _nts.getNetwork();
		netList.add(resNet);
		when(mockFinder.findParentNetworks(allNets, net)).thenReturn(netList);
		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(null, null, mockFinder,
				                                    null, null, mockNetManager,
		                                            null, null, null, null, null, null);
		TaskIterator res = factory.createTaskIterator(mockNetworkView);
		assertNotNull(res);
		assertTrue(res.next() instanceof SubNetworkTask);
	}

	@Test
	public void testCreateTaskIteratorCreateGUIReturnedFalse() throws Exception{
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		when(mockNetworkView.getModel()).thenReturn(net);
		CyNetworkManager mockNetManager = mock(CyNetworkManager.class);
		Set<CyNetwork> allNets = new HashSet<>();
		when(mockNetManager.getNetworkSet()).thenReturn(allNets);
		ParentNetworkFinder mockFinder = mock(ParentNetworkFinder.class);

		when(mockFinder.findParentNetworks(allNets, net)).thenReturn(null);
		
		ParentNetworkChooserDialog mockParentDialog = mock(ParentNetworkChooserDialog.class);
		when(mockParentDialog.createGUI(null)).thenReturn(false);
		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(null, null, mockFinder,
				                                    mockParentDialog, null, mockNetManager,
		                                            null, null, null, null, null, null);
		TaskIterator res = factory.createTaskIterator(mockNetworkView);
		assertNotNull(res);
		assertTrue(res.next() instanceof DoNothingTask);
	}
	
	@Test
	public void testCreateTaskIteratorDialogReturnsZero() throws Exception{
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		net.getDefaultNetworkTable().createColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class, false,1L);

		CyNetwork parentNet = _nts.getNetwork();
		when(mockNetworkView.getModel()).thenReturn(net);
		CyNetworkManager mockNetManager = mock(CyNetworkManager.class);
		Set<CyNetwork> allNets = new HashSet<>();
		when(mockNetManager.getNetworkSet()).thenReturn(allNets);
		ParentNetworkFinder mockFinder = mock(ParentNetworkFinder.class);

		when(mockFinder.findParentNetworks(allNets, net)).thenReturn(null);
		
		ParentNetworkChooserDialog mockParentDialog = mock(ParentNetworkChooserDialog.class);
		when(mockParentDialog.createGUI(null)).thenReturn(true);
		
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		JFrame mockJFrame = mock(JFrame.class);
		
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(mockJFrame);
		when(mockDialog.showOptionDialog(any(Component.class), any(Object.class), eq("Parent Network Chooser"),
				eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE), any(), eq(options), eq(options[0]))).thenReturn(0);
		
		when(mockParentDialog.rememberChoice()).thenReturn(true);
		when(mockParentDialog.getSelection()).thenReturn(parentNet);
		
		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(mockApp, mockDialog, mockFinder,
				                                    mockParentDialog, null, mockNetManager,
		                                            null, null, null, null, null, null);
		TaskIterator res = factory.createTaskIterator(mockNetworkView);
		assertNotNull(res);
		assertTrue(res.next() instanceof SubNetworkTask);
		assertEquals(parentNet.getSUID(), net.getRow(net).get(AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class));
	}
	
	@Test
	public void testCreateTaskIteratorDialogReturnsZeroRememberChoiceFalse() throws Exception{
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		net.getDefaultNetworkTable().createColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class, false,1L);

		CyNetwork parentNet = _nts.getNetwork();
		when(mockNetworkView.getModel()).thenReturn(net);
		CyNetworkManager mockNetManager = mock(CyNetworkManager.class);
		Set<CyNetwork> allNets = new HashSet<>();
		when(mockNetManager.getNetworkSet()).thenReturn(allNets);
		ParentNetworkFinder mockFinder = mock(ParentNetworkFinder.class);

		when(mockFinder.findParentNetworks(allNets, net)).thenReturn(null);
		
		ParentNetworkChooserDialog mockParentDialog = mock(ParentNetworkChooserDialog.class);
		when(mockParentDialog.createGUI(null)).thenReturn(true);
		
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		JFrame mockJFrame = mock(JFrame.class);
		
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(mockJFrame);
		when(mockDialog.showOptionDialog(any(Component.class), any(Object.class), eq("Parent Network Chooser"),
				eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE), any(), eq(options), eq(options[0]))).thenReturn(0);
		
		when(mockParentDialog.rememberChoice()).thenReturn(false);
		when(mockParentDialog.getSelection()).thenReturn(parentNet);
		
		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(mockApp, mockDialog, mockFinder,
				                                    mockParentDialog, null, mockNetManager,
		                                            null, null, null, null, null, null);
		TaskIterator res = factory.createTaskIterator(mockNetworkView);
		assertNotNull(res);
		assertTrue(res.next() instanceof SubNetworkTask);
		assertEquals(Long.valueOf(1L), net.getRow(net).get(AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class));
	}
	
	@Test
	public void testCreateTaskIteratorFindParentNetworksRaisesException() throws Exception{
		CyNetworkView mockNetworkView = mock(CyNetworkView.class);
		CyNetwork net = _nts.getNetwork();
		when(mockNetworkView.getModel()).thenReturn(net);
		CyNetworkManager mockNetManager = mock(CyNetworkManager.class);
		Set<CyNetwork> allNets = new HashSet<>();
		when(mockNetManager.getNetworkSet()).thenReturn(allNets);
		ParentNetworkFinder mockFinder = mock(ParentNetworkFinder.class);

		when(mockFinder.findParentNetworks(allNets, net)).thenThrow(new ParentNetworkFinderException("anerror"));

		SubNetworkTaskFactoryImpl factory = new SubNetworkTaskFactoryImpl(null, null, mockFinder,
				                                    null, null, mockNetManager,
		                                            null, null, null, null, null, null);
		TaskIterator res = factory.createTaskIterator(mockNetworkView);
		assertNotNull(res);
		assertTrue(res.next() instanceof DoNothingTask);
	}
}
