package org.cytoscape.app.communitydetection.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class HierarchyHelper {

	private Map<CyNode, Set<CyNode>> childNodeMap;
	private Map<CyNode, Set<CyNode>> memberNodeMap;
	private Set<CyNode> memberNodeList;

	private HierarchyHelper() {
		childNodeMap = new HashMap<CyNode, Set<CyNode>>();
		memberNodeMap = new HashMap<CyNode, Set<CyNode>>();
		memberNodeList = new HashSet<CyNode>();
	}

	private static class SingletonHelper {
		private static final HierarchyHelper INSTANCE = new HierarchyHelper();
	}

	public static HierarchyHelper getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public void addMemberNode(CyNode memberNode) {
		memberNodeList.add(memberNode);
	}

	public Set<CyNode> getChildNodes(CyNode currentNode) {
		if (childNodeMap.containsKey(currentNode)) {
			return childNodeMap.get(currentNode);
		}
		return null;
	}

	public void addChildNode(CyNode currentNode, CyNode childNode) {
		Set<CyNode> childNodeSet;
		if (childNodeMap.containsKey(currentNode)) {
			childNodeSet = childNodeMap.get(currentNode);
			childNodeSet.add(childNode);
		} else {
			childNodeSet = new HashSet<CyNode>();
			childNodeSet.add(childNode);
		}
		childNodeMap.put(currentNode, childNodeSet);
	}

	public Set<CyNode> getMemberList(CyNetwork currentNetwork, CyNode currentNode) {
		if (!memberNodeMap.containsKey(currentNode)) {
			createMemberList(currentNetwork, currentNode);
		}
		return memberNodeMap.get(currentNode);
	}

	private void createMemberList(CyNetwork currentNetwork, CyNode currentNode) {
		if (!childNodeMap.containsKey(currentNode)) {
			return;
		}
		Set<CyNode> memberNodes = new HashSet<CyNode>();
		for (CyNode childNode : getChildNodes(currentNode)) {
			if (memberNodeList.contains(childNode)) {
				memberNodes.add(childNode);
			} else {
				if (memberNodeMap.get(childNode) == null) {
					createMemberList(currentNetwork, childNode);
				}
				memberNodes.addAll(memberNodeMap.get(childNode));
			}
		}
		memberNodeMap.put(currentNode, memberNodes);
	}

}