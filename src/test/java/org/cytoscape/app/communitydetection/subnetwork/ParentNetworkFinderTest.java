package org.cytoscape.app.communitydetection.subnetwork;

import java.util.HashSet;
import java.util.List;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class ParentNetworkFinderTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();
	
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
	
	@Test
	public void testFindParentNetworksHierarchyHasValidParentSUID() throws Exception {
		CyNetwork parentNet = _nts.getNetwork();
		CyNetwork hierarchy = _nts.getNetwork();
		hierarchy.getDefaultNetworkTable().createColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK,
				Long.class, false, parentNet.getSUID());
		ParentNetworkFinder pnf = new ParentNetworkFinder();
		HashSet<CyNetwork> allNets = new HashSet<>();
		allNets.add(parentNet);
		allNets.add(hierarchy);
		List<CyNetwork> res = pnf.findParentNetworks(allNets, hierarchy);
		assertEquals(1, res.size());
		assertTrue(res.get(0).getSUID().equals(parentNet.getSUID()));
	}
	
	@Test
	public void testFindParentNetworksHierarchyNoMatchingParentSUID() throws Exception {
		CyNetwork aNetOne = _nts.getNetwork();
		CyNetwork aNetTwo = _nts.getNetwork();
		CyNetwork hierarchy = _nts.getNetwork();
		hierarchy.getDefaultNetworkTable().createColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK,
				Long.class, false, aNetOne.getSUID() + aNetTwo.getSUID());
		ParentNetworkFinder pnf = new ParentNetworkFinder();
		HashSet<CyNetwork> allNets = new HashSet<>();
		allNets.add(hierarchy);
		allNets.add(aNetOne);
		allNets.add(aNetTwo);
		
		List<CyNetwork> res = pnf.findParentNetworks(allNets, hierarchy);
		assertEquals(2, res.size());
		assertTrue(res.get(0).getSUID().equals(aNetOne.getSUID()) || res.get(0).getSUID().equals(aNetTwo.getSUID()));
	}
}
