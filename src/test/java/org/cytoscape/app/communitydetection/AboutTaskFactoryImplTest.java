package org.cytoscape.app.communitydetection;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.util.JEditorPaneFactory;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import static org.mockito.Mockito.*;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author churas
 */
public class AboutTaskFactoryImplTest {
	
	@Test
	public void testCreateTaskIterator(){
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		JEditorPaneFactory mockPaneFac = mock(JEditorPaneFactory.class);
		JEditorPane mockEditorPane = mock(JEditorPane.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		when(mockPaneFac.getDescriptionFrame(anyString())).thenReturn(mockEditorPane);
		
		AboutTaskFactoryImpl tFac = new AboutTaskFactoryImpl(mockApp, mockPaneFac,
															mockDialog);
		tFac.createTaskIterator(null);
		verify(mockDialog).showMessageDialog(eq(null), eq(mockEditorPane),
				eq(AppUtils.APP_NAME), eq(JOptionPane.INFORMATION_MESSAGE), any(ImageIcon.class));
	}
	
	@Test
	public void testIsReady(){
		AboutTaskFactoryImpl tFac = new AboutTaskFactoryImpl(null, null,
															null);
		assertTrue(tFac.isReady(null));
		CyNetwork mockNet = mock(CyNetwork.class);
		assertTrue(tFac.isReady(mockNet));
	}
	
}
