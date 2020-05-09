package org.cytoscape.app.communitydetection.edgelist;

import java.io.IOException;
import java.io.OutputStream;
import org.cytoscape.app.communitydetection.util.AppUtils;

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
	private final String weightColumn;
	private final StringBuilder sBuilder;
	public static final String TAB = "\t";
	public static final String NEW_LINE = "\n";

	/**
	 * @param outStream
	 * @param network
	 * @param attribute
	 */
	public WriterTask(OutputStream outStream, CyNetwork network, String attribute) {
		this.outStream = outStream;
		this.network = network;
		this.weightColumn = attribute;
		sBuilder = new StringBuilder();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		if (weightColumn == null || weightColumn.equals(AppUtils.TYPE_NONE_VALUE)) {
			for (CyEdge edge : network.getEdgeList()) {
				writeEdgeToStream(edge.getSource().getSUID(),  edge.getTarget().getSUID(), null);
			}
		} else {
			for (CyEdge edge : network.getEdgeList()) {
				Number cellValue = (Number) network.getRow(edge).get(weightColumn, getColumnType());
				if (cellValue == null){
				    throw new Exception(weightColumn + " does not have a value for row with SUID: " + edge.getSUID().toString() +
					    " Please select a column with values in all cells");
				}
				if (cellValue.doubleValue() < 0) {
					throw new Exception(weightColumn
							+ " contains negative values. Please select a column with non-negative data values");
				}
				writeEdgeToStream(edge.getSource().getSUID(),
						edge.getTarget().getSUID(),
						cellValue.toString());
			}
		}
	}
	
	private void writeEdgeToStream(Long sourceId, Long targetId, String weight) throws Exception {
		sBuilder.setLength(0);
		sBuilder.append(sourceId.toString());
		sBuilder.append(TAB);
		sBuilder.append(targetId.toString());
		if (weight != null){
			sBuilder.append(TAB);
			sBuilder.append(weight);
		}
		sBuilder.append(NEW_LINE);
		outStream.write(sBuilder.toString().getBytes());
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
		return network.getDefaultEdgeTable().getColumn(weightColumn).getType();
	}
}
