package org.cytoscape.app.communitydetection.hierarchy;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributes;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributesFactory;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
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
	private AttributeNetworkUpdator _attributeNetworkUpdator;
	
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
		_attributeNetworkUpdator = new AttributeNetworkUpdator(_rootNetworkManager, _networkNaming);
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
	
	protected void setAlternateMemberListNetworkUpdator(MemberListNetworkUpdator updator){
		_memberListNetworkUpdator = updator;
	}
	
	protected void setAlternateAttributeNetworkUpdator(AttributeNetworkUpdator updator){
		_attributeNetworkUpdator = updator;
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

		if (parentNetwork == null){
			throw new CommunityDetectionException("parent network is null");
		}
		if (cdResult == null){
			throw new CommunityDetectionException("community detection object is null");
		}
		if (algorithm == null){
			throw new CommunityDetectionException("algorithm is null");
		}
		
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

		String edgeStr;
		JsonNode nodeAttrs = null;
		
		if (cdResult.getResult() == null){
			throw new CommunityDetectionException("community detection result is null");
		}
		if (cdResult.getResult().isContainerNode()){
			LOGGER.debug("This node is a container node");
			
			JsonNode cdR = cdResult.getResult().get(AppUtils.CD_ALGORITHM_OUTPUT_EDGELIST_KEY);
			if (cdR == null){
				throw new CommunityDetectionException("No " + AppUtils.CD_ALGORITHM_OUTPUT_EDGELIST_KEY
						+ " found in JSON output");
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
		_attributeNetworkUpdator.setNetworkAttributes(parentNetwork, newNetwork, weightColumn, algorithm, cdResult, customParameters);
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
}

