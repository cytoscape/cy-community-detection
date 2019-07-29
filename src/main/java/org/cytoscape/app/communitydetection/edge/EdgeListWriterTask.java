package org.cytoscape.app.communitydetection.edge;

import java.io.ByteArrayOutputStream;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class EdgeListWriterTask implements NetworkTaskFactory {

	private CyWriter writer;

	public EdgeListWriterTask() {
	}

	private AbstractTask getTaskWrapper(CyNetwork network) {

		AbstractTask wrapper = new AbstractTask() {

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				CyNetworkViewWriterFactory writerFactory = EdgeListReaderWriterTaskFactory.getInstance()
						.getEdgeListWriterFactory();
				writer = writerFactory.createWriter(outStream, network);
				writer.run(taskMonitor);
				CDRestClient.getInstance().postEdgeList(outStream.toString());
			}
		};
		return wrapper;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(getTaskWrapper(network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return network != null;
	}

}
