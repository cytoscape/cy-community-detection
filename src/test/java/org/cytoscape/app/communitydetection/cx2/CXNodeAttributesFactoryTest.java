package org.cytoscape.app.communitydetection.cx2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;

/**
 *
 * @author churas
 */
public class CXNodeAttributesFactoryTest {
	
	@Test
	public void testGetCX2NodeAttributesPassingInNull(){
		CX2NodeAttributesFactory fac = new CX2NodeAttributesFactory();
		assertNull(fac.getCX2NodeAttributes(null));
	}
	
	@Test
	public void testGetCX2NodeAttributesPassingInEmptyFragment(){
		JsonNode tnode = new TextNode("");
		
		CX2NodeAttributesFactory fac = new CX2NodeAttributesFactory();
		assertNull(fac.getCX2NodeAttributes(tnode));
	}
	
	@Test
	public void testGetCX2NodeAttributesValidJSON() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "{\"attributeDeclarations\": [{\"nodes\": { \"foo\": { \"d\": \"integer\", \"a\": \"p1\", \"v\": 0}}}],\"nodes\": [{\"id\": 11022,\"v\": { \"p1\": 0}}]}";
		JsonNode actualObj = mapper.readTree(jsonString);
		CX2NodeAttributesFactory fac = new CX2NodeAttributesFactory();
		CX2NodeAttributes nodeAttrs = fac.getCX2NodeAttributes(actualObj);
		assertNotNull(nodeAttrs);
		assertEquals(1, nodeAttrs.getAttributeDeclarations().size());
		CxAttributeDeclaration decl = nodeAttrs.getAttributeDeclarations().get(0);
		assertEquals("p1", decl.getDeclarations().get("nodes").get("foo").getAlias());
		
		assertEquals(1, nodeAttrs.getNodes().size());
		assertEquals(11022, nodeAttrs.getNodes().get(0).getId());
	}

	
}
