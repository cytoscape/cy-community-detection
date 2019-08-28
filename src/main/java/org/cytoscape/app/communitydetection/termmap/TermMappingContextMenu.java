package org.cytoscape.app.communitydetection.termmap;

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
import org.cytoscape.work.SynchronousTaskManager;

public class TermMappingContextMenu implements ActionListener, CyNodeViewContextMenuFactory {

	private final CyApplicationManager applicationManager;
	private final SynchronousTaskManager<?> syncTaskManager;
	private final TermMappingTaskFactoryImpl factoryImpl;
	private final String algorithm;

	public TermMappingContextMenu(CyApplicationManager applicationManager, SynchronousTaskManager<?> syncTaskManager,
			TermMappingTaskFactoryImpl factoryImpl, String algorithm) {
		this.applicationManager = applicationManager;
		this.syncTaskManager = syncTaskManager;
		this.factoryImpl = factoryImpl;
		this.algorithm = algorithm;
		;
	}

	@Override
	public CyMenuItem createMenuItem(CyNetworkView netView, View<CyNode> nodeView) {
		JMenuItem menuItem = new JMenuItem(AppUtils.TERM_MAPPING_ALGORITHMS.get(algorithm));
		menuItem.addActionListener(this);
		CyMenuItem cyMenuItem = new CyMenuItem(menuItem, 0);
		return cyMenuItem;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
		if (CyTableUtil.getSelectedNodes(currentNetwork).size() < 1) {
			JOptionPane.showMessageDialog(null, "Please select a node!");
			return;
		}
		syncTaskManager.execute(factoryImpl.createTaskIterator(currentNetwork));
	}

}
