package org.cytoscape.app.communitydetection.util;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author churas
 */
public class IconJLabelDialogFactory {
	
	private ShowDialogUtil _dialogUtil;
	private ImageIconHolderFactory _iconFactory;
	private JEditorPaneFactory _editorPaneFactory;
	private ImageIconHolder _iconHolder;
	
	public IconJLabelDialogFactory(ShowDialogUtil dialogUtil, ImageIconHolderFactory iconFactory,
			JEditorPaneFactory editorPaneFactory){
		_dialogUtil = dialogUtil;
		_iconFactory = iconFactory;
		_editorPaneFactory = editorPaneFactory;
	}
	
	public JLabel getJLabelIcon(Component parent, final String iconImagePrefix, final String iconImageSuffix,
			final String title, final String dialogMessage, int smallSize, int largeSize){
		_iconHolder = this._iconFactory.getImageIconHolder(iconImagePrefix,
				iconImageSuffix, smallSize, largeSize);
		JLabel restUrlLabel = new JLabel(_iconHolder.getSmallIcon(), JLabel.CENTER);

		restUrlLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				showInputInfoDialog(parent, title, dialogMessage);
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		restUrlLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				showInputInfoDialog(parent, title, dialogMessage);
			}
		});

		return restUrlLabel;
	}
	
	/**
	 * Displays info dialog describing REST URL setting
	 * @param message 
	 */
	private void showInputInfoDialog(final Component parent,
			final String title, final String message){
		_dialogUtil.showMessageDialog(parent,
				_editorPaneFactory.getDescriptionFrame(message),
					title,
					JOptionPane.INFORMATION_MESSAGE,
					_iconHolder.getLargeIcon());
	}
}
