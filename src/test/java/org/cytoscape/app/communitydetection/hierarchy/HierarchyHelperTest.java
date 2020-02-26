package org.cytoscape.app.communitydetection.hierarchy;

import java.util.Set;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class HierarchyHelperTest {
	
	@Test
	public void testAllGetMethodsWithEmptyObject(){
		HierarchyHelper helper = HierarchyHelper.getInstance();
		helper.clearAll();
		CyNode mockNode = mock(CyNode.class);
		CyNetwork mockNetwork = mock(CyNetwork.class);
		
		when(mockNode.getSUID()).thenReturn(1L);
		assertEquals(null, helper.getChildNodes(mockNode));
		assertEquals(null, helper.getMemberList(mockNetwork, mockNode));
		helper.clearAll();
	}
	
	@Test
	public void testAddChildNode(){
		HierarchyHelper helper = HierarchyHelper.getInstance();
		
		try {
			helper.clearAll();
			CyNode mockNode = mock(CyNode.class);
			when(mockNode.getSUID()).thenReturn(1L);
			CyNode childNode = mock(CyNode.class);
			when(childNode.getSUID()).thenReturn(2L);
			helper.addChildNode(mockNode, childNode);
			Set<CyNode> cNodes = helper.getChildNodes(mockNode);
			assertEquals(1, cNodes.size());
			assertTrue(cNodes.contains(childNode));
			
			helper.addChildNode(mockNode, childNode);
			cNodes = helper.getChildNodes(mockNode);
			assertEquals(1, cNodes.size());
			assertTrue(cNodes.contains(childNode));
			helper.clearAll();
			
			assertEquals(null, helper.getChildNodes(mockNode));
			
		} finally {
			helper.clearAll();
		}
	}
	
	@Test
	public void testAddMemberNode(){
		HierarchyHelper helper = HierarchyHelper.getInstance();
		try {
			helper.clearAll();
			
			CyNode mockMemberNode = mock(CyNode.class);
			helper.addMemberNode(mockMemberNode);
			helper.addMemberNode(mockMemberNode);
			helper.addMemberNode(mockMemberNode);
		} finally {
			helper.clearAll();
		}
	}
	// @ TODO complete testing of this
}
