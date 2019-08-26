package org.cytoscape.app.communitydetection;

import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.app.communitydetection.cx.CxTaskFactory;
import org.cytoscape.app.communitydetection.edgelist.TaskExecutor;
import org.cytoscape.app.communitydetection.edgelist.TaskListenerFactory;
import org.cytoscape.app.communitydetection.edgelist.reader.ReaderTaskFactoryImpl;
import org.cytoscape.app.communitydetection.edgelist.writer.WriterTaskFactoryImpl;
import org.cytoscape.app.communitydetection.subnetwork.CommunityContextMenu;
import org.cytoscape.app.communitydetection.subnetwork.SubNetworkTaskFactoryImpl;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {

		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		final CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		final CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		final CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		final CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
		final CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class);
		final VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
		final CyLayoutAlgorithmManager layoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);
		final SynchronousTaskManager<?> syncTaskManager = getService(bc, SynchronousTaskManager.class);
		final CyNetworkNaming networkNaming = getService(bc, CyNetworkNaming.class);

		// Setting up CX IO service listeners
		CxTaskFactory cxTaskFactory = CxTaskFactory.getInstance();
		registerServiceListener(bc, cxTaskFactory, "addReaderFactory", "removeReaderFactory",
				InputStreamTaskFactory.class);
		registerServiceListener(bc, cxTaskFactory, "addWriterFactory", "removeWriterFactory",
				CyNetworkViewWriterFactory.class);

		// Setting up Edge List I/O services
		final StreamUtil streamUtil = getService(bc, StreamUtil.class);

		final BasicCyFileFilter edgeFilter = new BasicCyFileFilter(new String[] {}, new String[] { "text/edgelist" },
				"Adjacency List", DataCategory.NETWORK, streamUtil);

		final Properties writerProperties = new Properties();
		writerProperties.put(ID, AppUtils.EDGE_WRITER_ID);
		final WriterTaskFactoryImpl writerTaskWrapper = new WriterTaskFactoryImpl(edgeFilter);
		registerService(bc, writerTaskWrapper, CyNetworkViewWriterFactory.class, writerProperties);

		final Properties readerProperties = new Properties();
		readerProperties.put(ID, AppUtils.EDGE_READER_ID);
		final ReaderTaskFactoryImpl readerTaskWrapper = new ReaderTaskFactoryImpl(edgeFilter, networkViewFactory,
				networkFactory, networkManager, networkViewManager, rootNetworkManager, visualMappingManager,
				layoutAlgorithmManager, syncTaskManager, networkNaming);
		registerService(bc, readerTaskWrapper, InputStreamTaskFactory.class, readerProperties);

		TaskListenerFactory edgeTaskFactory = TaskListenerFactory.getInstance();
		registerServiceListener(bc, edgeTaskFactory, "addReaderFactory", "removeReaderFactory",
				InputStreamTaskFactory.class);
		registerServiceListener(bc, edgeTaskFactory, "addWriterFactory", "removeWriterFactory",
				CyNetworkViewWriterFactory.class);

		// Registering Edge List services
		for (String key : AppUtils.ALGORITHMS.keySet()) {
			Properties taskExecProps = new Properties();
			taskExecProps.setProperty(PREFERRED_MENU, AppUtils.MENU + "." + AppUtils.ALGORITHMS.get(key));
			taskExecProps.setProperty(MENU_GRAVITY, "1.0");
			taskExecProps.setProperty(TITLE, "(none)");
			registerAllServices(bc, new TaskExecutor(key, AppUtils.TYPE_NONE), taskExecProps);

			taskExecProps = new Properties();
			taskExecProps.setProperty(PREFERRED_MENU, AppUtils.MENU + "." + AppUtils.ALGORITHMS.get(key));
			taskExecProps.setProperty(MENU_GRAVITY, "1.0");
			taskExecProps.setProperty(TITLE, "Weighted");
			registerAllServices(bc, new TaskExecutor(key, AppUtils.TYPE_WEIGHTED), taskExecProps);
		}

		SubNetworkTaskFactoryImpl factoryImpl = new SubNetworkTaskFactoryImpl(rootNetworkManager, networkManager,
				networkViewManager, networkViewFactory, visualMappingManager, layoutAlgorithmManager, syncTaskManager,
				networkNaming);
		registerAllServices(bc, factoryImpl);

		CyNodeViewContextMenuFactory contextMenu = new CommunityContextMenu(applicationManager, factoryImpl);
		Properties contextMenuProps = new Properties();
		contextMenuProps.put(PREFERRED_MENU, AppUtils.MENU);
		registerAllServices(bc, contextMenu, contextMenuProps);
	}

}
