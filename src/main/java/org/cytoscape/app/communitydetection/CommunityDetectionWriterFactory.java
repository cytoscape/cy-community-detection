package org.cytoscape.app.communitydetection;

import java.io.OutputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class CommunityDetectionWriterFactory extends CxNetworkWriterFactory
implements NetworkTaskFactory, NetworkViewTaskFactory {

	private OutputStream outputStream;

	public CommunityDetectionWriterFactory(CyFileFilter filter, OutputStream outputStream) {
		super(filter);
		this.outputStream = outputStream;
	}

	public CommunityDetectionWriterFactory(CyFileFilter filter, VisualMappingManager visual_mapping_manager,
			CyApplicationManager application_manager, CyNetworkViewManager networkview_manager,
			CyNetworkManager network_manager, CyGroupManager group_manager, CyNetworkTableManager table_manager,
			OutputStream outputStream) {
		super(filter, visual_mapping_manager, application_manager, networkview_manager, network_manager, group_manager,
				table_manager);
		this.outputStream = outputStream;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		CyWriter writerTask = createWriter(outputStream, network);
		return new TaskIterator(writerTask);
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return network != null;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		CyWriter writerTask = createWriter(outputStream, networkView);
		return new TaskIterator(writerTask);
	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		return networkView != null;
	}

}
