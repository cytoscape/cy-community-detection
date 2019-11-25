package org.cytoscape.app.communitydetection.termmap;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
			JOptionPane.showMessageDialog(null, getDescriptionFrame(algorithm.getDescription()),
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
