package org.cytoscape.app.communitydetection.hierarchy;

import java.util.HashMap;
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
	private CyNetworkUtil _cyNetworkUtil;
	
	public CustomDataNetworkUpdator(){
		_cyNetworkUtil = new CyNetworkUtil();
	}
	
	protected void updateNetworkWithCustomData(CyNetwork network, CX2NodeAttributes nodeAttrs, Map<Long, CyNode> nMap) throws CommunityDetectionException {
		Map<String, String> aliasMap = createColumnsSuppliedByAlgorithm(network, nodeAttrs);
		populateColumns(network, nodeAttrs, aliasMap, nMap);
	}
	
	private Map<String, String> createColumnsSuppliedByAlgorithm(CyNetwork network, CX2NodeAttributes nodeAttrs) throws CommunityDetectionException {
		Map<String, String> aliasMap = new HashMap<>();
		CyTable nodeTable = network.getDefaultNodeTable();
		for (CxAttributeDeclaration nad : nodeAttrs.getAttributeDeclarations()){
			for (String decEntryKey : nad.getDeclarations().keySet()){
				if (!decEntryKey.equals(CxNode.ASPECT_NAME)){
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

	private void populateColumns(CyNetwork network, CX2NodeAttributes nodeAttrs,
		Map<String, String> aliasMap, Map<Long, CyNode> nMap){
		for (CxNode node : nodeAttrs.getNodes()){
			for (String nodeAttrKey: node.getAttributes().keySet()){
				if (!aliasMap.containsKey(nodeAttrKey)){
					continue;
				}
				network.getRow(nMap.get(node.getId())).set(aliasMap.get(nodeAttrKey),
						node.getAttributes().get(nodeAttrKey));
			}
		}
	}
	
	/**
	 * Examines {@link org.ndexbio.cx2.aspect.element.core.DeclarationEntry#getDataType()} in
	 * {@code entry} passed in and returns based on these rules:
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
