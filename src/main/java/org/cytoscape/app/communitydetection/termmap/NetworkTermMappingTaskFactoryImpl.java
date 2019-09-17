package org.cytoscape.app.communitydetection.termmap;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * {@link NetworkTaskFactory} implementation to create {@link TermMappingTask}
 * for Menu Bar.
 *
 */
public class NetworkTermMappingTaskFactoryImpl implements NetworkTaskFactory {

	private final String algorithm;

	public NetworkTermMappingTaskFactoryImpl(String algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new TermMappingTask(algorithm, network, false));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		if (network != null && network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) == null) {
			return false;
		}
		return network != null;
	}

}
