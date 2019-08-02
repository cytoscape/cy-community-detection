package org.cytoscape.app.communitydetection.edge;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;

public class EdgeListReader extends AbstractCyNetworkReader {

	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyRootNetworkManager rootNetworkManager;
	private final VisualMappingManager visualMappingManager;
	private final String collectionName;

	public EdgeListReader(final InputStream inputStream, final CyNetworkViewFactory networkViewFactory,
			final CyNetworkFactory networkFactory, final CyNetworkManager networkManager,
			final CyNetworkViewManager networkViewManager, final CyRootNetworkManager rootNetworkManager,
			final VisualMappingManager visualMappingManager, final String collectionName) {
		super(inputStream, networkViewFactory, networkFactory, networkManager, rootNetworkManager);
		this.collectionName = collectionName;
		this.rootNetworkManager = rootNetworkManager;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.visualMappingManager = visualMappingManager;
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {

		CyNetwork originalNetwork = getRootNetworkByName(collectionName).getSubNetworkList().get(0);
		CyNetworkView originalNetworkView = null;
		for (CyNetworkView netView : networkViewManager.getNetworkViews(originalNetwork)) {
			if (originalNetworkView == null) {
				originalNetworkView = netView;
				break;
			}
		}
		CyNetworkView networkView = cyNetworkViewFactory.createNetworkView(network);
		VisualStyle style = visualMappingManager.getVisualStyle(originalNetworkView);
		visualMappingManager.setVisualStyle(style, networkView);
		style.apply(networkView);
		networkView.fitContent();
		networkViewManager.addNetworkView(networkView);
		return networkView;
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

	private final void loadNetwork(TaskMonitor taskMonitor) throws Exception {

		CySubNetwork subNetwork = null;
		CyRootNetwork rootNetwork = getRootNetwork();
		if (rootNetwork != null) {
			subNetwork = rootNetwork.addSubNetwork();
		} else {
			rootNetwork = getRootNetworkByName(collectionName);
			if (rootNetwork == null) {
				subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
			} else {
				subNetwork = rootNetwork.addSubNetwork();
			}
		}
		if (subNetwork.getDefaultEdgeTable().getColumn(AppUtils.SOURCE) == null)
			subNetwork.getDefaultEdgeTable().createColumn(AppUtils.SOURCE, String.class, true);
		if (subNetwork.getDefaultEdgeTable().getColumn(AppUtils.TARGET) == null)
			subNetwork.getDefaultEdgeTable().createColumn(AppUtils.TARGET, String.class, true);

		Map<Long, CyNode> nMap = new HashMap<Long, CyNode>();
		String line;
		final BufferedReader br = new BufferedReader(
				new InputStreamReader(inputStream, Charset.forName("UTF-8").newDecoder()), 128 * 1024);
		while ((line = br.readLine()) != null) {
			if (line.trim().length() <= 0)
				continue;
			final String[] parts = AppUtils.SPLIT_PATTERN.split(line);

			if (parts.length >= 2) {

				long sourceSUID = Long.parseLong(parts[0]);
				String sourceName = "Term" + sourceSUID;
				long targetSUID = Long.parseLong(parts[1]);
				String targetName = "Term" + targetSUID;

				CyNode sourceNode = nMap.get(sourceSUID);
				if (sourceNode == null) {
					sourceNode = rootNetwork.getNode(sourceSUID);
					if (sourceNode == null) {
						sourceNode = subNetwork.addNode();
						rootNetwork.getRow(sourceNode).set(CyNetwork.SUID, sourceSUID);
						rootNetwork.getDefaultNodeTable().getRow(sourceSUID).set("name", sourceName);
					} else {
						subNetwork.addNode(sourceNode);
						sourceName = getSharedNameFromNodeTable(rootNetwork, sourceNode);
					}
					nMap.put(sourceSUID, sourceNode);
					subNetwork.getRow(sourceNode).set(CyNetwork.SUID, sourceSUID);
					subNetwork.getDefaultNodeTable().getRow(sourceSUID).set("name", sourceName);
				}

				CyNode targetNode = nMap.get(targetSUID);
				if (targetNode == null) {
					targetNode = rootNetwork.getNode(targetSUID);
					if (targetNode == null) {
						targetNode = subNetwork.addNode();
						rootNetwork.getRow(targetNode).set(CyNetwork.SUID, targetSUID);
						rootNetwork.getDefaultNodeTable().getRow(targetSUID).set("name", targetName);
					} else {
						subNetwork.addNode(targetNode);
						targetName = getSharedNameFromNodeTable(rootNetwork, targetNode);
					}
					nMap.put(targetSUID, targetNode);
					subNetwork.getRow(targetNode).set(CyNetwork.SUID, targetSUID);
					subNetwork.getDefaultNodeTable().getRow(targetSUID).set("name", targetName);
				}

				final CyEdge edge = subNetwork.addEdge(sourceNode, targetNode, true);
				subNetwork.getRow(edge).set(CyEdge.INTERACTION, "-");
				subNetwork.getRow(edge).set(AppUtils.SOURCE, sourceName);
				subNetwork.getRow(edge).set(AppUtils.TARGET, targetName);
			} else if (parts.length == 1) {
				// No edge will be added. Node only.
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
		this.networks = new CyNetwork[] { subNetwork };
	}

	private CyRootNetwork getRootNetworkByName(final String collectionName) {

		for (CyNetwork net : networkManager.getNetworkSet()) {
			final CyRootNetwork rootNet = rootNetworkManager.getRootNetwork(net);
			if (rootNet.getRow(rootNet).get(CyRootNetwork.NAME, String.class).equals(collectionName)) {
				return rootNet;
			}
		}
		return null;
	}

	private String getSharedNameFromNodeTable(final CyNetwork network, final CyNode node) {
		if (network == null || node == null) {
			return null;
		}
		final CyRow row = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS).getRow(node.getSUID());
		if (row != null) {
			final Object o = row.getRaw(AppUtils.SHARED_NAME_COL);
			if ((o != null) && (o instanceof String)) {
				return String.valueOf(o);
			}
		}
		return null;
	}

}
