package org.cytoscape.app.communitydetection.termmap;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

/**
 * Implementation of {@link NetworkViewTaskFactory} and
 * {@link AbstractNodeViewTaskFactory} to create {@link TermMappingTask}
 * using logic in {@link NodeTermMapppingTaskFactoryImpl} cause in order
 * for a menu option to be enabled in the context menu of a view one must
 * extend AbstractNodeViewTaskFactory
 * 
 * TODO: NetworkTermMappingTaskFactoryImpl should be modified to extend and
 *       implement these interfaces. The trick is to get the menus right in
 *       Cytoscape. Initial attempt at getting the 
 *       "Apps" => "Community Detection"
 *       menu in correct order along with getting the Context Menu 
 *       "Apps" => "Community Detection" also with correct order and making
 *       sure the menu option stayed active proved problematic. I am guessing
 *       this problem is due to my ignorance
 *
 */
public class NodeTermMapppingTaskFactoryImpl extends AbstractNodeViewTaskFactory implements NetworkViewTaskFactory {

    private NetworkTermMappingTaskFactoryImpl _termFactory;

	public NodeTermMapppingTaskFactoryImpl(NetworkTermMappingTaskFactoryImpl termFactory) {
	    _termFactory = termFactory;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
	    return _termFactory.createTaskIterator(networkView.getModel());
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
	    if (networkView != null && networkView.getModel() != null) {
		return true;
	    }
	    return false;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
	    return this.createTaskIterator(networkView);
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
	    return this.isReady(networkView);
	}
}
