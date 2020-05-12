package org.cytoscape.app.communitydetection.hierarchy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class MemberListNetworkUpdator {
	
	
	/**
	 * Creates member list for each node in the hierarchy network.
	 * 
	 * @param hierarchyNetwork
	 * @param parentNetwork
	 * @param helper 
	 */
	protected void createMemberListsInNetwork(CyNetwork hierarchyNetwork,
			CyNetwork parentNetwork, HierarchyHelper helper) throws CommunityDetectionException {
		if (hierarchyNetwork == null){
			throw new CommunityDetectionException("hierarchy network is null");
		}
		if (parentNetwork == null){
			throw new CommunityDetectionException("parent network is null");
		}
		if (helper == null){
			throw new CommunityDetectionException("hierarchy helper is null");
		}
		StringBuilder memberList = new StringBuilder();
		
		for (CyNode node : hierarchyNetwork.getNodeList()) {
			// TODO check if helper.getMemberList can ever return null
			List<CyNode> memberNodes = helper.getMemberList(hierarchyNetwork, node).stream()
					.collect(Collectors.toList());
			Collections.sort(memberNodes, new Comparator<CyNode>() {
				@Override
				public int compare(CyNode node1, CyNode node2) {
					return parentNetwork.getRow(node1).get(CyNetwork.NAME, String.class)
							.compareTo(parentNetwork.getRow(node2).get(CyNetwork.NAME, String.class));
				}
			});
			for (CyNode memberNode : memberNodes) {
				if (memberList.length() > 0) {
					memberList.append(" ");
				}
				String name = parentNetwork.getRow(memberNode).get(CyNetwork.NAME, String.class);
				memberList.append(name);
			}
			hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST, memberList.toString());
			hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, memberNodes.size());
			BigDecimal bd = new BigDecimal(Double.toString(log2(memberNodes.size())));
			BigDecimal roundbd = bd.setScale(3, RoundingMode.HALF_UP);
			hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, roundbd.doubleValue());
			memberList.setLength(0); // clear member list for next node
		}
	}
	
	private double log2(double x) {
		return (Math.log(x) / Math.log(2));
	}
}
