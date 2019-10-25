package org.cytoscape.app.communitydetection.termmap;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

/**
 * {@link NetworkTaskFactory} implementation to create {@link TermMappingTask}
 * for Menu Bar.
 *
 */
public class NetworkTermMappingTaskFactoryImpl implements NetworkTaskFactory {

	private final CommunityDetectionAlgorithm algorithm;
	private final String type;

	public NetworkTermMappingTaskFactoryImpl(CommunityDetectionAlgorithm algorithm, String type) {
		this.algorithm = algorithm;
		this.type = type;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		if (type.equals(AppUtils.TYPE_ABOUT)) {
			JOptionPane.showMessageDialog(null, getDescriptionTextPane(algorithm.getDescription()),
					"About " + algorithm.getDisplayName(), JOptionPane.INFORMATION_MESSAGE);
			return new TaskIterator(new TermMappingTask(algorithm.getName(), AppUtils.TYPE_ABOUT, network, false));
		}
		return new TaskIterator(new TermMappingTask(algorithm.getName(), AppUtils.TYPE_NONE, network, false));
	}

	@Override
	public boolean isReady(CyNetwork network) {
		if (type.equals(AppUtils.TYPE_ABOUT)) {
			return true;
		}
		if (network != null && network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) == null) {
			return false;
		}
		return network != null;
	}

	private JTextPane getDescriptionTextPane(String description) {
		JTextPane textPane = new JTextPane();
		Document doc = textPane.getDocument();
		try {
			doc.insertString(0, description, new SimpleAttributeSet());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		textPane.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
		int width = textPane.getPreferredSize().width > 600 ? 600 : textPane.getPreferredSize().width;
		textPane.setSize(new Dimension(width, 10));
		textPane.setPreferredSize(new Dimension(width, textPane.getPreferredSize().height));
		return textPane;
	}

}
