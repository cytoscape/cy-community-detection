package org.cytoscape.app.communitydetection.edge;

import java.io.FileOutputStream;

import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class EdgeWriterTask implements NetworkTaskFactory {

	private CyWriter writer;

	public EdgeWriterTask() {
	}

	private AbstractTask getTaskWrapper(CyNetwork network) {

		AbstractTask wrapper = new AbstractTask() {

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				FileOutputStream outStream = new FileOutputStream(
						"C:\\Workspace\\Cytoscape\\cy-community-detection\\testEdgeList.txt");
				CyNetworkViewWriterFactory writerFactory = EdgeReaderWriterTaskFactory.getInstance()
						.getCxWriterFactory();
				writer = writerFactory.createWriter(outStream, network);
				writer.run(taskMonitor);
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
