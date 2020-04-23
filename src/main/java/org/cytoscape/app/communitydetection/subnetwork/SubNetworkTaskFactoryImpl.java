package org.cytoscape.app.communitydetection.subnetwork;

import org.cytoscape.app.communitydetection.util.AppUtils;
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

	private final CyRootNetworkManager rootNetworkManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final VisualMappingManager visualMappingManager;
	private final CyLayoutAlgorithmManager layoutManager;
	private final SynchronousTaskManager<?> syncTaskManager;
	private final CyNetworkNaming networkNaming;

	public SubNetworkTaskFactoryImpl(CyRootNetworkManager rootNetworkManager, CyNetworkManager networkManager,
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
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		/*
		CyNetwork hierarchyNetwork = networkView.getModel();
		List<CyNetwork> parentNetworks =  _parentNetworkFinder.findParents(networkManager.getNetworks(), 
		                                                                   hierarchyNetwork);
		if (parentNetworks.size() == 1){
			return new TaskIterator(
				new SubNetworkTask(rootNetworkManager, networkManager, networkViewManager, networkViewFactory,
						visualMappingManager, layoutManager, syncTaskManager, networkNaming, hierarchyNetwork, parentNetworks.get(0))));
		} 
		
		int res = _parentNetworkChooserDialog.show(parentNetworks);
		if (res == 0){
			CyNetwork selectedParentNetwork = _parentNetworkChooserDialog.getSelection();
			if (_parentNetworkChooserDialog.rememberChoice() == true){
				updateHierarchySUID(hierarchyNetwork, selectedParentNetwork);
			}
			return new TaskIterator(
				new SubNetworkTask(rootNetworkManager, networkManager, networkViewManager, networkViewFactory,
						visualMappingManager, layoutManager, syncTaskManager, networkNaming, hierarchyNetwork, selectedParentNetwork)));

		
		// fail throw new exception
		
		*/
		
		return new TaskIterator(
				new SubNetworkTask(rootNetworkManager, networkManager, networkViewManager, networkViewFactory,
						visualMappingManager, layoutManager, syncTaskManager, networkNaming, networkView.getModel()));
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
}
