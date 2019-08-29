package org.cytoscape.app.communitydetection.termmap;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

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
		if (network != null
				&& network.getDefaultNetworkTable().getColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK) == null) {
			return false;
		}
		return network != null;
	}

}
