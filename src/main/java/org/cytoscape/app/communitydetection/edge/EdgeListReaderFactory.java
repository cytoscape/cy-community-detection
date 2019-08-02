package org.cytoscape.app.communitydetection.edge;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

/**
 * Factory for Edge List reader objects.
 * 
 */
public class EdgeListReaderFactory extends AbstractInputStreamTaskFactory {

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkViewManager cyNetworkViewManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	private final VisualMappingManager visualMappingManager;

	public EdgeListReaderFactory(CyFileFilter filter, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
			final CyNetworkViewManager cyNetworkViewManager, CyRootNetworkManager cyRootNetworkManager,
			final VisualMappingManager visualMappingManager) {
		super(filter);
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.visualMappingManager = visualMappingManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String collectionName) {
		return new TaskIterator(new EdgeListReader(inputStream, cyNetworkViewFactory, cyNetworkFactory,
				cyNetworkManager, cyNetworkViewManager, cyRootNetworkManager, visualMappingManager, collectionName));
	}
}
