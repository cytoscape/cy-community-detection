package org.cytoscape.app.communitydetection.edgelist;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.cytoscape.app.communitydetection.hierarchy.HierarchyHelper;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to create a hierarchy network from an edge list received in form of
 * input stream. Implements {@link AbstractCyNetworkReader}.
 *
 */
public class ReaderTask extends AbstractCyNetworkReader {

	private final CyNetworkManager networkManager;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyNetworkViewManager networkViewManager;
	private final VisualMappingManager visualMappingManager;
	private final LoadVizmapFileTaskFactory vizmapFileTaskFactory;
	private final CyLayoutAlgorithmManager layoutManager;
	private final SynchronousTaskManager<?> syncTaskManager;
	private final CyNetworkNaming networkNaming;
	private final CyNetwork originalNetwork;

	public ReaderTask(InputStream inputStream, CyNetworkViewFactory networkViewFactory, CyNetworkFactory networkFactory,
			CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
			CyRootNetworkManager rootNetworkManager, VisualMappingManager visualMappingManager,
			LoadVizmapFileTaskFactory vizmapFileTaskFactory, CyLayoutAlgorithmManager layoutManager,
			SynchronousTaskManager<?> syncTaskManager, CyNetworkNaming networkNaming, Long originalNetworkSUID) {
		super(inputStream, networkViewFactory, networkFactory, networkManager, rootNetworkManager);
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.rootNetworkManager = rootNetworkManager;
		this.visualMappingManager = visualMappingManager;
		this.vizmapFileTaskFactory = vizmapFileTaskFactory;
		this.layoutManager = layoutManager;
		this.syncTaskManager = syncTaskManager;
		this.networkNaming = networkNaming;
		this.originalNetwork = networkManager.getNetwork(originalNetworkSUID);
	}

	/**
	 * Overrides AbstractCyNetworkReader's method to create the same visual style as
	 * that of the original interaction network.
	 *
	 */
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		try {
			VisualStyle hierarchyStyle = null;
			for (VisualStyle style : visualMappingManager.getAllVisualStyles()) {
				if (style.getTitle().equalsIgnoreCase("CD_Hierarchy")) {
					hierarchyStyle = style;
					break;
				}
			}
			if (hierarchyStyle == null) {
				ClassLoader classLoader = getClass().getClassLoader();
				InputStream resourceStream = classLoader.getResourceAsStream("/styles/cd_hierarchy.xml");
				File styleFile = File.createTempFile("cd_hierarchy", "xml");
				Files.copy(resourceStream, styleFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Style set size: " + vizmapFileTaskFactory.loadStyles(styleFile).size());
				hierarchyStyle = vizmapFileTaskFactory.loadStyles(styleFile).iterator().next();
				styleFile.delete();
			}
			CyNetworkView networkView = cyNetworkViewFactory.createNetworkView(network);
			visualMappingManager.setVisualStyle(hierarchyStyle, networkView);
			hierarchyStyle.apply(networkView);
			CyLayoutAlgorithm layout = layoutManager.getDefaultLayout();
			TaskIterator layoutTasks = layout.createTaskIterator(networkView, layout.createLayoutContext(),
					CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
			syncTaskManager.execute(layoutTasks);
			networkViewManager.addNetworkView(networkView);
			return networkView;
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return null;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		try {
			loadNetwork(taskMonitor);
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	public void setNetworkName(String algorithmName, String attribute) {
		String name;
		if (attribute.equals(AppUtils.TYPE_NONE)) {
			name = networkNaming.getSuggestedNetworkTitle(
					algorithmName + "_" + originalNetwork.getRow(originalNetwork).get(CyNetwork.NAME, String.class));
		} else {
			name = networkNaming.getSuggestedNetworkTitle(algorithmName + "_" + attribute + "_"
					+ originalNetwork.getRow(originalNetwork).get(CyNetwork.NAME, String.class));
		}

		CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(getNetworks()[0]);
		rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, name);
		getNetworks()[0].getRow(getNetworks()[0]).set(CyNetwork.NAME, name);
	}

	/**
	 * Creates the hierarchy network and populates all of its tables.
	 * 
	 * @param taskMonitor
	 * @throws Exception
	 */
	private final void loadNetwork(TaskMonitor taskMonitor) throws Exception {

		CyNetwork newNetwork = cyNetworkFactory.createNetwork();

		if (newNetwork.getDefaultNetworkTable().getColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK) == null) {
			newNetwork.getDefaultNetworkTable().createColumn(AppUtils.COLUMN_CD_ORIGINAL_NETWORK, Long.class, false,
					originalNetwork.getSUID());
		}
		if (newNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) == null) {
			newNetwork.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_MEMBER_LIST, String.class, false, null);
		}
		if (newNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE) == null) {
			newNetwork.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, Integer.class, false, 0);
		}
		if (newNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE) == null) {
			newNetwork.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, Double.class, false,
					0.0);
		}
		if (newNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME) == null) {
			newNetwork.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME, String.class, false, null);
		}
		if (newNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS) == null) {
			newNetwork.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, String.class, false,
					null);
		}
		if (newNetwork.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_LABELED) == null) {
			newNetwork.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_LABELED, Boolean.class, true, false);
		}

		Map<Long, CyNode> nMap = new HashMap<Long, CyNode>();
		String line;
		final BufferedReader br = new BufferedReader(
				new InputStreamReader(inputStream, Charset.forName("UTF-8").newDecoder()), 128 * 1024);
		while ((line = br.readLine()) != null) {
			if (line.trim().length() <= 0)
				continue;
			final String[] parts = line.split(AppUtils.EDGE_LIST_SPLIT_PATTERN);
			if (parts.length >= 3) {
				long sourceSUID = Long.parseLong(parts[0]);
				long targetSUID = Long.parseLong(parts[1]);
				String[] interaction = parts[parts.length - 1].split("-");

				if (interaction[0].equalsIgnoreCase("c")) {
					CyNode sourceNode = nMap.get(sourceSUID);
					if (sourceNode == null) {
						sourceNode = newNetwork.addNode();
						String sourceName = "C" + sourceSUID;
						newNetwork.getRow(sourceNode).set(CyNetwork.NAME, sourceName);
						nMap.put(sourceSUID, sourceNode);
					}
				}
				if (interaction[1].equalsIgnoreCase("c")) {
					CyNode targetNode = nMap.get(targetSUID);
					if (targetNode == null) {
						targetNode = newNetwork.addNode();
						String targetName = "C" + targetSUID;
						newNetwork.getRow(targetNode).set(CyNetwork.NAME, targetName);
						nMap.put(targetSUID, targetNode);
					}
					CyEdge edge = newNetwork.addEdge(nMap.get(sourceSUID), targetNode, true);
					newNetwork.getRow(edge).set(CyEdge.INTERACTION, parts[parts.length - 1]);
					HierarchyHelper.getInstance().addChildNode(nMap.get(sourceSUID), targetNode);
				} else {
					HierarchyHelper.getInstance().addMemberNode(originalNetwork.getNode(targetSUID));
					HierarchyHelper.getInstance().addChildNode(nMap.get(sourceSUID),
							originalNetwork.getNode(targetSUID));
				}
			} else {
				JOptionPane.showMessageDialog(null, "Incorrect number of columns!");
			}
			// Cancel called. Clean up the garbage.
			if (cancelled) {
				nMap.clear();
				br.close();
				return;
			}
		}
		nMap.clear();
		br.close();

		this.networks = new CyNetwork[] { newNetwork };
		networkManager.addNetwork(newNetwork);
		createMemberList(newNetwork);
	}

	/**
	 * Creates member list for each node in the hierarchy network.
	 * 
	 * @param hierarchyNetwork
	 */
	private void createMemberList(CyNetwork hierarchyNetwork) {
		for (CyNode node : hierarchyNetwork.getNodeList()) {
			List<CyNode> memberNodes = HierarchyHelper.getInstance().getMemberList(hierarchyNetwork, node).stream()
					.collect(Collectors.toList());
			Collections.sort(memberNodes, new Comparator<CyNode>() {
				@Override
				public int compare(CyNode node1, CyNode node2) {
					return originalNetwork.getRow(node1).get(CyNetwork.NAME, String.class)
							.compareTo(originalNetwork.getRow(node2).get(CyNetwork.NAME, String.class));
				}
			});
			StringBuffer memberList = new StringBuffer();
			for (CyNode memberNode : memberNodes) {
				if (memberList.length() > 0) {
					memberList.append(" ");
				}
				String name = originalNetwork.getRow(memberNode).get(CyNetwork.NAME, String.class);
				memberList.append(name);
			}
			if (memberList != null) {
				hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST, memberList.toString());
				hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE, memberNodes.size());
				hierarchyNetwork.getRow(node).set(AppUtils.COLUMN_CD_MEMBER_LIST_LOG_SIZE, log2(memberNodes.size()));
			}
		}
		HierarchyHelper.getInstance().clearAll();
	}

	private double log2(double x) {
		return (Math.log(x) / Math.log(2));
	}
}
