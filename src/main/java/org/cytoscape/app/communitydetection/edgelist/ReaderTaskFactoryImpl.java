package org.cytoscape.app.communitydetection.edgelist;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

/**
 * {@link AbstractInputStreamTaskFactory} implementation to create
 * {@link ReaderTask}.
 * 
 */
public class ReaderTaskFactoryImpl extends AbstractInputStreamTaskFactory {

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkViewManager cyNetworkViewManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	private final VisualMappingManager visualMappingManager;
	private final CyLayoutAlgorithmManager layoutManager;
	private final SynchronousTaskManager<?> syncTaskManager;
	private final CyNetworkNaming networkNaming;

	private ReaderTask readerTask;

	public ReaderTaskFactoryImpl(CyFileFilter filter, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
			final CyNetworkViewManager cyNetworkViewManager, CyRootNetworkManager cyRootNetworkManager,
			final VisualMappingManager visualMappingManager, final CyLayoutAlgorithmManager layoutManager,
			SynchronousTaskManager<?> syncTaskManager, CyNetworkNaming networkNaming) {
		super(filter);
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.visualMappingManager = visualMappingManager;
		this.layoutManager = layoutManager;
		this.syncTaskManager = syncTaskManager;
		this.networkNaming = networkNaming;
	}

	public TaskIterator createTaskIterator(InputStream inputStream, String collectionName, Long originalNetSUID) {
		readerTask = new ReaderTask(inputStream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager,
				cyNetworkViewManager, cyRootNetworkManager, visualMappingManager, layoutManager, syncTaskManager,
				networkNaming, originalNetSUID);
		return this.createTaskIterator(inputStream, collectionName);
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String collectionName) {
		return new TaskIterator(readerTask);
	}
}
