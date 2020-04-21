package org.cytoscape.app.communitydetection.hierarchy;

import javax.swing.JEditorPane;
import org.cytoscape.app.communitydetection.util.JEditorPaneFactoryImpl;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ndexbio.communitydetection.rest.model.CustomParameter;


/**
 *
 * @author churas
 */
public class CustomParameterHelpJEditorPaneFactoryImplTest {
	
	@Test
	public void testGetCustomParameterHelpNullParameter(){
		JEditorPaneFactoryImpl paneFac = new JEditorPaneFactoryImpl();
		CustomParameterHelpJEditorPaneFactoryImpl fac = new CustomParameterHelpJEditorPaneFactoryImpl(paneFac);
		JEditorPane pane = fac.getCustomParameterHelp(null);
		assertTrue(pane.getText().contains("No parameter set, unable to generate help"));
	}
	
	@Test
	public void testGetCustomParameterHelpNullDefaultAndNullValidation(){
		JEditorPaneFactoryImpl paneFac = new JEditorPaneFactoryImpl();
		CustomParameterHelpJEditorPaneFactoryImpl fac = new CustomParameterHelpJEditorPaneFactoryImpl(paneFac);
		CustomParameter param = new CustomParameter();
		param.setDisplayName("displayname");
		param.setName("name");
		param.setDescription("description");
		JEditorPane pane = fac.getCustomParameterHelp(param);
		assertTrue(pane.getText().contains("displayname (name)"));
		assertTrue(pane.getText().contains("description"));
		assertFalse(pane.getText().contains("[Default: "));
	}
	
	@Test
	public void testGetCustomParameterHelpEverythingSet(){
		JEditorPaneFactoryImpl paneFac = new JEditorPaneFactoryImpl();
		CustomParameterHelpJEditorPaneFactoryImpl fac = new CustomParameterHelpJEditorPaneFactoryImpl(paneFac);
		CustomParameter param = new CustomParameter();
		param.setDisplayName("displayname");
		param.setName("name");
		param.setDescription("description");
		param.setValidationHelp("validhelp");
		param.setDefaultValue("defaultvalue");
		JEditorPane pane = fac.getCustomParameterHelp(param);
		assertTrue(pane.getText().contains("displayname (name)"));
		assertTrue(pane.getText().contains("description"));
		assertTrue(pane.getText().contains("[Default: defaultvalue]"));
		assertTrue(pane.getText().contains("validhelp"));
		
	}
}
