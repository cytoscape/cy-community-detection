package org.cytoscape.app.communitydetection.subnetwork;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.IconJLabelDialogFactory;
import org.cytoscape.model.CyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
@SuppressWarnings("serial")
public class ParentNetworkChooserDialog extends JPanel{
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ParentNetworkChooserDialog.class);
	private boolean _guiLoaded;
	private IconJLabelDialogFactory _iconFactory;
	private JCheckBox _rememberCheckBox;
	private Map<String, CyNetwork> _networkMap;
	private JComboBox _comboBox;
	private final static String REMEMBER_TEXT = "Remember selection";
	private final static String REMEMBER_TOOLTIP = "If set, remembers selected parent network";
	private final static String REMEMBER_MESSAGE = "If set, updates hierarchy network attribute <b>" 
			+ AppUtils.COLUMN_CD_ORIGINAL_NETWORK + "</b><br/>with selected value.";
	
	private final static String CHOOSER_TEXT = "Choose parent network";
	
	private final static String CHOOSER_TOOLTIP = "Click here for information about " + CHOOSER_TEXT;
	
	private final static String CHOOSER_MESSAGE = "Please select the parent network for this hierarchy.<br/><br/>"
			+ "The network attribute: <b>"
			+ AppUtils.COLUMN_CD_ORIGINAL_NETWORK + "</b> "
			+ "contains a transient unique id (SUID)<br/>"
			+ " of the parent network that no longer matches any networks "
			+ "currently loaded in Cytoscape.<br/><br/>"
			+ "This can happen for a number of reasons such "
			+ "as the parent network was removed or this session<br/>"
			+ "was saved and reloaded.";
	
	public ParentNetworkChooserDialog(IconJLabelDialogFactory iconFactory){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		_iconFactory = iconFactory;
		_guiLoaded = false;
	}

	/**
	 * Creates the GUI
	 * @return true upon success otherwise false
	 */
	public boolean createGUI(List<CyNetwork> networks) {
	    if (_guiLoaded == false){
			this.add(getChooserPanel());
			_networkMap = new HashMap<>();
			_guiLoaded = true;
	    }
		_comboBox.removeAllItems();
		_networkMap.clear();
		for (CyNetwork curNet: networks){
			String netLabel = getNetworkName(curNet);
			_networkMap.put(netLabel, curNet);
			_comboBox.addItem(netLabel);
		}
		return true;
	}
	
	/**
	 * Gets currently selected item from dropdown box
	 * @return currently selected item or {@code null} if dropdown box is {@code null}
	 */
	public CyNetwork getSelection(){
		if (_comboBox == null){
			return null;
		}
		if (_networkMap == null){
			return null;
		}
		return _networkMap.get((String)_comboBox.getSelectedItem());
	}
	
	/**
	 * Gets value of "Remember selection" checkbox
	 * @return value of checkbox or {@code false} if checkbox is null
	 */
	public boolean rememberChoice(){
		if (_rememberCheckBox == null){
			return false;
		}
		return _rememberCheckBox.isSelected();
	}
	
	/**
	 * Gets String with name of network and number of nodes and edges
	 * @param network
	 * @return String in format: NETWORK NAME (# nodes, # edges)
	 */
	private String getNetworkName(CyNetwork network){
		String netName = network.getRow(network).get(CyNetwork.NAME, String.class);
		return netName + "(" + Integer.toString(network.getNodeCount()) + " nodes, "
				       + Integer.toString(network.getEdgeCount()) + " edges)";
	}
	
	/**
	 * Creates Panel with dropdown list of possible parent networks
	 * and a remember selection checkbox
	 * @return 
	 */
	private JPanel getChooserPanel(){
	    
	    JPanel chooserPanel = new JPanel();
		chooserPanel.setLayout(new GridBagLayout());
		chooserPanel.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createTitledBorder("Parent network Chooser"),
			    BorderFactory.createEmptyBorder(5,5,5,5)));
		
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridy = 0;
		labelConstraints.gridx = 0;
		labelConstraints.anchor = GridBagConstraints.LINE_START;
		labelConstraints.fill = GridBagConstraints.NONE;
		labelConstraints.insets = new Insets(0, 5, 5, 0);
		chooserPanel.add(new JLabel(CHOOSER_TEXT + ": "), labelConstraints);
		
		GridBagConstraints comboConstraints = new GridBagConstraints();
		comboConstraints.gridy = 0;
		comboConstraints.gridx = 1;
		comboConstraints.insets = new Insets(0, 0, 5, 0);

		_comboBox = new JComboBox();

		chooserPanel.add(_comboBox, comboConstraints);

		GridBagConstraints chooserIconConstraints = new GridBagConstraints();
		chooserIconConstraints.gridy = 0;
		chooserIconConstraints.gridx = 2;
		chooserIconConstraints.anchor = GridBagConstraints.LINE_END;
		chooserIconConstraints.insets = new Insets(0, 0, 5, 0);
		chooserPanel.add(getChooserInfoIcon(), chooserIconConstraints);
		
		GridBagConstraints rememberConstraints = new GridBagConstraints();
		rememberConstraints.gridy = 1;
		rememberConstraints.gridx = 1;
		rememberConstraints.anchor = GridBagConstraints.LINE_END;
		rememberConstraints.insets = new Insets(0, 5, 0, 0);
		_rememberCheckBox = new JCheckBox(REMEMBER_TEXT);
		_rememberCheckBox.setSelected(true);
		_rememberCheckBox.setToolTipText(REMEMBER_TOOLTIP);
		chooserPanel.add(_rememberCheckBox, rememberConstraints);
		
		GridBagConstraints rememberIcon = new GridBagConstraints();
		rememberIcon.gridy = 1;
		rememberIcon.gridx = 2;
		rememberIcon.anchor = GridBagConstraints.LINE_END;
		rememberIcon.insets = new Insets(0, 5, 5, 0);
		chooserPanel.add(getRememberInfoIcon(), rememberIcon);

		return chooserPanel;
	}

	/**
	 * Gets Chooser dropdown info icon
	 * @return 
	 */
	private JLabel getChooserInfoIcon(){
		JLabel iconLabel = _iconFactory.getJLabelIcon(this,"info_icon", "png",
				ParentNetworkChooserDialog.CHOOSER_TEXT + " chooser", 
				CHOOSER_MESSAGE, 20, 40);
		iconLabel.setName("chooserIcon");
		iconLabel.setToolTipText(CHOOSER_TOOLTIP);
		return iconLabel;
	}
	
	/**
	 * Gets Chooser dropdown info icon
	 * @return 
	 */
	private JLabel getRememberInfoIcon(){
		JLabel iconLabel = _iconFactory.getJLabelIcon(this,"info_icon", "png",
				ParentNetworkChooserDialog.REMEMBER_TEXT + " checkbox", 
				REMEMBER_MESSAGE, 20, 40);
		iconLabel.setName("rememberIcon");
		iconLabel.setToolTipText("Click here for more information about " +
				ParentNetworkChooserDialog.REMEMBER_TEXT);
		return iconLabel;
	}
}
