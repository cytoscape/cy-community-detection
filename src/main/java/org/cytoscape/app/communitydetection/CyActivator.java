package org.cytoscape.app.communitydetection;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.app.communitydetection.edgelist.ReaderTaskFactoryImpl;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.HierarchyTaskFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.JEditorPaneFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.LauncherDialog;
import org.cytoscape.app.communitydetection.hierarchy.TaskListenerFactory;
import org.cytoscape.app.communitydetection.subnetwork.SubNetworkTaskFactoryImpl;
import org.cytoscape.app.communitydetection.termmap.NetworkTermMappingTaskFactoryImpl;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
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

		final CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		final CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		final CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		final CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
		final CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class);
		final VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
		final LoadVizmapFileTaskFactory vizmapFileTaskFactory = getService(bc, LoadVizmapFileTaskFactory.class);
		final CyLayoutAlgorithmManager layoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);
		final SynchronousTaskManager<?> syncTaskManager = getService(bc, SynchronousTaskManager.class);
		final CyNetworkNaming networkNaming = getService(bc, CyNetworkNaming.class);
		final CySwingApplication swingApplication = getService(bc, CySwingApplication.class);
		
		PropertiesReader propReader = new PropertiesReader(AppUtils.APP_NAME, AppUtils.PROP_NAME);
		Properties propReaderProperties = new Properties();
		propReaderProperties.setProperty("cyPropertyName", AppUtils.PROP_NAME);
		registerAllServices(bc, propReader, propReaderProperties);

		final CyProperty<Properties> cyProperties = getService(bc, CyProperty.class,
				"(cyPropertyName=" + AppUtils.PROP_NAME + ")");
		PropertiesHelper.getInstance().setBaseurl(cyProperties.getProperties().getProperty(AppUtils.PROP_APP_BASEURL));
		PropertiesHelper.getInstance()
				.setThreadcount(cyProperties.getProperties().getProperty(AppUtils.PROP_APP_THREADCOUNT));

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
				vizmapFileTaskFactory, layoutAlgorithmManager, syncTaskManager, networkNaming);
		registerService(bc, readerTaskWrapper, InputStreamTaskFactory.class, readerProperties);

		TaskListenerFactory edgeTaskFactory = TaskListenerFactory.getInstance();
		registerServiceListener(bc, edgeTaskFactory, "addReaderFactory", "removeReaderFactory",
				InputStreamTaskFactory.class);
		registerServiceListener(bc, edgeTaskFactory, "addWriterFactory", "removeWriterFactory",
				CyNetworkViewWriterFactory.class);

		JEditorPaneFactoryImpl editorPaneFac = new JEditorPaneFactoryImpl();
		// Add Run Community Detection under Apps => Community Detection
		// menu
		Properties taskExecProps = new Properties();
		taskExecProps.setProperty(MENU_GRAVITY, "1.0");
		taskExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		taskExecProps.setProperty(TITLE, "Run Community Detection");
		LauncherDialog clusterAlgoDialog = new LauncherDialog(editorPaneFac,
		                                                      AppUtils.CD_ALGORITHM_INPUT_TYPE);
		registerAllServices(bc, new HierarchyTaskFactoryImpl(swingApplication, clusterAlgoDialog), taskExecProps);
		
		// Add Run Functional Enrichment under Apps => Community Detection
		// menu
		Properties tmExecProps = new Properties();
		tmExecProps.setProperty(MENU_GRAVITY, "2.0");
		tmExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		tmExecProps.setProperty(TITLE, "Run Functional Enrichment");
		LauncherDialog tmAlgoDialog = new LauncherDialog(new JEditorPaneFactoryImpl(),
		                                                      AppUtils.TM_ALGORITHM_INPUT_TYPE);
		NetworkTermMappingTaskFactoryImpl termFac = new NetworkTermMappingTaskFactoryImpl(swingApplication, tmAlgoDialog); 
		registerAllServices(bc, termFac, tmExecProps);

		// add About undern Apps => Community Detection
		// menu
		Properties aboutProps = new Properties();
		aboutProps.setProperty(MENU_GRAVITY, "3.0");
		aboutProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		aboutProps.setProperty(TITLE, "About");
		registerAllServices(bc, new AboutTaskFactoryImpl(swingApplication, editorPaneFac), aboutProps);
		
                // add View Interactions for this Community in context menu
		// displayed when user right clicks on a node
		Properties contextMenuProps = new Properties();
		contextMenuProps.setProperty(PREFERRED_MENU, AppUtils.CONTEXT_MENU_CD);
		contextMenuProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
		contextMenuProps.setProperty(TITLE, "View Interactions for Selected Node");
		contextMenuProps.put(IN_MENU_BAR, false);
		contextMenuProps.put(IN_CONTEXT_MENU, true);
		SubNetworkTaskFactoryImpl subnetworkfactoryImpl = new SubNetworkTaskFactoryImpl(rootNetworkManager,
				networkManager, networkViewManager, networkViewFactory, visualMappingManager, layoutAlgorithmManager,
				syncTaskManager, networkNaming);
		registerAllServices(bc, subnetworkfactoryImpl, contextMenuProps);
	}

}
