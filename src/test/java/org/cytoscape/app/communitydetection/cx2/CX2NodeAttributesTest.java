package org.cytoscape.app.communitydetection.cx2;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxNode;

/**
 *
 * @author churas
 */
public class CX2NodeAttributesTest {
	
	@Test
	public void testGettersAndSetters(){
		CX2NodeAttributes nodeAttrs = new CX2NodeAttributes();
		assertNull(nodeAttrs.getAttributeDeclarations());
		assertNull(nodeAttrs.getNodes());
		
		List<CxNode> nodes = new ArrayList<>();
		CxNode aNode = new CxNode();
		aNode.setId(1);
		nodes.add(aNode);
		
		List<CxAttributeDeclaration> attrDecls = new ArrayList<>();
		CxAttributeDeclaration decl = new CxAttributeDeclaration();
		attrDecls.add(decl);
		nodeAttrs.setAttributeDeclarations(attrDecls);
		nodeAttrs.setNodes(nodes);
		
		assertEquals(1, nodeAttrs.getNodes().size());
		assertEquals(1, nodeAttrs.getNodes().get(0).getId());
		
		assertEquals(1, nodeAttrs.getAttributeDeclarations().size());
	}
}
