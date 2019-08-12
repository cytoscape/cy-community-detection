package org.cytoscape.app.communitydetection.edgelist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.rest.Result;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class EdgeListTaskFactory implements NetworkTaskFactory {

	private final CyRootNetworkManager rootNetworkManager;
	final SynchronousTaskManager<?> syncTaskManager;
	private final String algorithm;

	public EdgeListTaskFactory(CyRootNetworkManager rootNetworkManager, SynchronousTaskManager<?> syncTaskManager,
			String algorithm) {
		this.rootNetworkManager = rootNetworkManager;
		this.syncTaskManager = syncTaskManager;
		this.algorithm = algorithm;
	}

	private AbstractTask getWriterTask(CyNetwork network) {

		AbstractTask writerTask = new AbstractTask() {

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				taskMonitor.setTitle("Community Detection service");
				taskMonitor.setStatusMessage("Exporting the network");
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				CyNetworkViewWriterFactory writerFactory = EdgeListTaskListenerFactory.getInstance()
						.getEdgeListWriterFactory();
				CyWriter writer = writerFactory.createWriter(outStream, network);
				writer.run(taskMonitor);
				String resultURI = CDRestClient.getInstance().postEdgeList(algorithm, "true",
						getRootNetworkName(network), outStream);
				taskMonitor.setProgress(0.25);

				taskMonitor.setStatusMessage("Network exported, retrieving the hierarchy");
				Result cdResult = CDRestClient.getInstance().getEdgeList(resultURI);
				InputStream inStream = new ByteArrayInputStream(
						cdResult.getEdgeList().trim().replace(';', '\n').getBytes());
				taskMonitor.setProgress(0.5);

				taskMonitor.setStatusMessage("Received heirarchy, creating a new network");
				InputStreamTaskFactory readerFactory = EdgeListTaskListenerFactory.getInstance()
						.getEdgeListReaderFactory();
				TaskIterator iterator = readerFactory.createTaskIterator(inStream, cdResult.getRootnetwork());
				AbstractCyNetworkReader reader = (AbstractCyNetworkReader) iterator.next();
				reader.run(taskMonitor);
				taskMonitor.setProgress(0.75);

				taskMonitor.setStatusMessage("Creating a view for the network");
				reader.buildCyNetworkView(reader.getNetworks()[0]);
				taskMonitor.setProgress(1.0);
			}
		};
		return writerTask;
	}

	private String getRootNetworkName(CyNetwork network) {
		final CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(network);
		return rootNetwork.getRow(rootNetwork).get(CyRootNetwork.NAME, String.class);
	}

	private void setNetworkName(CyNetwork network, String name) {
		network.getRow(network).set(CyNetwork.NAME, name);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(getWriterTask(network));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return network != null;
	}
}
