package org.cytoscape.app.communitydetection;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.hierarchy.LauncherDialogAlgorithmFactory;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import static org.mockito.Mockito.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

/**
 *
 * @author churas
 */
public class SettingsTaskFactoryImplTest {
	
	@Test
	public void testCreateTaskIteratorCreateGUIisFalse(){
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(false);
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties,
		                                                          mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		verify(mockSettingsDialog).createGUI();
		
		//verify(mockDialog).showMessageDialog(eq(null), eq(mockEditorPane),
		//		eq(AppUtils.APP_NAME), eq(JOptionPane.INFORMATION_MESSAGE), any(ImageIcon.class));
	}
	
	@Test
	public void testCreateTaskIteratorShowDialogReturnsOne(){
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(true);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		when(mockDialog.showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(1);
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties, mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		verify(mockSettingsDialog).createGUI();
		verify(mockDialog).showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
	
	@Test
	public void testCreateTaskIteratorShowDialogReturnsZeroAndNullBaseurl(){
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(true);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		when(mockSettingsDialog.getBaseurl()).thenReturn(null);
		Properties props = new Properties();
		when(mockCyProperties.getProperties()).thenReturn(props);
		when(mockDialog.showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties, mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		verify(mockSettingsDialog).createGUI();
		verify(mockDialog).showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		verify(mockSettingsDialog).getBaseurl();
		assertEquals(PropertiesHelper.DEFAULT_BASEURL, props.getProperty(AppUtils.PROP_APP_BASEURL));
		assertEquals(PropertiesHelper.DEFAULT_BASEURL, PropertiesHelper.getInstance().getBaseurl());
	}
	
	@Test
	public void testCreateTaskIteratorShowDialogReturnsZeroAndAlgosReturnsNullOnBothCalls(){
		
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(true);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		when(mockSettingsDialog.getBaseurl()).thenReturn(null);
		Properties props = new Properties();
		when(mockCyProperties.getProperties()).thenReturn(props);
		when(mockDialog.showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		when(mockAlgoFac.getAlgorithms(null, null, true)).thenReturn(null);
		
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties, mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		verify(mockSettingsDialog).createGUI();
		verify(mockDialog).showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		verify(mockSettingsDialog).getBaseurl();
		assertEquals(PropertiesHelper.DEFAULT_BASEURL, props.getProperty(AppUtils.PROP_APP_BASEURL));
		assertEquals(PropertiesHelper.DEFAULT_BASEURL, PropertiesHelper.getInstance().getBaseurl());
		verify(mockDialog).showMessageDialog(null, "Fatal Error querying default server", "Error querying service", JOptionPane.ERROR_MESSAGE);
	}
	
	@Test
	public void testCreateTaskIteratorShowDialogReturnsZeroAndAlgosReturnsNullOnFirstCall(){
		
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(true);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		when(mockSettingsDialog.getBaseurl()).thenReturn(null);
		Properties props = new Properties();
		when(mockCyProperties.getProperties()).thenReturn(props);
		when(mockDialog.showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		List<CommunityDetectionAlgorithm> algos = new ArrayList<>();
		when(mockAlgoFac.getAlgorithms(null, null, true)).thenReturn(null, algos);
		
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties, mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		verify(mockSettingsDialog).createGUI();
		verify(mockDialog).showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		verify(mockSettingsDialog).getBaseurl();
		assertEquals(PropertiesHelper.DEFAULT_BASEURL, props.getProperty(AppUtils.PROP_APP_BASEURL));
		assertEquals(PropertiesHelper.DEFAULT_BASEURL, PropertiesHelper.getInstance().getBaseurl());
		
		verify(mockDialog).showMessageDialog(null, "Error querying "
				+ PropertiesHelper.DEFAULT_BASEURL
				+ " settings have been reset to default server",
				"Error querying service", JOptionPane.ERROR_MESSAGE);
	}
	
	@Test
	public void testCreateTaskIteratorShowDialogReturnsZeroAndHostNameOnlyBaseurl(){
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);

		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(true);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		when(mockSettingsDialog.getBaseurl()).thenReturn("foo.com");
		Properties props = new Properties();
		when(mockCyProperties.getProperties()).thenReturn(props);
		when(mockDialog.showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties, mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		verify(mockSettingsDialog).createGUI();
		verify(mockDialog).showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		verify(mockSettingsDialog).getBaseurl();
		String newurl = "http://foo.com/cd/communitydetection/v1";
		assertEquals(newurl, props.getProperty(AppUtils.PROP_APP_BASEURL));
		assertEquals(newurl, PropertiesHelper.getInstance().getBaseurl());
	}
	
	@Test
	public void testCreateTaskIteratorShowDialogReturnsZeroAndhttpPrefixBaseurl(){
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(true);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		when(mockSettingsDialog.getBaseurl()).thenReturn("http://foo.com");
		Properties props = new Properties();
		when(mockCyProperties.getProperties()).thenReturn(props);
		when(mockDialog.showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties, mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		verify(mockSettingsDialog).createGUI();
		verify(mockDialog).showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		verify(mockSettingsDialog).getBaseurl();
		String newurl = "http://foo.com/cd/communitydetection/v1";
		assertEquals(newurl, props.getProperty(AppUtils.PROP_APP_BASEURL));
		assertEquals(newurl, PropertiesHelper.getInstance().getBaseurl());
	}
	
	@Test
	public void testCreateTaskIteratorShowDialogReturnsZeroAndWhiteSpaceAtStartEnd(){
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		CySwingApplication mockApp = mock(CySwingApplication.class);
		when(mockApp.getJFrame()).thenReturn(null);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		SettingsDialog mockSettingsDialog = mock(SettingsDialog.class);
		CyProperty<Properties> mockCyProperties = mock(CyProperty.class);
		when(mockSettingsDialog.createGUI()).thenReturn(true);
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
		when(mockSettingsDialog.getBaseurl()).thenReturn(" foo.com ");
		Properties props = new Properties();
		when(mockCyProperties.getProperties()).thenReturn(props);
		when(mockDialog.showOptionDialog(null, mockSettingsDialog, "Community Detection Settings",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(mockApp, mockSettingsDialog,
															      mockDialog,
		                                                          mockCyProperties, mockAlgoFac);
		assertNotNull(tFac.createTaskIterator(null));
		String newurl = "http://foo.com/cd/communitydetection/v1";
		assertEquals(newurl, props.getProperty(AppUtils.PROP_APP_BASEURL));
		assertEquals(newurl, PropertiesHelper.getInstance().getBaseurl());
	}
	
	@Test
	public void testIsReady(){
		SettingsTaskFactoryImpl tFac = new SettingsTaskFactoryImpl(null, null,
															       null, null, null);
		assertTrue(tFac.isReady(null));
		CyNetwork mockNet = mock(CyNetwork.class);
		assertTrue(tFac.isReady(mockNet));
	}
}
