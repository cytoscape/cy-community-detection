package org.cytoscape.app.communitydetection.hierarchy;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Task factory impl to create {@link HierarchyTask}.
 *
 */
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
			String[] columnNames = getNumericColumns(network.getDefaultEdgeTable());
			if (columnNames.length > 0) {
				String attribute = (String) JOptionPane.showInputDialog(null, "Select...", "Select an Edge Attribute",
						JOptionPane.PLAIN_MESSAGE, null, columnNames, columnNames[0]);
				return new TaskIterator(new HierarchyTask(network, algorithm, attribute));
			} else {
				JOptionPane.showMessageDialog(null,
						"There are no edge attributes for this network. Continuing with the default option.");
				return new TaskIterator(new HierarchyTask(network, algorithm, AppUtils.TYPE_NONE));
			}
		}
	}

	@Override
	public boolean isReady(CyNetwork network) {
		if (network != null) {
			return true;
		}
		return false;
	}

	private String[] getNumericColumns(CyTable table) {
		Set<String> columnNames = new HashSet<String>();
		for (CyColumn column : table.getColumns()) {
			if (Number.class.isAssignableFrom(column.getType())) {
				columnNames.add(column.getName());
			}
		}
		if (columnNames.contains(CyNetwork.SUID)) {
			columnNames.remove(CyNetwork.SUID);
		}
		return columnNames.toArray(new String[0]);
	}
}
