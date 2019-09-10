package org.cytoscape.app.communitydetection.termmap;

import java.util.Iterator;
import java.util.List;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

import com.fasterxml.jackson.databind.JsonNode;

public class TermMappingTask extends AbstractTask {

	private final String algorithm;
	private final CyNetwork network;
	private final boolean isContextMenu;

	public TermMappingTask(String algorithm, CyNetwork network, boolean isContextMenu) {
		this.algorithm = algorithm;
		this.network = network;
		this.isContextMenu = isContextMenu;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Community Detection: Term Mapping");
		taskMonitor.setProgress(0.0);
		taskMonitor.setStatusMessage("Running " + AppUtils.TERM_MAPPING_ALGORITHMS.get(algorithm));
		if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME) == null) {
			network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_COMMUNITY_NAME, String.class, false, null);
		}
		if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS) == null) {
			network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, String.class, false, null);
		}
		if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_LABELED) == null) {
			network.getDefaultNodeTable().createColumn(AppUtils.COLUMN_CD_LABELED, Boolean.class, true, false);
		}
		float progress = 0;
		List<CyNode> selectedNodes;
		if (isContextMenu) {
			selectedNodes = CyTableUtil.getSelectedNodes(network);
			if (selectedNodes.size() == 0) {
				return;
			}
		} else {
			selectedNodes = network.getNodeList();
		}
		for (CyNode node : selectedNodes) {
			progress = progress + 1;
			taskMonitor.setProgress(progress / selectedNodes.size());
			String memberList = network.getRow(node).get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class)
					.replaceAll(AppUtils.CD_MEMBER_LIST_DELIMITER, ",");
			String URI = CDRestClient.getInstance().postCDData(algorithm, true, memberList);
			CommunityDetectionResult cdResult = CDRestClient.getInstance().getCDResult(URI, 300);
			String name = AppUtils.TYPE_NONE_VALUE;
			String annotatedList = "";
			if (cdResult != null && cdResult.getResult() != null && cdResult.getResult().size() > 0) {
				name = cdResult.getResult().get("name").asText(name);
				if (cdResult.getResult().get("intersections").size() > 0) {
					Iterator<JsonNode> iterator = cdResult.getResult().get("intersections").elements();
					while (iterator.hasNext()) {
						if (!annotatedList.isEmpty()) {
							annotatedList += " ";
						}
						annotatedList += iterator.next().asText();
					}
				}
			}
			network.getRow(node).set(AppUtils.COLUMN_CD_COMMUNITY_NAME, name);
			network.getRow(node).set(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, annotatedList);
			if (name != AppUtils.TYPE_NONE_VALUE) {
				network.getRow(node).set(AppUtils.COLUMN_CD_LABELED, true);
			}

			if (cancelled) {
				CDRestClient.getInstance().setTaskCanceled(false);
				return;
			}
		}
	}

	@Override
	public void cancel() {
		CDRestClient.getInstance().setTaskCanceled(true);
		super.cancel();
	}
}
