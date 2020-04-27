package org.cytoscape.app.communitydetection;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.cytoscape.app.communitydetection.util.IconJLabelDialogFactory;
import org.cytoscape.app.communitydetection.util.ImageIconHolderFactory;
import org.cytoscape.app.communitydetection.util.JEditorPaneFactory;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class SettingsDialogTest {
	
	@Test
	public void testGetBaseurlBeforeGUICreated(){
		SettingsDialog sd = new SettingsDialog(null, null);
		assertNull(sd.getBaseurl());
	}
	
	@Test
	public void testCreateGUITwice(){
		ImageIconHolderFactory ifac = new ImageIconHolderFactory();
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		JEditorPaneFactory mockJEditorPaneFactory = mock(JEditorPaneFactory.class);
		IconJLabelDialogFactory iconFac = new IconJLabelDialogFactory(mockDialogUtil,
				ifac, mockJEditorPaneFactory);
		PropertiesHelper mockPropertiesHelper = mock(PropertiesHelper.class);
		when(mockPropertiesHelper.getBaseurlHostNameOnly()).thenReturn("foo.com", "blah.com");

		SettingsDialog sd = new SettingsDialog(iconFac, mockPropertiesHelper);
		assertTrue(sd.createGUI());

		assertEquals("foo.com", sd.getBaseurl());
		
		// simple check to verify GUI hasnt changed
		assertEquals(1, sd.getComponentCount());
		JPanel panel = (JPanel)sd.getComponents()[0];
		
		JTextField restURLTextField = null;
		JLabel restURLIcon = null;
		for (Component c : panel.getComponents()){
			
			if (c.getName() != null){
				if (c.getName().equals("baseurl")){
					restURLTextField = (JTextField)c;
				}
				else if (c.getName().equals("restURLIcon")){
					restURLIcon = (JLabel)c;
				}
			}
		}
		
		assertNotNull(restURLTextField);
		assertEquals("foo.com", restURLTextField.getText());
		
		//simulate key release on REST URL info icon
		for (KeyListener kl : restURLIcon.getKeyListeners()){
			kl.keyReleased(null);
			break;
		}
		
		//simulate mouse click on REST URL info icon
		for (MouseListener ml : restURLIcon.getMouseListeners()){
			ml.mouseClicked(null);
			break;
		}
		// TODO add check that ShowDialogUtil was invoked with proper
		//      data
		
		// 2nd create GUI call
		assertTrue(sd.createGUI());
		assertEquals("blah.com", sd.getBaseurl());
		

	}
}
