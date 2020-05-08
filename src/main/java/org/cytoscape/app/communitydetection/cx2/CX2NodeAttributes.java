package org.cytoscape.app.communitydetection.cx2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxNode;

/**
 * CX 2.0 fragment that contains attributeDeclarations and
 * nodes aspect.
 * @author churas
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CX2NodeAttributes {
	
	private List<CxAttributeDeclaration> attributeDeclarations;
	private List<CxNode> nodes;

	/**
	 * Gets Attribute Declarations
	 * @return 
	 */
	public List<CxAttributeDeclaration> getAttributeDeclarations() {
		return attributeDeclarations;
	}

	/**
	 * Sets Attribute Declarations
	 * @param attributeDeclarations 
	 */
	public void setAttributeDeclarations(List<CxAttributeDeclaration> attributeDeclarations) {
		this.attributeDeclarations = attributeDeclarations;
	}

	/**
	 * Gets CxNodes
	 * @return 
	 */
	public List<CxNode> getNodes() {
		return nodes;
	}

	/**
	 * Sets CxNodes
	 * @param nodes 
	 */
	public void setNodes(List<CxNode> nodes) {
		this.nodes = nodes;
	}
}
