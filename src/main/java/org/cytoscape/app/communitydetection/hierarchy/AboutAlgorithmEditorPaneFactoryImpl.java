package org.cytoscape.app.communitydetection.hierarchy;

import javax.swing.JEditorPane;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

/**
 *
 * 
 * @author churas
 */
public class AboutAlgorithmEditorPaneFactoryImpl {
	
	private JEditorPaneFactory _editorPaneFac;
	
	public AboutAlgorithmEditorPaneFactoryImpl(JEditorPaneFactory editorPaneFac){
		_editorPaneFac = editorPaneFac;
	}
	
	/**
	 * Creates {@link javax.swing.JEditorPane} with text from 
	 * {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm#getDescription()} 
	 * method or if that is null a simple string saying no information is available
	 * @param algorithm Algorithm to get description from
	 * @return {@link javax.swing.JEditorPane} with text and links describing algorithm passed in
	 */
	public JEditorPane getAlgorithmAboutFrame(CommunityDetectionAlgorithm algorithm){
	    return _editorPaneFac.getDescriptionFrame(algorithm.getDescription() == null ? "No additional information available" : algorithm.getDescription());
	}
}
