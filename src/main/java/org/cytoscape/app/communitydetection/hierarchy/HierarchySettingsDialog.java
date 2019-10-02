package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

@SuppressWarnings("serial")
public class HierarchySettingsDialog extends JDialog implements ActionListener {

	private String selectedAlgorithm;
	private Double resolutionParamater;

	public HierarchySettingsDialog(CySwingApplication swingApplication) {
		super(swingApplication.getJFrame().getOwner(), "CD Settings", ModalityType.MODELESS);

		JPanel contentPane = new JPanel();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		Component c1, c2, c3, c4, c6;
		c1 = getLabel("Select algorithm");
		c3 = getLabel("Resolution Paramter:");
		c2 = getDropDown(AppUtils.HIERARCHY_ALGORITHMS.keySet().toArray(new String[0]));
		c4 = getTextField();
		c6 = getOKButton();
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(c1).addComponent(c3))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(c2).addComponent(c4))
				.addComponent(c6));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(c1).addComponent(c2))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(c3).addComponent(c4))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(c6)));

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

	private Component getLabel(String text) {
		return new JLabel(text);
	}

	private Component getDropDown(String[] data) {
		JComboBox<String> dropDown = new JComboBox<String>(data);
		dropDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectedAlgorithm = (String) dropDown.getSelectedItem();
			}
		});
		return dropDown;
	}

	private Component getTextField() {
		JTextField textField = new JTextField();
		textField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("textField: " + textField.getText());
				resolutionParamater = Double.parseDouble(textField.getText());
			}
		});
		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				try {
					resolutionParamater = Double.parseDouble(textField.getText());
				} catch (Exception E) {
					System.out.println("Text not a number yet.");
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		return textField;
	}

	private Component getOKButton() {
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("selectedAlgorithm: " + selectedAlgorithm);
				System.out.println("resolutionParamater: " + resolutionParamater);
				CDRestClient.getInstance().addToResolutionParamMap(selectedAlgorithm, resolutionParamater);
				dispose();
			}
		});
		return button;
	}
}
