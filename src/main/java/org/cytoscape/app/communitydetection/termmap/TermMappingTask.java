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
	private final boolean _useSelectedNodes;
	private static final String ON_SELECTED_NODES = "on Selected Nodes";
	private static final String ON_ALL_NODES = "on All Nodes";

	/**
	 * Constructor that creates a Task to run Term Mapping aka 
	 * Functional Enrichment on selected or all nodes of the given network.
	 * 
	 * @param network network to run term mapping on
	 * @param algorithm The algorithm to use
	 * @param customParameters a Map of custom parameters
	 * @param useSelectedNodes If true only process the selected nodes otherwise
	 *                         process all nodes
	 */
	public TermMappingTask(CyNetwork network, CommunityDetectionAlgorithm algorithm,
		Map<String, String> customParameters, boolean useSelectedNodes) {
		this._algorithm = algorithm;
		this._network = network;
		this._customParameters = customParameters;
		this._useSelectedNodes = useSelectedNodes;
	}

	/**
	 * Runs Term Mapping aka Functional Enrichment by sending requests
	 * to CD Service. If the algorithm passed in via constructor is
	 * null then this method does nothing and returns immediately. What
	 * is run by this method is all defined by the constructor of this
	 * class. For improved
	 * @param taskMonitor
	 * @throws Exception 
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (_algorithm == null) {
			return;
		}

		long startTime = System.currentTimeMillis();
		String nodeSelectionStr = getNodeSelectionString();
		
		taskMonitor.setTitle("Community Detection: Term Mapping " +
			nodeSelectionStr);
		taskMonitor.setProgress(0.0);
		
		taskMonitor.setStatusMessage("Running " + _algorithm.getDisplayName() +
			" " + nodeSelectionStr);
				
		// get nodes to process
		List<CyNode> selectedNodes = get_nodes_to_process();
		if (selectedNodes.isEmpty()) {
		    taskMonitor.setStatusMessage("No nodes to process");
		    return;
		}
		
		//adds needed columns to network
		add_columns_to_network();

		// use a threadpool to run TermMappingCallable objects to 
		// perform term mapping
		int processorCount = Integer.parseInt(PropertiesHelper.getInstance().getThreadcount());
		ExecutorService executor = Executors.newFixedThreadPool(processorCount);
		List<Future<Boolean>> futureList = new ArrayList<>();
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
				taskMonitor.setStatusMessage("Running " +
					_algorithm.getDisplayName() + " (" +
					Integer.toString(progress) + totalTaskStr);
				progress++;
			} else {
				CDRestClient.getInstance().setTaskCanceled(false);
				return;
			}
		}
		System.out.println(
				"Time elapsed by TermMappingTask: " +
					(System.currentTimeMillis() - startTime) / 1000 +
					" sec");
	}

	/**
	 * Called when user cancels task. When that happens this object
	 * tells the service that the task has been canceled
	 */
	@Override
	public void cancel() {
		CDRestClient.getInstance().setTaskCanceled(true);
		super.cancel();
	}
	
	/**
	 * Return a human readable string denoting whether this
	 * task will be processing all nodes or just selected nodes
	 * as defined in constructor
	 * @return 
	 */
	private String getNodeSelectionString(){
	    if (this._useSelectedNodes){
		return ON_SELECTED_NODES;
	    }
	    return ON_ALL_NODES;
	}
	
	/**
	 * Gets list of nodes to process either returning all
	 * nodes or selected nodes as defined in constructor
	 * @return 
	 */
	private List<CyNode> get_nodes_to_process(){		
	    if (_useSelectedNodes) {
		return CyTableUtil.getSelectedNodes(_network);
	    }  
	    return _network.getNodeList();
	}
	
	/**
	 * Adds new columns to network that will be filled by this
	 * task
	 */
	private void add_columns_to_network(){
	    if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME) == null) {
		_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME,
			String.class, false, null);
	    }
	    if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS) == null) {
		_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS,
			String.class, false, null);
	    }
	    if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE) == null) {
		_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE,
			Integer.class, false, null);
	    }
	    if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP) == null) {
		_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP,
			Double.class, false, 0.0);
	    }
	    if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_PVALUE) == null) {
		_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_PVALUE,
			Double.class, false, 0.0);
	    }
	    if (_network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_LABELED) == null) {
		_network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_LABELED,
			Boolean.class, true, false);
	    }
	}
}
