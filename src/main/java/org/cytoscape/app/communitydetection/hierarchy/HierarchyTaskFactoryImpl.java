package org.cytoscape.app.communitydetection.hierarchy;

import javax.swing.JOptionPane;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

public class HierarchyTaskFactoryImpl implements NetworkTaskFactory {

	private final String algorithm;
	private final String type;

	public HierarchyTaskFactoryImpl(String algorithm, String type) {
		this.algorithm = algorithm;
		this.type = type;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		if (type.equals(AppUtils.TYPE_NONE)) {
			return new TaskIterator(new HierarchyTask(network, algorithm, AppUtils.TYPE_NONE));
		} else {
			String attribute = JOptionPane.showInputDialog("Please enter attribute name");
			return new TaskIterator(new HierarchyTask(network, algorithm, attribute));
		}
	}

	@Override
	public boolean isReady(CyNetwork network) {
		return network != null;
	}
}
