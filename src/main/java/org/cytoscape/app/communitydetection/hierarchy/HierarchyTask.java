package org.cytoscape.app.communitydetection.hierarchy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import org.cytoscape.app.communitydetection.PropertiesHelper;

import org.cytoscape.app.communitydetection.edgelist.ReaderTask;
import org.cytoscape.app.communitydetection.edgelist.ReaderTaskFactory;
import org.cytoscape.app.communitydetection.edgelist.ReaderTaskFactoryImpl;
import org.cytoscape.app.communitydetection.edgelist.WriterTask;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactory;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactoryImpl;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

/**
 * Wraps {@link WriterTask} and {@link ReaderTask}. Executes a community
 * detection algorithm on the selected network.
 *
 */
public class HierarchyTask extends AbstractTask {

	private final CyNetwork network;
	private final CommunityDetectionAlgorithm algorithm;
	private final String weightColumn;
	private final Map<String, String> customParameters;
	private WriterTaskFactory _writerFactory;
	private ReaderTaskFactory _readerFactory;
	

	public HierarchyTask(ReaderTaskFactory readerFactory, CyNetwork network, CommunityDetectionAlgorithm algorithm, Map<String, String> customParameters,
		final String weightColumn){
	    this.network = network;
	    this.algorithm = algorithm;
	    this.weightColumn = weightColumn;
	    this.customParameters = customParameters;
		_writerFactory = new WriterTaskFactoryImpl();
		_readerFactory = readerFactory;
	}
	
	protected void setAlternateWriterTaskFactory(WriterTaskFactory altFactory){
		_writerFactory = altFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (this.algorithm == null){
		    return;
		}
		long startTime = System.currentTimeMillis();
		taskMonitor.setTitle("Community Detection: Creating Hierarchy Network");
		taskMonitor.setStatusMessage("Exporting the network");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		CyWriter writer = _writerFactory.createWriter(outStream, network, weightColumn);
		writer.run(taskMonitor);
		String resultURI = CDRestClient.getInstance().postCDData(algorithm.getName(),
			this.customParameters, outStream.toString());
		if (cancelled) {
			CDRestClient.getInstance().setTaskCanceled(false);
			return;
		}
		taskMonitor.setProgress(0.1);
		taskMonitor.setStatusMessage("Network exported, retrieving the hierarchy");
		CommunityDetectionResult cdResult = CDRestClient.getInstance().getCDResult(resultURI, taskMonitor, 0.1f, 0.8f,
				PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis());
		if (cancelled) {
			CDRestClient.getInstance().setTaskCanceled(false);
			return;
		}
		InputStream inStream = new ByteArrayInputStream(
				cdResult.getResult().asText().trim().replace(';', '\n').getBytes());
		taskMonitor.setProgress(0.9);
		System.out.printf("CD app: Hierarchy edge list received in %d ms\n", (System.currentTimeMillis() - startTime));
		taskMonitor.setStatusMessage("Received hierarchy, creating a new network");

		TaskIterator iterator = _readerFactory.createTaskIterator(inStream, null, network.getSUID());
		ReaderTask reader = (ReaderTask) iterator.next();
		reader.run(taskMonitor);
		
		reader.setNetworkAttributes(weightColumn, algorithm, cdResult, this.customParameters);
		taskMonitor.setProgress(0.95);
		System.out.printf("CD app: Newtork created in %d ms\n", (System.currentTimeMillis() - startTime));

		taskMonitor.setStatusMessage("Creating a view for the network");
		reader.buildCyNetworkView(reader.getNetworks()[0]);
		taskMonitor.setProgress(1.0);
		System.out.printf("CD app: Total time: %d ms\n", (System.currentTimeMillis() - startTime));
	}

	@Override
	public void cancel() {
		CDRestClient.getInstance().setTaskCanceled(true);
		super.cancel();
	}

}
