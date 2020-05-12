package org.cytoscape.app.communitydetection.hierarchy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributes;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxNode;
import org.ndexbio.cx2.aspect.element.core.DeclarationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds any custom data provided by Community Detection algorithm as new column(s)
 * in the node table
 * @author churas
 */
public class CustomDataNetworkUpdator {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CustomDataNetworkUpdator.class);
	private final CyNetworkUtil _cyNetworkUtil;
	
	/**
	 * Constructor
	 */
	public CustomDataNetworkUpdator(){
		_cyNetworkUtil = new CyNetworkUtil();
	}
	
	/**
	 * Using {@code nodeAttrs} as input this method creates one or more columns in
	 * the node table
	 * @param network The network where the columns and data will be added
	 * @param nodeAttrs Data to add
	 * @param nMap Map of node SUID => CyNode
	 * @throws CommunityDetectionException if there are errors
	 */
	protected void updateNetworkWithCustomData(CyNetwork network,
			CX2NodeAttributes nodeAttrs, Map<Long, CyNode> nMap) throws CommunityDetectionException {
		if (network == null){
			throw new CommunityDetectionException("network is null");
		}
		if (nodeAttrs == null){
			throw new CommunityDetectionException("nodeAttrs is null");
		}
		if (nMap == null){
			throw new CommunityDetectionException("node map is null");
		}
		long startTime = System.currentTimeMillis();
		try {
			Map<String, String> aliasMap = createColumnsSuppliedByAlgorithm(network, nodeAttrs);
			populateColumns(network, nodeAttrs, aliasMap, nMap);
		} finally {
			LOGGER.debug("Populating custom data took: " + (System.currentTimeMillis() - startTime) + " ms");
		}
	}
	
	/**
	 * Adds data to node column(s) from {@code nodeAttrs} to {@code network} passed
	 * in. 
	 * @param network
	 * @param nodeAttrs
	 * @return
	 * @throws CommunityDetectionException if attribute declarations are 
	 *         missing in {@code nodeAttrs}
	 */
	private Map<String, String> createColumnsSuppliedByAlgorithm(CyNetwork network,
			CX2NodeAttributes nodeAttrs) throws CommunityDetectionException {

		List<CxAttributeDeclaration> attribDeclarations = nodeAttrs.getAttributeDeclarations();
		if (attribDeclarations == null){
			throw new CommunityDetectionException("Attribute Declarations missing");
		}
		Map<String, String> aliasMap = new HashMap<>();
		CyTable nodeTable = network.getDefaultNodeTable();

		for (CxAttributeDeclaration nad : attribDeclarations){
			for (String decEntryKey : nad.getDeclarations().keySet()){
				if (!decEntryKey.equals(CxNode.ASPECT_NAME)){
					LOGGER.warn("Ignoring unexpected attribute declaration: "
							+ decEntryKey);
					continue;
				}
				Map<String, DeclarationEntry> decEntryMap = nad.getDeclarations().get(decEntryKey);
				for (String attrName : decEntryMap.keySet()){
					DeclarationEntry entry = decEntryMap.get(attrName);
					entry.processValue();
					LOGGER.debug(attrName + " alias: " + entry.getAlias()
							+ " datatype: " + entry.getDataTypeStr()
							+ " default: " + entry.getDefaultValue());
					if (entry.getAlias() != null){
						aliasMap.put(entry.getAlias(), attrName);
					} else {
						aliasMap.put(attrName, attrName);
					}
					_cyNetworkUtil.createTableColumn(nodeTable, attrName,
							getColTypeForDeclarationEntry(entry), false, entry.getDefaultValue());
				}
			}
		}
		return aliasMap;
	}

	/**
	 * Fill columns with data
	 * @param network Network to update
	 * @param nodeAttrs Data to load
	 * @param aliasMap Map of column name to alias name
	 * @param nMap Map of node SUID => CyNode
	 */
	private void populateColumns(CyNetwork network, CX2NodeAttributes nodeAttrs,
		Map<String, String> aliasMap, Map<Long, CyNode> nMap){
		boolean nodeUpdated = false;
		long nodeCount = 0;
		for (CxNode node : nodeAttrs.getNodes()){
			nodeUpdated = false;
			for (String nodeAttrKey: node.getAttributes().keySet()){
				if (!aliasMap.containsKey(nodeAttrKey)){
					continue;
				}
				network.getRow(nMap.get(node.getId())).set(aliasMap.get(nodeAttrKey),
						node.getAttributes().get(nodeAttrKey));
				nodeUpdated = true;
			}
			if (nodeUpdated == true){
				nodeCount++;
			}
		}
		LOGGER.debug("Updated column(s) in " + nodeCount + " nodes");
	}
	
	/**
	 * Examines
	 * {@link org.ndexbio.cx2.aspect.element.core.DeclarationEntry#getDataType()}
	 * in {@code entry} passed in and returns based on these rules:
	 *
	 * INTEGER => {@code Integer.class}
	 * LONG => {@code Long.class}
	 * BOOLEAN => {@code Boolean.class}
	 * DOUBLE => {@code Double.class}
	 * if none of above return {@code String.class}
	 * @param entry
	 * @return Appropriate class for datatype in {@code entry} with fallback being {@code String.class}
	 */
	private Class getColTypeForDeclarationEntry(DeclarationEntry entry){
		if (entry.getDataType() == null){
			return String.class;
		}
		switch (entry.getDataType()) {
			case INTEGER:
				return Integer.class;
			case LONG:
				return Long.class;
			case BOOLEAN:
				return Boolean.class;
			case DOUBLE:
				return Double.class;
			default:
				return String.class;
		}
	}
}
