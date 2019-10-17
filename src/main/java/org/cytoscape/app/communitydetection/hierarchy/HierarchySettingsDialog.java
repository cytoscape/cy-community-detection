package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
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

	private JLabel getParamLabel() {
		if (paramLabel != null) {
			return paramLabel;
		}
		String text = getParameterDisplayName();
		paramLabel = new JLabel(text);
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
			}
		});
		return algoDropDown;
	}

	private JTextField getParamTextField() {
		if (paramInput != null) {
			return paramInput;
		}
		paramInput = new JTextField();
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
				double resParam = Double.parseDouble(getParamTextField().getText());
				pMap.put(getParameterName(), resParam);
				CDRestClient.getInstance().addToResolutionParamMap(getAlgorithmName(), pMap);
				dispose();
			}
		});
		return button;
	}
}
