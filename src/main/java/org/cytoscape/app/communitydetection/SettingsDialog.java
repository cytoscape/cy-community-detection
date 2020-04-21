package org.cytoscape.app.communitydetection;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.cytoscape.app.communitydetection.util.JEditorPaneFactory;
import org.cytoscape.app.communitydetection.util.ImageIconHolder;
import org.cytoscape.app.communitydetection.util.ImageIconHolderFactory;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates Settings dialog that lets user update 
 * {@link org.cytoscape.app.communitydetection.util.AppUtils#PROP_APP_BASEURL}
 * property which sets the REST server endpoint
 * @author churas
 */
@SuppressWarnings("serial")
public class SettingsDialog extends JPanel {

	private final static Logger LOGGER = LoggerFactory.getLogger(SettingsDialog.class);
	private boolean _guiLoaded;
	private ShowDialogUtil _dialogUtil;
	private ImageIconHolderFactory _iconFactory;
	private JEditorPaneFactory _editorPaneFactory;
	private JLabel _restUrlIcon;
	private JTextField _restUrlTextField;
	private ImageIconHolder _iconHolder;
	private PropertiesHelper _pHelper;
	private final static String REST_URL_LABEL_TEXT = "REST Server (app.baseurl)";
	
	private final static String REST_URL_MESSAGE_TOOLTIP = "Click here for information about " + REST_URL_LABEL_TEXT + " setting";
	
	private final static String REST_URL_MESSAGE = "Updates host name of CDAPS REST server. (default <a href=\"http://cdservice.cytoscape.org/cd/\">cdservice.cytoscape.org)</a><br/><br/>"
			+ "The CDAPS REST server is the server that actually runs CDAPS Community Detection <br/>"
			+ "and Functional Enrichment algorithms. "
			+ "(default <a href=\"http://cdservice.cytoscape.org/cd/\">cdservice.cytoscape.org)</a><br/><br/>"
			+ "<a href=\"https://cdaps.readthedocs.io\">Click here for information about setting up a custom CDAPS REST server</a><br/><br/>"
			+ "Advanced users can directly update this setting by navigating using Cytoscape menus via<br/><br/>"
			+ "&nbsp;&nbsp;Edit -> Preferences -> Properies -> CyCommunityDetection -> <i>app.baseurl<i/>";
			
	/**
	 * Width of base/REST url text field to use
	 */
	private static final int TEXT_FIELD_WIDTH = 200;
	
	/**
	 * Constructor 
	 * @param dialogUtil Used to display dialogs to user
	 * @param editorPaneFactory Factory to get JEditorPane to display html text to user
	 * @param iconFactory Factory to get icons needed by this dialog
	 * @param pHelper Instance of PropertiesHelper
	 */
	public SettingsDialog(ShowDialogUtil dialogUtil, JEditorPaneFactory editorPaneFactory,
			ImageIconHolderFactory iconFactory, PropertiesHelper pHelper){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this._dialogUtil = dialogUtil;
		this._editorPaneFactory = editorPaneFactory;
		this._iconFactory = iconFactory;
		_guiLoaded = false;
		_pHelper = pHelper;
	}

	/**
	 * Creates the GUI
	 * @return true upon success otherwise false
	 */
	public boolean createGUI() {
	    if (_guiLoaded == false){
			this.add(getSettingsPanel());
			_guiLoaded = true;
	    }
		// update REST URL settings field...
		_restUrlTextField.setText(_pHelper.getBaseurlHostNameOnly());
		return true;
	}

	/**
	 * Creates settings panel
	 * @return 
	 */
	private JPanel getSettingsPanel(){
	    
	    JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new GridBagLayout());
		settingsPanel.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createTitledBorder("Settings"),
			    BorderFactory.createEmptyBorder(5,5,5,5)));
		
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridy = 0;
		labelConstraints.gridx = 0;
		labelConstraints.anchor = GridBagConstraints.LINE_START;
		labelConstraints.fill = GridBagConstraints.NONE;
		labelConstraints.insets = new Insets(0, 5, 5, 0);
		settingsPanel.add(new JLabel(REST_URL_LABEL_TEXT + ": "), labelConstraints);
		
		GridBagConstraints restUrlConstraints = new GridBagConstraints();
		restUrlConstraints.gridy = 0;
		restUrlConstraints.gridx = 1;
		restUrlConstraints.insets = new Insets(0, 0, 5, 0);

		_restUrlTextField = new JTextField();

	    _restUrlTextField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, _restUrlTextField.getPreferredSize().height));
	    _restUrlTextField.setName("baseurl");
		settingsPanel.add(_restUrlTextField, restUrlConstraints);

		GridBagConstraints restUrlIconConstraints = new GridBagConstraints();
		restUrlIconConstraints.gridy = 0;
		restUrlIconConstraints.gridx = 2;
		restUrlIconConstraints.anchor = GridBagConstraints.LINE_END;
		restUrlIconConstraints.insets = new Insets(0, 0, 5, 0);
		_restUrlIcon = getRestUrlIcon();
		settingsPanel.add(_restUrlIcon, restUrlIconConstraints);
		
		return settingsPanel;
	}
	
	/**
	 * Gets the REST url set by user.
	 * @return 
	 */
	public String getBaseurl(){
		if (_restUrlTextField == null){
			LOGGER.warn("Attempt to retreive value of REST Server "
					+ "aka Base url before initialization");
			return null;
		}
		LOGGER.info(_restUrlTextField.getText());
		return _restUrlTextField.getText();
	}

	/**
	 * Creates a {@link javax.swing.JLabel} with an info icon that when clicked
	 * displays a small dialog that displays information about the parameter
	 * passed in
	 * @param parameter The parameter
	 * @return 
	 * @throws IOException 
	 */
	private JLabel getRestUrlIcon() {
		_iconHolder = this._iconFactory.getImageIconHolder("info_icon","png", 20, 40);
		JLabel restUrlLabel = new JLabel(_iconHolder.getSmallIcon(), JLabel.CENTER);
		restUrlLabel.setName("restURLIcon");
		restUrlLabel.setToolTipText(REST_URL_MESSAGE_TOOLTIP); 

		restUrlLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				showInputInfoDialog(REST_URL_MESSAGE);
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
				showInputInfoDialog(REST_URL_MESSAGE);
			}
		});

		return restUrlLabel;
	}
	
	/**
	 * Displays info dialog describing REST URL setting
	 * @param message 
	 */
	private void showInputInfoDialog(final String message){
		_dialogUtil.showMessageDialog(getParent(), _editorPaneFactory.getDescriptionFrame(message),
					SettingsDialog.REST_URL_LABEL_TEXT + " setting",
					JOptionPane.INFORMATION_MESSAGE,
					_iconHolder.getLargeIcon());
	}
}
