package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.event.ActionEvent;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

@SuppressWarnings("serial")
public class HierarchySettingsMenu extends AbstractCyAction implements SetCurrentNetworkViewListener {

	private HierarchySettingsDialog settingsDialog;

	public HierarchySettingsMenu(CySwingApplication swingApplication) throws Exception {
		super("Settings...");
		setPreferredMenu(AppUtils.TOP_MENU_CD);
		setMenuGravity(10.0f);
		insertSeparatorBefore();
		settingsDialog = new HierarchySettingsDialog(swingApplication, new JEditorPaneFactoryImpl());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		settingsDialog.actionPerformed(e);
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		// TODO Auto-generated method stub

	}

}
