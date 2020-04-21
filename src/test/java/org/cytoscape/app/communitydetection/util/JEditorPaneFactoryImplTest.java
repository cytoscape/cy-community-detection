package org.cytoscape.app.communitydetection.util;

import javax.swing.JEditorPane;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class JEditorPaneFactoryImplTest {
	
	@Test
	public void testGetDescriptionFrame(){
		JEditorPaneFactoryImpl fac = new JEditorPaneFactoryImpl();
		JEditorPane jpane = fac.getDescriptionFrame("hello");
		assertTrue(jpane.getText().contains("hello"));
		assertTrue(jpane.getText().contains("<html>"));
		assertTrue(jpane.getText().contains("</html>"));
		assertFalse(jpane.isEditable());
		assertFalse(jpane.isOpaque());
		assertEquals(600, jpane.getPreferredSize().width);
		
	}
}
