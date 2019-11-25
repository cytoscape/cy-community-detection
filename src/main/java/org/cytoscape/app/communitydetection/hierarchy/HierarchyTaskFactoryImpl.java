package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

/**
 * Task factory impl to create {@link HierarchyTask}.
 *
 */
public class HierarchyTaskFactoryImpl implements NetworkTaskFactory {

	private final CommunityDetectionAlgorithm algorithm;
	private final String type;

	public HierarchyTaskFactoryImpl(CommunityDetectionAlgorithm algorithm, String type) {
		this.algorithm = algorithm;
		this.type = type;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		if (type.equals(AppUtils.TYPE_NONE)) {
			return new TaskIterator(new HierarchyTask(network, algorithm, AppUtils.TYPE_NONE));
		} else if (type.equals(AppUtils.TYPE_WEIGHTED)) {
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
		} else {
			JOptionPane.showMessageDialog(null, getDescriptionFrame(algorithm.getDescription()),
					"About " + algorithm.getDisplayName(), JOptionPane.INFORMATION_MESSAGE);
			return new TaskIterator(new HierarchyTask(network, algorithm, AppUtils.TYPE_ABOUT));
		}
	}

	@Override
	public boolean isReady(CyNetwork network) {
		if (type.equals(AppUtils.TYPE_ABOUT)) {
			return true;
		}
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

	private JEditorPane getDescriptionFrame(String description) {
		JEditorPane editorPane = new JEditorPane("text/html", description);
		editorPane.setEditable(false);
		editorPane.setOpaque(false);

		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent linkEvent) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(linkEvent.getEventType())) {
					try {
						Desktop.getDesktop().browse(linkEvent.getURL().toURI());
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null,
								"Default browser window could not be opened. Please copy/paste this link to your browser: "
										+ linkEvent.getURL());
					}
					System.out.println(linkEvent.getURL());
				}
			}
		});
		int width = editorPane.getPreferredSize().width > 600 ? 600 : editorPane.getPreferredSize().width;
		editorPane.setSize(new Dimension(width, 10));
		editorPane.setPreferredSize(new Dimension(width, editorPane.getPreferredSize().height));
		editorPane.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
		return editorPane;
	}
}
