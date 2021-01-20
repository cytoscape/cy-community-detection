package org.cytoscape.app.communitydetection.tally;

import java.util.Collection;
import java.util.List;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class TallyTask extends AbstractTask {

	public static final int ONE = 1;
	private final static Logger LOGGER = LoggerFactory.getLogger(TallyTask.class);
	
	private CyNetwork _parentNetwork;
	private CyNetwork _hierarchyNetwork;
	private List<CyColumn> _tallyColumns;
	private CyNetworkUtil _cyNetworkUtil;

	public TallyTask(CyNetworkUtil cyNetworkUtil,
			CyNetwork parentNetwork, CyNetwork hierarchyNetwork,
			List<CyColumn> tallyColumns){
		_cyNetworkUtil = cyNetworkUtil;
		_parentNetwork = parentNetwork;
		_hierarchyNetwork = hierarchyNetwork;
		_tallyColumns = tallyColumns;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Community Detection: Tally columns on hierarchy network");
		taskMonitor.setProgress(0.0);
		if (cancelled){
			taskMonitor.setStatusMessage("User cancelled Task");
			return;
		}
		
		//create tally columns in hierarchy network
		// plus an unmatched column
		boolean unMatched = true;
		int curVal = 0;
		Object res = null;
		taskMonitor.setStatusMessage("Remove existing and add new tally columns");
		removeExistingTallyColumns();
		createTallyColumnsInHierarchyNetwork();
		if (cancelled){
			taskMonitor.setStatusMessage("User cancelled Task");
			return;
		}
		
		taskMonitor.setStatusMessage("Iterate over cluster nodes in "
				+ "hierarchy to generate tally columns");
		// iterate through memberlist for each node in 
		// hierarchy get fast look up set of node names
		int nodeCount = _hierarchyNetwork.getNodeCount();
		int counter = 0;
		for (CyNode node : _hierarchyNetwork.getNodeList()) {
			taskMonitor.setProgress((double)counter/(double)nodeCount);
			counter++;
			
			if (cancelled){
				taskMonitor.setStatusMessage("User cancelled Task");
				return;
			}
			
			List<String> memberList = _cyNetworkUtil.getMemberListForNode(_hierarchyNetwork, node);
			if (memberList == null){
				continue;
			}
			for (String member : memberList){
				unMatched = true;
				for (CyColumn tallyCol : this._tallyColumns){
					CyNode parentNode = _cyNetworkUtil.getNodeMatchingName(_parentNetwork, member);
					
					if (parentNode != null){
						curVal = 0;
						res = _parentNetwork.getRow(parentNode).get(tallyCol.getNamespace(),
									tallyCol.getName(), tallyCol.getType());
						if (tallyCol.getType() == Boolean.class){
							if (res != null && (boolean)res == true){
								curVal = ONE;
							}
						} else if (tallyCol.getType() == Integer.class) {
							if (res != null){
								curVal = (int)res;
							}
						} else if (tallyCol.getType() == Double.class){
							if (res != null){
								Long lVal = Math.round((double)res);
								curVal =  lVal.intValue();
							}
						}
						if (curVal > 0){
							unMatched = false;
							addToValueInHierarchy(node, tallyCol.getName(), ONE);
						}
					}
				}
				if (unMatched == true){
					addToValueInHierarchy(node, AppUtils.COLUMN_CD_UNMATCHED, ONE);
				}				
			}
		}
		
	}
	
	/**
	 * Adds value to column in hierarchy by adding {@code valueToAdd} to existing value
	 * 
	 * @param node node to modify
	 * @param name column to modify
	 * @param valueToAdd value to add to existing value
	 */
	private void addToValueInHierarchy(CyNode node, final String name, int valueToAdd){
		int curVal = _hierarchyNetwork.getRow(node).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
				name, Integer.class);
		_hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
				name, curVal+valueToAdd);
	}
	
	/**
	 * Removes any columns in hierarchy with AppUtils.COLUMN_CD_TALLY_NAMESPACE namespace
	 * @throws Exception 
	 */
	private void removeExistingTallyColumns() throws Exception {
		Collection<CyColumn> oldTallyCols = _hierarchyNetwork.getDefaultNodeTable().getColumns(AppUtils.COLUMN_CD_TALLY_NAMESPACE);
		if (oldTallyCols == null){
			LOGGER.debug("No columns with namespace: "
					+ AppUtils.COLUMN_CD_TALLY_NAMESPACE
					+ " found. Skipping delete.");
			return;
		}
		for (CyColumn col : oldTallyCols){
			LOGGER.debug("Removing old tally column: " + col.getNamespace() + "::" + col.getNameOnly());
			_hierarchyNetwork.getDefaultNodeTable().deleteColumn(col.getNamespace(),
				col.getNameOnly());
		}
	}
	
	/**
	 * Creates the tally columns in hierarchy setting type to {@code Integer.class} and 
	 * default value to {@code 0}. The columns are added to AppUtils.COLUMN_CD_TALLY_NAMESPACE
	 * namespace.
	 * @throws Exception 
	 */
	private void createTallyColumnsInHierarchyNetwork() throws Exception {
		for (CyColumn col : _tallyColumns){
			LOGGER.debug("Creating column: " + col.getName());
			_cyNetworkUtil.createTableColumn(_hierarchyNetwork.getDefaultNodeTable(),
					AppUtils.COLUMN_CD_TALLY_NAMESPACE, col.getName(), Integer.class, false, 0);
		}
		_cyNetworkUtil.createTableColumn(_hierarchyNetwork.getDefaultNodeTable(),
					AppUtils.COLUMN_CD_TALLY_NAMESPACE, AppUtils.COLUMN_CD_UNMATCHED,
					Integer.class, false, 0);
	}

	@Override
	public void cancel() {
		super.cancel();
	}
	
	
}
