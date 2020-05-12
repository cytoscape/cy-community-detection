package org.cytoscape.app.communitydetection.hierarchy;

import java.util.LinkedHashSet;
import java.util.Set;
import junit.framework.Assert;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class MemberListNetworkUpdatorTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();
	
	@Test
	public void testCreateMemberListsInNetworkNullChecks(){
		
		CyNetwork hierarchyNetwork = mock(CyNetwork.class);
		CyNetwork parentNetwork = mock(CyNetwork.class);
		HierarchyHelper helper = mock(HierarchyHelper.class);
		MemberListNetworkUpdator updator = new MemberListNetworkUpdator();
		try {
			updator.createMemberListsInNetwork(null, parentNetwork, helper);
		} catch(CommunityDetectionException cde){
			assertEquals("hierarchy network is null", cde.getMessage());
		}
		
		try {
			updator.createMemberListsInNetwork(hierarchyNetwork, null, helper);
		} catch(CommunityDetectionException cde){
			assertEquals("parent network is null", cde.getMessage());
		}
		
		try {
			updator.createMemberListsInNetwork(hierarchyNetwork, parentNetwork, null);
		} catch(CommunityDetectionException cde){
			assertEquals("hierarchy helper is null", cde.getMessage());
		}
	}
	
	@Test
	public void testCreateMemberListsInNetworkEmptyNetwork() throws CommunityDetectionException {
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		CyNetwork parentNetwork = mock(CyNetwork.class);
		HierarchyHelper helper = mock(HierarchyHelper.class);
		MemberListNetworkUpdator updator = new MemberListNetworkUpdator();
		updator.createMemberListsInNetwork(hierarchyNetwork, parentNetwork, helper);
		verifyNoInteractions(parentNetwork);
		verifyNoInteractions(helper);	
	}
	
	@Test
	public void testCreateMemberListsInNetworkSimple() throws CommunityDetectionException {
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		CyNetworkUtil netUtil = new CyNetworkUtil();
		netUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST , String.class, false, null);
		netUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(), AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, Integer.class, false, 0);
		netUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(), AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, Double.class, false, 0.0);
		
		CyNode hNode = hierarchyNetwork.addNode();
		
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNode pNodeOne = parentNetwork.addNode();
		parentNetwork.getRow(pNodeOne).set(CyNetwork.NAME, "nodeb");
		CyNode pNodeTwo = parentNetwork.addNode();
		parentNetwork.getRow(pNodeTwo).set(CyNetwork.NAME, "nodea");
		
		Set<CyNode> pNodes = new LinkedHashSet<>();
		pNodes.add(pNodeOne);
		pNodes.add(pNodeTwo);
		
		HierarchyHelper helper = mock(HierarchyHelper.class);
		when(helper.getMemberList(hierarchyNetwork, hNode)).thenReturn(pNodes);
		
		MemberListNetworkUpdator updator = new MemberListNetworkUpdator();
		updator.createMemberListsInNetwork(hierarchyNetwork, parentNetwork, helper);
		
		String memberList = hierarchyNetwork.getRow(hNode).get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class);
		assertEquals("nodea nodeb", memberList);
		int memberListSize = hierarchyNetwork.getRow(hNode).get(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, Integer.class);
		assertEquals(2, memberListSize);
		
		double logSize = hierarchyNetwork.getRow(hNode).get(AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, Double.class);
		assertEquals(1.0, logSize, 0.001);
	}
}
