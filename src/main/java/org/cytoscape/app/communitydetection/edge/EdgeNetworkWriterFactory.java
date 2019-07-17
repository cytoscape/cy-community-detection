package org.cytoscape.app.communitydetection.edge;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class EdgeNetworkWriterFactory implements CyNetworkViewWriterFactory {

	private final CyFileFilter fileFilter;

	public EdgeNetworkWriterFactory(CyFileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new EdgeNetworkWriter(os, view.getModel());
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new EdgeNetworkWriter(os, network);
	}

}
