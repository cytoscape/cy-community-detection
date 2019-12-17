package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Image;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import org.ndexbio.communitydetection.rest.model.CustomParameter;

@SuppressWarnings("serial")
public class HierarchySettingsDialog extends JDialog implements ActionListener,ItemListener {

	private Map<String, List<CustomParameter>> paramMap;
	private List<CommunityDetectionAlgorithm> algorithmList;

	private JLabel paramLabel;
	private JComboBox<String> algoDropDown;
	private JTextField paramInput;
	private JPanel cards;
	private JEditorPaneFactory editorPaneFac;
	private ImageIcon infoIconSmall;
	private ImageIcon infoIconLarge;
	
	
	

	public HierarchySettingsDialog(CySwingApplication swingApplication,
		JEditorPaneFactory editorPaneFac) throws Exception {
		super(swingApplication.getJFrame().getOwner(), "CD Settings", ModalityType.MODELESS);
		this.editorPaneFac = editorPaneFac;
		loadImageIcon();
		paramMap = CDRestClient.getInstance().getResolutionParameters();
		algorithmList = CDRestClient.getInstance().getAlgorithmsByType(AppUtils.CD_ALGORITHM_INPUT_TYPE);

		cards = new JPanel(new CardLayout());
		
		JPanel contentPane = new JPanel();
		
		String[] algoNames  = new String[algorithmList.size()];
		int counter = 0;
		for (CommunityDetectionAlgorithm cda : algorithmList){
		    Map<String, CustomParameter> pMap = cda.getCustomParameterMap();
		    if (pMap == null){
			continue;
		    }
		    JPanel algoCard = new JPanel();
		    algoCard.setLayout(new BoxLayout(algoCard, BoxLayout.Y_AXIS));
		    algoNames[counter++] = cda.getDisplayName();
		    for (String key : pMap.keySet()){
			CustomParameter cp = pMap.get(key);
			JPanel rowPanel = new JPanel();
			JLabel paramLabel = new JLabel(cp.getDisplayName()); 
			rowPanel.add(paramLabel);
			if (cp.getType() == null || cp.getType().equalsIgnoreCase("value")){
			    JTextField inputField = null;
			    if (cp.getDefaultValue() != null){
				inputField = new JTextField(cp.getDefaultValue());
			    }
			    else {
				inputField = new JTextField();
			    }
			    rowPanel.add(inputField);
			} else if (cp.getType().equalsIgnoreCase("flag")){
			    rowPanel.add(new JCheckBox());
			}
			if (cp.getDescription() != null){
			    rowPanel.setToolTipText(cp.getDescription());
			}
			rowPanel.add(getParameterInfoIcon(cp));
			algoCard.add(rowPanel);
		    }
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
		
		JPanel okPanel = new JPanel();
		okPanel.add(new JButton(AppUtils.CANCEL));
		okPanel.add(new JButton(AppUtils.APPLY));
		
		
		add(contentPane, BorderLayout.PAGE_START);
		
		add(masterPanel, BorderLayout.LINE_START);
		add(okPanel, BorderLayout.PAGE_END);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		pack();
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

	private JComboBox<String> getDropDown() {
		if (algoDropDown != null) {
			return algoDropDown;
		}
		List<String> algoNames = new ArrayList<String>();
		for (CommunityDetectionAlgorithm algo : algorithmList) {
			algoNames.add(algo.getDisplayName());
		}
		String[] data = algoNames.toArray(new String[0]);
		algoDropDown = new JComboBox<String>(data);
		algoDropDown.setSelectedIndex(0);
		algoDropDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				paramLabel.setText(getParameterDisplayName());
				paramLabel.setToolTipText(getParameterDescription());
				setParamInput();
			}
		});
		return algoDropDown;
	}

	private JTextField getParamTextField() {
		if (paramInput != null) {
			return paramInput;
		}
		paramInput = new JTextField();
		setParamInput();
		return paramInput;
	}

	private Component getOKButton() {
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Map<String, Double> pMap = new LinkedHashMap<String, Double>();
				double resParam = 0.0;
				try {
					resParam = Double.parseDouble(getParamTextField().getText());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(getParent(), "Please provide a number greater than 0");
					return;
				}
				if (resParam < 0.0) {
					JOptionPane.showMessageDialog(getParent(), "Please provide a number greater than 0");
					return;
				}
				pMap.put(getParameterName(), resParam);
				CDRestClient.getInstance().addToResolutionParamMap(getAlgorithmName(), pMap);
				dispose();
			}
		});
		return button;
	}

	private String getAlgorithmName() {
		String algoName = null;
		for (CommunityDetectionAlgorithm algo : algorithmList) {
			if (algo.getDisplayName().equalsIgnoreCase((String) getDropDown().getSelectedItem())) {
				algoName = algo.getName();
				break;
			}
		}
		return algoName;
	}

	private String getParameterDisplayName() {
		String paramDisplayName = null;
		for (CommunityDetectionAlgorithm algo : algorithmList) {
			if (algo.getDisplayName().equalsIgnoreCase((String) getDropDown().getSelectedItem())) {
				paramDisplayName = paramMap.get(algo.getName()).get(0).getDisplayName();
				break;
			}
		}
		return paramDisplayName;
	}

	private String getParameterName() {
		String paramName = null;
		for (CommunityDetectionAlgorithm algo : algorithmList) {
			if (algo.getDisplayName().equalsIgnoreCase((String) getDropDown().getSelectedItem())) {
				paramName = paramMap.get(algo.getName()).get(0).getName();
				break;
			}
		}
		return paramName;
	}

	private String getParameterDefaultValue() {
		String paramDefault = null;
		for (CommunityDetectionAlgorithm algo : algorithmList) {
			if (algo.getDisplayName().equalsIgnoreCase((String) getDropDown().getSelectedItem())) {
				paramDefault = paramMap.get(algo.getName()).get(0).getDefaultValue();
				break;
			}
		}
		return paramDefault;
	}

	private String getParameterDescription() {
		String paramDes = null;
		for (CommunityDetectionAlgorithm algo : algorithmList) {
			if (algo.getDisplayName().equalsIgnoreCase((String) getDropDown().getSelectedItem())) {
				paramDes = paramMap.get(algo.getName()).get(0).getDescription();
				break;
			}
		}
		return paramDes;
	}

	private void setParamInput() {
		String paramVal = null;
		if (CDRestClient.getInstance().getResolutionParam((String) getDropDown().getSelectedItem()) != null) {
			paramVal = Double.toString(CDRestClient.getInstance()
					.getResolutionParam((String) getDropDown().getSelectedItem()).get(getParameterName()));
		} else {
			paramVal = getParameterDefaultValue();
		}
		paramInput.setText(paramVal);
	}
}
