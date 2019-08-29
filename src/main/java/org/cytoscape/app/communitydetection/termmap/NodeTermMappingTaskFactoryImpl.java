package org.cytoscape.app.communitydetection.termmap;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class NodeTermMappingTaskFactoryImpl extends AbstractNodeViewTaskFactory {

	private final String algorithm;

	public NodeTermMappingTaskFactoryImpl(String algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new TermMappingTask(algorithm, networkView.getModel(), true));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		if (networkView != null && networkView.getModel() != null && networkView.getModel().getDefaultNetworkTable()
				.getColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK) == null) {
			return false;
		}
		return super.isReady(nodeView, networkView);
	}
}
