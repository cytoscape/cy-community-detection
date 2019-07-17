package org.cytoscape.app.communitydetection.edge;

import java.util.Map;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;

public class EdgeReaderWriterTaskFactory {

	// ID of the CX writer service
	private static final String EDGE_READER_ID = "cdEdgeReaderFactory";
	private static final String EDGE_WRITER_ID = "edgeNetworkWriterFactory";
	private static final String ID_TAG = "id";

	private CyNetworkViewWriterFactory writerFactory;
	private InputStreamTaskFactory readerFactory;


	private EdgeReaderWriterTaskFactory() {
	}

	private static class SingletonHelper {

		private static final EdgeReaderWriterTaskFactory INSTANCE = new EdgeReaderWriterTaskFactory();
	}

	public static EdgeReaderWriterTaskFactory getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public InputStreamTaskFactory getCxReaderFactory() {
		return readerFactory;
	}

	public CyNetworkViewWriterFactory getCxWriterFactory() {
		return writerFactory;
	}

	@SuppressWarnings("rawtypes")
	public void addWriterFactory(final CyNetworkViewWriterFactory factory, final Map properties) {
		final String id = (String) properties.get(ID_TAG);
		if (id != null && id.equals(EDGE_WRITER_ID)) {
			writerFactory = factory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeWriterFactory(final CyNetworkViewWriterFactory factory, Map properties) {
		final String id = (String) properties.get(ID_TAG);

		if (id != null && id.equals(EDGE_WRITER_ID)) {
			writerFactory = null;
		}
	}

	@SuppressWarnings("rawtypes")
	public void addReaderFactory(final InputStreamTaskFactory factory, final Map properties) {
		final String id = (String) properties.get(ID_TAG);
		if (id != null && id.equals(EDGE_READER_ID)) {
			readerFactory = factory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeReaderFactory(final InputStreamTaskFactory factory, Map properties) {
		final String id = (String) properties.get(ID_TAG);

		if (id != null && id.equals(EDGE_READER_ID)) {
			readerFactory = null;
		}
	}
}
