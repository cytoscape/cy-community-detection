package org.cytoscape.app.communitydetection.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Singleton class to create and access member of each community node in a
 * hierarchy network.
 *
 */
public class HierarchyHelper {

	/**
	 * A map to hold a set of all child nodes of a community node in the current
	 * hierarchy network.
	 */
	private Map<CyNode, Set<CyNode>> childNodeMap;

	/**
	 * A map to hold a set of all member nodes of a community node in the current
	 * hierarchy network. If the hierarchy network is a tree, then member nodes are
	 * leaf nodes of the subtree rooted by that community node.
	 */
	private Map<CyNode, Set<CyNode>> memberNodeMap;

	/**
	 * Set of all the member nodes in the current hierarchy network
	 */
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

	public void clearAll() {
		childNodeMap.clear();
		memberNodeMap.clear();
		memberNodeList.clear();
	}

	/**
	 * Add a CyNode object to memberNodeList
	 * 
	 * @param memberNode
	 */
	public void addMemberNode(CyNode memberNode) {
		memberNodeList.add(memberNode);
	}

	/**
	 * Returns child nodes of the given CyNode object
	 * 
	 * @param currentNode
	 * @return child nodes of currentNode, null if currentNode is not in
	 *         childNodeMap
	 */
	public Set<CyNode> getChildNodes(CyNode currentNode) {
		if (childNodeMap.containsKey(currentNode)) {
			return childNodeMap.get(currentNode);
		}
		return null;
	}

	/**
	 * Adds childNode to the list of child nodes of currentNode in childNodeMap
	 * 
	 * @param currentNode
	 * @param childNode
	 */
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