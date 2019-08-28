package org.cytoscape.app.communitydetection.subnetwork;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

public class SubNetworkContextMenu implements ActionListener, CyNodeViewContextMenuFactory {

	private final CyApplicationManager applicationManager;
	private final SubNetworkTaskFactoryImpl factoryImpl;

	public SubNetworkContextMenu(CyApplicationManager applicationManager, SubNetworkTaskFactoryImpl factoryImpl) {
		this.applicationManager = applicationManager;
		this.factoryImpl = factoryImpl;
	}

	@Override
	public CyMenuItem createMenuItem(CyNetworkView netView, View<CyNode> nodeView) {
		JMenuItem menuItem = new JMenuItem("New Network from Member Nodes");
		menuItem.addActionListener(this);
		CyMenuItem cyMenuItem = new CyMenuItem(menuItem, 0);
		return cyMenuItem;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
		if (CyTableUtil.getSelectedNodes(currentNetwork).size() > 1) {
			JOptionPane.showMessageDialog(null, "Works with only 1 node.");
			return;
		}
		if (currentNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) == null) {
			JOptionPane.showMessageDialog(null, "Not applicable for this network.");
			return;
		}
		CyNode selectedNode = CyTableUtil.getSelectedNodes(currentNetwork).get(0);
		factoryImpl.createTaskIterator(currentNetwork, selectedNode);
	}
}
