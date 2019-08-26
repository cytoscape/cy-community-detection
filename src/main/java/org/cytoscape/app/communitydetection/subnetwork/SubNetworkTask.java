package org.cytoscape.app.communitydetection.subnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.HierarchyHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class SubNetworkTask extends AbstractTask {

	private final CyRootNetworkManager rootNetworkManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final VisualMappingManager visualMappingManager;
	private final CyLayoutAlgorithmManager layoutManager;
	private final SynchronousTaskManager<?> syncTaskManager;
	private final CyNetworkNaming networkNaming;

	private final CyNetwork hierarchyNetwork;
	private final CyNode communityNode;

	public SubNetworkTask(CyRootNetworkManager rootNetworkManager, CyNetworkManager networkManager,
			CyNetworkViewManager networkViewManager, CyNetworkViewFactory networkViewFactory,
			VisualMappingManager visualMappingManager, CyLayoutAlgorithmManager layoutManager,
			SynchronousTaskManager<?> syncTaskManager, CyNetworkNaming networkNaming, CyNetwork hierarchyNetwork,
			CyNode communityNode) {
		this.rootNetworkManager = rootNetworkManager;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.networkViewFactory = networkViewFactory;
		this.visualMappingManager = visualMappingManager;
		this.layoutManager = layoutManager;
		this.syncTaskManager = syncTaskManager;
		this.networkNaming = networkNaming;
		this.hierarchyNetwork = hierarchyNetwork;
		this.communityNode = communityNode;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Community Detection: Creating Subnetwork");
		taskMonitor.setProgress(0.0);
		Long originalNetworkSUID = hierarchyNetwork.getRow(hierarchyNetwork).get(AppUtils.COLUMN_CD_ORIGINAL_NETWORK,
				Long.class);
		CyNetwork originalNetwork = networkManager.getNetwork(originalNetworkSUID);
		Set<CyNode> leafNodes = getMemberList(originalNetwork, communityNode);
		Set<CyEdge> connectingEdges = getEdges(originalNetwork, leafNodes);
		taskMonitor.setProgress(0.2);

		CySubNetwork newNetwork = rootNetworkManager.getRootNetwork(originalNetwork).addSubNetwork();

		String newNetworkName = hierarchyNetwork.getRow(communityNode).get(CyNetwork.NAME, String.class) + "_"
				+ originalNetwork.getRow(originalNetwork).get(CyNetwork.NAME, String.class);
		newNetwork.getRow(newNetwork).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle(newNetworkName));

		addColumns(originalNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS),
				newNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
		addColumns(originalNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS),
				newNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS));
		addColumns(originalNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS),
				newNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));
		taskMonitor.setProgress(0.4);

		for (final CyNode node : leafNodes) {
			newNetwork.addNode(node);
			cloneRow(originalNetwork.getRow(node), newNetwork.getRow(node));
			newNetwork.getRow(node).set(CyNetwork.SELECTED, false);
		}
		for (final CyEdge edge : connectingEdges) {
			newNetwork.addEdge(edge);
			cloneRow(originalNetwork.getRow(edge), newNetwork.getRow(edge));
			newNetwork.getRow(edge).set(CyNetwork.SELECTED, false);
		}
		networkManager.addNetwork(newNetwork);
		taskMonitor.setProgress(0.7);

		CyNetworkView originalNetworkView = null;
		for (CyNetworkView netView : networkViewManager.getNetworkViews(originalNetwork)) {
			originalNetworkView = netView;
			break;
		}
		VisualStyle style = visualMappingManager.getVisualStyle(originalNetworkView);

		CyNetworkView networkView = networkViewFactory.createNetworkView(newNetwork);
		visualMappingManager.setVisualStyle(style, networkView);
		style.apply(networkView);
		CyLayoutAlgorithm layout = layoutManager.getDefaultLayout();
		TaskIterator layoutTasks = layout.createTaskIterator(networkView, layout.createLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
		syncTaskManager.execute(layoutTasks);
		networkViewManager.addNetworkView(networkView);
		taskMonitor.setProgress(1.0);
	}

	private Set<CyEdge> getEdges(CyNetwork network, Set<CyNode> nodes) {
		Set<CyEdge> edgeList = new HashSet<CyEdge>();
		for (CyNode n1 : nodes) {
			for (CyNode n2 : nodes) {
				edgeList.addAll(network.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
			}
		}
		return edgeList;
	}

	private Set<CyNode> getMemberList(CyNetwork originalNetwork, CyNode selectedNode) throws Exception {
		Set<CyNode> leafNodes = HierarchyHelper.getInstance().getMemberList(hierarchyNetwork, selectedNode);
		if (leafNodes == null) {
			leafNodes = new HashSet<CyNode>();
			List<String> memberList = Arrays.asList(hierarchyNetwork.getRow(selectedNode)
					.get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class).split(AppUtils.CD_MEMBER_LIST_DELIMITER));
			if (memberList == null) {
				// TODO create the whole list again
				throw new Exception(AppUtils.COLUMN_CD_MEMBER_LIST + " does not exist!");
			}
			for (CyNode node : originalNetwork.getNodeList()) {
				if (memberList.contains(originalNetwork.getRow(node).get(CyNetwork.NAME, String.class))) {
					leafNodes.add(node);
				}
			}
		}
		return leafNodes;
	}

	private void addColumns(CyTable parentTable, CyTable subTable) {
		List<CyColumn> colsToAdd = new ArrayList<>();

		for (CyColumn col : parentTable.getColumns())
			if (subTable.getColumn(col.getName()) == null)
				colsToAdd.add(col);

		for (CyColumn col : colsToAdd) {
			VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
			if (colInfo.isVirtual())
				addVirtualColumn(col, subTable);
			else
				copyColumn(col, subTable);
		}
	}

	private void addVirtualColumn(CyColumn col, CyTable subTable) {
		VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
		CyColumn checkCol = subTable.getColumn(col.getName());

		if (checkCol == null)
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(),
					colInfo.getTargetJoinKey(), col.isImmutable());

		else if (!checkCol.getVirtualColumnInfo().isVirtual()
				|| !checkCol.getVirtualColumnInfo().getSourceTable().equals(colInfo.getSourceTable())
				|| !checkCol.getVirtualColumnInfo().getSourceColumn().equals(colInfo.getSourceColumn()))
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(),
					colInfo.getTargetJoinKey(), col.isImmutable());
	}

	private void copyColumn(CyColumn col, CyTable subTable) {
		if (List.class.isAssignableFrom(col.getType()))
			subTable.createListColumn(col.getName(), col.getListElementType(), false);
		else
			subTable.createColumn(col.getName(), col.getType(), false);
	}

	private void cloneRow(final CyRow from, final CyRow to) {
		for (final CyColumn column : from.getTable().getColumns()) {
			if (!column.getVirtualColumnInfo().isVirtual())
				to.set(column.getName(), from.getRaw(column.getName()));
		}
	}
}
