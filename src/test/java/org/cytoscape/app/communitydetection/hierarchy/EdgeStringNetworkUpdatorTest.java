package org.cytoscape.app.communitydetection.hierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class EdgeStringNetworkUpdatorTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();

	@Test
	public void testUpdateNetworkWithEdgeStringWithNullArgs(){
		CyNetwork parentNetwork = mock(CyNetwork.class);
		CyNetwork newNetwork = mock(CyNetwork.class);
		String edgeStr = "hi";
		EdgeStringNetworkUpdator updator = new EdgeStringNetworkUpdator();
		try {
			updator.updateNetworkWithEdgeString(null, newNetwork, edgeStr);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("Parent network is null", cde.getMessage());
		}

		try {
			updator.updateNetworkWithEdgeString(parentNetwork, null, edgeStr);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("New network is null", cde.getMessage());
		}
		
		try {
			updator.updateNetworkWithEdgeString(parentNetwork, newNetwork, null);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("Edge list is null", cde.getMessage());
		}
		verifyNoInteractions(parentNetwork);
		verifyNoInteractions(newNetwork);
	}
	
	@Test
	public void testUpdateNetworkWithEdgeStringWithEmptyString() throws CommunityDetectionException {
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNetwork newNetwork = _nts.getNetwork();
		EdgeStringNetworkUpdator updator = new EdgeStringNetworkUpdator();
		Map<Long, CyNode> res = updator.updateNetworkWithEdgeString(parentNetwork, newNetwork, "");
		assertEquals(0, res.size());
		
	}
	
	@Test
	public void testUpdateNetworkWithEdgeStringWithInvalidString() throws CommunityDetectionException {
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNetwork newNetwork = _nts.getNetwork();
		EdgeStringNetworkUpdator updator = new EdgeStringNetworkUpdator();
		try {
			updator.updateNetworkWithEdgeString(parentNetwork, newNetwork, "hi,bye;there");
			fail("Expected Exception");
		} catch(CommunityDetectionException cde){
			assertEquals("Invalid edge entry: hi,bye", cde.getMessage());
		}
	}
	
	/**
	 * Testing a successful edge list of format
	 * @throws CommunityDetectionException 
	 */
	@Test
	public void testUpdateNetworkSuccess() throws CommunityDetectionException {
		CyNetwork parentNetwork = _nts.getNetwork();
		
		CyNode pNodeOne = parentNetwork.addNode();
		parentNetwork.getRow(pNodeOne).set(CyNetwork.NAME, "one");
		
		CyNode pNodeTwo = parentNetwork.addNode();
		parentNetwork.getRow(pNodeTwo).set(CyNetwork.NAME, "two");

		CyNode pNodeThree = parentNetwork.addNode();
		parentNetwork.getRow(pNodeThree).set(CyNetwork.NAME, "three");

		CyNode pNodeFour = parentNetwork.addNode();
		parentNetwork.getRow(pNodeFour).set(CyNetwork.NAME, "four");

		CyNetwork newNetwork = _nts.getNetwork();
		EdgeStringNetworkUpdator updator = new EdgeStringNetworkUpdator();
		
		List<Long> parentNodeSUIDs = new ArrayList<>();
		for (CyNode pNode: parentNetwork.getNodeList()){
			parentNodeSUIDs.add(pNode.getSUID());
		}
		long nextNodeId = Collections.max(parentNodeSUIDs) + 1;

		long newNodeOne = nextNodeId++;
		long newNodeTwo = nextNodeId++;
		String edgeStr = newNodeOne + "," + newNodeTwo + ",c-c;";
		boolean first = true;
		for (long nodeId: parentNodeSUIDs){
			if (first == true){
				edgeStr += newNodeOne;
				first = false;
			} else {
				edgeStr += newNodeTwo;
			}
			edgeStr += "," + nodeId + ",c-m;";
		}
		edgeStr += ";";
		HierarchyHelper helper = HierarchyHelper.getInstance();
		helper.clearAll();
		Map<Long, CyNode> nMap = updator.updateNetworkWithEdgeString(parentNetwork, newNetwork, edgeStr);
		
		assertEquals(2, newNetwork.getNodeCount());
		assertEquals(1, newNetwork.getEdgeCount());

		CyNode nodeOne = nMap.get(newNodeOne);
		assertNotNull(nodeOne);
		assertEquals(EdgeStringNetworkUpdator.COMMUNITY_CHAR + newNodeOne,
				newNetwork.getRow(nodeOne).get(CyNetwork.NAME, String.class));

		CyNode nodeTwo = nMap.get(newNodeTwo);
		assertNotNull(nodeTwo);
		assertEquals(EdgeStringNetworkUpdator.COMMUNITY_CHAR + newNodeTwo,
				newNetwork.getRow(nodeTwo).get(CyNetwork.NAME, String.class));

		assertEquals(3, helper.getMemberList(newNetwork, nodeTwo).size());
		
		assertEquals(4, helper.getMemberList(newNetwork, nodeOne).size());
		
	}
}
