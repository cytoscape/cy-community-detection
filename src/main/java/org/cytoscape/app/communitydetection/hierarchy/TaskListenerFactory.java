package org.cytoscape.app.communitydetection.hierarchy;

import java.util.Map;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;

public class TaskListenerFactory {

	private static final String ID_TAG = "id";

	private CyNetworkViewWriterFactory writerFactory;
	private InputStreamTaskFactory readerFactory;

	private TaskListenerFactory() {
	}

	private static class SingletonHelper {

		private static final TaskListenerFactory INSTANCE = new TaskListenerFactory();
	}

	public static TaskListenerFactory getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public InputStreamTaskFactory getEdgeListReaderFactory() {
		return readerFactory;
	}

	public CyNetworkViewWriterFactory getEdgeListWriterFactory() {
		return writerFactory;
	}

	public void addWriterFactory(final CyNetworkViewWriterFactory factory, final Map<String, String> properties) {
		final String id = properties.get(ID_TAG);
		if (id != null && id.equals(AppUtils.EDGE_WRITER_ID)) {
			writerFactory = factory;
		}
	}

	public void removeWriterFactory(final CyNetworkViewWriterFactory factory, Map<String, String> properties) {
		final String id = properties.get(ID_TAG);

		if (id != null && id.equals(AppUtils.EDGE_WRITER_ID)) {
			writerFactory = null;
		}
	}

	public void addReaderFactory(final InputStreamTaskFactory factory, final Map<String, String> properties) {
		final String id = (String) properties.get(ID_TAG);
		if (id != null && id.equals(AppUtils.EDGE_READER_ID)) {
			readerFactory = factory;
		}
	}

	public void removeReaderFactory(final InputStreamTaskFactory factory, Map<String, String> properties) {
		final String id = (String) properties.get(ID_TAG);

		if (id != null && id.equals(AppUtils.EDGE_READER_ID)) {
			readerFactory = null;
		}
	}
}
