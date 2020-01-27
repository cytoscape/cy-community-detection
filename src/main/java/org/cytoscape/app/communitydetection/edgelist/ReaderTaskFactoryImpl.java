package org.cytoscape.app.communitydetection.edgelist;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
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
public class ReaderTaskFactoryImpl implements ReaderTaskFactory {

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkViewManager cyNetworkViewManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	private final VisualMappingManager visualMappingManager;
	private final LoadVizmapFileTaskFactory vizmapFileTaskFactory;
	private final CyLayoutAlgorithmManager layoutManager;
	private final SynchronousTaskManager<?> syncTaskManager;
	private final CyNetworkNaming networkNaming;

	private ReaderTask readerTask;

	public ReaderTaskFactoryImpl(CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager,
			CyNetworkViewManager cyNetworkViewManager, CyRootNetworkManager cyRootNetworkManager,
			VisualMappingManager visualMappingManager, LoadVizmapFileTaskFactory vizmapFileTaskFactory,
			CyLayoutAlgorithmManager layoutManager, SynchronousTaskManager<?> syncTaskManager,
			CyNetworkNaming networkNaming) {
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.visualMappingManager = visualMappingManager;
		this.vizmapFileTaskFactory = vizmapFileTaskFactory;
		this.layoutManager = layoutManager;
		this.syncTaskManager = syncTaskManager;
		this.networkNaming = networkNaming;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String collectionName, Long originalNetSUID) {
		readerTask = new ReaderTask(inputStream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager,
				cyNetworkViewManager, cyRootNetworkManager, visualMappingManager, vizmapFileTaskFactory, layoutManager,
				syncTaskManager, networkNaming, originalNetSUID);
		return new TaskIterator(readerTask);
	}
}
