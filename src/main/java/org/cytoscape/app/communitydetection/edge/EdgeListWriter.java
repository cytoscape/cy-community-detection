package org.cytoscape.app.communitydetection.edge;

import java.io.IOException;
import java.io.OutputStream;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeListWriter implements CyWriter {

	private final static Logger logger = LoggerFactory.getLogger(EdgeListWriter.class);

	private final OutputStream outStream;
	private final CyNetwork network;

	public EdgeListWriter(OutputStream outStream, CyNetwork network) {
		this.outStream = outStream;
		this.network = network;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		for (CyEdge edge : network.getEdgeList()) {
			String s = edge.getSource().getSUID().toString() + "\t" + edge.getTarget().getSUID().toString() + "\n";
			outStream.write(s.getBytes());
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

	private final static String getInteractionFromEdgeTable(final CyNetwork network, final CyEdge edge) {
		final CyRow row = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS).getRow(edge.getSUID());
		if (row != null) {
			final Object o = row.getRaw(AppUtils.SHARED_INTERACTION);
			if ((o != null) && (o instanceof String)) {
				return String.valueOf(o);
			}
		}
		return "";
	}
}
