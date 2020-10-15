package org.cytoscape.app.communitydetection;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;
import org.cytoscape.app.communitydetection.hierarchy.AboutAlgorithmEditorPaneFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.CustomParameterHelpJEditorPaneFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.HierarchyNetworkFactory;
import org.cytoscape.app.communitydetection.hierarchy.HierarchyNetworkViewFactory;

import org.cytoscape.app.communitydetection.hierarchy.HierarchyTaskFactoryImpl;
import org.cytoscape.app.communitydetection.util.JEditorPaneFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.LauncherDialog;
import org.cytoscape.app.communitydetection.hierarchy.LauncherDialogAlgorithmFactoryImpl;
import org.cytoscape.app.communitydetection.hierarchy.LayoutFactory;
import org.cytoscape.app.communitydetection.hierarchy.VisualStyleFactory;
import org.cytoscape.app.communitydetection.iquery.IQueryTaskFactoryImpl;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.subnetwork.ParentNetworkChooserDialog;
import org.cytoscape.app.communitydetection.subnetwork.ParentNetworkFinder;
import org.cytoscape.app.communitydetection.subnetwork.SubNetworkTaskFactoryImpl;
import org.cytoscape.app.communitydetection.tally.TallyAttributesTaskFactoryImpl;
import org.cytoscape.app.communitydetection.tally.TallyDialog;
import org.cytoscape.app.communitydetection.termmap.NetworkTermMappingTaskFactoryImpl;
import org.cytoscape.app.communitydetection.termmap.NodeTermMapppingTaskFactoryImpl;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.app.communitydetection.util.IconJLabelDialogFactory;
import org.cytoscape.app.communitydetection.util.ImageIconHolderFactory;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyActivator extends AbstractCyActivator {

	private final static Logger LOGGER = LoggerFactory.getLogger(CyActivator.class);
	public CyActivator() {
		super();
	}

	private CyProperty<Properties> loadPropertyReaderService(BundleContext bc) throws Exception {
		PropertiesReader propReader = new PropertiesReader(AppUtils.APP_NAME, AppUtils.PROP_NAME + ".props");
		Properties propReaderProperties = new Properties();
		propReaderProperties.setProperty("cyPropertyName", AppUtils.PROP_NAME + ".props");
		registerAllServices(bc, propReader, propReaderProperties);

		final CyProperty<Properties> cyProperties = getService(bc, CyProperty.class,
				"(cyPropertyName=" + AppUtils.PROP_NAME + ".props)");
		PropertiesHelper.getInstance().updateViaProperties(cyProperties.getProperties());
		return cyProperties;
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
		
		// sets up the PropertiesHelper and links it to properties that a user can
		// view and edit in Edit => Preferences menu
		CyProperty<Properties> cyProperties = loadPropertyReaderService(bc);
		
		ShowDialogUtil dialogUtil = new ShowDialogUtil();
		ImageIconHolderFactory iconHolderFactory = new ImageIconHolderFactory();
		JEditorPaneFactoryImpl editorPaneFac = new JEditorPaneFactoryImpl();
		IconJLabelDialogFactory iconJLabelFactory = new IconJLabelDialogFactory(dialogUtil,
				iconHolderFactory, editorPaneFac);

		AboutAlgorithmEditorPaneFactoryImpl aboutAlgoFac = new AboutAlgorithmEditorPaneFactoryImpl(editorPaneFac);
		CustomParameterHelpJEditorPaneFactoryImpl customHelpParameterFac = new CustomParameterHelpJEditorPaneFactoryImpl(editorPaneFac);
		LauncherDialogAlgorithmFactoryImpl algoFac = new LauncherDialogAlgorithmFactoryImpl(CDRestClient.getInstance(), dialogUtil);

		final HierarchyNetworkFactory hierarchyNetworkFactory = new HierarchyNetworkFactory(networkFactory, networkNaming, rootNetworkManager, networkManager);
		// Add Run Community Detection under Apps => Community Detection
		// menu
		Properties taskExecProps = new Properties();
		taskExecProps.setProperty(MENU_GRAVITY, "1.0");
		taskExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		taskExecProps.setProperty(TITLE, "Run Community Detection");
		LauncherDialog clusterAlgoDialog = new LauncherDialog(aboutAlgoFac, customHelpParameterFac, algoFac, dialogUtil,
		                                                      AppUtils.CD_ALGORITHM_INPUT_TYPES);
		PropertiesHelper.getInstance().addBaseurlUpdatedListener(clusterAlgoDialog);
		HierarchyNetworkViewFactory hierarchyNetworkViewFactory = new HierarchyNetworkViewFactory(networkViewManager,
		                          networkViewFactory, visualMappingManager, layoutAlgorithmManager, syncTaskManager);
		
		VisualStyleFactory styleFactory = new VisualStyleFactory(visualMappingManager, vizmapFileTaskFactory);
		LayoutFactory layoutFactory = new LayoutFactory(layoutAlgorithmManager);
		registerAllServices(bc, new HierarchyTaskFactoryImpl(swingApplication,
				clusterAlgoDialog, dialogUtil, hierarchyNetworkFactory,
		        hierarchyNetworkViewFactory, styleFactory, layoutFactory), taskExecProps);
		
		// Add Run Functional Enrichment under Apps => Community Detection
		// menu
		Properties tmExecProps = new Properties();
		tmExecProps.setProperty(MENU_GRAVITY, "2.0");
		tmExecProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		tmExecProps.setProperty(TITLE, "Run Functional Enrichment");
		LauncherDialog tmAlgoDialog = new LauncherDialog(aboutAlgoFac, customHelpParameterFac, algoFac, dialogUtil,
		                                                      AppUtils.TM_ALGORITHM_INPUT_TYPES);
		PropertiesHelper.getInstance().addBaseurlUpdatedListener(tmAlgoDialog);
		NetworkTermMappingTaskFactoryImpl termFac = new NetworkTermMappingTaskFactoryImpl(swingApplication, tmAlgoDialog); 
		registerAllServices(bc, termFac, tmExecProps);

		ParentNetworkFinder parentNetworkFinder = new ParentNetworkFinder();
		ParentNetworkChooserDialog parentNetworkDialog = new ParentNetworkChooserDialog(iconJLabelFactory);

		CyNetworkUtil cyNetworkUtil = new CyNetworkUtil();
		// add Tally Columns/Attributes under Apps => Community Detection
		// menu
		Properties tallyProps = new Properties();
		tallyProps.setProperty(MENU_GRAVITY, "3.0");
		tallyProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		tallyProps.setProperty(TITLE, "Tally Attributes on Hierarchy");
		TallyDialog tallyDialog = new TallyDialog(dialogUtil);
		TallyAttributesTaskFactoryImpl tallyFac = new TallyAttributesTaskFactoryImpl(swingApplication,
				dialogUtil, tallyDialog, parentNetworkFinder, parentNetworkDialog, cyNetworkUtil,
		networkManager);
		registerAllServices(bc, tallyFac, tallyProps);
		
		
		// add Settings under Apps => Community Detection
		// menu
		Properties settingsProps = new Properties();
		settingsProps.setProperty(MENU_GRAVITY, "4.0");
		settingsProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		settingsProps.setProperty(TITLE, "Settings");
		SettingsDialog settingsDialog = new SettingsDialog(iconJLabelFactory,
		                                                   PropertiesHelper.getInstance());
		registerAllServices(bc, new SettingsTaskFactoryImpl(swingApplication, settingsDialog, dialogUtil, cyProperties, algoFac), settingsProps);
		
		// add About undern Apps => Community Detection
		// menu
		Properties aboutProps = new Properties();
		aboutProps.setProperty(MENU_GRAVITY, "5.0");
		aboutProps.setProperty(PREFERRED_MENU, AppUtils.TOP_MENU);
		aboutProps.setProperty(TITLE, "About");
		registerAllServices(bc, new AboutTaskFactoryImpl(swingApplication, editorPaneFac, dialogUtil), aboutProps);
		
		// add View Interactions for this Community in context menu
		// displayed when user right clicks on a node
		Properties contextMenuProps = new Properties();
		contextMenuProps.setProperty(PREFERRED_MENU, AppUtils.CONTEXT_MENU_CD);
		contextMenuProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
		contextMenuProps.setProperty(TITLE, "View Interactions for Selected Node");
		contextMenuProps.put(IN_MENU_BAR, false);
		contextMenuProps.put(IN_CONTEXT_MENU, true);
		SubNetworkTaskFactoryImpl subnetworkfactoryImpl = new SubNetworkTaskFactoryImpl(swingApplication, dialogUtil, parentNetworkFinder,
				parentNetworkDialog, rootNetworkManager,
				networkManager, networkViewManager, networkViewFactory, visualMappingManager, layoutAlgorithmManager,
				syncTaskManager, networkNaming);
		registerAllServices(bc, subnetworkfactoryImpl, contextMenuProps);
		
		Properties enrichCMenuProps = new Properties();
		enrichCMenuProps.setProperty(PREFERRED_MENU, AppUtils.CONTEXT_MENU_CD);
		enrichCMenuProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
		enrichCMenuProps.setProperty(TITLE, "Run Functional Enrichment");
		enrichCMenuProps.put(IN_MENU_BAR, false);
		enrichCMenuProps.put(IN_CONTEXT_MENU, true);
		NodeTermMapppingTaskFactoryImpl nodeTermMapFac = new NodeTermMapppingTaskFactoryImpl(termFac);
		registerAllServices(bc, nodeTermMapFac, enrichCMenuProps);
		
		Properties iQueryCMenuProps = new Properties();
		iQueryCMenuProps.setProperty(PREFERRED_MENU, AppUtils.CONTEXT_MENU_CD);
		iQueryCMenuProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
		iQueryCMenuProps.setProperty(TITLE, "View Terms for Selected Node in iQuery");
		iQueryCMenuProps.put(IN_MENU_BAR, false);
		iQueryCMenuProps.put(IN_CONTEXT_MENU, true);
		IQueryTaskFactoryImpl iQueryFac = new IQueryTaskFactoryImpl(swingApplication, dialogUtil);
		registerAllServices(bc, iQueryFac, iQueryCMenuProps);
	}

}
