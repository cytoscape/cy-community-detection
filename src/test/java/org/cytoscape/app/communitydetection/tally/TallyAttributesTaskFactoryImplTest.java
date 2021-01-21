package org.cytoscape.app.communitydetection.tally;

import java.util.Arrays;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.DoNothingTask;
import org.cytoscape.app.communitydetection.subnetwork.ParentNetworkChooserDialog;
import org.cytoscape.app.communitydetection.subnetwork.ParentNetworkFinder;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.work.TaskIterator;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class TallyAttributesTaskFactoryImplTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();
	private CyNetworkUtil _cyNetworkUtil = new CyNetworkUtil();
	
	@Test
	public void testIsReady(){
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(null, null, null, null, null, null, null);
		assertTrue(tfac.isReady(null));
		assertTrue(tfac.isReady(mock(CyNetwork.class)));
	}
	
	@Test
	public void testCreateTaskIteratorNetworkPassedInIsNull(){
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, null, null, null, null, null);
		
		TaskIterator ti = tfac.createTaskIterator(null);
		assertTrue(ti.next() instanceof DoNothingTask);
		
		verify(mockDialogUtil).showMessageDialog(any(),
				contains("network must be selected"), eq(AppUtils.APP_NAME),
				eq(JOptionPane.ERROR_MESSAGE));
	}
	
	@Test
	public void testCreateTaskIteratorNetworkLacksMemberListColumn(){
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, null, null, null, null, null);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
		
		verify(mockDialogUtil).showMessageDialog(any(),
				contains("with a node column named"), eq(AppUtils.APP_NAME),
				eq(JOptionPane.ERROR_MESSAGE));
	}
	
	@Test
	public void testCreateTaskIteratorGetParentNetworkReturnsNull() throws Exception {
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);
		when(mockParentNetworkDialog.createGUI(any())).thenReturn(false);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(null);
		
		
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, null, mockParentNetworkFinder,
				mockParentNetworkDialog, null, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
		
		Mockito.verifyNoInteractions(mockDialogUtil, mockSwing);
		
	}
	
	// test getParentNetwork dialog displayed 0 returned with network to remember
	@Test
	public void testGetParentNetworkDisplayDialogReturnZeroAndRememberNetwork() throws Exception {
		TallyDialog mockTallyDialog = mock(TallyDialog.class);
		
		when(mockTallyDialog.getColumnsToTally()).thenReturn(Arrays.asList(mock(CyColumn.class)));
		when(mockTallyDialog.createGUI(any())).thenReturn(false);
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(any(), any(), any(),anyInt(),anyInt(),
				any(), any(), any())).thenReturn(0);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNetwork parentNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(),
				"intcol", Integer.class, false, 0);
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(null);
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);
		when(mockParentNetworkDialog.createGUI(any())).thenReturn(true);
		when(mockParentNetworkDialog.rememberChoice()).thenReturn(true);
		
		CyNetworkUtil mockNetworkUtil = mock(CyNetworkUtil.class);
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, mockTallyDialog, mockParentNetworkFinder,
				mockParentNetworkDialog, mockNetworkUtil, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
		verify(mockDialogUtil).showOptionDialog(any(),any(),
				eq("Parent Network Chooser"), eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE), any(), any(), any());
		verify(mockNetworkUtil).updateHierarchySUID(any(), any());
	}


	// test getParentNetwork dialog displayed 0 returned do NOT remember network
	@Test
	public void testGetParentNetworkDisplayDialogReturnZeroNoRememberNetwork() throws Exception {
		TallyDialog mockTallyDialog = mock(TallyDialog.class);
		
		when(mockTallyDialog.getColumnsToTally()).thenReturn(Arrays.asList(mock(CyColumn.class)));
		when(mockTallyDialog.createGUI(any())).thenReturn(false);
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(any(), any(), any(),anyInt(),anyInt(),
				any(), any(), any())).thenReturn(0);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNetwork parentNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(),
				"intcol", Integer.class, false, 0);
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(null);
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);
		when(mockParentNetworkDialog.createGUI(any())).thenReturn(true);
		when(mockParentNetworkDialog.rememberChoice()).thenReturn(false);
		
		CyNetworkUtil mockNetworkUtil = mock(CyNetworkUtil.class);
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, mockTallyDialog, mockParentNetworkFinder,
				mockParentNetworkDialog, mockNetworkUtil, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
		verify(mockDialogUtil).showOptionDialog(any(),any(),
				eq("Parent Network Chooser"), eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE), any(), any(), any());
		Mockito.verifyNoInteractions(mockNetworkUtil);
	}
	
	// test getParentNetwork dialog displayed non zero returned
	@Test
	public void testGetParentNetworkDisplayDialogReturnNonZeroReturned() throws Exception {
		TallyDialog mockTallyDialog = mock(TallyDialog.class);
		
		when(mockTallyDialog.getColumnsToTally()).thenReturn(Arrays.asList(mock(CyColumn.class)));
		when(mockTallyDialog.createGUI(any())).thenReturn(false);
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(any(), any(), any(),anyInt(),anyInt(),
				any(), any(), any())).thenReturn(1);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNetwork parentNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(),
				"intcol", Integer.class, false, 0);
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(null);
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);
		when(mockParentNetworkDialog.createGUI(any())).thenReturn(true);
		when(mockParentNetworkDialog.rememberChoice()).thenReturn(false);
		
		CyNetworkUtil mockNetworkUtil = mock(CyNetworkUtil.class);
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, mockTallyDialog, mockParentNetworkFinder,
				mockParentNetworkDialog, mockNetworkUtil, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
		verify(mockDialogUtil).showOptionDialog(any(),any(),
				eq("Parent Network Chooser"), eq(JOptionPane.YES_NO_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE), any(), any(), any());
		Mockito.verifyNoInteractions(mockNetworkUtil);
	}
	
	@Test
	public void testCreateTaskIteratorTallyCreateGuiFalse() throws Exception {
		TallyDialog mockTallyDialog = mock(TallyDialog.class);
		when(mockTallyDialog.createGUI(any())).thenReturn(false);
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(any(), any(), any(),anyInt(),anyInt(),
				any(), any(), any())).thenReturn(1);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNetwork parentNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"intcol", Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"doublecol", Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"doublecol", Boolean.class, false, false);
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(Arrays.asList(parentNetwork));
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);			
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, mockTallyDialog, mockParentNetworkFinder,
				mockParentNetworkDialog, null, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
	}
	
	@Test
	public void testCreateTaskIteratorUserCanceledTask() throws Exception {
		TallyDialog mockTallyDialog = mock(TallyDialog.class);
		when(mockTallyDialog.createGUI(any())).thenReturn(true);
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(any(), any(), any(),anyInt(),anyInt(),
				any(), any(), any())).thenReturn(1);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNetwork parentNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"intcol", Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"doublecol", Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"doublecol", Boolean.class, false, false);
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(Arrays.asList(parentNetwork));
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);			
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, mockTallyDialog, mockParentNetworkFinder,
				mockParentNetworkDialog, null, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
	}
	
	@Test
	public void testCreateTaskIteratorSuccessExceptNoTallyColumns() throws Exception {
		TallyDialog mockTallyDialog = mock(TallyDialog.class);
		when(mockTallyDialog.getColumnsToTally()).thenReturn(null);
		when(mockTallyDialog.createGUI(any())).thenReturn(true);
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(any(), any(), any(),anyInt(),anyInt(),
				any(), any(), any())).thenReturn(0);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNetwork parentNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"intcol", Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"doublecol", Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				"doublecol", Boolean.class, false, false);
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(Arrays.asList(parentNetwork));
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);			
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, mockTallyDialog, mockParentNetworkFinder,
				mockParentNetworkDialog, null, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof DoNothingTask);
		verify(mockDialogUtil).showMessageDialog(any(),
				eq("No columns selected to tally"),
				eq(AppUtils.APP_NAME), eq(JOptionPane.ERROR_MESSAGE));
	}
	
	@Test
	public void testCreateTaskIteratorSuccess() throws Exception {
		TallyDialog mockTallyDialog = mock(TallyDialog.class);
		
		when(mockTallyDialog.getColumnsToTally()).thenReturn(Arrays.asList(mock(CyColumn.class)));
		when(mockTallyDialog.createGUI(any())).thenReturn(true);
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(any(), any(), any(),anyInt(),anyInt(),
				any(), any(), any())).thenReturn(0);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		CyNetworkManager mockNetworkManager = mock(CyNetworkManager.class);
		when(mockNetworkManager.getNetworkSet()).thenReturn(null);
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNetwork parentNetwork = _nts.getNetwork();
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(),
				"intcol", Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(),
				"doublecol", Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(),
				"boolcol", Boolean.class, false, false);
		
		ParentNetworkFinder mockParentNetworkFinder = mock(ParentNetworkFinder.class);
		when(mockParentNetworkFinder.findParentNetworks(any(), any())).thenReturn(Arrays.asList(parentNetwork));
		ParentNetworkChooserDialog mockParentNetworkDialog = mock(ParentNetworkChooserDialog.class);			
		
		TallyAttributesTaskFactoryImpl tfac = new TallyAttributesTaskFactoryImpl(mockSwing,
				mockDialogUtil, mockTallyDialog, mockParentNetworkFinder,
				mockParentNetworkDialog, null, mockNetworkManager);
		
		TaskIterator ti = tfac.createTaskIterator(hierarchyNetwork);
		assertTrue(ti.next() instanceof TallyTask);
	}
}
