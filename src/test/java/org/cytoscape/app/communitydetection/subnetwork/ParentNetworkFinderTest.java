package org.cytoscape.app.communitydetection.subnetwork;

import java.util.HashSet;
import org.cytoscape.model.CyNetwork;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class ParentNetworkFinderTest {
	
	@Test
	public void testfindParentNetworksAllNetworksNull(){
		CyNetwork mockHierarchy = mock(CyNetwork.class);
		ParentNetworkFinder pnf = new ParentNetworkFinder();
		try {
			pnf.findParentNetworks(null, mockHierarchy);
			fail("Expected ParentNetworkFinderException");
		} catch(ParentNetworkFinderException pe){
			assertEquals("allNetworks is null", pe.getMessage());
		}
			
	}
	
	@Test
	public void testfindParentNetworksHierarchyNetworkNull(){
		ParentNetworkFinder pnf = new ParentNetworkFinder();
		HashSet<CyNetwork> allNets = new HashSet<>();
		try {
			pnf.findParentNetworks(allNets, null);
			fail("Expected ParentNetworkFinderException");
		} catch(ParentNetworkFinderException pe){
			assertEquals("Hierarchy network is null", pe.getMessage());
		}
			
	}
}
