/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.app.communitydetection.util;

import org.cytoscape.app.communitydetection.util.JEditorPaneFactory;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author churas
 */
public class JEditorPaneFactoryImpl implements JEditorPaneFactory {
    
    @Override
    public JEditorPane getDescriptionFrame(String description) {
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
	int width = editorPane.getPreferredSize().width < 600 ? 600 : editorPane.getPreferredSize().width;	
	editorPane.setPreferredSize(new Dimension(width, editorPane.getPreferredSize().height));
	editorPane.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
	return editorPane;
    }
}
