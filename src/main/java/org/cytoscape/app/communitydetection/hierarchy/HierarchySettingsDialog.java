package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.swing.CySwingApplication;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CustomParameter;

@SuppressWarnings("serial")
public class HierarchySettingsDialog extends JDialog implements ActionListener,ItemListener {

	private List<CommunityDetectionAlgorithm> algorithmList;

	private JLabel paramLabel;
	private JComboBox<String> algoDropDown;
	private JTextField paramInput;
	private JPanel cards;
	private JEditorPaneFactory editorPaneFac;
	private ImageIcon infoIconSmall;
	private ImageIcon infoIconLarge;
	private Map<String, JPanel> algoCardMap;

	public HierarchySettingsDialog(CySwingApplication swingApplication,
		JEditorPaneFactory editorPaneFac) throws Exception {
		super(swingApplication.getJFrame().getOwner(), AppUtils.APP_NAME + " settings",
			ModalityType.MODELESS);
		this.editorPaneFac = editorPaneFac;
		loadImageIcon();
		algoCardMap = new LinkedHashMap<>();
		algorithmList = CDRestClient.getInstance().getAlgorithmsByType(AppUtils.CD_ALGORITHM_INPUT_TYPE);

		cards = new JPanel(new CardLayout());
		
		JPanel contentPane = new JPanel();
		
		String[] algoNames  = new String[algorithmList.size()];
		int counter = 0;
		int rowIndex = 1;
		for (CommunityDetectionAlgorithm cda : algorithmList){
		    Map<String, CustomParameter> pMap = cda.getCustomParameterMap();
		    if (pMap == null){
			continue;
		    }
		    CommunityDetectionRequest request = CDRestClient.getInstance().getRequestForAlgorithm(cda.getName());
		    JPanel algoCard = new JPanel();
		    algoCard.setName(cda.getName());
		    algoCardMap.put(cda.getName(), algoCard);
		    algoCard.setLayout(new GridBagLayout());
		    algoCard.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Parameters"),
                                BorderFactory.createEmptyBorder(5,5,5,5)));
		    algoNames[counter++] = cda.getDisplayName();
		    for (String key : pMap.keySet()){
			CustomParameter cp = pMap.get(key);
			JLabel paramLabel = new JLabel(cp.getDisplayName() + ":");
			
			GridBagConstraints labelConstraints = new GridBagConstraints();
			labelConstraints.gridy = rowIndex;
			labelConstraints.gridx = 0;
			labelConstraints.anchor = GridBagConstraints.LINE_END;
			labelConstraints.insets = new Insets(0, 5, 5, 0);
			algoCard.add(paramLabel, labelConstraints);
			
			JComponent inputComponent = getCustomParameterInput(cda.getName(), request, cp);
			if (cp.getDescription() != null){
			    inputComponent.setToolTipText(cp.getDescription());
			    paramLabel.setToolTipText(cp.getDescription());
			}
			
			GridBagConstraints inputConstraints = new GridBagConstraints();
			inputConstraints.gridy = rowIndex;
			inputConstraints.gridx = 1;
			inputConstraints.gridwidth = 1;
			inputConstraints.weightx = 1.0;
			inputConstraints.anchor = GridBagConstraints.LINE_START;
			inputConstraints.insets = new Insets(0, 0, 5, 0);
			inputConstraints.fill = GridBagConstraints.HORIZONTAL;
			
			algoCard.add(inputComponent, inputConstraints);
			
			GridBagConstraints infoConstraints = new GridBagConstraints();
			infoConstraints.gridy = rowIndex;
			infoConstraints.gridx = 2;
			infoConstraints.insets = new Insets(0, 0, 5, 0);
			algoCard.add(getParameterInfoIcon(cp), infoConstraints);
			
			rowIndex++;
		    }
		    GridBagConstraints resetConstraints = new GridBagConstraints();
		    resetConstraints.gridy = 0;
		    resetConstraints.gridx = 0;
		    resetConstraints.insets = new Insets(0, 5, 0, 0);
		    resetConstraints.anchor = GridBagConstraints.LINE_START;
		    algoCard.add(this.getResetButton(cda.getName()), resetConstraints);
		    cards.add(algoCard, cda.getDisplayName());
		}
		JComboBox cb = new JComboBox(algoNames);
		cb.setEditable(false);
		cb.addItemListener(this);
		contentPane.add(new JLabel("Algorithm: "));
		contentPane.add(cb);
		
		
		JPanel masterPanel = new JPanel();
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		masterPanel.add(cards);
		
		JPanel closePanel = new JPanel();
		closePanel.add(getCloseButton());
		
		add(contentPane, BorderLayout.PAGE_START);
		
		add(masterPanel, BorderLayout.CENTER);
		add(closePanel, BorderLayout.PAGE_END);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		pack();
	}
	
	private JComponent getCustomParameterInput(final String algorithm, final CommunityDetectionRequest request,
		final CustomParameter parameter){
	    if (parameter.getType() != null && parameter.getType().equalsIgnoreCase("flag")){
		    JCheckBox checkBox = new JCheckBox();
		    checkBox.setName(algorithm + ":::" + parameter.getName());
		    if (request != null && request.getCustomParameters().containsKey(parameter.getName())){
			checkBox.setSelected(true);
		    }
		    return checkBox;
	    }
	    if (parameter.getDefaultValue() != null){
		JTextField textField = null;
		if (request != null && request.getCustomParameters().containsKey(parameter.getName())){
		    textField = new JTextField(request.getCustomParameters().get(parameter.getName()));
		} else {
		    textField = new JTextField(parameter.getDefaultValue());
		}
		textField.setName(algorithm + ":::" + parameter.getName());
	    }
	    JTextField textField = new JTextField();
	    textField.setName(algorithm + ":::" + parameter.getName());
	    return textField;
	}
	
	private void loadImageIcon(){
	    try {
		    File imgFile = File.createTempFile("info_icon", "png");
	        InputStream imgStream = getClass().getClassLoader().getResourceAsStream("info_icon.png");
		Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		infoIconSmall = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		infoIconLarge = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
	    }
	    catch (IOException ex){
		    
	    }
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, (String)evt.getItem());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(true);
	}
	
	private JEditorPane getCustomParameterHelp(final CustomParameter parameter){
	    if (parameter == null){
		return editorPaneFac.getDescriptionFrame("No parameter set, unable to generate help");
	    }
	    StringBuilder sb = new StringBuilder();
	    sb.append("<b>Parameter:</b> ");
	    sb.append(parameter.getDisplayName());
	    sb.append(" (");
	    sb.append(parameter.getName());
	    sb.append(")");
	    if (parameter.getDefaultValue() != null){
		sb.append(" [Default: ");
		sb.append(parameter.getDefaultValue());
		sb.append("]");
	    }
	    sb.append("<br/><h3>Description</h3> ");
	    sb.append(parameter.getDescription());
	    if (parameter.getValidationHelp() != null){
		sb.append("<br/>");
		sb.append(parameter.getValidationHelp());
	    }
	    return editorPaneFac.getDescriptionFrame(sb.toString());
	}
	
	/**
	 * Creates a {@link javax.swing.JLabel} with an info icon that when clicked
	 * displays a small dialog that displays information about the parameter
	 * passed in
	 * @param parameter The parameter
	 * @return 
	 * @throws IOException 
	 */
	private JLabel getParameterInfoIcon(final CustomParameter parameter) throws IOException {
		JLabel paramLabel = new JLabel(infoIconSmall, JLabel.CENTER);
		paramLabel.setToolTipText("Click here for more information about " +
			parameter.getDisplayName() + "parameter");

		paramLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				JOptionPane.showMessageDialog(getParent(), getCustomParameterHelp(parameter),
					"Parameter " + parameter.getDisplayName(), JOptionPane.INFORMATION_MESSAGE,
					infoIconLarge);
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
				JOptionPane.showMessageDialog(getParent(), getCustomParameterHelp(parameter),
					"Parameter " + parameter.getDisplayName(), JOptionPane.INFORMATION_MESSAGE,
					infoIconLarge);
			}
		});

		return paramLabel;
	}

	
	private Component getCloseButton() {
		JButton button = new JButton(AppUtils.CLOSE);
		button.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			dispose();
		    }
		});
		return button;
	}
	
	private void resetAlgorithmToDefaults(final String algorithm){
	    System.out.println("Resetting " + algorithm + " to defaults");
	    if (algoCardMap.containsKey(algorithm) == false){
		return;
	    }
	    JPanel algoCard = algoCardMap.get(algorithm);
	    for (Component c : algoCard.getComponents()){
		if (c.getName() == null || !c.getName().startsWith(algorithm + ":::")){
		    continue;
		}
		System.out.println("Component: " + c.getName());
		
	    }
	}

	private JButton getResetButton(final String algorithm) {
		JButton button = new JButton(AppUtils.RESET);
		button.setName(algorithm);
		button.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			System.out.println("Reset button clicked");
			JComponent c = (JComponent)e.getSource();
			String algorithm = c.getName();
			System.out.println("Reset button clicked on algorithm: " + algorithm);
			resetAlgorithmToDefaults(algorithm);
		    }
		});
		return button;
	}
}
