package org.cytoscape.app.communitydetection.iquery;


import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
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
}
