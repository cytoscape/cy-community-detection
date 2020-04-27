package org.cytoscape.app.communitydetection.subnetwork;

import java.util.List;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.DoNothingTask;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link NetworkViewTaskFactory} and
 * {@link AbstractNodeViewTaskFactory} to create {@link SubNetworkTask}.
 *
 */
public class SubNetworkTaskFactoryImpl extends AbstractNodeViewTaskFactory implements NetworkViewTaskFactory {

	private final static Logger LOGGER = LoggerFactory.getLogger(SubNetworkTaskFactoryImpl.class);

	private CySwingApplication _swingApplication;
	private ShowDialogUtil _dialogUtil;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final VisualMappingManager visualMappingManager;
	private final CyLayoutAlgorithmManager layoutManager;
	private final SynchronousTaskManager<?> syncTaskManager;
	private final CyNetworkNaming networkNaming;
	private ParentNetworkFinder _parentNetworkFinder;
	private ParentNetworkChooserDialog _parentNetworkDialog;

	public SubNetworkTaskFactoryImpl(CySwingApplication swingApplication, ShowDialogUtil dialogUtil,
			ParentNetworkFinder parentNetworkFinder, ParentNetworkChooserDialog parentNetworkDialog,
			CyRootNetworkManager rootNetworkManager, CyNetworkManager networkManager,
			CyNetworkViewManager networkViewManager, CyNetworkViewFactory networkViewFactory,
			VisualMappingManager visualMappingManager, CyLayoutAlgorithmManager layoutManager,
			SynchronousTaskManager<?> syncTaskManager, CyNetworkNaming networkNaming) {
		this.rootNetworkManager = rootNetworkManager;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.networkViewFactory = networkViewFactory;
		this.visualMappingManager = visualMappingManager;
		this.layoutManager = layoutManager;
		this.syncTaskManager = syncTaskManager;
		this.networkNaming = networkNaming;
		
		this._parentNetworkFinder = parentNetworkFinder;
		this._parentNetworkDialog = parentNetworkDialog;
		this._dialogUtil = dialogUtil;
		this._swingApplication = swingApplication;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		
		CyNetwork hierarchyNetwork = networkView.getModel();
		try {
			List<CyNetwork> parentNetworks =  _parentNetworkFinder.findParentNetworks(networkManager.getNetworkSet(), 
			                                                                          hierarchyNetwork);
			if (parentNetworks.size() == 1){
				return new TaskIterator(
					new SubNetworkTask(rootNetworkManager, networkManager, networkViewManager, networkViewFactory,
							visualMappingManager, layoutManager, syncTaskManager, networkNaming, hierarchyNetwork, parentNetworks.get(0)));
			} 

			if (_parentNetworkDialog.createGUI(parentNetworks) == false){
				LOGGER.error("ParentNetworkChooserDialog.createGUI() returned false");
				return new TaskIterator(new DoNothingTask());
			}
			Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
			int res = _dialogUtil.showOptionDialog(_swingApplication.getJFrame(),
				                               this._parentNetworkDialog,
									"Parent Network Chooser",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, 
									null, 
									options,
									options[0]);
			if (res == 0){
				CyNetwork selectedParentNetwork = _parentNetworkDialog.getSelection();
				if (_parentNetworkDialog.rememberChoice() == true){
					updateHierarchySUID(hierarchyNetwork, selectedParentNetwork);
				}
				return new TaskIterator(
					new SubNetworkTask(rootNetworkManager, networkManager, networkViewManager, networkViewFactory,
							visualMappingManager, layoutManager, syncTaskManager, networkNaming, hierarchyNetwork, selectedParentNetwork));
			}
		}
		catch(ParentNetworkFinderException pe){
			LOGGER.error("Caught exception trying to find parent network", pe);
		}
		
		return new TaskIterator(new DoNothingTask());
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (networkView != null && networkView.getModel() != null) {
			if (CyTableUtil.getSelectedNodes(networkView.getModel()).size() != 1) {
				return false;
			}
			if (networkView.getModel().getDefaultNetworkTable()
					.getColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK) == null) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return this.createTaskIterator(networkView);
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return this.isReady(networkView);
	}
	
	/**
	 * Updates 
	 * {@link org.cytoscape.app.communitydetection.util.AppUtils#COLUMN_CD_ORIGINAL_NETWORK}
	 * network attribute in {@code hierarchyNetwork} with SUID of
	 * {@code selectedParentNetwork}
	 * 
	 * @param hierarchyNetwork hierarchy network to update
	 * @param selectedParentNetwork parent network
	 */
	private void updateHierarchySUID(CyNetwork hierarchyNetwork, CyNetwork selectedParentNetwork){
		hierarchyNetwork.getRow(hierarchyNetwork).set(AppUtils.COLUMN_CD_ORIGINAL_NETWORK,
				selectedParentNetwork.getSUID());
	}
}
