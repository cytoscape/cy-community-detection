package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.event.ActionEvent;
import org.cytoscape.app.communitydetection.rest.CDRestClient;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

@SuppressWarnings("serial")
public class HierarchyLauncherMenu extends AbstractCyAction implements SetCurrentNetworkViewListener {

	private LauncherDialog settingsDialog;

	public HierarchyLauncherMenu(CySwingApplication swingApplication) throws Exception {
		super("Settings...");
		setPreferredMenu(AppUtils.TOP_MENU);
		setMenuGravity(10.0f);
		insertSeparatorBefore();
		//settingsDialog = new LauncherDialog(new JEditorPaneFactoryImpl());
		//CDRestClient.getInstance().setHierarchySettingsDialog(settingsDialog);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//settingsDialog.actionPerformed(e);
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		// TODO Auto-generated method stub

	}

}
