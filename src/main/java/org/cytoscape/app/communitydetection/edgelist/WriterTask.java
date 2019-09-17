package org.cytoscape.app.communitydetection.edgelist;

import java.io.IOException;
import java.io.OutputStream;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates output stream from the edge list of the selected interaction network.
 * Implements {@link CyWriter}.
 *
 */
public class WriterTask implements CyWriter {

	private final static Logger logger = LoggerFactory.getLogger(WriterTask.class);

	private final OutputStream outStream;
	private final CyNetwork network;
	private final String attribute;

	/**
	 * @param outStream
	 * @param network
	 * @param attribute
	 */
	public WriterTask(OutputStream outStream, CyNetwork network, String attribute) {
		this.outStream = outStream;
		this.network = network;
		this.attribute = attribute;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (attribute.equals("none")) {
			for (CyEdge edge : network.getEdgeList()) {
				String s = edge.getSource().getSUID().toString() + "\t" + edge.getTarget().getSUID().toString() + "\n";
				outStream.write(s.getBytes());
			}
		} else {
			for (CyEdge edge : network.getEdgeList()) {
				if ((Double) network.getRow(edge).get(attribute, getColumnType()) < 0) {
					throw new Exception(attribute
							+ " contains negative values. Please select an attribute with non-negative values");
				}
				String s = edge.getSource().getSUID().toString() + "\t" + edge.getTarget().getSUID().toString() + "\t"
						+ network.getRow(edge).get(attribute, getColumnType()) + "\n";
				outStream.write(s.getBytes());
			}
		}
	}

	@Override
	public void cancel() {
		if (outStream == null) {
			return;
		}
		try {
			outStream.close();
		} catch (IOException e) {
			logger.error("Could not close Outputstream for EdgeNetworkWriter.", e);
		}
	}

	private Class<?> getColumnType() {
		return network.getDefaultEdgeTable().getColumn(attribute).getType();
	}
}
