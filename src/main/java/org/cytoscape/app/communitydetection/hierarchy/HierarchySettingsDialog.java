package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
public class HierarchySettingsDialog extends JDialog implements ActionListener {

	private Map<String, List<CustomParameter>> paramMap;
	private List<CommunityDetectionAlgorithm> algorithmList;

	private JLabel paramLabel;
	private JComboBox<String> algoDropDown;
	private JTextField paramInput;

	public HierarchySettingsDialog(CySwingApplication swingApplication) throws Exception {
		super(swingApplication.getJFrame().getOwner(), "CD Settings", ModalityType.MODELESS);

		paramMap = CDRestClient.getInstance().getResolutionParameters();
		algorithmList = CDRestClient.getInstance().getAlgorithmsByType(AppUtils.CD_ALGORITHM_INPUT_TYPE);

		JPanel contentPane = new JPanel();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel algoLabel = new JLabel("Select algorithm");
		Component doneBtn = getOKButton();
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(algoLabel)
						.addComponent(getParamLabel()))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(getDropDown())
						.addComponent(getParamTextField()))
				.addComponent(doneBtn));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(algoLabel)
						.addComponent(getDropDown()))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(getParamLabel())
						.addComponent(getParamTextField()))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(doneBtn)));

		add(contentPane, BorderLayout.CENTER);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(getOwner());
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(true);
	}

	private JLabel getParamLabel() throws IOException {
		if (paramLabel != null) {
			return paramLabel;
		}
		String text = getParameterDisplayName();
		InputStream imgStream = getClass().getClassLoader().getResourceAsStream("images/info_icon.png");
		File imgFile = File.createTempFile("info_icon", "png");
		Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		ImageIcon infoIcon = new ImageIcon(
				new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		paramLabel = new JLabel(text, infoIcon, JLabel.CENTER);
		paramLabel.setToolTipText(getParameterDescription());

		paramLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				JOptionPane.showMessageDialog(getParent(), getParameterDescription());
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
				JOptionPane.showMessageDialog(getParent(), getParameterDescription());
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
				System.out.println("selectedAlgorithm: " + getDropDown().getSelectedItem());
				System.out.println("resolutionParamater: " + getParamTextField().getText());
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
