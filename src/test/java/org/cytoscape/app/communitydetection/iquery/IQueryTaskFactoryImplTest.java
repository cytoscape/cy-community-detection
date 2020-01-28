package org.cytoscape.app.communitydetection.iquery;


import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import javax.swing.JFrame;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.DesktopUtil;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 * @author churas
 */
public class IQueryTaskFactoryImplTest {
	
	@Test
	public void testIsReadyNullNetwork(){
		CySwingApplication mockApp = mock(CySwingApplication.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		assertEquals(false, tFac.isReady(null));
		assertEquals(false, tFac.isReady(null, null));
	}
	
	@Test
	public void testIsReadyNoSelectedNodes(){
		CySwingApplication mockApp = mock(CySwingApplication.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		final CyNetwork network = nts.getNetwork();
		when(mockView.getModel()).thenReturn(network);
		assertEquals(false, tFac.isReady(mockView));
	}
	
	@Test
	public void testIsReadyTooManySelectedNodes(){
		CySwingApplication mockApp = mock(CySwingApplication.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		final CyNetwork network = nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		network.getRow(nodeOne).set(CyNetwork.SELECTED, true);
		network.getRow(nodeTwo).set(CyNetwork.SELECTED, true);
		when(mockView.getModel()).thenReturn(network);
		assertEquals(false, tFac.isReady(mockView));
	}
	
	@Test
	public void testIsReadyMissingColumn(){
		CySwingApplication mockApp = mock(CySwingApplication.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		final CyNetwork network = nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		network.getRow(nodeOne).set(CyNetwork.SELECTED, true);
		when(mockView.getModel()).thenReturn(network);
		assertEquals(false, tFac.isReady(mockView));
	}
	
	@Test
	public void testIsReadyWithColumn(){
		CySwingApplication mockApp = mock(CySwingApplication.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		final CyNetwork network = nts.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		CyNode nodeOne = network.addNode();
		network.addNode();
		network.getRow(nodeOne).set(CyNetwork.SELECTED, true);
		when(mockView.getModel()).thenReturn(network);
		assertEquals(true, tFac.isReady(mockView));
	}
	
	@Test
	public void testCreateTaskIteratorNoTerms(){
		CySwingApplication mockApp = mock(CySwingApplication.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		final CyNetwork network = nts.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		CyNode nodeOne = network.addNode();
		network.addNode();
		network.getRow(nodeOne).set(CyNetwork.SELECTED, true);
		when(mockView.getModel()).thenReturn(network);
		
		assertNotNull(tFac.createTaskIterator(mockView));
	}
	
	@Test
	public void testCreateTaskIteratorTwoTerms() throws Exception {
		PropertiesHelper.getInstance().setiQueryurl("http://foo.com");
		CySwingApplication mockApp = mock(CySwingApplication.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		DesktopUtil mockDeskTopUtil = mock(DesktopUtil.class);
		Desktop mockDesktop = mock(Desktop.class);
		when(mockDeskTopUtil.getDesktop()).thenReturn(mockDesktop);
		tFac.setAlternateDesktopUtil(mockDeskTopUtil);
		final CyNetwork network = nts.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		CyNode nodeOne = network.addNode();
		network.addNode();
		network.getRow(nodeOne).set(AppUtils.COLUMN_CD_MEMBER_LIST, "mtor tp53");
		network.getRow(nodeOne).set(CyNetwork.SELECTED, true);
		when(mockView.getModel()).thenReturn(network);
		
		assertNotNull(tFac.createTaskIterator(mockView));
		verify(mockDesktop, times(1)).browse(new URI("http://foo.com?genes=mtor%20tp53"));
	}
	
	@Test
	public void testCreateTaskIteratorDesktopBrowseRaisesException() throws Exception {
		PropertiesHelper.getInstance().setiQueryurl("http://foo.com");
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(new JFrame());
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		DesktopUtil mockDeskTopUtil = mock(DesktopUtil.class);
		Desktop mockDesktop = mock(Desktop.class);
		when(mockDeskTopUtil.getDesktop()).thenReturn(mockDesktop);
		doThrow(new UnsupportedOperationException("nope")).when(mockDesktop).browse(any(URI.class));
		tFac.setAlternateDesktopUtil(mockDeskTopUtil);
		final CyNetwork network = nts.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		CyNode nodeOne = network.addNode();
		network.addNode();
		network.getRow(nodeOne).set(AppUtils.COLUMN_CD_MEMBER_LIST, "mtor tp53");
		network.getRow(nodeOne).set(CyNetwork.SELECTED, true);
		when(mockView.getModel()).thenReturn(network);
		
		assertNotNull(tFac.createTaskIterator(mockView));
		verify(mockDialog).showMessageDialog(any(Component.class), eq("Default browser window could not be opened. Please copy/paste this link to your browser: http://foo.com?genes=mtor%20tp53"));
	}
	
	@Test
	public void testCreateTaskIteratorURISyntaxException() throws Exception {
		PropertiesHelper.getInstance().setiQueryurl("http://foo^");
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(new JFrame());
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CyNetworkView mockView = mock(CyNetworkView.class);
		final NetworkTestSupport nts = new NetworkTestSupport();
		IQueryTaskFactoryImpl tFac = new IQueryTaskFactoryImpl(mockApp, mockDialog);
		DesktopUtil mockDeskTopUtil = mock(DesktopUtil.class);
		Desktop mockDesktop = mock(Desktop.class);
		when(mockDeskTopUtil.getDesktop()).thenReturn(mockDesktop);
		//doThrow(new UnsupportedOperationException("nope")).when(mockDesktop).browse(any(URI.class));
		tFac.setAlternateDesktopUtil(mockDeskTopUtil);
		final CyNetwork network = nts.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		CyNode nodeOne = network.addNode();
		network.addNode();
		network.getRow(nodeOne).set(AppUtils.COLUMN_CD_MEMBER_LIST, "mtor tp53");
		network.getRow(nodeOne).set(CyNetwork.SELECTED, true);
		when(mockView.getModel()).thenReturn(network);
		
		assertNotNull(tFac.createTaskIterator(mockView));
		verify(mockDialog).showMessageDialog(any(Component.class), eq("Unable to generate URL for iQuery: Illegal character in authority at index 7: http://foo^?genes=mtor%20tp53"));
	}
}
