package org.cytoscape.app.communitydetection.hierarchy;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.model.CyNetwork;
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
 *
 * @author churas
 */
public class AttributeNetworkUpdator {
	private final static Logger LOGGER = LoggerFactory.getLogger(AttributeNetworkUpdator.class);

	private CyNetworkNaming _networkNaming;
	private CyRootNetworkManager _rootNetworkManager;
	private CyNetworkUtil _networkUtil;
	
	public AttributeNetworkUpdator(CyRootNetworkManager rootNetworkManager,
			CyNetworkNaming networkNaming){
		_rootNetworkManager = rootNetworkManager;
		_networkNaming = networkNaming;
		_networkUtil = new CyNetworkUtil();
		
	}
	
	/**
	 * Creates a name for hierarchy network in format:
	 * ALGORITHM_WEIGHTCOLUMN_PARENTNETWORKNAME
	 * 
	 * @param parentNetwork
	 * @param hierarchyNetwork
	 * @param weightColumn
	 * @param algorithm
	 * @return 
	 */
	private String getNameForHierarchyNetwork(CyNetwork parentNetwork, CyNetwork hierarchyNetwork,
			final String weightColumn, CommunityDetectionAlgorithm algorithm){
		if (weightColumn == null || weightColumn.equals(AppUtils.TYPE_NONE)) {
			return _networkNaming.getSuggestedNetworkTitle(
					algorithm.getName() + "_" + parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class));
		}
		return _networkNaming.getSuggestedNetworkTitle(algorithm.getName() + "_" + weightColumn + "_"
					+ parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class));
	}
	/**
	 * Sets network attributes on {@code hierarchyNetwork} passed in
	 * @param parentNetwork parent network
	 * @param hierarchyNetwork hierarchy network to update with new network attributes
	 * @param weightColumn weight column name or {@code null}
	 * @param algorithm algorithm run to generate hierarchy
	 * @param cdResult raw result data from running algorithm
	 * @param customParameters any custom parameters or {@code null}
	 * @throws CommunityDetectionException thrown if there is a serious problem
	 */
	protected void setNetworkAttributes(CyNetwork parentNetwork, CyNetwork hierarchyNetwork,
			final String weightColumn, CommunityDetectionAlgorithm algorithm,
		CommunityDetectionResult cdResult, Map<String, String> customParameters) throws CommunityDetectionException {

		String name = getNameForHierarchyNetwork(parentNetwork, hierarchyNetwork, weightColumn,
				algorithm);
		CyRootNetwork rootNetwork = _rootNetworkManager.getRootNetwork(hierarchyNetwork);
		rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, name);
		hierarchyNetwork.getRow(hierarchyNetwork).set(CyNetwork.NAME, name);

		String origNetName = parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class);
		String derivedFrom = origNetName;
		
		StringBuilder description = new StringBuilder("Original network: ");
		description.append(origNetName);
		
		description.append("\nAlgorithm used for community detection: ");
		description.append(algorithm.getName());
		
		description.append("\nEdge table column used as weight: ");
		if (weightColumn != null){
		   description.append(weightColumn);
		} else {
		   description.append("no column used");
		}
		
		description.append("\nCustomParameters: ");
		if (customParameters != null){
			description.append(customParameters);
		} else {
			description.append("{}");
		}
		
		String UUID = parentNetwork.getRow(parentNetwork,
				CyNetwork.HIDDEN_ATTRS).get("NDEx UUID", String.class);
		
		if (UUID != null) {
			description.append("\nOriginal network's NDEx UUID: ");
			description.append(UUID);
			derivedFrom += " UUID: " + UUID;
		}
		CyTable netTable = hierarchyNetwork.getDefaultNetworkTable();
		_networkUtil.createTableColumn(netTable, AppUtils.COLUMN_DESCRIPTION, String.class, false, null);
		_networkUtil.createTableColumn(netTable, AppUtils.COLUMN_DERIVED_FROM, String.class, false, null);
		_networkUtil.createTableColumn(netTable, AppUtils.COLUMN_GENERATED_BY, String.class, false, null);
		
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_DESCRIPTION, description.toString());
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_DERIVED_FROM, derivedFrom);
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_GENERATED_BY,
				getGeneratedByString(algorithm));
	}
	
	/**
	 * Creates generated by string
	 * @param algorithm
	 * @return 
	 */
	private String getGeneratedByString(final CommunityDetectionAlgorithm algorithm){
		StringBuilder generatedBy = new StringBuilder();
		generatedBy.append("App: ");
		try {
			Properties properties = new Properties();
			properties.load(getClass().getClassLoader().getResourceAsStream(AppUtils.PROP_NAME + ".props"));
			
			generatedBy.append(properties.getProperty(AppUtils.PROP_PROJECT_NAME));
			generatedBy.append(" (");
			generatedBy.append(properties.getProperty(AppUtils.PROP_PROJECT_VERSION));
			generatedBy.append(")");
		} catch(IOException io){
			LOGGER.error("Unable to get app version and name from properties");
			generatedBy.append("(Unable to get app name/version from properties: ");
			generatedBy.append(io.getMessage());
			generatedBy.append(")");
		}
		generatedBy.append(" Docker Image: ");
		generatedBy.append(algorithm.getDockerImage());
		return generatedBy.toString();
	}
	
}
