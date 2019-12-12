package org.cytoscape.app.communitydetection.termmap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

/**
 * Task to run a term mapping algorithm on the selected network or nodes.
 *
 */
public class TermMappingTask extends AbstractTask {

	private final String algorithm;
	private final String attribute;
	private final CyNetwork network;
	private final boolean isContextMenu;

	public TermMappingTask(String algorithm, String attribute, CyNetwork network, boolean isContextMenu) {
		this.algorithm = algorithm;
		this.attribute = attribute;
		this.network = network;
		this.isContextMenu = isContextMenu;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (attribute == null || attribute.equals(AppUtils.TYPE_ABOUT)) {
			return;
		}

		long startTime = System.currentTimeMillis();
		taskMonitor.setTitle("Community Detection: Term Mapping");
		taskMonitor.setProgress(0.0);
		String algoDisplayName = null;
		for (CommunityDetectionAlgorithm algo : CDRestClient.getInstance()
				.getAlgorithmsByType(AppUtils.TM_ALGORITHM_INPUT_TYPE)) {
			if (algo.getName().equalsIgnoreCase(algorithm)) {
				algoDisplayName = algo.getDisplayName();
				break;
			}
		}
		taskMonitor.setStatusMessage("Running " + algoDisplayName);
		if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME) == null) {
			network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME, String.class, false, null);
		}
		if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS) == null) {
			network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, String.class, false, null);
		}
		if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_LABELED) == null) {
			network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_LABELED, Boolean.class, true, false);
		}
		List<CyNode> selectedNodes;
		if (isContextMenu) {
			selectedNodes = CyTableUtil.getSelectedNodes(network);
			if (selectedNodes.size() == 0) {
				return;
			}
		} else {
			selectedNodes = network.getNodeList();
		}
		int processorCount = Integer.parseInt(PropertiesHelper.getInstance().getThreadcount());
		ExecutorService executor = Executors.newFixedThreadPool(processorCount);
		List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
		for (CyNode node : selectedNodes) {
			TermMappingCallable tmTask = new TermMappingCallable(algorithm, network, node);
			Future<Boolean> future = executor.submit(tmTask);
			futureList.add(future);
		}
		int progress = 1;
		taskMonitor.setProgress((double) progress / selectedNodes.size());
		for (Future<Boolean> future : futureList) {
			if (future.get()) {
				taskMonitor.setProgress((double) progress / selectedNodes.size());
				progress++;
			} else {
				CDRestClient.getInstance().setTaskCanceled(false);
				return;
			}
		}
		System.out.println(
				"Time elapsed by TermMappingTask: " + (System.currentTimeMillis() - startTime) / 1000 + " sec");
	}

	@Override
	public void cancel() {
		CDRestClient.getInstance().setTaskCanceled(true);
		super.cancel();
	}
}
