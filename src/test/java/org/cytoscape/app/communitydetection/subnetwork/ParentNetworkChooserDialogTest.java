package org.cytoscape.app.communitydetection.subnetwork;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.cytoscape.app.communitydetection.util.IconJLabelDialogFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class ParentNetworkChooserDialogTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();
	
	@Test
	public void testGetSelectionAndRememberChoiceBeforeCreateGUI(){
		IconJLabelDialogFactory mockIconFac = mock(IconJLabelDialogFactory.class);
		ParentNetworkChooserDialog chooser = new ParentNetworkChooserDialog(mockIconFac);

		assertNull(chooser.getSelection());
		assertFalse(chooser.rememberChoice());
	}

	@Test
	public void testCreateGUINullNetworks(){
		IconJLabelDialogFactory mockIconFac = mock(IconJLabelDialogFactory.class);
		when(mockIconFac.getJLabelIcon(any(Component.class), eq("info_icon"), eq("png"),
				any(String.class), any(String.class), eq(20), eq(40))).thenReturn(new JLabel("x"));
		ParentNetworkChooserDialog chooser = new ParentNetworkChooserDialog(mockIconFac);
		assertEquals(true, chooser.createGUI(null));
		
		assertNull(chooser.getSelection());
		assertTrue(chooser.rememberChoice());
		
		//try a second call
		assertEquals(true, chooser.createGUI(null));
		assertNull(chooser.getSelection());
		assertTrue(chooser.rememberChoice());
	}
	
	@Test
	public void testCreateGUIWithNetworks(){
		
		IconJLabelDialogFactory mockIconFac = mock(IconJLabelDialogFactory.class);
		when(mockIconFac.getJLabelIcon(any(Component.class), eq("info_icon"), eq("png"),
				any(String.class), any(String.class), eq(20), eq(40))).thenReturn(new JLabel("x"));
		ParentNetworkChooserDialog chooser = new ParentNetworkChooserDialog(mockIconFac);
		
		CyNetwork netOne = _nts.getNetwork();
		netOne.getRow(netOne).set(CyNetwork.NAME, "netOne");
	
		List<CyNetwork> networks = new ArrayList<>();
		networks.add(netOne);
		assertEquals(true, chooser.createGUI(networks));
		
		JPanel chooserPanel = null;
		for (Component c : chooser.getComponents()){
			if (c.getName() != null && c.getName().equals("chooserPanel")){
				chooserPanel = (JPanel)c;
				break;
			}
		}
		assertNotNull(chooserPanel);
		JComboBox comboBox = null;
		JCheckBox checkBox = null;
		for (Component c : chooserPanel.getComponents()){
			if (c.getName() == null){
				continue;
			}
			if (c.getName().equals("comboBox")){
				comboBox = (JComboBox)c;			
			} else if (c.getName().equals("rememberCheckBox")){
				checkBox = (JCheckBox)c;
			}
		}
		assertNotNull(comboBox);
		assertNotNull(checkBox);
		checkBox.setSelected(false);
		String curItem = (String)comboBox.getItemAt(0);
		assertEquals("netOne(0 nodes, 0 edges)", curItem);
		
		CyNetwork selectedNet = chooser.getSelection();
		assertNotNull(selectedNet);
		assertEquals("netOne", selectedNet.getRow(netOne).get(CyNetwork.NAME, String.class));
		
		assertFalse(chooser.rememberChoice());
	}

}
