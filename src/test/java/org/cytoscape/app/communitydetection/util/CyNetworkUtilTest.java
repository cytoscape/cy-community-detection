package org.cytoscape.app.communitydetection.util;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class CyNetworkUtilTest {
	private NetworkTestSupport _nts = new NetworkTestSupport();
	
	@Test
	public void testCreateTableColumnNullParameters(){
		CyNetworkUtil util = new CyNetworkUtil();
		
		try {
			util.createTableColumn(null, "foo", Integer.class, false, 0);
		} catch(CommunityDetectionException cde){
			assertEquals("table is null", cde.getMessage());
		}
		CyNetwork network = _nts.getNetwork();
		try {
			util.createTableColumn(network.getDefaultNodeTable(),
					null, Integer.class, false, 0);
		} catch(CommunityDetectionException cde){
			assertEquals("column name is null", cde.getMessage());
		}
		
		try {
			util.createTableColumn(network.getDefaultNodeTable(),
					"hi", null, false, 0);
		} catch(CommunityDetectionException cde){
			assertEquals("type is null", cde.getMessage());
		}
		

	}
	
	@Test
	public void testCreateTableValid() throws CommunityDetectionException {
		CyNetwork network = _nts.getNetwork();
		CyNetworkUtil util = new CyNetworkUtil();
		util.createTableColumn(network.getDefaultNetworkTable(), "foo", Integer.class, false, 5);
		
		CyColumn col = network.getDefaultNetworkTable().getColumn("foo");
		assertEquals(5, col.getDefaultValue());
		assertFalse(col.isImmutable());
		assertEquals(Integer.class, col.getType());
		
		util.createTableColumn(network.getDefaultNetworkTable(), "foo", Integer.class, false, 6);
		assertEquals(5, col.getDefaultValue());
		assertFalse(col.isImmutable());
		assertEquals(Integer.class, col.getType());
	}
	
	@Test
	public void testupdateHierarchySUIDWithNullArgs(){
		CyNetworkUtil util = new CyNetworkUtil();
		try {
			util.updateHierarchySUID(null, null);
			fail("Expected CommunityDetectionException");
		} catch(CommunityDetectionException cde){
			assertEquals("hierarchy network is null", cde.getMessage());
		}
		CyNetwork network = _nts.getNetwork();
		try {
			util.updateHierarchySUID(network, null);
			fail("Expected CommunityDetectionException");
		} catch(CommunityDetectionException cde){
			assertEquals("parent network is null", cde.getMessage());
		}
	}
	
	@Test
	public void testupdateHierarchySUID() throws CommunityDetectionException {
		CyNetworkUtil util = new CyNetworkUtil();
		
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNetwork hierNetwork = _nts.getNetwork();	
		util.createTableColumn(hierNetwork.getDefaultNetworkTable(),
				AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class, false, 0L);
		util.updateHierarchySUID(hierNetwork, parentNetwork);
		Long val = hierNetwork.getRow(hierNetwork).get(AppUtils.COLUMN_CD_ORIGINAL_NETWORK,
				Long.class);
		assertEquals(val, parentNetwork.getSUID());
	}
	
	
}
