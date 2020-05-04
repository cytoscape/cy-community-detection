package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import org.cytoscape.app.communitydetection.event.BaseurlUpdatedEvent;
import org.cytoscape.app.communitydetection.event.BaseurlUpdatedListener;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CustomParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class LauncherDialog extends JPanel implements ItemListener, BaseurlUpdatedListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(LauncherDialog.class);
	private static final String WEIGHT_ALERT_MSG = "Alert: The weight column is an advanced\n"
			+ "option requiring direct knowledge\n"
			+ "of algorithm being used.";
	private static final String SELECTED_NODES_BUTTON_LABEL = "Selected Nodes";
	public static final String INPUTDELIM = ":::";
	private List<CommunityDetectionAlgorithm> _algorithmList;
	private static final float ALGO_FONTSIZE_BOOST = 4.0f;
	private static final float EXPERIMENTAL_FONT_BOOST = 4.0f;
	private static final double TEXT_FIELD_WIDTH = 160.0;

	private JPanel _cards;
	private AboutAlgorithmEditorPaneFactoryImpl _aboutAlgoFac;
	private CustomParameterHelpJEditorPaneFactoryImpl  _customParamHelpFac;
	private ImageIcon _infoIconSmall;
	private ImageIcon _infoIconLarge;
	private ImageIcon _alertIconSmall;
	private ImageIcon _alertIconLarge;
	private JLabel _alertIconLabel;
	private Map<String, JPanel> _algoCardMap;
	private JComboBox _algorithmComboBox;
	private JComboBox _weightComboBox;
	private JRadioButton _selectedButton;
	private JRadioButton _allButton;
	private boolean _guiLoaded = false;
	private String _algorithmType;
	private ShowDialogUtil _dialogUtil;
	private String _currentlySelectedAlgorithm;
	private LauncherDialogAlgorithmFactory _algoFac;
	private AtomicBoolean _algorithmEndPointUpdated = new AtomicBoolean();

	public LauncherDialog(AboutAlgorithmEditorPaneFactoryImpl aboutAlgoFac,
			CustomParameterHelpJEditorPaneFactoryImpl customParamHelpFac,
			LauncherDialogAlgorithmFactory algoFac,
			ShowDialogUtil dialogUtil,
		final String algorithmType) throws Exception {
		this._customParamHelpFac = customParamHelpFac;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this._algorithmType = algorithmType;
		this._dialogUtil = dialogUtil;
		_aboutAlgoFac = aboutAlgoFac;
		_algoFac = algoFac;
		_currentlySelectedAlgorithm = null;
	}

	public boolean createGUI(Component parentWindow) {
	    if (_guiLoaded == true){
			return refreshAlgorithmsIfEndPointChanged(parentWindow);
	    }
	    loadImageIcon();
		loadAlertIcon();
		_algoCardMap = new LinkedHashMap<>();
		_algorithmList = _algoFac.getAlgorithms(parentWindow, _algorithmType, false);
		if (_algorithmList == null){
			return false;
		}
		_cards = new JPanel(new CardLayout());
		_algorithmComboBox = new JComboBox();
		
		// grab the font for the combo box
		// and derive a new font that is larger and is bold and update
		// the combo box to use this font
		Font comboFont = _algorithmComboBox.getFont();
		
		_algorithmComboBox.setFont(comboFont.deriveFont(comboFont.getStyle() | Font.BOLD,
			comboFont.getSize2D() + ALGO_FONTSIZE_BOOST));
		_algorithmComboBox.setEditable(false);
		_algorithmComboBox.setToolTipText("Algorithm to run");
		_algorithmComboBox.addItemListener(this);

		loadAlgorithmCards();
		
		JPanel contentPane = new JPanel();
		JLabel aLabel = new JLabel("Algorithm: ");
		// grab the font for the label
		// and derive a new font that is larger and is bold and update
		// the label to use this font
		Font aLabelFont = aLabel.getFont();
		Font newFont = aLabelFont.deriveFont(aLabelFont.getStyle() | Font.BOLD,
			aLabelFont.getSize2D() + ALGO_FONTSIZE_BOOST);
		aLabel.setFont(newFont);
		contentPane.add(aLabel);
		
		contentPane.add(_algorithmComboBox);
		
		contentPane.add(this.getAboutAlgorithmIcon());
		
		JPanel masterPanel = new JPanel();
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		masterPanel.add(_cards);

		add(contentPane, BorderLayout.PAGE_START);
		// only add node selection panel if its not null
		JPanel nodePanel = this.getNodeSelectionPanel();
		if (nodePanel != null){
		    add(nodePanel, BorderLayout.CENTER);
		}
		
		// only add weight panel if its not null
		JPanel weightPanel = this.getWeightPanel();
		if (weightPanel != null){
		    add(weightPanel, BorderLayout.CENTER);
		}
		
		add(masterPanel, BorderLayout.CENTER);
		
		_guiLoaded = true;
		updateWeightColumnCombo(null);
		return true;
	}
	
	@Override
	public void urlUpdatedEvent(BaseurlUpdatedEvent event) {
		_algorithmEndPointUpdated.set(true);
	}
	
	private boolean refreshAlgorithmsIfEndPointChanged(Component parentWindow){
		if (_algorithmEndPointUpdated.get() == true){
			LOGGER.debug("CD Service rest endpoint changed. Updating algorithms");
			_algorithmList = _algoFac.getAlgorithms(parentWindow, _algorithmType, false);
			if (_algorithmList == null){
				return false;
			}
			this.loadAlgorithmCards();
			_algorithmEndPointUpdated.set(false);
		}
		return true;
	}
	
	private JPanel getNodeSelectionPanel(){
	    if (!_algorithmType.equals(AppUtils.TM_ALGORITHM_INPUT_TYPE)){
		return null;
	    }
	    JPanel nodePanel = new JPanel();
		nodePanel.setLayout(new GridLayout(2, 1));
		nodePanel.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createTitledBorder("Node Selection"),
			    BorderFactory.createEmptyBorder(5,5,5,5)));
	    _allButton = new JRadioButton("All Nodes", true);
	    
	    _allButton.setToolTipText("Perform Term Mapping on all nodes in "
		    + "network");
	    _selectedButton = new JRadioButton(SELECTED_NODES_BUTTON_LABEL);
	    _selectedButton.setToolTipText("Perform Term Mapping on selected "
		    + "nodes in network");
	    
	    // button group is needed cause only one of
	    // the buttons should be set
            ButtonGroup bgroup = new ButtonGroup();
            bgroup.add(_allButton);
            bgroup.add(_selectedButton);
                    
            nodePanel.add(_allButton);
            nodePanel.add(_selectedButton);
	    return nodePanel;
	}

	/**
	 * Creates column weight panel if algorithm type is community detection
	 * otherwise return null cause the dialog is for functional enrichment
	 * and the weight column is not needed.
	 * @return 
	 */
	private JPanel getWeightPanel(){
	    if (!_algorithmType.equals(AppUtils.CD_ALGORITHM_INPUT_TYPE)){
		return null;
	    }
	    JPanel weightPanel = new JPanel();
		weightPanel.setLayout(new GridBagLayout());
		weightPanel.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createTitledBorder("Weight Column"),
			    BorderFactory.createEmptyBorder(5,5,5,5)));
		
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridy = 0;
		labelConstraints.gridx = 0;
		labelConstraints.anchor = GridBagConstraints.LINE_START;
		labelConstraints.insets = new Insets(0, 5, 5, 0);
		weightPanel.add(new JLabel("Weight Column: "), labelConstraints);

		_weightComboBox = getWeightColumnComboBox();
		
		GridBagConstraints weightComboConstraints = new GridBagConstraints();
		weightComboConstraints.gridy = 0;
		weightComboConstraints.gridx = 1;
		weightComboConstraints.insets = new Insets(0, 0, 5, 0);
		weightPanel.add(_weightComboBox, weightComboConstraints);

		GridBagConstraints weightAlertConstraints = new GridBagConstraints();
		weightAlertConstraints.gridy = 0;
		weightAlertConstraints.gridx = 2;
		weightAlertConstraints.anchor = GridBagConstraints.LINE_END;
		weightAlertConstraints.insets = new Insets(0, 0, 5, 0);
		_alertIconLabel = getWeightColumnAlertIcon();
		weightPanel.add(_alertIconLabel, weightAlertConstraints);
		return weightPanel;
	}
	
	/**
	 * Gets weight column selection combo box that has an
	 * attached listener which makes an alert icon visible
	 * when a user selects a column for weight
	 * @return 
	 */
	private JComboBox getWeightColumnComboBox(){
		JComboBox comboBox = new JComboBox();
		comboBox.setEditable(false);
		comboBox.setToolTipText("Advanced: Numeric edge column to use for "
			+ "edge weights in Community Detection. Select '" +
			AppUtils.TYPE_NONE_VALUE + "' to"
			+ " not use a column");
		comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String selected = getWeightColumn();
                if (selected == null || selected.equals(AppUtils.TYPE_NONE_VALUE)){
					_alertIconLabel.setVisible(false);
				}
				else {
					_alertIconLabel.setVisible(true);
				}
            }
        });
		return comboBox;
	}

	/**
	 * Updates the radio buttons related to node selection based
	 * on network passed in. If there are selected nodes in the
	 * network then the selected button is enabled and selected.
	 * In addition, the number of nodes
	 * selected is displayed in radio button text
	 * @param network 
	 */
	public void updateNodeSelectionButtons(CyNetwork network){
	    if (_allButton == null || _selectedButton == null){
		return;
	    }
	    _allButton.setSelected(true);
	    if (network == null){
		_selectedButton.setEnabled(false);
		_selectedButton.setText(SELECTED_NODES_BUTTON_LABEL);
		return;
	    }
	    List<CyNode> nodes = CyTableUtil.getSelectedNodes(network);
	    if (nodes == null || nodes.isEmpty()){
		
		_selectedButton.setEnabled(false);
		_selectedButton.setText(SELECTED_NODES_BUTTON_LABEL);
		return;
	    }
	    _selectedButton.setSelected(true);
	    _selectedButton.setEnabled(true);
	    _selectedButton.setText(Integer.toString(nodes.size()) + " " +
		    SELECTED_NODES_BUTTON_LABEL);
	}
	
	/**
	 * Returns true if user wishes to run on selected nodes, false otherwise
	 * @return 
	 */
	public boolean runOnSelectedNodes(){
	    if (_selectedButton == null){
		return false;
	    }
	    return _selectedButton.isSelected();
	}
	
	/**
	 * Updates the weight column Combo box with columns passed in
	 * plus a default value denoting to not to use any column
	 * @param columns new weight columns to use
	 */
	public void updateWeightColumnCombo(Set<String> columns){
	    if (_weightComboBox == null){
		return;
	    }
	    _weightComboBox.removeAllItems();
	    
	    _weightComboBox.addItem(AppUtils.TYPE_NONE_VALUE);
	    if (columns == null){
		return;
	    }
	    for (String cName : columns){
		_weightComboBox.addItem(cName);
	    }
	}
	
	/**
	 * Gets the value of the weight column dropdown box
	 * @return 
	 */
	public String getWeightColumn(){
	    if (_weightComboBox == null){
		return null;
	    }
	    return (String)_weightComboBox.getSelectedItem();
	}
	
	private void loadAlgorithmCards(){
		_currentlySelectedAlgorithm = null;
	    _algoCardMap.clear();
	    _cards.removeAll();
	    _algorithmComboBox.removeAllItems();
	    int rowIndex = 1;
	    for (CommunityDetectionAlgorithm cda : _algorithmList){
		Map<String, CustomParameter> pMap = cda.getCustomParameterMap();
		
		JPanel algoCard = new JPanel();
		algoCard.setName(cda.getName());
		_algoCardMap.put(cda.getName(), algoCard);
		algoCard.setLayout(new GridBagLayout());
		algoCard.setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createTitledBorder("Parameters"),
			    BorderFactory.createEmptyBorder(5,5,5,5)));
		_algorithmComboBox.addItem(cda.getDisplayName());
		if (pMap != null){
			for (String key : pMap.keySet()){
				CustomParameter cp = pMap.get(key);
				JLabel paramLabel = new JLabel(cp.getDisplayName() + ":");

				GridBagConstraints labelConstraints = new GridBagConstraints();
				labelConstraints.gridy = rowIndex;
				labelConstraints.gridx = 0;
				labelConstraints.weightx = 1.0;
				labelConstraints.anchor = GridBagConstraints.NORTHEAST;
				labelConstraints.insets = new Insets(5, 5, 0, 5);
				algoCard.add(paramLabel, labelConstraints);

				JComponent inputComponent = getCustomParameterInput(cda.getName(), cp);
				if (cp.getDescription() != null){
				inputComponent.setToolTipText(cp.getDescription());
				paramLabel.setToolTipText(cp.getDescription());
				}

				GridBagConstraints inputConstraints = new GridBagConstraints();
				inputConstraints.gridy = rowIndex;
				inputConstraints.gridx = 1;
				inputConstraints.gridwidth = 1;
				inputConstraints.weightx = 0.;
				inputConstraints.anchor = GridBagConstraints.LINE_START;
				inputConstraints.insets = new Insets(0, 0, 5, 0);
				inputConstraints.fill = GridBagConstraints.HORIZONTAL;

				algoCard.add(inputComponent, inputConstraints);

				GridBagConstraints infoConstraints = new GridBagConstraints();
				infoConstraints.gridy = rowIndex;
				infoConstraints.gridx = 2;
				infoConstraints.anchor = GridBagConstraints.NORTH;
				infoConstraints.insets = new Insets(5, 5, 5, 0);
				algoCard.add(getParameterInfoIcon(cp), infoConstraints);

				rowIndex++;
			}
			GridBagConstraints resetConstraints = new GridBagConstraints();
			resetConstraints.gridy = 0;
			resetConstraints.gridx = 0;
			resetConstraints.insets = new Insets(0, 5, 0, 0);
			resetConstraints.anchor = GridBagConstraints.LINE_START;
			algoCard.add(this.getResetButton(cda.getName()), resetConstraints);
		}
		_cards.add(algoCard, cda.getDisplayName());
		this.resetAlgorithmToDefaults(cda.getName());
	    }
	}
	
	/**
	 * Creates a gui component that represents an input field for the parameter passed in
	 * @param algorithm the name of the algorithm
	 * @param request the request
	 * @param parameter
	 * @return 
	 */
	private JComponent getCustomParameterInput(final String algorithm,
		final CustomParameter parameter){
		String componentName = algorithm + INPUTDELIM + parameter.getName();
	    if (parameter.getType() != null && parameter.getType().equalsIgnoreCase("flag")){
		    JCheckBox checkBox = new JCheckBox();
		    checkBox.setName(componentName);
		    checkBox.setBorder(BorderFactory.createEmptyBorder(5, 0, 0,0));
		    return checkBox;
	    }
	    JTextField textField = null;
	    if (parameter.getDefaultValue() != null){
			textField = new JTextField(parameter.getDefaultValue());
	    } 
	    else {
			textField = new JTextField();
	    }
	    
	    textField.setName(componentName);
	    JScrollPane textScrollPane = new JScrollPane(textField, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		// set the name so we can find this component later even though
		// we need the text field within.
		textScrollPane.setName(componentName);
	    Dimension prefSize = textScrollPane.getPreferredSize();
	    prefSize.setSize(TEXT_FIELD_WIDTH, prefSize.getHeight()*1.1);
	    textScrollPane.setPreferredSize(prefSize);
	    return textScrollPane;
	}
	
	/**
	 * Loads a small and large info image icon
	 */
	private void loadImageIcon(){
	    try {
		    File imgFile = File.createTempFile("info_icon", "png");
	        InputStream imgStream = getClass().getClassLoader().getResourceAsStream("info_icon.png");
			Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			_infoIconSmall = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
			_infoIconLarge = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
	    }
	    catch (IOException ex){
		    
	    }
	}
	
	/**
	 * Loads a small alert image icon
	 */
	private void loadAlertIcon(){
	    try {
		    File imgFile = File.createTempFile("alert_icon", "png");
	        InputStream imgStream = getClass().getClassLoader().getResourceAsStream("alert_icon.png");
			Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			_alertIconSmall = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
			_alertIconLarge = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
		}
	    catch (IOException ex){
		    
	    }
	}
	

	/**
	 * Event invoked when user interacts with algorithm combo box
	 * to select a different algorithm. When that happens this code
	 * switches the parameter panel/card to correspond to the 
	 * newly selected algorithm
	 * @param evt 
	 */
	@Override
	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout)(_cards.getLayout());
		_currentlySelectedAlgorithm = (String)evt.getItem();
		cl.show(_cards, _currentlySelectedAlgorithm);
	}
	
	/**
	 * Creates a {@link javax.swing.JLabel} with an info icon that when clicked
	 * displays a small dialog that displays information about the parameter
	 * passed in
	 * @param parameter The parameter
	 * @return 
	 * @throws IOException 
	 */
	private JLabel getParameterInfoIcon(final CustomParameter parameter) {
		JLabel paramLabel = new JLabel(_infoIconSmall, JLabel.CENTER);
		paramLabel.setToolTipText("Click here for more information about '" +
			parameter.getDisplayName() + "' parameter");

		paramLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				showCustomHelpParameterDialog(parameter);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		paramLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				showCustomHelpParameterDialog(parameter);
			}
		});

		return paramLabel;
	}
	
	private void showCustomHelpParameterDialog(final CustomParameter parameter){
		_dialogUtil.showMessageDialog(getParent(),  _customParamHelpFac.getCustomParameterHelp(parameter),
					"Parameter " + parameter.getDisplayName(), JOptionPane.INFORMATION_MESSAGE,
					_infoIconLarge);
	}
	
	/**
	 * Creates a {@link javax.swing.JLabel} with an info icon that when clicked
	 * displays a small dialog that displays information about the parameter
	 * passed in
	 * @param parameter The parameter
	 * @return 
	 * @throws IOException 
	 */
	private JLabel getWeightColumnAlertIcon() {
		JLabel alertLabel = new JLabel(_alertIconSmall, JLabel.CENTER);
		alertLabel.setToolTipText(WEIGHT_ALERT_MSG); 

		alertLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				showWeightColumnAlertDialog(WEIGHT_ALERT_MSG);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		alertLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				showWeightColumnAlertDialog(WEIGHT_ALERT_MSG);
			}
		});

		return alertLabel;
	}
	
	private void showWeightColumnAlertDialog(final String message){
		_dialogUtil.showMessageDialog(getParent(), message,
					"Weight Column Alert", JOptionPane.INFORMATION_MESSAGE,
					_alertIconLarge);
	}

	
	/**
	 * Given an internal algorithm name 'algorithm' this method 
	 * sets the UI panel input fields for this algorithm to their defaults
	 * @param algorithm 
	 */
	private void resetAlgorithmToDefaults(final String algorithm){
	    LOGGER.debug("Resetting " + algorithm + " to defaults");
	    if (_algoCardMap.containsKey(algorithm) == false){
		return;
	    }
	    CommunityDetectionAlgorithm cda = getCommunityDetectionAlgorithm(algorithm);
	    if (cda == null){
			return;
	    }
	    Map<String, CustomParameter> pMap = cda.getCustomParameterMap();
		if (pMap == null){
			return;
		}
	    JPanel algoCard = _algoCardMap.get(algorithm);
	    for (Component c : algoCard.getComponents()){
		if (c.getName() == null || !c.getName().startsWith(algorithm + INPUTDELIM)){
		    continue;
		}
		String paramName = c.getName().replaceAll("^.*" + INPUTDELIM, "");
		if (pMap.containsKey(paramName) == false){
		    continue;
		}
		CustomParameter cp = pMap.get(paramName);
		c = getTextFieldFromJScrollPane(c);
		
		if (c instanceof JTextField){
		    JTextField tField = (JTextField)c;
		    if (cp.getDefaultValue() == null){
			tField.setText("");
		    } else {
			tField.setText(cp.getDefaultValue());
		    }
		} else if (c instanceof JCheckBox){
		    JCheckBox checkBox = (JCheckBox)c;
		    checkBox.setSelected(false);
		}
	    }
	}

	/**
	 * Given 'algorithm' internal name, this method returns 
	 * {@link org.ndexbio.communitydetection.reste.model.CommunityDetectionAlgorithm}
	 * obtained from constructor of this object.
	 * 
	 * @param algorithm internal algorithm name
	 * @return 
	 */
	private CommunityDetectionAlgorithm getCommunityDetectionAlgorithm(final String algorithm){
	    for (CommunityDetectionAlgorithm cda : this._algorithmList){
		if (cda.getName().equals(algorithm)){
		    return cda;
		}
	    }
	    return null;
	}
	
	/**
	 * Given the display name for a Community Detection Algorithm get
	 * the corresponding {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm}
	 * from set of algorithms loaded in {@link #createGUI()} method
	 * @param displayName
	 * @return algorithm that matches or null
	 */
	private CommunityDetectionAlgorithm getCommunityDetectionAlgorithmByDisplayName(final String displayName){
	    for (CommunityDetectionAlgorithm cda : this._algorithmList){
		if (cda.getDisplayName().equals(displayName)){
		    return cda;
		}
	    }
	    return null;
	}
	
	/**
	 * Gets the currently selected community detection algorithm 
	 * @return Community Detection Algorithm or null if not found
	 */
	public CommunityDetectionAlgorithm getSelectedCommunityDetectionAlgorithm(){
	    if (_guiLoaded == false){
		LOGGER.info("gui not loaded");
		return null;
	    }
	    if (_algorithmComboBox == null){
		LOGGER.info("no combo box loaded");
		return null;
	    }
	    String algo = (String)_algorithmComboBox.getSelectedItem();
	    if (algo == null){
		LOGGER.info("no algorithm selected in combo box");
		return null;
	    }
	    LOGGER.debug("Algorithm: " + algo);
	    return getCommunityDetectionAlgorithmByDisplayName(algo);
	}
	/**
	 * Given an 'algorithm' name this method looks at the settings UI for the
	 * parameters for the 'algorithm' The code then gets all the user values
	 * set. The way it works is the {@link javax.swing.JComponent} containing
	 * the algorithm parameters is named 'algorithm' and each JTextField or JCheckBox 
	 * within that pnael is named
	 * "<ALGORITHM>:::<PARAMETER NAME>" so this method looks for the UI
	 * components with that naming convention extracting the values which are
	 * returned as a {@link java.util.Map}
	 * @param algorithm
	 * @return {@link java.util.Map} where the key is parameter name ie --overlap and
	 *         value is the user set value. If value was a checkbox then this is set
	 *         to empty string, otherwise it is not included in {@link java.util.Map}
	 */
	public Map<String, String> getAlgorithmCustomParameters(final String algorithm){
	    if (_guiLoaded == false){
			return null;
	    }
	    JPanel algoCard = _algoCardMap.get(algorithm);
	    if (algoCard == null){
		return null;
	    }
	    Map<String, String> cParam = new LinkedHashMap<String, String>();
	    
	    for (Component c : algoCard.getComponents()){
		if (c.getName() == null || !c.getName().startsWith(algorithm + INPUTDELIM)){
		    continue;
		}
		String paramName = c.getName().replaceAll("^.*" + INPUTDELIM, "");
		
		c = getTextFieldFromJScrollPane(c);
		
		if (c instanceof JTextField){
		    JTextField tField = (JTextField)c;
		    if (tField.getText() == null || tField.getText().trim().length() == 0){
			continue;
		    }
		    cParam.put(paramName, tField.getText());
		} else if (c instanceof JCheckBox){
		    JCheckBox checkBox = (JCheckBox)c;
		    if (checkBox.isSelected() == true){
			cParam.put(paramName, "");
		    }
		}
	    }
	    return cParam;
	}
	
	private Component getTextFieldFromJScrollPane(Component c){
		if (c instanceof JScrollPane){
			LOGGER.debug("Found a scrollpane with name: " + c.getName());
			JScrollPane scrollPane = (JScrollPane)c;
			JViewport viewport = scrollPane.getViewport();
			for (Component textComp : viewport.getComponents()){
				LOGGER.debug("Found a component under JScrollPane with name: " +
						(textComp.getName() == null ? "null" : textComp.getName()));

				if (textComp instanceof JTextField){
					return textComp;
				}
			}
		}
		LOGGER.debug("Didnt find a scrollpane, returning original component named: " +
				(c.getName() == null ? "null" : c.getName()));
		return c;
	}

	/**
	 * Creates reset button that when clicked resets all parameters for that
	 * algorithm to default values. The link is done by setting the name of the
	 * button to the algorithm name
	 * @param algorithm internal algorithm name
	 * @return reset button
	 */
	private JButton getResetButton(final String algorithm) {
		JButton button = new JButton(AppUtils.RESET);
		button.setName(algorithm);
		button.setToolTipText("Resets all parameters for this "
			+ "algorithm to default values");
		button.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			JComponent c = (JComponent)e.getSource();
			String algorithm = c.getName();
			LOGGER.debug("Reset button clicked "
				      + "on algorithm: " + algorithm);
			resetAlgorithmToDefaults(algorithm);
		    }
		});
		return button;
	}
	
	/**
	 * Creates a {@link javax.swing.JLabel} with an info icon that when clicked
	 * displays a small dialog that displays information about the Algorithm
	 * selected in the drop down menu
	 * @param parameter The parameter
	 * @return 
	 * @throws IOException 
	 */
	private JLabel getAboutAlgorithmIcon() {
		JLabel infoIcon = new JLabel(_infoIconSmall, JLabel.CENTER);
		infoIcon.setToolTipText("Click here for more "
				+ "information about currently selected algorithm");

		infoIcon.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				showAboutAlgorithmFrame();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		infoIcon.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				showAboutAlgorithmFrame();
			}
		});

		return infoIcon;
	}

	private void showAboutAlgorithmFrame(){
		CommunityDetectionAlgorithm cda = getSelectedCommunityDetectionAlgorithm();
				_dialogUtil.showMessageDialog(getParent(), _aboutAlgoFac.getAlgorithmAboutFrame(cda),
					"Algorithm " + cda.getDisplayName(), JOptionPane.INFORMATION_MESSAGE,
					_infoIconLarge);
	}
}
