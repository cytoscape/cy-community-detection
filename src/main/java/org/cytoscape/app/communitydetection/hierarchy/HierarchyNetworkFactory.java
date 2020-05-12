package org.cytoscape.app.communitydetection.hierarchy;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributes;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributesFactory;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
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
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates Hierarchy Network from Community Detection algorithm result
 * @author churas
 */
public class HierarchyNetworkFactory {
	private final static Logger LOGGER = LoggerFactory.getLogger(HierarchyNetworkFactory.class);
	private CyNetworkFactory _cyNetworkFactory;
	private CyNetworkNaming _networkNaming;
	private CyRootNetworkManager _rootNetworkManager;
	private CyNetworkManager _networkManager;
	private EdgeStringNetworkUpdator _networkUpdator;
	private CyNetworkUtil _cyNetworkUtil;
	private CustomDataNetworkUpdator _customDataNetworkUpdator;
	private MemberListNetworkUpdator _memberListNetworkUpdator;
	
	public HierarchyNetworkFactory(CyNetworkFactory cyNetworkFactory,
			CyNetworkNaming networkNaming,
			CyRootNetworkManager rootNetworkManager,
			CyNetworkManager networkManager){
		_cyNetworkFactory = cyNetworkFactory;
		_networkNaming = networkNaming;
		_rootNetworkManager = rootNetworkManager;
		_networkManager = networkManager;
		_networkUpdator = new EdgeStringNetworkUpdator();
		_cyNetworkUtil = new CyNetworkUtil();
		_customDataNetworkUpdator = new CustomDataNetworkUpdator();
		_memberListNetworkUpdator = new MemberListNetworkUpdator();
	}

	/**
	 * Sets alternate updator
	 * @param updator 
	 */
	protected void setAlternateEdgeStringNetworkUpdator(EdgeStringNetworkUpdator updator){
		_networkUpdator = updator;
	}
	
	protected void setAlternateCyNetworkUtil(CyNetworkUtil networkUtil){
		_cyNetworkUtil = networkUtil;
	}
	
	protected void setAlternateCustomDataNetworkUpdator(CustomDataNetworkUpdator updator){
		_customDataNetworkUpdator = updator;
	}
	
	/**
	 * Creates Hierarchy Network from {@code cdResult}
	 * @param parentNetwork parent network for hierarchy
	 * @param cdResult Community Detection algorithm result to parse
	 * @param weightColumn Name of weight column used or {@code null} if none
	 * @param algorithm The Community Detection Algorithm run
	 * @param customParameters Any custom parameters or null/empty map if none
	 * @return Hierarchy network with proper columns and annotations added
	 */
	public CyNetwork getHierarchyNetwork(CyNetwork parentNetwork, CommunityDetectionResult cdResult,
				final String weightColumn, CommunityDetectionAlgorithm algorithm,
				Map<String, String> customParameters) throws CommunityDetectionException {	

		CyNetwork newNetwork = _cyNetworkFactory.createNetwork();

		_cyNetworkUtil.createTableColumn(newNetwork.getDefaultNetworkTable(), AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class, false,
				parentNetwork.getSUID());
		CyTable nodeTable = newNetwork.getDefaultNodeTable();
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_COMMUNITY_NAME, String.class, false, null);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, String.class, false, null);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE, Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_OVERLAP, Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_ANNOTATED_PVALUE, Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(nodeTable, AppUtils.COLUMN_CD_LABELED, Boolean.class, true, false);
		
		CyTable netTable = newNetwork.getDefaultNetworkTable();
		_cyNetworkUtil.createTableColumn(netTable, AppUtils.COLUMN_DESCRIPTION, String.class, false, null);
		_cyNetworkUtil.createTableColumn(netTable, AppUtils.COLUMN_DERIVED_FROM, String.class, false, null);
		_cyNetworkUtil.createTableColumn(netTable, AppUtils.COLUMN_GENERATED_BY, String.class, false, null);
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
		
		Map<Long, CyNode> nMap = _networkUpdator.updateNetworkWithEdgeString(parentNetwork, newNetwork, edgeStr);
		
		annotateNetwork(newNetwork, nMap, nodeAttrs);

		_networkManager.addNetwork(newNetwork);
		_memberListNetworkUpdator.createMemberListsInNetwork(newNetwork, parentNetwork, HierarchyHelper.getInstance());
		HierarchyHelper.getInstance().clearAll();
		setNetworkAttributes(parentNetwork, newNetwork, weightColumn, algorithm, cdResult, customParameters);
		return newNetwork;
	}

	private void annotateNetwork(CyNetwork network, Map<Long, CyNode> nMap, JsonNode nodeAttrsAsCX2) throws CommunityDetectionException {
		if (nodeAttrsAsCX2 == null){
			return;
		}
		CX2NodeAttributesFactory nodeAttrFac = new CX2NodeAttributesFactory();
		CX2NodeAttributes nodeAttrs = nodeAttrFac.getCX2NodeAttributes(nodeAttrsAsCX2);
		if (nodeAttrs == null){
			LOGGER.error("Errors parsing nodeAttributes");
		}
		_customDataNetworkUpdator.updateNetworkWithCustomData(network, nodeAttrs, nMap);
	}

	protected void setNetworkAttributes(CyNetwork parentNetwork, CyNetwork hierarchyNetwork,
			final String weightColumn, CommunityDetectionAlgorithm algorithm,
		CommunityDetectionResult cdResult, Map<String, String> customParameters) throws CommunityDetectionException {

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

		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_DESCRIPTION, description.toString());
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_DERIVED_FROM, derivedFrom);
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_GENERATED_BY, generatedBy);
	}
}

