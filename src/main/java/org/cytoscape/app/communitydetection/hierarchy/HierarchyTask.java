package org.cytoscape.app.communitydetection.hierarchy;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.app.communitydetection.edgelist.WriterTask;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactory;
import org.cytoscape.app.communitydetection.edgelist.WriterTaskFactoryImpl;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps {@link WriterTask}. Executes a community
 detection _algorithm on the selected _network.
 *
 */
public class HierarchyTask extends AbstractTask {

	private final static Logger LOGGER = LoggerFactory.getLogger(HierarchyTask.class);
	private final CyNetwork _network;
	private final CommunityDetectionAlgorithm _algorithm;
	private final String _weightColumn;
	private final Map<String, String> _customParameters;
	private WriterTaskFactory _writerFactory;
	private HierarchyNetworkFactory _networkFactory;
	private CDRestClient _restClient;
	private HierarchyNetworkViewFactory _networkViewFactory;
	private VisualStyleFactory _styleFactory;
	private LayoutFactory _layoutFactory;
	

	public HierarchyTask(HierarchyNetworkFactory networkFactory,
			HierarchyNetworkViewFactory networkViewFactory,
			VisualStyleFactory styleFactory,
			LayoutFactory layoutFactory,
			CyNetwork network,
			CommunityDetectionAlgorithm algorithm, Map<String, String> customParameters,
		final String weightColumn){
	    _network = network;
	    _algorithm = algorithm;
	    _weightColumn = weightColumn;
	    _customParameters = customParameters;
		_writerFactory = new WriterTaskFactoryImpl();
		_networkFactory = networkFactory;
		_restClient = CDRestClient.getInstance();
		_networkViewFactory = networkViewFactory;
		_styleFactory = styleFactory;
		_layoutFactory = layoutFactory;
	}
	
	/**
	 * Sets alternate writer task factory. Used for testing purposes
	 * @param altFactory 
	 */
	protected void setAlternateWriterTaskFactory(WriterTaskFactory altFactory){
		_writerFactory = altFactory;
	}
	
	/**
	 * Sets alternate REST client. Used for testing purposes
	 * @param restClient 
	 */
	protected void setAlternateCDRestClient(CDRestClient restClient){
		_restClient = restClient;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (this._algorithm == null){
		    return;
		}
		long startTime = System.currentTimeMillis();
		taskMonitor.setTitle("Community Detection: Creating Hierarchy Network");
		taskMonitor.setStatusMessage("Exporting the network");
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		CyWriter writer = _writerFactory.createWriter(outStream, _network, _weightColumn);

		writer.run(taskMonitor);
		String resultURI = _restClient.postCDData(_algorithm.getName(),
			this._customParameters, outStream.toString());		
		if (cancelled) {
			_restClient.setTaskCanceled(false);
			return;
		}
		taskMonitor.setProgress(0.1);
		taskMonitor.setStatusMessage("Network exported, retrieving the hierarchy");
		CommunityDetectionResult cdResult = _restClient.getCDResult(resultURI, taskMonitor, 0.1f, 0.8f,
				PropertiesHelper.getInstance().getCommunityDetectionTimeoutMillis());
		if (cancelled) {
			_restClient.setTaskCanceled(false);
			return;
		}
		taskMonitor.setProgress(0.9);
		taskMonitor.setStatusMessage("Received hierarchy in " +
				Long.toString((System.currentTimeMillis() - startTime)) + " ms, creating a new network");

		CyNetwork hierarchyNetwork = _networkFactory.getHierarchyNetwork(_network, cdResult,
				_weightColumn, _algorithm, this._customParameters);
		if (hierarchyNetwork == null){
			throw new Exception("Error creating hierarchy from result");
		}
		taskMonitor.setProgress(0.95);
		taskMonitor.setStatusMessage("Network created in " +
				Long.toString((System.currentTimeMillis() - startTime)) + " ms");

		taskMonitor.setStatusMessage("Creating a view for the network");
		if (_networkViewFactory == null){
			throw new Exception("networkViewFactory is null");
		}
		if (_styleFactory == null){
			throw new Exception("styleFactory is null");
		}
		if (_layoutFactory == null){
			throw new Exception("layoutFactory is null");
		}
		_networkViewFactory.getHierarchyNetworkView(hierarchyNetwork,
				_styleFactory.getVisualStyle(),
				_layoutFactory.getLayoutAlgorithm());
		taskMonitor.setProgress(1.0);
		taskMonitor.setStatusMessage("Total time " +
				Long.toString((System.currentTimeMillis() - startTime)) + " ms");
	}

	@Override
	public void cancel() {
		_restClient.setTaskCanceled(true);
		super.cancel();
	}

}
