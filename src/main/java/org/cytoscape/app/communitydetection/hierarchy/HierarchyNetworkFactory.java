package org.cytoscape.app.communitydetection.hierarchy;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributes;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributesFactory;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxNode;
import org.ndexbio.cx2.aspect.element.core.DeclarationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class HierarchyNetworkFactory {
	private final static Logger LOGGER = LoggerFactory.getLogger(HierarchyNetworkFactory.class);
	private CyNetworkFactory _cyNetworkFactory;
	private CyNetworkNaming _networkNaming;
	private CyRootNetworkManager _rootNetworkManager;
	private CyNetworkManager _networkManager;
	public HierarchyNetworkFactory(CyNetworkFactory cyNetworkFactory,
			CyNetworkNaming networkNaming,
			CyRootNetworkManager rootNetworkManager,
			CyNetworkManager networkManager){
		_cyNetworkFactory = cyNetworkFactory;
		_networkNaming = networkNaming;
		_rootNetworkManager = rootNetworkManager;
		_networkManager = networkManager;
	}
	
	public CyNetwork getHierarchyNetwork(CyNetwork parentNetwork, CommunityDetectionResult cdResult,
				final String weightColumn, CommunityDetectionAlgorithm algorithm,
				Map<String, String> customParameters){	

		CyNetwork newNetwork = _cyNetworkFactory.createNetwork();

		createTableColumn(newNetwork.getDefaultNetworkTable(), AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class, false,
				parentNetwork.getSUID());
		CyTable nodeTable = newNetwork.getDefaultNodeTable();
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, Integer.class, false, 0);
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, Double.class, false, 0.0);
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_COMMUNITY_NAME, String.class, false, null);
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, String.class, false, null);
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE, Integer.class, false, 0);
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_OVERLAP, Double.class, false, 0.0);
		createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_PVALUE, Double.class, false, 0.0);

		createTableColumn(nodeTable, AppUtils.COLUMN_CD_LABELED, Boolean.class, true, false);
		
		String edgeStr;
		JsonNode nodeAttrs = null;
		if (cdResult.getResult().isContainerNode()){
			LOGGER.debug("This node is a container node");
			
			JsonNode cdR = cdResult.getResult().get(AppUtils.CD_ALGORITHM_OUTPUT_EDGELIST_KEY);
			if (cdR == null){
				LOGGER.error("No " + AppUtils.CD_ALGORITHM_OUTPUT_EDGELIST_KEY
						+ " found in container " + cdResult.getResult().asText());
				return null;
			}
			nodeAttrs = cdResult.getResult().get("nodeAttributesAsCX2");
			edgeStr = cdR.asText().trim();
		} else {
			LOGGER.debug("Node is NOT a container node");
			edgeStr = cdResult.getResult().asText().trim();
		}
		
		Map<Long, CyNode> nMap = createNetworkFromEdgeList(parentNetwork, newNetwork, edgeStr);
		annotateNetwork(newNetwork, nMap, nodeAttrs);

		_networkManager.addNetwork(newNetwork);
		createMemberList(newNetwork, parentNetwork);
		setNetworkAttributes(parentNetwork, newNetwork, weightColumn, algorithm, cdResult, customParameters);
		return newNetwork;
	}

	private void annotateNetwork(CyNetwork network, Map<Long, CyNode> nMap, JsonNode nodeAttrsAsCX2){
		if (nodeAttrsAsCX2 == null){
			return;
		}
		CX2NodeAttributesFactory nodeAttrFac = new CX2NodeAttributesFactory();
		CX2NodeAttributes nodeAttrs = nodeAttrFac.getCX2NodeAttributes(nodeAttrsAsCX2);
		if (nodeAttrs == null){
			LOGGER.error("Errors parsing nodeAttributes");
		}
		Map<String, String> aliasMap = createColumnsSuppliedByAlgorithm(network, nodeAttrs);
		populateColumns(network, nodeAttrs, aliasMap, nMap);

	}
	
	private Map<String, String> createColumnsSuppliedByAlgorithm(CyNetwork network, CX2NodeAttributes nodeAttrs){
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
					createTableColumn(nodeTable, attrName,
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
	
	private Map<Long, CyNode> createNetworkFromEdgeList(CyNetwork parentNetwork, CyNetwork newNetwork, final String  edgeStr){
		Map<Long, CyNode> nMap = new HashMap<>();
		String edges[] = edgeStr.split(";");
		
		for (String line : edges) {
			if (line.trim().length() <= 0)
				continue;
			final String[] parts = line.split(AppUtils.EDGE_LIST_SPLIT_PATTERN);
			if (parts.length >= 3) {
				long sourceSUID = Long.parseLong(parts[0]);
				long targetSUID = Long.parseLong(parts[1]);
				String[] interaction = parts[parts.length - 1].split("-");

				if (interaction[0].equalsIgnoreCase("c")) {
					CyNode sourceNode = nMap.get(sourceSUID);
					if (sourceNode == null) {
						sourceNode = newNetwork.addNode();
						String sourceName = "C" + sourceSUID;
						newNetwork.getRow(sourceNode).set(CyNetwork.NAME, sourceName);
						nMap.put(sourceSUID, sourceNode);
					}
				}
				if (interaction[1].equalsIgnoreCase("c")) {
					CyNode targetNode = nMap.get(targetSUID);
					if (targetNode == null) {
						targetNode = newNetwork.addNode();
						String targetName = "C" + targetSUID;
						newNetwork.getRow(targetNode).set(CyNetwork.NAME, targetName);
						nMap.put(targetSUID, targetNode);
					}
					CyEdge edge = newNetwork.addEdge(nMap.get(sourceSUID), targetNode, true);
					newNetwork.getRow(edge).set(CyEdge.INTERACTION, parts[parts.length - 1]);
					HierarchyHelper.getInstance().addChildNode(nMap.get(sourceSUID), targetNode);
				} else {
					HierarchyHelper.getInstance().addMemberNode(parentNetwork.getNode(targetSUID));
					HierarchyHelper.getInstance().addChildNode(nMap.get(sourceSUID),
							parentNetwork.getNode(targetSUID));
				}
			} else {
				JOptionPane.showMessageDialog(null, "Incorrect number of columns!");
			}

		}
		return nMap;
	}
	
	public void setNetworkAttributes(CyNetwork parentNetwork, CyNetwork hierarchyNetwork,
			final String weightColumn, CommunityDetectionAlgorithm algorithm,
		CommunityDetectionResult cdResult, Map<String, String> customParameters) {

		String name;
		if (weightColumn == null || weightColumn.equals(AppUtils.TYPE_NONE)) {
			name = _networkNaming.getSuggestedNetworkTitle(
					algorithm.getName() + "_" + parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class));
		} else {
			name = _networkNaming.getSuggestedNetworkTitle(algorithm.getName() + "_" + weightColumn + "_"
					+ parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class));
		}
		CyRootNetwork rootNetwork = _rootNetworkManager.getRootNetwork(hierarchyNetwork);
		rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, name);
		hierarchyNetwork.getRow(hierarchyNetwork).set(CyNetwork.NAME, name);

		String origNetName = parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class);
		String derivedFrom = origNetName;
		StringBuilder description = new StringBuilder("Original network: " + origNetName + "\n");
		description.append("Algorithm used for community detection: " + algorithm.getName() + "\n");
		
    		   description.append("Edge table column used as weight: ");
		if (weightColumn != null){
		   description.append(weightColumn);
		   description.append("\n");
		} else {
		   description.append("no column used\n");
		}
		description.append("CustomParameters: ");
		description.append(customParameters);
		description.append("\n");
		String UUID = parentNetwork.getRow(parentNetwork, CyNetwork.HIDDEN_ATTRS).get("NDEx UUID", String.class);
		if (UUID != null) {
			description.append("Original network's NDEx UUID: " + UUID);
			derivedFrom += " UUID: " + UUID;
		}
		String generatedBy = "";
		try {
			Properties properties = new Properties();
			properties.load(getClass().getClassLoader().getResourceAsStream(AppUtils.PROP_NAME + ".props"));
			generatedBy += "App: " + properties.getProperty(AppUtils.PROP_PROJECT_NAME);
			generatedBy += " (" + properties.getProperty(AppUtils.PROP_PROJECT_VERSION) + ")";
		} catch(IOException io){
			
		}
		generatedBy += " Docker Image: " + algorithm.getDockerImage();

		CyTable netTable = hierarchyNetwork.getDefaultNetworkTable();
		createTableColumn(netTable, AppUtils.COLUMN_DESCRIPTION, String.class, false, null);
		createTableColumn(netTable, AppUtils.COLUMN_DERIVED_FROM, String.class, false, null);
		createTableColumn(netTable, AppUtils.COLUMN_GENERATED_BY, String.class, false, null);
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_DESCRIPTION, description.toString());
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_DERIVED_FROM, derivedFrom);
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_GENERATED_BY, generatedBy);
	}

	/**
	 * Creates member list for each node in the hierarchy network.
	 * 
	 * @param hierarchyNetwork
	 */
	private void createMemberList(CyNetwork hierarchyNetwork, CyNetwork parentNetwork) {
		for (CyNode node : hierarchyNetwork.getNodeList()) {
			List<CyNode> memberNodes = HierarchyHelper.getInstance().getMemberList(hierarchyNetwork, node).stream()
					.collect(Collectors.toList());
			Collections.sort(memberNodes, new Comparator<CyNode>() {
				@Override
				public int compare(CyNode node1, CyNode node2) {
					return parentNetwork.getRow(node1).get(CyNetwork.NAME, String.class)
							.compareTo(parentNetwork.getRow(node2).get(CyNetwork.NAME, String.class));
				}
			});
			StringBuffer memberList = new StringBuffer();
			for (CyNode memberNode : memberNodes) {
				if (memberList.length() > 0) {
					memberList.append(" ");
				}
				String name = parentNetwork.getRow(memberNode).get(CyNetwork.NAME, String.class);
				memberList.append(name);
			}
			if (memberList != null) {
				hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST, memberList.toString());
				hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, memberNodes.size());
				BigDecimal bd = new BigDecimal(Double.toString(log2(memberNodes.size())));
				BigDecimal roundbd = bd.setScale(3, RoundingMode.HALF_UP);
				hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, roundbd.doubleValue());
			}
		}
		HierarchyHelper.getInstance().clearAll();
	}

	<T> void createTableColumn(CyTable table, String colName, Class<? extends T> type, boolean isImmutable,
			T defaultValue) {
		if (table.getColumn(colName) == null) {
			table.createColumn(colName, type, isImmutable, defaultValue);
		}
	}

	private double log2(double x) {
		return (Math.log(x) / Math.log(2));
	}
}

