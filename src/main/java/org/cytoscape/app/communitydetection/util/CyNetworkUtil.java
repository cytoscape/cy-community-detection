package org.cytoscape.app.communitydetection.util;

import java.util.Arrays;
import java.util.List;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;


/**
 * Contains utility methods to interact with {@link org.cytoscape.model.CyNetwork}
 * objects
 * @author churas
 */
public class CyNetworkUtil {

	/**
	 * Creates column in table passed in if it does <b>NOT</b> already exist
	 * @param <T> type of column
	 * @param table where column will be added to
	 * @param colName name of column
	 * @param type column type
	 * @param isImmutable denotes if values in new column can be changed
	 * @param defaultValue the default values for cells in new column
	 * @throws CommunityDetectionException if table or column name is {@code null}
	 */
	public <T> void createTableColumn(CyTable table,
			String colName, Class<? extends T> type, boolean isImmutable,
			T defaultValue) throws CommunityDetectionException {
		createTableColumn(table, null, colName,type, isImmutable,
				         defaultValue);
	}
	
	/**
	 * Creates column in table passed in if it does <b>NOT</b> already exist
	 * @param <T> type of column
	 * @param table where column will be added to
	 * @param nameSpace namespace for column
	 * @param colName name of column
	 * @param type column type
	 * @param isImmutable denotes if values in new column can be changed
	 * @param defaultValue the default values for cells in new column
	 * @throws CommunityDetectionException if table or column name is {@code null}
	 */
	public <T> void createTableColumn(CyTable table, String nameSpace,
			String colName, Class<? extends T> type, boolean isImmutable,
			T defaultValue) throws CommunityDetectionException {
		if (table == null){
			throw new CommunityDetectionException("table is null");
		}
		if (colName == null){
			throw new CommunityDetectionException("column name is null");
		}
		if (type == null){
			throw new CommunityDetectionException("type is null");
		}
		
		if (table.getColumn(nameSpace, colName) == null) {
			table.createColumn(nameSpace, colName, type, isImmutable, defaultValue);
		}
	}
	
	/**
	 * Updates 
	 * {@link org.cytoscape.app.communitydetection.util.AppUtils#COLUMN_CD_ORIGINAL_NETWORK}
	 * network attribute in {@code hierarchyNetwork} with SUID of
	 * {@code selectedParentNetwork}
	 * 
	 * @param hierarchyNetwork hierarchy network to update
	 * @param selectedParentNetwork parent network
	 */
	public void updateHierarchySUID(CyNetwork hierarchyNetwork,
			CyNetwork selectedParentNetwork) throws CommunityDetectionException {
		if (hierarchyNetwork == null){
			throw new CommunityDetectionException("hierarchy network is null");
		}
		if (selectedParentNetwork == null){
			throw new CommunityDetectionException("parent network is null");
		}
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_CD_ORIGINAL_NETWORK,
				selectedParentNetwork.getSUID());
	}
	
	/**
	 * This method extracts the list of node names from the 
	 * AppUtils.COLUMN_CD_MEMBER_LIST column for the selected node. This is
	 * assumed to be a string that has AppUtils.CD_MEMBER_LIST_DELIMITER as
	 * a delimiter
	 * 
	 * @param hierarchyNetwork The hierarchy network
	 * @param selectedNode The node to examine
	 * @return List of node members for selected node
	 * @throws Exception 
	 * @throws CommunityDetectionException if hierarchy, or node is {@code null}
	 */
	public List<String> getMemberListForNode(CyNetwork hierarchyNetwork,
			CyNode selectedNode) throws Exception {
		if (hierarchyNetwork == null){
			throw new CommunityDetectionException("hierarchy network is null");
		}
		if (selectedNode == null){
			throw new CommunityDetectionException("selected node is null");
		}
		
		return Arrays.asList(hierarchyNetwork.getRow(selectedNode)
				.get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class).split(AppUtils.CD_MEMBER_LIST_DELIMITER));
	}
	
	public CyNode getNodeMatchingName(CyNetwork network, final String name){
		for (CyNode node : network.getNodeList()){
			if (name.equals(network.getRow(node).get(CyNetwork.NAME, String.class))){
				return node;
			}
		}
		return null;
	}
}
