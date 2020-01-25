package org.cytoscape.app.communitydetection.termmap;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

public class TermMappingCallable implements Callable<Boolean> {

	private final CommunityDetectionAlgorithm _algorithm;
	private final Map<String, String> _customParameters;
	private final CyNetwork _network;
	private final CyNode _node;

	public TermMappingCallable(CyNetwork network, CommunityDetectionAlgorithm algorithm,
		Map<String,String> customParameters, CyNode node) {
		this._algorithm = algorithm;
		this._network = network;
		this._customParameters = customParameters;
		this._node = node;
	}

	@Override
	public Boolean call() throws Exception {
		if (CDRestClient.getInstance().getIsTaskCanceled()) {
			return false;
		}
		String memberList = _network.getRow(_node).get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class)
				.replaceAll(AppUtils.CD_MEMBER_LIST_DELIMITER, ",");
		String taskId = CDRestClient.getInstance().postCDData(_algorithm.getName(),
			_customParameters, memberList);
		if (taskId == null) {
			return false;
		}
		CommunityDetectionResult cdResult = CDRestClient.getInstance().getCDResult(taskId, PropertiesHelper.getInstance().getFunctionalEnrichmentTimeoutMillis());
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
		_network.getRow(_node).set(AppUtils.COLUMN_CD_COMMUNITY_NAME, name);
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, annotatedList);
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE, counter);
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_PVALUE, pvalue);
		double overlap = 0.0;
		double inputGeneSize = _network.getRow(_node).get(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE,
			Integer.class);
		
		if (inputGeneSize > 0){
		    overlap = (double)counter/inputGeneSize;
		}
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP, overlap);
		if (name != AppUtils.TYPE_NONE_VALUE) {
			_network.getRow(_node).set(AppUtils.COLUMN_CD_LABELED, true);
		}
		return true;
	}

}
