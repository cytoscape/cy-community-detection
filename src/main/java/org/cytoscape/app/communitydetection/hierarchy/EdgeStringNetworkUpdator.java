package org.cytoscape.app.communitydetection.hierarchy;

import java.util.HashMap;
import java.util.Map;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds nodes and edges to network 
 * @author churas
 */
public class EdgeStringNetworkUpdator {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(EdgeStringNetworkUpdator.class);

	public static final String HYPHEN = "-";
	public static final String SEMI_COLON = ";";
	public static final String COMMUNITY_IDENTIFIER = "c";
	public static final String COMMUNITY_CHAR = COMMUNITY_IDENTIFIER.toUpperCase();
	/**
	 * Given an edge list as a string {@code edgeStr} this method
	 * adds appropriate edges and nodes to {@code newNetwork}
	 * @param parentNetwork parent network
	 * @param newNetwork hierarchy network to update
	 * @param edgeStr edge string which is assumed to be a string in format of
	 *                SOURCE1,TARGET1,NODETYPE;SOURCE2,TARGET2,NODETYPE;...\n
	 *                Visit: https://github.com/cytoscape/communitydetection-rest-server/wiki/COMMUNITYDETECTRESULTV-format
	 *                for full description of format
	 * @return a map of CyNode objects added by this method with the SUIDs of those nodes as keys
	 */
	protected Map<Long, CyNode> updateNetworkWithEdgeString(CyNetwork parentNetwork,
			CyNetwork newNetwork, final String  edgeStr) throws CommunityDetectionException {
		if (parentNetwork == null){
			throw new CommunityDetectionException("Parent network is null");
		}
		if (newNetwork == null){
			throw new CommunityDetectionException("New network is null");
		}
		if (edgeStr == null){
			throw new CommunityDetectionException("Edge list is null");
		}
		Map<Long, CyNode> nMap = new HashMap<>();
		String edges[] = edgeStr.split(SEMI_COLON);
		LOGGER.debug("Found " + edges.length + " edges to load");
		long startTime = System.currentTimeMillis();
		try {
			for (String line : edges) {
				if (line.trim().length() <= 0)
					continue;
				final String[] parts = line.split(AppUtils.EDGE_LIST_SPLIT_PATTERN);
				if (parts.length != 3) {
					throw new CommunityDetectionException("Invalid edge entry: " + line);
				}
				long sourceSUID = Long.parseLong(parts[0]);
				long targetSUID = Long.parseLong(parts[1]);
				String[] interaction = parts[parts.length - 1].split(HYPHEN);

				if (interaction[0].equalsIgnoreCase(COMMUNITY_IDENTIFIER)) {
					CyNode sourceNode = nMap.get(sourceSUID);
					if (sourceNode == null) {
						sourceNode = newNetwork.addNode();
						String sourceName = COMMUNITY_CHAR + sourceSUID;
						newNetwork.getRow(sourceNode).set(CyNetwork.NAME, sourceName);
						nMap.put(sourceSUID, sourceNode);
					}
				}
				if (interaction[1].equalsIgnoreCase(COMMUNITY_IDENTIFIER)) {
					CyNode targetNode = nMap.get(targetSUID);
					if (targetNode == null) {
						targetNode = newNetwork.addNode();
						String targetName = COMMUNITY_CHAR + targetSUID;
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
			}
		} finally {
			LOGGER.debug("Adding nodes and edges to network took: " + (System.currentTimeMillis() - startTime) + " ms");
		}
		return nMap;
	}
	
}
