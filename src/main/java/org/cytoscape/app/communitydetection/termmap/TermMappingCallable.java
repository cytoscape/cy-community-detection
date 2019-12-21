package org.cytoscape.app.communitydetection.termmap;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

import com.fasterxml.jackson.databind.JsonNode;

public class TermMappingCallable implements Callable<Boolean> {

	private final String algorithm;
	private final CyNetwork network;
	private final CyNode node;

	public TermMappingCallable(String algorithm, CyNetwork network, CyNode node) {
		this.algorithm = algorithm;
		this.network = network;
		this.node = node;
	}

	@Override
	public Boolean call() throws Exception {
		if (CDRestClient.getInstance().getIsTaskCanceled()) {
			return false;
		}
		String memberList = network.getRow(node).get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class)
				.replaceAll(AppUtils.CD_MEMBER_LIST_DELIMITER, ",");
		String URI = CDRestClient.getInstance().postCDData(algorithm, null, memberList);
		CommunityDetectionResult cdResult = CDRestClient.getInstance().getCDResult(URI, 300);
		String name = AppUtils.TYPE_NONE_VALUE;
		String annotatedList = "";
		int counter = 0;
		double pvalue = Double.NaN;
		if (cdResult != null && cdResult.getResult() != null && cdResult.getResult().size() > 0) {
			name = cdResult.getResult().get("name").asText(name);
			pvalue = cdResult.getResult().get("p_value").asDouble();
			if (cdResult.getResult().get("intersections").size() > 0) {
				Iterator<JsonNode> iterator = cdResult.getResult().get("intersections").elements();
				while (iterator.hasNext()) {
					if (!annotatedList.isEmpty()) {
						annotatedList += " ";
					}
					annotatedList += iterator.next().asText();
					counter++;
				}
			}
		}
		network.getRow(node).set(AppUtils.COLUMN_CD_COMMUNITY_NAME, name);
		network.getRow(node).set(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, annotatedList);
		network.getRow(node).set(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE, counter);
		network.getRow(node).set(AppUtils.COLUMN_CD_ANNOTATED_PVALUE, pvalue);
		double overlap = 0.0;
		double inputGeneSize = network.getRow(node).get(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE,
			Integer.class);
		
		if (inputGeneSize > 0){
		    overlap = (double)counter/inputGeneSize;
		}
		network.getRow(node).set(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP, overlap);
		if (name != AppUtils.TYPE_NONE_VALUE) {
			network.getRow(node).set(AppUtils.COLUMN_CD_LABELED, true);
		}
		return true;
	}

}
