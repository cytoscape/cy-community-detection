package org.cytoscape.app.communitydetection.hierarchy;

import javax.swing.JEditorPane;
import org.ndexbio.communitydetection.rest.model.CustomParameter;

/**
 *
 * 
 * @author churas
 */
public class CustomParameterHelpJEditorPaneFactoryImpl {
	
	private JEditorPaneFactory _editorPaneFac;
	
	public CustomParameterHelpJEditorPaneFactoryImpl(JEditorPaneFactory editorPaneFac){
		_editorPaneFac = editorPaneFac;
	}
	
	/**
	 * Gets a UI Component that describes the parameter passed in.
	 * @param parameter
	 * @return 
	 */
	protected JEditorPane getCustomParameterHelp(final CustomParameter parameter){
	    if (parameter == null){
		return _editorPaneFac.getDescriptionFrame("No parameter set, unable to generate help");
	    }
	    StringBuilder sb = new StringBuilder();
	    sb.append("<b>Parameter:</b> ");
	    sb.append(parameter.getDisplayName());
	    sb.append(" (");
	    sb.append(parameter.getName());
	    sb.append(")");
	    if (parameter.getDefaultValue() != null){
		sb.append(" [Default: ");
		sb.append(parameter.getDefaultValue());
		sb.append("]");
	    }
	    sb.append("<br/><h3>Description</h3> ");
	    sb.append(parameter.getDescription());
	    if (parameter.getValidationHelp() != null){
		sb.append("<br/>");
		sb.append(parameter.getValidationHelp());
	    }
	    return _editorPaneFac.getDescriptionFrame(sb.toString());
	}
}
