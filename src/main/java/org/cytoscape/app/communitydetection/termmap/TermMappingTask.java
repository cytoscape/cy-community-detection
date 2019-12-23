package org.cytoscape.app.communitydetection.termmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

	private CommunityDetectionAlgorithm _algorithm;
	private Map<String, String> _customParameters;
	private final CyNetwork _network;
	private final boolean _isContextMenu;

	public TermMappingTask(CyNetwork network, CommunityDetectionAlgorithm algorithm, Map<String, String> customParameters, boolean isContextMenu) {
		this._algorithm = algorithm;
		this._network = network;
		this._customParameters = customParameters;
		this._isContextMenu = isContextMenu;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (_algorithm == null) {
			return;
		}

		long startTime = System.currentTimeMillis();
		taskMonitor.setTitle("Community Detection: Term Mapping");
		taskMonitor.setProgress(0.0);
		
		taskMonitor.setStatusMessage("Running " + _algorithm.getDisplayName());
		if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME) == null) {
			_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME, String.class, false, null);
		}
		if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS) == null) {
			_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, String.class, false, null);
		}
		if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE) == null) {
			_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE, Integer.class, false, null);
		}
		if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP) == null) {
			_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP, Double.class, false, 0.0);
		}
		if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_PVALUE) == null) {
			_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_PVALUE, Double.class, false, 0.0);
		}
		if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_LABELED) == null) {
			_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_LABELED, Boolean.class, true, false);
		}
		List<CyNode> selectedNodes = CyTableUtil.getSelectedNodes(_network);
		if (_isContextMenu) {
			if (selectedNodes.size() == 0) {
				return;
			}
		} else {
		    if (selectedNodes.size() == 0){
			selectedNodes = _network.getNodeList();
		    }
		}
		int processorCount = Integer.parseInt(PropertiesHelper.getInstance().getThreadcount());
		ExecutorService executor = Executors.newFixedThreadPool(processorCount);
		List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
		for (CyNode node : selectedNodes) {
			TermMappingCallable tmTask = new TermMappingCallable(_network, _algorithm,
				_customParameters, node);
			Future<Boolean> future = executor.submit(tmTask);
			futureList.add(future);
		}
		int progress = 1;
		int totalTasks = selectedNodes.size();
		String totalTaskStr = " of " + Integer.toString(totalTasks) + " complete)";
		taskMonitor.setProgress((double) progress / totalTasks);
		for (Future<Boolean> future : futureList) {
			if (future.get()) {
				taskMonitor.setProgress((double) progress / totalTasks);
				taskMonitor.setStatusMessage("Running " + _algorithm.getDisplayName() + " (" + Integer.toString(progress) + totalTaskStr);
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
