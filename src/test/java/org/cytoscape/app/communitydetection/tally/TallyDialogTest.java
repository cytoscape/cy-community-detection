package org.cytoscape.app.communitydetection.tally;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import org.cytoscape.app.communitydetection.util.IconJLabelDialogFactory;
import org.cytoscape.model.CyColumn;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class TallyDialogTest {
	
	@Test
	public void testCreateGUIFirstCallWithNullAndEmptyMapPassedIn(){
		TallyDialog td = new TallyDialog(null);
		
		assertNull(td.getColumnsToTally());
		
		assertFalse(td.createGUI(null));
		assertFalse(td.createGUI(new HashMap<String, CyColumn>()));
		
		assertNull(td.getColumnsToTally());
	}
	
	@Test
	public void testCreateGUIFirstCallWithNullArgsPassedToConstructor(){
		TallyDialog td = new TallyDialog(null);
		try {
			HashMap<String, CyColumn> cols = new HashMap<>();
			cols.put("hi", null);
			td.createGUI(cols);
			fail("Expected NullPointerException");
		} catch(NullPointerException npe){
			
		}
	}
	
	@Test
	public void testCreateGUIWithValidData(){
		IconJLabelDialogFactory mockFac = mock(IconJLabelDialogFactory.class);
		JLabel jLabel = new JLabel();
		when(mockFac.getJLabelIcon(any(Component.class),
				any(String.class), any(String.class),
				any(String.class), any(String.class),
				any(Integer.class), any(Integer.class))).thenReturn(jLabel);
		TallyDialog td = new TallyDialog(mockFac);
		
		Map<String, CyColumn> cols = new HashMap<>();
		CyColumn fooCol = mock(CyColumn.class);
		CyColumn blahCol = mock(CyColumn.class);
		cols.put("foo", fooCol);
		cols.put("blah", blahCol);
		assertTrue(td.createGUI(cols));
		
		assertEquals(0, td.getColumnsToTally().size());
		
		assertEquals("infoIcon", jLabel.getName());
		assertEquals("Click here for more information about "
				+ "Tallying Attributes on Hierarchy", jLabel.getToolTipText());
		
		verify(mockFac).getJLabelIcon(any(Component.class),eq("info_icon"),
				eq("png"), eq("Tally Attributes on hierarchy description"),
				eq(TallyDialog.DESC_MESSAGE), eq(20), eq(40));
		
		JScrollPane listScrollPane = null;
		JList jList = null;
		JPanel mainPanel = (JPanel)td.getComponents()[0];
		
		for (Component c : mainPanel.getComponents()){
			if (c.getName() == null){
				continue;
			}
			if (c.getName().equals("columnListScrollPane")){
				listScrollPane = (JScrollPane)c;
				break;
			}
		}
		for (Component c : listScrollPane.getComponents()){
			if (c.getClass().getTypeName().contains("JViewport")){
				JViewport port = (JViewport)c;
				jList = (JList)port.getComponents()[0];
				break;
			}
		}
		assertNotNull(jList);
		jList.setSelectedIndex(0);
			
		List<CyColumn> selectedCols = td.getColumnsToTally();
		assertEquals(1, selectedCols.size());
		assertEquals(fooCol, selectedCols.get(0));
	}
}
