package org.cytoscape.app.communitydetection.cx2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxNode;
/**
 *
 * @author churas
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CX2NodeAttributes {
	
	private List<CxAttributeDeclaration> attributeDeclarations;
	private List<CxNode> nodes;

	public List<CxAttributeDeclaration> getAttributeDeclarations() {
		return attributeDeclarations;
	}

	public void setAttributeDeclarations(List<CxAttributeDeclaration> attributeDeclarations) {
		this.attributeDeclarations = attributeDeclarations;
	}

	public List<CxNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<CxNode> nodes) {
		this.nodes = nodes;
	}
}
