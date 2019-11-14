package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.event.ActionEvent;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.view.model.CyNetworkViewManager;

@SuppressWarnings("serial")
public class HierarchySettingsAction extends AbstractCyAction implements SetCurrentNetworkViewListener {

	private HierarchySettingsDialog settingsDialog;

	public HierarchySettingsAction(CyApplicationManager applicationManager, CyNetworkViewManager networkViewManager,
			CySwingApplication swingApplication) throws Exception {
		super("Settings...", applicationManager, ActionEnableSupport.ENABLE_FOR_ALWAYS, networkViewManager);
		setPreferredMenu(AppUtils.TOP_MENU_CD);
		setMenuGravity(2.0f);
		settingsDialog = new HierarchySettingsDialog(swingApplication);
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
