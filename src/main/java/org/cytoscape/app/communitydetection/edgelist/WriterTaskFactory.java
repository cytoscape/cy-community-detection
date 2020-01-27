package org.cytoscape.app.communitydetection.edgelist;

import java.io.OutputStream;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;

/**
 *
 * @author churas
 */
public interface WriterTaskFactory {

	CyWriter createWriter(OutputStream os, CyNetwork network, String attribute);
	
}
