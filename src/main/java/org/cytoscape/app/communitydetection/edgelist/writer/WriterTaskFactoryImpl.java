package org.cytoscape.app.communitydetection.edgelist.writer;

import java.io.OutputStream;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class WriterTaskFactoryImpl implements CyNetworkViewWriterFactory {

	private final CyFileFilter fileFilter;

	public WriterTaskFactoryImpl(CyFileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new WriterTask(os, view.getModel(), AppUtils.TYPE_NONE);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new WriterTask(os, network, AppUtils.TYPE_NONE);
	}

	public CyWriter createWriter(OutputStream os, CyNetwork network, String attribute) {
		return new WriterTask(os, network, attribute);
	}

}
