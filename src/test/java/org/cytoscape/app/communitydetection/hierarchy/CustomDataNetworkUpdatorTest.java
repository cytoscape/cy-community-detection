package org.cytoscape.app.communitydetection.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cytoscape.app.communitydetection.cx2.CX2NodeAttributes;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxEdge;
import org.ndexbio.cx2.aspect.element.core.CxNode;
import org.ndexbio.cx2.aspect.element.core.DeclarationEntry;
import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
/**
 *
 * @author churas
 */
public class CustomDataNetworkUpdatorTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();

	@Test
	public void testUpdateNetworkWithCustomDataWithNullArgs(){
		CustomDataNetworkUpdator updator = new CustomDataNetworkUpdator();
		CX2NodeAttributes nodeAttrs = new CX2NodeAttributes();
		Map<Long, CyNode> nMap = new LinkedHashMap<>();
		CyNetwork network = mock(CyNetwork.class);
		
		try {
			updator.updateNetworkWithCustomData(null, nodeAttrs, nMap);
			fail("Expected exception");
		} catch (CommunityDetectionException cde){
			assertEquals("network is null", cde.getMessage());
		}
		
		try {
			updator.updateNetworkWithCustomData(network, null, nMap);
			fail("Expected exception");
		} catch (CommunityDetectionException cde){
			assertEquals("nodeAttrs is null", cde.getMessage());
		}
		
		try {
			updator.updateNetworkWithCustomData(network, nodeAttrs, null);
			fail("Expected exception");
		} catch (CommunityDetectionException cde){
			assertEquals("node map is null", cde.getMessage());
		}
	}
	
	@Test
	public void testUpdateNetworkWithCustomDataWithEmptyNodeAttrs() throws CommunityDetectionException {
		CustomDataNetworkUpdator updator = new CustomDataNetworkUpdator();
		CX2NodeAttributes nodeAttrs = new CX2NodeAttributes();
		Map<Long, CyNode> nMap = new LinkedHashMap<>();
		CyNetwork network = mock(CyNetwork.class);
		try {
			updator.updateNetworkWithCustomData(network, nodeAttrs, nMap);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("Attribute Declarations missing", cde.getMessage());
		}
		verifyNoInteractions(network);
	}
	
	@Test
	public void testUpdateValidIntegerData() throws CommunityDetectionException {
		String hidefColName = "HiDeF_persistence";
		CyNetwork network = _nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		CyNode nodeThree = network.addNode();

		CX2NodeAttributes nodeAttrs = new CX2NodeAttributes();
		
		List<CxNode> nodes = new ArrayList<>();
		Map<String, Object> oneMap = new HashMap<>();
		oneMap.put("p1", 1);
		CxNode cxOne = new CxNode(nodeOne.getSUID(), oneMap);
		nodes.add(cxOne);
		
		Map<String, Object> twoMap = new HashMap<>();
		twoMap.put("p1", 2);
		CxNode cxTwo = new CxNode(nodeTwo.getSUID(), twoMap);
		nodes.add(cxTwo);
		
		List<CxAttributeDeclaration> attributeDeclarations = new ArrayList<>();
		CxAttributeDeclaration cad = new CxAttributeDeclaration();
		Map<String, DeclarationEntry> deMap = new HashMap<>();
		DeclarationEntry deEntry = new DeclarationEntry(ATTRIBUTE_DATA_TYPE.INTEGER, 0, "p1");
		deMap.put(hidefColName, deEntry);
		cad.add(CxNode.ASPECT_NAME, deMap);
		attributeDeclarations.add(cad);
		
		nodeAttrs.setAttributeDeclarations(attributeDeclarations);
		nodeAttrs.setNodes(nodes);
		
		Map<Long, CyNode> nMap = new HashMap<>();
		nMap.put(nodeOne.getSUID(), nodeOne);
		nMap.put(nodeTwo.getSUID(), nodeTwo);
		nMap.put(nodeThree.getSUID(), nodeThree);
		CustomDataNetworkUpdator updator = new CustomDataNetworkUpdator();
		updator.updateNetworkWithCustomData(network, nodeAttrs, nMap);
		
		assertEquals((Integer)1, (Integer)network.getRow(nodeOne).get(hidefColName, Integer.class));
		assertEquals((Integer)2, (Integer)network.getRow(nodeTwo).get(hidefColName, Integer.class));
		assertEquals((Integer)0, (Integer)network.getRow(nodeThree).get(hidefColName, Integer.class));
	}
	
	@Test
	public void testUpdateValidDoubleDataAliasNull() throws CommunityDetectionException {
		String hidefColName = "HiDeF_persistence";
		CyNetwork network = _nts.getNetwork();
		CyNode nodeOne = network.addNode();
		CyNode nodeTwo = network.addNode();
		CyNode nodeThree = network.addNode();

		CX2NodeAttributes nodeAttrs = new CX2NodeAttributes();
		
		List<CxNode> nodes = new ArrayList<>();
		Map<String, Object> oneMap = new HashMap<>();
		oneMap.put(hidefColName, 1.0);
		CxNode cxOne = new CxNode(nodeOne.getSUID(), oneMap);
		nodes.add(cxOne);
		
		Map<String, Object> twoMap = new HashMap<>();
		twoMap.put(hidefColName, 2.0);
		twoMap.put("p1", 3.0); // this will be ignored
		CxNode cxTwo = new CxNode(nodeTwo.getSUID(), twoMap);
		nodes.add(cxTwo);
		
		List<CxAttributeDeclaration> attributeDeclarations = new ArrayList<>();
		CxAttributeDeclaration cad = new CxAttributeDeclaration();
		Map<String, DeclarationEntry> deMap = new HashMap<>();
		DeclarationEntry deEntry = new DeclarationEntry(ATTRIBUTE_DATA_TYPE.DOUBLE, 0.0, null);
		deMap.put(hidefColName, deEntry);
		cad.add(CxNode.ASPECT_NAME, deMap);
		attributeDeclarations.add(cad);
		
		cad.add(CxEdge.ASPECT_NAME, deMap);
		
		nodeAttrs.setAttributeDeclarations(attributeDeclarations);
		nodeAttrs.setNodes(nodes);
		
		Map<Long, CyNode> nMap = new HashMap<>();
		nMap.put(nodeOne.getSUID(), nodeOne);
		nMap.put(nodeTwo.getSUID(), nodeTwo);
		nMap.put(nodeThree.getSUID(), nodeThree);
		CustomDataNetworkUpdator updator = new CustomDataNetworkUpdator();
		updator.updateNetworkWithCustomData(network, nodeAttrs, nMap);
		
		assertEquals((Double)1.0, (Double)network.getRow(nodeOne).get(hidefColName, Double.class), 0.1);
		assertEquals((Double)2.0, (Double)network.getRow(nodeTwo).get(hidefColName, Double.class), 0.1);
		assertEquals((Double)0.0, (Double)network.getRow(nodeThree).get(hidefColName, Double.class), 0.1);
	}
	
	@Test
	public void testUpdateValidWithString() throws CommunityDetectionException {
		testUpdateValidWithDifferentTypes(ATTRIBUTE_DATA_TYPE.STRING, String.class,
				"hi", "default");
	}
	
	@Test
	public void testUpdateValidWithBoolean() throws CommunityDetectionException {
		testUpdateValidWithDifferentTypes(ATTRIBUTE_DATA_TYPE.BOOLEAN, Boolean.class,
				true, false);
	}
	
	@Test
	public void testUpdateValidWithLong() throws CommunityDetectionException {
		testUpdateValidWithDifferentTypes(ATTRIBUTE_DATA_TYPE.LONG, Long.class,
				3l, 4l);
	}
	
	/**
	 * Used to test the different types of data
	 * @param <T>
	 * @param attrType
	 * @param theClass
	 * @param theObj
	 * @param defObj
	 * @throws CommunityDetectionException 
	 */
	private <T> void testUpdateValidWithDifferentTypes(ATTRIBUTE_DATA_TYPE attrType,
			Class theClass,T theObj, T defObj) throws CommunityDetectionException {
		String colName = "foo";
		CyNetwork network = _nts.getNetwork();
		CyNode nodeOne = network.addNode();
	
		CX2NodeAttributes nodeAttrs = new CX2NodeAttributes();
		
		List<CxNode> nodes = new ArrayList<>();
		Map<String, Object> oneMap = new HashMap<>();
		oneMap.put("p1", theObj);
		CxNode cxOne = new CxNode(nodeOne.getSUID(), oneMap);
		nodes.add(cxOne);
		
		List<CxAttributeDeclaration> attributeDeclarations = new ArrayList<>();
		CxAttributeDeclaration cad = new CxAttributeDeclaration();
		Map<String, DeclarationEntry> deMap = new HashMap<>();
		DeclarationEntry deEntry = new DeclarationEntry(attrType, defObj, "p1");
		deMap.put(colName, deEntry);
		cad.add(CxNode.ASPECT_NAME, deMap);
		attributeDeclarations.add(cad);
		
		nodeAttrs.setAttributeDeclarations(attributeDeclarations);
		nodeAttrs.setNodes(nodes);
		
		Map<Long, CyNode> nMap = new HashMap<>();
		nMap.put(nodeOne.getSUID(), nodeOne);
	
		CustomDataNetworkUpdator updator = new CustomDataNetworkUpdator();
		updator.updateNetworkWithCustomData(network, nodeAttrs, nMap);
		
		assertEquals(theObj, network.getRow(nodeOne).get(colName, theClass));
	}
}
