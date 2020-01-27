package org.cytoscape.app.communitydetection.edgelist;

import java.io.OutputStream;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

/**
 * {@link CyNetworkViewWriterFactory} implementation to create
 * {@link WriterTask}.
 *
 */
public class WriterTaskFactoryImpl implements WriterTaskFactory {


	public WriterTaskFactoryImpl(){

	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetwork network, String attribute) {
		return new WriterTask(os, network, attribute);
	}

}
