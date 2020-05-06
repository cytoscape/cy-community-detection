package org.cytoscape.app.communitydetection.hierarchy;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class HierarchyNetworkViewFactory {
	
		private final static Logger LOGGER = LoggerFactory.getLogger(HierarchyNetworkViewFactory.class);

	private CyNetworkViewFactory _cyNetworkViewFactory;
	private VisualMappingManager _visualMappingManager;
	private CyLayoutAlgorithmManager _layoutManager;
	private SynchronousTaskManager<?> _syncTaskManager;
	private CyNetworkViewManager _networkViewManager;
	
	public HierarchyNetworkViewFactory(CyNetworkViewManager networkViewManager,
			CyNetworkViewFactory cyNetworkViewFactory,
			VisualMappingManager visualMappingManager,
			CyLayoutAlgorithmManager layoutManager,
			SynchronousTaskManager<?> syncTaskManager){
		_cyNetworkViewFactory = cyNetworkViewFactory;
		_visualMappingManager = visualMappingManager;
		_layoutManager = layoutManager;
		_networkViewManager = networkViewManager;
		_syncTaskManager = syncTaskManager;
		if (_cyNetworkViewFactory == null){
			LOGGER.error("NetworkViewFactory is null");
		}
		if (_visualMappingManager == null){
			LOGGER.error("VisualMappingManager is null");
		}
		if (_layoutManager == null){
			LOGGER.error("LayoutManager is null");
		}
		if (_networkViewManager == null){
			LOGGER.error("NetworkViewManager is null");
		}
		if (_syncTaskManager == null){
			LOGGER.error("SyncTaskManager is null");
		}
	}
	
	public CyNetworkView getHierarchyNetworkView(CyNetwork network, VisualStyle desiredStyle,
			CyLayoutAlgorithm desiredLayout){
		if (network == null){
			LOGGER.error("network is null");
			return null;
		}
		if (desiredStyle == null){
			LOGGER.error("style is null");
			return null;
		}
		if (desiredLayout == null){
			LOGGER.error("desired layout is null");
			return null;
		}
		
		LOGGER.debug("Creating network view");
		CyNetworkView networkView = _cyNetworkViewFactory.createNetworkView(network);
		_visualMappingManager.setVisualStyle(desiredStyle, networkView);
		desiredStyle.apply(networkView);
		CyLayoutAlgorithm layout = _layoutManager.getDefaultLayout();
		TaskIterator layoutTasks = layout.createTaskIterator(networkView, layout.createLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
		_syncTaskManager.execute(layoutTasks);
		_networkViewManager.addNetworkView(networkView);
		return networkView;
	}
}
