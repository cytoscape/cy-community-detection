package org.cytoscape.app.communitydetection.hierarchy;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.DoNothingTask;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

/**
 *
 * @author churas
 */
public class HierarchyTaskFactoryImplTest {
	
	@Test
	public void testIsReady(){
		//always returns true
		HierarchyTaskFactoryImpl fac = null; //new HierarchyTaskFactoryImpl(null, null,
				//null, null);
		assertEquals(true, fac.isReady(null));
		assertEquals(true, fac.isReady(mock(CyNetwork.class)));
	}
	
	@Test
	public void testCreateTaskIteratorWithNullNetwork(){
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		JFrame mockJFrame = mock(JFrame.class);
		when(mockSwing.getJFrame()).thenReturn(mockJFrame);
		HierarchyTaskFactoryImpl fac = null;//new HierarchyTaskFactoryImpl(mockSwing, null,
				//null, mockDialog);
		TaskIterator res = fac.createTaskIterator(null);
		assertEquals(1, res.getNumTasks());
		assertTrue(res.next() instanceof DoNothingTask);
		verify(mockDialog).showMessageDialog(eq(mockJFrame),
				eq("A network must be selected in Cytoscape to Run "
						+ "Community Detection. For more information "
						+ "click About menu item under Apps => "
						+ "Community Detection"),
				eq(AppUtils.APP_NAME), eq(JOptionPane.ERROR_MESSAGE));
		
	}
	
	@Test
	public void testCreateTaskIteratorLauncherDialogCreateGuiReturnsFalse(){
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetwork mockNetwork = mock(CyNetwork.class);
		LauncherDialog mockLauncher = mock(LauncherDialog.class);
		JFrame mockJFrame = mock(JFrame.class);
		when(mockSwing.getJFrame()).thenReturn(mockJFrame);
		when(mockLauncher.createGUI(mockJFrame)).thenReturn(Boolean.FALSE);

		HierarchyTaskFactoryImpl fac = null; //new HierarchyTaskFactoryImpl(mockSwing, mockLauncher,
				//null, mockDialog);
		TaskIterator res = fac.createTaskIterator(mockNetwork);
		assertEquals(1, res.getNumTasks());
		assertTrue(res.next() instanceof DoNothingTask);
		verifyNoInteractions(mockDialog);
		verifyNoInteractions(mockNetwork);
		
	}
	
	@Test
	public void testCreateTaskIteratorUserCancels(){
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);

		CyTable mockTable = mock(CyTable.class);
		when(mockTable.getColumns()).thenReturn(Collections.EMPTY_LIST);
		
		CyNetwork mockNetwork = mock(CyNetwork.class);
		when(mockNetwork.getDefaultEdgeTable()).thenReturn(mockTable);
		
		JFrame mockJFrame = mock(JFrame.class);
		when(mockSwing.getJFrame()).thenReturn(mockJFrame);
		
		LauncherDialog mockLauncher = mock(LauncherDialog.class);
		when(mockLauncher.createGUI(mockJFrame)).thenReturn(Boolean.TRUE);
		
		Object[] options = {AppUtils.RUN, AppUtils.CANCEL};
		
		when(mockDialog.showOptionDialog(eq(mockJFrame),
				any(), eq("Run Community Detection"),
				eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE),
				any(), eq(options), eq(options[0]))).thenReturn(1);
		
		HierarchyTaskFactoryImpl fac = null;//new HierarchyTaskFactoryImpl(mockSwing, mockLauncher,
				//null, mockDialog);
		TaskIterator res = fac.createTaskIterator(mockNetwork);
		assertEquals(1, res.getNumTasks());
		assertTrue(res.next() instanceof DoNothingTask);
		verify(mockNetwork).getDefaultEdgeTable();
		verify(mockTable).getColumns();
		verify(mockLauncher).updateWeightColumnCombo(Collections.EMPTY_SET);	
	}
	
	@Test
	public void testCreateTaskIteratorSuccessWithNullCustomParameters(){
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);

		CyTable mockTable = mock(CyTable.class);
		when(mockTable.getColumns()).thenReturn(Collections.EMPTY_LIST);
		
		CyNetwork mockNetwork = mock(CyNetwork.class);
		when(mockNetwork.getDefaultEdgeTable()).thenReturn(mockTable);
		
		JFrame mockJFrame = mock(JFrame.class);
		when(mockSwing.getJFrame()).thenReturn(mockJFrame);
		
		LauncherDialog mockLauncher = mock(LauncherDialog.class);
		when(mockLauncher.createGUI(mockJFrame)).thenReturn(Boolean.TRUE);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		when(mockLauncher.getWeightColumn()).thenReturn(null);
		when(mockLauncher.getAlgorithmCustomParameters(cda.getName())).thenReturn(null);
		when(mockLauncher.getSelectedCommunityDetectionAlgorithm()).thenReturn(cda);
		
		Object[] options = {AppUtils.RUN, AppUtils.CANCEL};
		
		when(mockDialog.showOptionDialog(eq(mockJFrame),
				any(), eq("Run Community Detection"),
				eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE),
				any(), eq(options), eq(options[0]))).thenReturn(0);
		
		HierarchyTaskFactoryImpl fac = null;//new HierarchyTaskFactoryImpl(mockSwing, mockLauncher,
				//null, mockDialog);
		TaskIterator res = fac.createTaskIterator(mockNetwork);
		assertEquals(1, res.getNumTasks());
		Task resTask = res.next();
		assertTrue(resTask instanceof HierarchyTask);
		verify(mockNetwork).getDefaultEdgeTable();
		verify(mockTable).getColumns();
		verify(mockLauncher).updateWeightColumnCombo(Collections.EMPTY_SET);
		
	}
	
	@Test
	public void testCreateTaskIteratorFailUnableToGetSelectedAlgorithm(){
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);

		CyTable mockTable = mock(CyTable.class);
		when(mockTable.getColumns()).thenReturn(Collections.EMPTY_LIST);
		
		CyNetwork mockNetwork = mock(CyNetwork.class);
		when(mockNetwork.getDefaultEdgeTable()).thenReturn(mockTable);
		
		JFrame mockJFrame = mock(JFrame.class);
		when(mockSwing.getJFrame()).thenReturn(mockJFrame);
		
		LauncherDialog mockLauncher = mock(LauncherDialog.class);
		when(mockLauncher.createGUI(mockJFrame)).thenReturn(Boolean.TRUE);

		when(mockLauncher.getWeightColumn()).thenReturn(null);
		when(mockLauncher.getSelectedCommunityDetectionAlgorithm()).thenReturn(null);
		
		Object[] options = {AppUtils.RUN, AppUtils.CANCEL};
		
		when(mockDialog.showOptionDialog(eq(mockJFrame),
				any(), eq("Run Community Detection"),
				eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE),
				any(), eq(options), eq(options[0]))).thenReturn(0);
		
		HierarchyTaskFactoryImpl fac = null;//new HierarchyTaskFactoryImpl(mockSwing, mockLauncher,
				//null, mockDialog);
		TaskIterator res = fac.createTaskIterator(mockNetwork);
		assertEquals(1, res.getNumTasks());
		Task resTask = res.next();
		assertTrue(resTask instanceof DoNothingTask);
		verify(mockNetwork).getDefaultEdgeTable();
		verify(mockTable).getColumns();
		verify(mockLauncher).updateWeightColumnCombo(Collections.EMPTY_SET);
		
	}
	
	@Test
	public void testCreateTaskIteratorSuccessWithCustomParameters(){
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);

		CyTable mockTable = mock(CyTable.class);
		when(mockTable.getColumns()).thenReturn(Collections.EMPTY_LIST);
		
		CyNetwork mockNetwork = mock(CyNetwork.class);
		when(mockNetwork.getDefaultEdgeTable()).thenReturn(mockTable);
		
		JFrame mockJFrame = mock(JFrame.class);
		when(mockSwing.getJFrame()).thenReturn(mockJFrame);
		
		LauncherDialog mockLauncher = mock(LauncherDialog.class);
		when(mockLauncher.createGUI(mockJFrame)).thenReturn(Boolean.TRUE);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		when(mockLauncher.getWeightColumn()).thenReturn(null);
		Map<String, String> custParams = new HashMap<>();
		custParams.put("key", "value");

		when(mockLauncher.getAlgorithmCustomParameters(cda.getName())).thenReturn(custParams);
		when(mockLauncher.getSelectedCommunityDetectionAlgorithm()).thenReturn(cda);
		
		Object[] options = {AppUtils.RUN, AppUtils.CANCEL};
		
		when(mockDialog.showOptionDialog(eq(mockJFrame),
				any(), eq("Run Community Detection"),
				eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE),
				any(), eq(options), eq(options[0]))).thenReturn(0);
		
		HierarchyTaskFactoryImpl fac = null;//new HierarchyTaskFactoryImpl(mockSwing, mockLauncher,
				//null, mockDialog);
		TaskIterator res = fac.createTaskIterator(mockNetwork);
		assertEquals(1, res.getNumTasks());
		Task resTask = res.next();
		assertTrue(resTask instanceof HierarchyTask);
		verify(mockNetwork).getDefaultEdgeTable();
		verify(mockTable).getColumns();
		verify(mockLauncher).updateWeightColumnCombo(Collections.EMPTY_SET);
	}
}
