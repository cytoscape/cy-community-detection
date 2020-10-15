package org.cytoscape.app.communitydetection.tally;

import java.util.ArrayList;
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
		taskMonitor.setTitle("Community Detection: Tally Attributes on hierarchy network");
		taskMonitor.setProgress(0.0);
		if (cancelled){
			return;
		}
		
		//create tally columns in hierarchy network
		// plus an unmatched column
		boolean unMatched = true;
		int curVal = 0;
		Object res = null;
		createTallyColumnsInHierarchyNetwork();
		// iterate through memberlist for each node in 
		// hierarchy get fast look up set of node names
		for (CyNode node : _hierarchyNetwork.getNodeList()) {
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
						if (tallyCol.getType() == Boolean.class){
							res = _parentNetwork.getRow(parentNode).get(tallyCol.getNamespace(),
									tallyCol.getName(), tallyCol.getType());
							if (res != null && (boolean)res == true){
								curVal = 1;
							}
						} else {
							
							res = _parentNetwork.getRow(parentNode).get(tallyCol.getNamespace(),
									     tallyCol.getName(), tallyCol.getType());
							if (res != null){
								curVal = (int)res;
							}
						}
						if (curVal > 0){
							unMatched = false;
							addToValueInHierarchy(node, tallyCol.getName(), curVal);
						}
					}
				}
				if (unMatched == true){
					addToValueInHierarchy(node, AppUtils.COLUMN_CD_UNMATCHED, 1);
				}				
			}
		}
		
	}
	
	private void addToValueInHierarchy(CyNode node, final String name, int valueToAdd){
		LOGGER.debug("Attempting to update value in " + name + " column");
		int curVal = _hierarchyNetwork.getRow(node).get(AppUtils.COLUMN_NAMESPACE,
				name, Integer.class);
		_hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_NAMESPACE,
				name, curVal+valueToAdd);
	}
	
	private void createTallyColumnsInHierarchyNetwork() throws Exception {
		CyColumn existingCol = null;
		for (CyColumn col : _tallyColumns){
			LOGGER.debug("Deleting and creating column: " + col.getName());
			existingCol = _hierarchyNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_NAMESPACE,
					col.getName());
			if (existingCol != null){
				_hierarchyNetwork.getDefaultNodeTable().deleteColumn(AppUtils.COLUMN_NAMESPACE,
					col.getName());
			}
			_cyNetworkUtil.createTableColumn(_hierarchyNetwork.getDefaultNodeTable(),
					AppUtils.COLUMN_NAMESPACE, col.getName(), Integer.class, false, 0);
		}
		
		existingCol = _hierarchyNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_NAMESPACE,
					AppUtils.COLUMN_CD_UNMATCHED);
		if (existingCol != null){
			_hierarchyNetwork.getDefaultNodeTable().deleteColumn(AppUtils.COLUMN_NAMESPACE,
				AppUtils.COLUMN_CD_UNMATCHED);
		}
		
		_cyNetworkUtil.createTableColumn(_hierarchyNetwork.getDefaultNodeTable(),
					AppUtils.COLUMN_NAMESPACE, AppUtils.COLUMN_CD_UNMATCHED,
					Integer.class, false, 0);
	}

	@Override
	public void cancel() {
		super.cancel(); //To change body of generated methods, choose Tools | Templates.
	}
	
	
}
