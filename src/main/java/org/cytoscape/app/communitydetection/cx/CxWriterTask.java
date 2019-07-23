package org.cytoscape.app.communitydetection.cx;

import java.io.ByteArrayOutputStream;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class CxWriterTask implements NetworkTaskFactory {

	private CyWriter writer;

	public CxWriterTask() {
	}

	private AbstractTask getTaskWrapper(CyNetwork network) {

		AbstractTask wrapper = new AbstractTask() {

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				// FileOutputStream outStream = new FileOutputStream(
				// "C:\\Workspace\\Cytoscape\\cy-community-detection\\test\\test.json");
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				CyNetworkViewWriterFactory writerFactory = CxReaderWriterTaskFactory.getInstance().getCxWriterFactory();
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
