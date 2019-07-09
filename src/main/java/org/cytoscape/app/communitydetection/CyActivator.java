package org.cytoscape.app.communitydetection;

import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxFileFilter;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	private static final String MENU = "Apps.MyApps";
	private static final String MENU_ITEM = "Community Detection";

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {

		final StreamUtil streamUtil = getService(bc, StreamUtil.class);

		final CytoscapeCxFileFilter cx_filter = new CytoscapeCxFileFilter(new String[] { "cx" },
				new String[] { "application/json" }, "CX JSON", DataCategory.NETWORK, streamUtil);

		// Writer:
		final VisualMappingManager visual_mapping_manager = getService(bc, VisualMappingManager.class);
		final CyApplicationManager application_manager = getService(bc, CyApplicationManager.class);
		final CyNetworkViewManager networkview_manager = getService(bc, CyNetworkViewManager.class);
		final CyNetworkManager network_manager = getService(bc, CyNetworkManager.class);
		final CyGroupManager group_manager = getService(bc, CyGroupManager.class);
		final CyNetworkTableManager table_manager = getService(bc, CyNetworkTableManager.class);
		getService(bc, CyNetworkViewFactory.class);
		final OutputStream outputstream = new FileOutputStream("file.cx");
		final CommunityDetectionWriterFactory networkWriterFactory = new CommunityDetectionWriterFactory(cx_filter,
				visual_mapping_manager, application_manager, networkview_manager, network_manager, group_manager,
				table_manager, outputstream);

		final Properties cxWriterFactoryProperties = new Properties();

		cxWriterFactoryProperties.put(ID, "cxNetworkWriterFactory");
		cxWriterFactoryProperties.setProperty(MENU_GRAVITY, "1.0");
		cxWriterFactoryProperties.setProperty(PREFERRED_MENU, MENU);
		cxWriterFactoryProperties.setProperty(TITLE, MENU_ITEM);

		// registerService(bc, networkWriterFactory, NetworkViewTaskFactory.class,
		// cxWriterFactoryProperties);
		registerAllServices(bc, networkWriterFactory, cxWriterFactoryProperties);

		/*
		 * final CyLayoutAlgorithmManager layoutManager = getService(bc,
		 * CyLayoutAlgorithmManager.class); getService(bc, CyNetworkFactory.class);
		 * getService(bc, CyRootNetworkManager.class); getService(bc,
		 * RenderingEngineManager.class); getService(bc, VisualStyleFactory.class);
		 * getService(bc, CyGroupFactory.class); new CytoscapeCxFileFilter(new String[]
		 * { "cx" }, new String[] { "application/json" }, "CX JSON",
		 * DataCategory.NETWORK, streamUtil);
		 *
		 * getService(bc, VisualMappingFunctionFactory.class,
		 * "(mapping.type=continuous)"); getService(bc,
		 * VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		 * getService(bc, VisualMappingFunctionFactory.class,
		 * "(mapping.type=passthrough)");
		 *
		 * final CytoscapeCxNetworkReaderFactory cx_reader_factory = new
		 * CytoscapeCxNetworkReaderFactory(cxfilter, application_manager,
		 * network_factory, network_manager, root_network_manager,
		 * visual_mapping_manager, visual_style_factory, group_factory,
		 * rendering_engine_manager, network_view_factory, vmfFactoryC, vmfFactoryD,
		 * vmfFactoryP, layoutManager); final Properties reader_factory_properties = new
		 * Properties();
		 *
		 * // This is the unique identifier for this reader. 3rd party developer // can
		 * use this service by using this ID. reader_factory_properties.put(ID,
		 * "cytoscapeCxNetworkReaderFactory"); registerService(bc, cx_reader_factory,
		 * InputStreamTaskFactory.class, reader_factory_properties);
		 */

	}

}
