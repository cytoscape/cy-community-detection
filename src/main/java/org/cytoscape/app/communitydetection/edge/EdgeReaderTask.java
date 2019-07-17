package org.cytoscape.app.communitydetection.edge;

import java.io.FileInputStream;

import javax.swing.SwingUtilities;

import org.cytoscape.app.communitydetection.cx.CxReaderWriterTaskFactory;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;

public class EdgeReaderTask extends AbstractTaskFactory {

	private final CyNetworkManager networkManager;
	private final DialogTaskManager dialogManager;

	private AbstractCyNetworkReader reader;

	public EdgeReaderTask(CyNetworkManager networkManager, DialogTaskManager dialogManager) {
		this.networkManager = networkManager;
		this.dialogManager = dialogManager;
	}

	// Block UI thread while network gets populated
	private void execute(TaskIterator iterator) {
		Object lock = new Object();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				dialogManager.execute(iterator, new TaskObserver() {

					@Override
					public void taskFinished(ObservableTask task) {
					}

					@Override
					public void allFinished(FinishStatus finishStatus) {
						synchronized (lock) {
							lock.notify();
						}
					}
				});
			}
		};

		try {
			SwingUtilities.invokeAndWait(runnable);
			synchronized (lock) {
				lock.wait();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Wrapper to populate network from input stream
	private AbstractTask getTaskWrapper() {

		AbstractTask wrapper = new AbstractTask() {

			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				FileInputStream inStream = new FileInputStream(
						"C:\\Workspace\\Cytoscape\\cy-community-detection\\test.json");
				InputStreamTaskFactory readerFactory = CxReaderWriterTaskFactory.getInstance().getCxReaderFactory();
				TaskIterator iterator = readerFactory.createTaskIterator(inStream, null);

				reader = (AbstractCyNetworkReader) iterator.next();
				reader.setRootNetworkList(new ListSingleSelection<String>());
				iterator.append(reader);
				execute(iterator);
				for (CyNetwork net : reader.getNetworks()) {
					networkManager.addNetwork(net);
				}
				reader.buildCyNetworkView(reader.getNetworks()[0]);
			}
		};
		return wrapper;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(getTaskWrapper());
	}
}
