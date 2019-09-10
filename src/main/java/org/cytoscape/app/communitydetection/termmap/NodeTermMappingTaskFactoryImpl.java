package org.cytoscape.app.communitydetection.termmap;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class NodeTermMappingTaskFactoryImpl extends AbstractNodeViewTaskFactory implements NetworkViewTaskFactory {

	private final String algorithm;

	public NodeTermMappingTaskFactoryImpl(String algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new TermMappingTask(algorithm, networkView.getModel(), true));
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (networkView != null && networkView.getModel() != null) {
			if (networkView.getModel().getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) == null) {
				return false;
			}
			if (CyTableUtil.getSelectedNodes(networkView.getModel()).size() < 1) {
				return false;
			}
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
