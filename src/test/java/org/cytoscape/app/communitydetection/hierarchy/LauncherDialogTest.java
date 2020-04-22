package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CustomParameter;

/**
 *
 * @author churas
 */
public class LauncherDialogTest {
	

	@Test
	public void testCreateGUIFirstCallNullArgsPassedToConstructor() throws Exception{
		
		LauncherDialog ld = new LauncherDialog(null, null, null, null,null);
		try {
			ld.createGUI(null);
			fail("Expected NullPointerException");
		} catch(NullPointerException npe){
			
		}
	}
	
	@Test
	public void testgetSelectedCommunityDetectionAlgorithmBeforeGuiLoaded() throws Exception{
		
		Component mockComponent = mock(Component.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		AboutAlgorithmEditorPaneFactoryImpl mockAbout = mock(AboutAlgorithmEditorPaneFactoryImpl.class);
		CustomParameterHelpJEditorPaneFactoryImpl mockCustom = mock(CustomParameterHelpJEditorPaneFactoryImpl.class);
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		LauncherDialog ld = new LauncherDialog(mockAbout, mockCustom,
				mockAlgoFac, mockDialog,AppUtils.CD_ALGORITHM_INPUT_TYPE);
		assertNull(ld.getSelectedCommunityDetectionAlgorithm());
	}
	
	@Test
	public void testCreateGUIFirstCallSuccessCDAlgoNoCustomParams() throws Exception {
		Component mockComponent = mock(Component.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		AboutAlgorithmEditorPaneFactoryImpl mockAbout = mock(AboutAlgorithmEditorPaneFactoryImpl.class);
		CustomParameterHelpJEditorPaneFactoryImpl mockCustom = mock(CustomParameterHelpJEditorPaneFactoryImpl.class);
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		List<CommunityDetectionAlgorithm> cdaList = new ArrayList<>();
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		cda.setInputDataFormat(AppUtils.CD_ALGORITHM_INPUT_TYPE);
		cda.setDescription("description");
		cda.setDisplayName("displayname");
		cdaList.add(cda);
		when(mockAlgoFac.getAlgorithms(mockComponent, AppUtils.CD_ALGORITHM_INPUT_TYPE, false)).thenReturn(cdaList);
		LauncherDialog ld = new LauncherDialog(mockAbout, mockCustom,
				mockAlgoFac, mockDialog,AppUtils.CD_ALGORITHM_INPUT_TYPE);
		assertTrue(ld.createGUI(mockComponent));
		Map<String, String> algoCustParams = ld.getAlgorithmCustomParameters("foo");
		assertEquals(0, algoCustParams.size());
	}
	
	@Test
	public void testCreateGUIFirstCallSuccessCDAlgoWithCustomParams() throws Exception {
		Component mockComponent = mock(Component.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		AboutAlgorithmEditorPaneFactoryImpl mockAbout = mock(AboutAlgorithmEditorPaneFactoryImpl.class);
		CustomParameterHelpJEditorPaneFactoryImpl mockCustom = mock(CustomParameterHelpJEditorPaneFactoryImpl.class);
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		List<CommunityDetectionAlgorithm> cdaList = new ArrayList<>();
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		cda.setInputDataFormat(AppUtils.CD_ALGORITHM_INPUT_TYPE);
		cda.setDescription("description");
		cda.setDisplayName("displayname");
		HashSet<CustomParameter> custParams = new HashSet<>();
		CustomParameter custOne = new CustomParameter();
		custOne.setName("--custone");
		custOne.setDisplayName("custonedisplay");
		custOne.setDescription("custonedescription");
		custOne.setType("flag");
		custParams.add(custOne);
		CustomParameter custTwo = new CustomParameter();
		custTwo.setName("--custtwo");
		custTwo.setDisplayName("custtwodisplay");
		custTwo.setDescription("custtwodescription");
		custTwo.setType("value");
		custTwo.setDefaultValue("custtwodefault");
		custParams.add(custTwo);
		cda.setCustomParameters(custParams);
		cdaList.add(cda);
		
		when(mockAlgoFac.getAlgorithms(mockComponent, AppUtils.CD_ALGORITHM_INPUT_TYPE, false)).thenReturn(cdaList);
		LauncherDialog ld = new LauncherDialog(mockAbout, mockCustom,
				mockAlgoFac, mockDialog,AppUtils.CD_ALGORITHM_INPUT_TYPE);
		assertTrue(ld.createGUI(mockComponent));
		Map<String, String> algoCustParams = ld.getAlgorithmCustomParameters("foo");
		
		assertEquals(1, algoCustParams.size());	
		assertEquals("custtwodefault", algoCustParams.get("--custtwo"));
		CommunityDetectionAlgorithm selectedcda = ld.getSelectedCommunityDetectionAlgorithm();
		assertNotNull(cda);
	}
	
}
