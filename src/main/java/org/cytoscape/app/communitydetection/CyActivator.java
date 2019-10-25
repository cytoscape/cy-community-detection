package org.cytoscape.app.communitydetection;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.List;
import java.util.Properties;

import org.cytoscape.app.communitydetection.edgelist.ReaderTaskFactoryImpl;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.HierarchySettingsAction;
import org.cytoscape.app.communitydetection.hierarchy.HierarchyTaskFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.TaskListenerFactory;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.subnetwork.SubNetworkTaskFactoryImpl;
import org.cytoscape.app.communitydetection.termmap.NetworkTermMappingTaskFactoryImpl;
import org.cytoscape.app.communitydetection.termmap.NodeTermMappingTaskFactoryImpl;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
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
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
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
		final LoadVizmapFileTaskFactory vizmapFileTaskFactory = getService(bc, LoadVizmapFileTaskFactory.class);
		final CyLayoutAlgorithmManager layoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);
		final SynchronousTaskManager<?> syncTaskManager = getService(bc, SynchronousTaskManager.class);
		final CyNetworkNaming networkNaming = getService(bc, CyNetworkNaming.class);
		final CySwingApplication swingApplication = getService(bc, CySwingApplication.class);

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

		Properties taskExecProps = new Properties();
		taskExecProps.setProperty(MENU_GRAVITY, "1.0");
		// Registering Edge List services
		List<CommunityDetectionAlgorithm> cdAlgos = CDRestClient.getInstance()
				.getAlgorithmsByType(AppUtils.CD_ALGORITHM_INPUT_TYPE);
		for (CommunityDetectionAlgorithm algo : cdAlgos) {
			taskExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU + "." + algo.getDisplayName());
			taskExecProps.setProperty(TITLE, AppUtils.TYPE_NONE_VALUE);
			registerAllServices(bc, new HierarchyTaskFactoryImpl(algo, AppUtils.TYPE_NONE), taskExecProps);

			taskExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU + "." + algo.getDisplayName());
			taskExecProps.setProperty(TITLE, AppUtils.TYPE_WEIGHTED_VALUE);
			registerAllServices(bc, new HierarchyTaskFactoryImpl(algo, AppUtils.TYPE_WEIGHTED), taskExecProps);

			taskExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU + "." + algo.getDisplayName());
			taskExecProps.setProperty(TITLE, AppUtils.TYPE_ABOUT_VALUE);
			registerAllServices(bc, new HierarchyTaskFactoryImpl(algo, AppUtils.TYPE_ABOUT), taskExecProps);
		}
		List<CommunityDetectionAlgorithm> tmAlgos = CDRestClient.getInstance()
				.getAlgorithmsByType(AppUtils.TM_ALGORITHM_INPUT_TYPE);
		for (CommunityDetectionAlgorithm algo : tmAlgos) {
			taskExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU + "." + algo.getDisplayName());
			taskExecProps.setProperty(TITLE, algo.getDisplayName());
			registerAllServices(bc, new NetworkTermMappingTaskFactoryImpl(algo, AppUtils.TYPE_NONE), taskExecProps);

			taskExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU + "." + algo.getDisplayName());
			taskExecProps.setProperty(TITLE, AppUtils.TYPE_ABOUT_VALUE);
			registerAllServices(bc, new NetworkTermMappingTaskFactoryImpl(algo, AppUtils.TYPE_ABOUT), taskExecProps);
		}
		registerAllServices(bc, new HierarchySettingsAction(applicationManager, networkViewManager, swingApplication));

		Properties contextMenuProps = new Properties();
		contextMenuProps.setProperty(PREFERRED_MENU, AppUtils.CONTEXT_MENU);
		contextMenuProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
		contextMenuProps.setProperty(TITLE, "View Interactions for this Community");
		contextMenuProps.put(IN_MENU_BAR, false);
		contextMenuProps.put(IN_CONTEXT_MENU, true);
		SubNetworkTaskFactoryImpl subnetworkfactoryImpl = new SubNetworkTaskFactoryImpl(rootNetworkManager,
				networkManager, networkViewManager, networkViewFactory, visualMappingManager, layoutAlgorithmManager,
				syncTaskManager, networkNaming);
		registerAllServices(bc, subnetworkfactoryImpl, contextMenuProps);
		for (CommunityDetectionAlgorithm algo : tmAlgos) {
			contextMenuProps.setProperty(TITLE, algo.getDisplayName());
			registerAllServices(bc, new NodeTermMappingTaskFactoryImpl(algo), contextMenuProps);
		}
	}

}
