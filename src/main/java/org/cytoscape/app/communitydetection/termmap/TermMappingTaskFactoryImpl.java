package org.cytoscape.app.communitydetection.termmap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

public class TermMappingTaskFactoryImpl implements NetworkTaskFactory {

	private final String algorithm;
	private final Boolean isContextMenu;

	public TermMappingTaskFactoryImpl(String algorithm, Boolean isContextMenu) {
		this.algorithm = algorithm;
		this.isContextMenu = isContextMenu;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new TermMappingTask(algorithm, network, isContextMenu));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return network != null;
	}

}
