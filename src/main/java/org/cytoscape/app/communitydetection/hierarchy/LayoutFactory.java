package org.cytoscape.app.communitydetection.hierarchy;

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;

/**
 *
 * @author churas
 */
public class LayoutFactory {
	
	private final CyLayoutAlgorithmManager _layoutManager;
	public LayoutFactory(final CyLayoutAlgorithmManager layoutManager){
		_layoutManager = layoutManager;
		
	}
	
	public CyLayoutAlgorithm getLayoutAlgorithm(){
		return _layoutManager.getDefaultLayout();
	}
}
