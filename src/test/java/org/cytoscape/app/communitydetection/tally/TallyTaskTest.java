package org.cytoscape.app.communitydetection.tally;

import java.util.ArrayList;
import java.util.List;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.work.TaskMonitor;

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class TallyTaskTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();
	private CyNetworkUtil _cyNetworkUtil = new CyNetworkUtil();
	
	@Test
	public void testRunTaskAlreadyCanceled() throws Exception {
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		TallyTask tt = new TallyTask(null, null, null, null);
		tt.cancel();
		tt.run(mockMonitor);
		verify(mockMonitor).setTitle("Community Detection: Tally columns on hierarchy network");
		verify(mockMonitor).setProgress(0.0);
		verify(mockMonitor).setStatusMessage("User cancelled Task");
	}
	
	@Test
	public void testRunWithExistingTallyColumnsAllUnmatched() throws Exception {
		CyNetwork parentNetwork = _nts.getNetwork();
		
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(), "foo", Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(), "bar", Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(), "gee", Boolean.class, false, false);

		// add three nodes
		CyNode nodeOne = parentNetwork.addNode();
		parentNetwork.getRow(nodeOne).set(CyNetwork.NAME, "nodeOne");
		
		CyNode nodeTwo = parentNetwork.addNode();
		parentNetwork.getRow(nodeTwo).set(CyNetwork.NAME, "nodeTwo");
		
		CyNode nodeThree = parentNetwork.addNode();
		parentNetwork.getRow(nodeThree).set(CyNetwork.NAME, "nodeThree");

		List<CyColumn> tallyColumns = new ArrayList<>();
		
		tallyColumns.add(parentNetwork.getDefaultNodeTable().getColumn("foo"));
		tallyColumns.add(parentNetwork.getDefaultNodeTable().getColumn("bar"));
		tallyColumns.add(parentNetwork.getDefaultNodeTable().getColumn("gee"));
		
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_TALLY_NAMESPACE, "foo", String.class, false, "");
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_TALLY_NAMESPACE, "bar", String.class, false, "");
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_TALLY_NAMESPACE, "gee", String.class, false, "");
		
		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNode hNodeOne = hierarchyNetwork.addNode();
		hierarchyNetwork.getRow(hNodeOne).set(AppUtils.COLUMN_CD_MEMBER_LIST,
				"nodeOne nodeTwo nodeThree");
		
		CyNode hNodeTwo = hierarchyNetwork.addNode();
		hierarchyNetwork.getRow(hNodeTwo).set(AppUtils.COLUMN_CD_MEMBER_LIST, null);
		
		CyNode hNodeThree = hierarchyNetwork.addNode();
		hierarchyNetwork.getRow(hNodeThree).set(AppUtils.COLUMN_CD_MEMBER_LIST,
				"nodeTwo nodeFour");
		
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		TallyTask tt = new TallyTask(_cyNetworkUtil, parentNetwork, hierarchyNetwork, tallyColumns);
		tt.run(mockMonitor);	
		
		// check hierarchy node 1
		Integer val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
							"foo", Integer.class);
		
		assertEquals(0, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"bar", Integer.class);
		assertEquals(0, val.intValue());
		val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"gee", Integer.class);
		assertEquals(0, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						AppUtils.COLUMN_CD_UNMATCHED, Integer.class);
		assertEquals(3, val.intValue());
		
		// check hierarchy node 2
		val = hierarchyNetwork.getRow(hNodeTwo).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
							"foo", Integer.class);
		
		assertEquals(0, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeTwo).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"bar", Integer.class);
		assertEquals(0, val.intValue());
		val = hierarchyNetwork.getRow(hNodeTwo).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"gee", Integer.class);
		assertEquals(0, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeTwo).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						AppUtils.COLUMN_CD_UNMATCHED, Integer.class);
		assertEquals(0, val.intValue());
		
		// check hierarchy node 3
		val = hierarchyNetwork.getRow(hNodeThree).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
							"foo", Integer.class);
		
		assertEquals(0, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeThree).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"bar", Integer.class);
		assertEquals(0, val.intValue());
		val = hierarchyNetwork.getRow(hNodeThree).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"gee", Integer.class);
		assertEquals(0, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeThree).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						AppUtils.COLUMN_CD_UNMATCHED, Integer.class);
		assertEquals(2, val.intValue());
		
		
	}
	
	@Test
	public void testRunWithMatches() throws Exception {
		CyNetwork parentNetwork = _nts.getNetwork();
		
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(), "foo", Integer.class, false, 0);
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(), "bar", Double.class, false, 0.0);
		_cyNetworkUtil.createTableColumn(parentNetwork.getDefaultNodeTable(), "gee", Boolean.class, false, false);

		// add three nodes
		CyNode nodeOne = parentNetwork.addNode();
		parentNetwork.getRow(nodeOne).set(CyNetwork.NAME, "nodeOne");
		parentNetwork.getRow(nodeOne).set("foo", 3);
		
		CyNode nodeTwo = parentNetwork.addNode();
		parentNetwork.getRow(nodeTwo).set(CyNetwork.NAME, "nodeTwo");
		parentNetwork.getRow(nodeTwo).set("bar", 0.7);
		
		CyNode nodeThree = parentNetwork.addNode();
		parentNetwork.getRow(nodeThree).set(CyNetwork.NAME, "nodeThree");
		parentNetwork.getRow(nodeThree).set("gee", true);

		List<CyColumn> tallyColumns = new ArrayList<>();
		
		tallyColumns.add(parentNetwork.getDefaultNodeTable().getColumn("foo"));
		tallyColumns.add(parentNetwork.getDefaultNodeTable().getColumn("bar"));
		tallyColumns.add(parentNetwork.getDefaultNodeTable().getColumn("gee"));
		
		CyNetwork hierarchyNetwork = _nts.getNetwork();

		_cyNetworkUtil.createTableColumn(hierarchyNetwork.getDefaultNodeTable(),
				AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, "");
		
		CyNode hNodeOne = hierarchyNetwork.addNode();
		hierarchyNetwork.getRow(hNodeOne).set(AppUtils.COLUMN_CD_MEMBER_LIST,
				"nodeOne nodeTwo nodeThree");
		
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		TallyTask tt = new TallyTask(_cyNetworkUtil, parentNetwork, hierarchyNetwork, tallyColumns);
		tt.run(mockMonitor);	
		
		// check hierarchy node 1
		Integer val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
							"foo", Integer.class);
		
		assertEquals(1, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"bar", Integer.class);
		assertEquals(1, val.intValue());
		val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						"gee", Integer.class);
		assertEquals(1, val.intValue());
		
		val = hierarchyNetwork.getRow(hNodeOne).get(AppUtils.COLUMN_CD_TALLY_NAMESPACE,
						AppUtils.COLUMN_CD_UNMATCHED, Integer.class);
		assertEquals(0, val.intValue());
		
	}
}
