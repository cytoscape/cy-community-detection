package org.cytoscape.app.communitydetection;

import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.app.communitydetection.cx.CxTaskFactory;
import org.cytoscape.app.communitydetection.edgelist.EdgeListTaskFactory;
import org.cytoscape.app.communitydetection.edgelist.EdgeListTaskListenerFactory;
import org.cytoscape.app.communitydetection.edgelist.reader.EdgeListReaderFactory;
import org.cytoscape.app.communitydetection.edgelist.writer.EdgeListWriterFactory;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	private static final String MENU = "Community Detection";
	private static final String WRITE_CX_MENU_ITEM = "Export Network to CX";
	private static final String READ_CX_MENU_ITEM = "Import Network from CX";
	private static final String MENU_ITEM_LOUVAIN = "Louvain";
	private static final String MENU_ITEM_INFOMAP = "Infomap";

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {

		final SynchronousTaskManager<?> syncTaskManager = getService(bc, SynchronousTaskManager.class);
		final CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class);

		// Setting up CX IO service listeners
		CxTaskFactory cxTaskFactory = CxTaskFactory.getInstance();
		registerServiceListener(bc, cxTaskFactory, "addReaderFactory", "removeReaderFactory",
				InputStreamTaskFactory.class);
		registerServiceListener(bc, cxTaskFactory, "addWriterFactory", "removeWriterFactory",
				CyNetworkViewWriterFactory.class);

		// Registering CX writing service
		Properties cxWriterTaskProps = new Properties();
		cxWriterTaskProps.setProperty(PREFERRED_MENU, MENU);
		cxWriterTaskProps.setProperty(MENU_GRAVITY, "1.0");
		cxWriterTaskProps.setProperty(TITLE, WRITE_CX_MENU_ITEM);
		// registerAllServices(bc, new CxWriterTask(), cxWriterTaskProps);

		// Registering CX reading service
		Properties cxReaderTaskProps = new Properties();
		cxReaderTaskProps.setProperty(PREFERRED_MENU, MENU);
		cxReaderTaskProps.setProperty(MENU_GRAVITY, "1.0");
		cxReaderTaskProps.setProperty(TITLE, READ_CX_MENU_ITEM);
		// registerAllServices(bc, new CxReaderTask(networkManager, dialogManager),
		// cxReaderTaskProps);

		// Setting up Edge List I/O services
		setupEdgeListIOServices(bc);

		// Registering Edge List services
		Properties edgeListWriterTaskProps = new Properties();
		edgeListWriterTaskProps.setProperty(PREFERRED_MENU, MENU);
		edgeListWriterTaskProps.setProperty(MENU_GRAVITY, "1.0");
		edgeListWriterTaskProps.setProperty(TITLE, MENU_ITEM_LOUVAIN);
		registerAllServices(bc, new EdgeListTaskFactory(rootNetworkManager, syncTaskManager, MENU_ITEM_LOUVAIN.toLowerCase()),
				edgeListWriterTaskProps);

		edgeListWriterTaskProps.replace(TITLE, MENU_ITEM_INFOMAP);
		registerAllServices(bc, new EdgeListTaskFactory(rootNetworkManager, syncTaskManager, MENU_ITEM_INFOMAP.toLowerCase()),
				edgeListWriterTaskProps);
	}

	private void setupEdgeListIOServices(BundleContext bc) {

		final CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		final CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		final CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		final CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
		final CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class);
		final VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
		final CyLayoutAlgorithmManager layoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);
		final SynchronousTaskManager<?> syncTaskManager = getService(bc, SynchronousTaskManager.class);

		final StreamUtil streamUtil = getService(bc, StreamUtil.class);

		final BasicCyFileFilter edgeFilter = new BasicCyFileFilter(new String[] {}, new String[] { "text/edgelist" },
				"Adjacency List", DataCategory.NETWORK, streamUtil);

		final Properties edgeListWriterProperties = new Properties();
		edgeListWriterProperties.put(ID, "edgeListWriterFactory");
		final EdgeListWriterFactory edgeNetworkWriterFactory = new EdgeListWriterFactory(edgeFilter);
		registerAllServices(bc, edgeNetworkWriterFactory, edgeListWriterProperties);

		final Properties edgeListReaderProperties = new Properties();
		edgeListReaderProperties.put(ID, "edgeListReaderFactory");
		final EdgeListReaderFactory edgeListReaderFactory = new EdgeListReaderFactory(edgeFilter, networkViewFactory,
				networkFactory, networkManager, networkViewManager, rootNetworkManager, visualMappingManager,
				layoutAlgorithmManager, syncTaskManager);
		registerAllServices(bc, edgeListReaderFactory, edgeListReaderProperties);

		EdgeListTaskListenerFactory edgeTaskFactory = EdgeListTaskListenerFactory.getInstance();
		registerServiceListener(bc, edgeTaskFactory, "addReaderFactory", "removeReaderFactory",
				InputStreamTaskFactory.class);
		registerServiceListener(bc, edgeTaskFactory, "addWriterFactory", "removeWriterFactory",
				CyNetworkViewWriterFactory.class);
	}

}
