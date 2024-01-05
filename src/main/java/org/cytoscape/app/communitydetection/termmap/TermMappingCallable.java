package org.cytoscape.app.communitydetection.termmap;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
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

	protected String getRawMemberList(){
		return _network.getRow(_node).get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class);
	}
	
	protected String getCommaDelimitedMemberList(final String rawMemberList){
		return rawMemberList.replaceAll(AppUtils.CD_MEMBER_LIST_DELIMITER, ",");
	}
	
	protected String getNonIntersectingMemberList(HashSet<String> intersectingMembers, final String rawMemberList){
		
		if (rawMemberList == null || intersectingMembers == null ||
				rawMemberList.trim().isEmpty() || intersectingMembers.isEmpty()){
			return "";
		}
		
		StringBuilder nonIntersectingMemberList = new StringBuilder();
		int counter = 0;
		for (String term : rawMemberList.split(AppUtils.CD_MEMBER_LIST_DELIMITER)){
			if (intersectingMembers.contains(term)){
				continue;
			}
			if (counter > 0){
				nonIntersectingMemberList.append(" ");
			}
			nonIntersectingMemberList.append(term);
			counter++;
		}
		return nonIntersectingMemberList.toString();
	}
	
	protected String getAnnotatedAlgorithmString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Annotated by ");
		
		sb.append(_algorithm.getDisplayName());
		sb.append(" [Docker: ");
		sb.append(_algorithm.getDockerImage());
		sb.append("] {");
		if (_customParameters != null && _customParameters.isEmpty() == false){
			sb.append(_customParameters.toString());
		} else {
			sb.append("default parameters");
		}
		sb.append("} via ");
		sb.append(PropertiesHelper.getInstance().getAppName());
		sb.append(" Cytoscape App (");
		sb.append(PropertiesHelper.getInstance().getAppVersion());
		sb.append(")");
		
		return sb.toString();
	}
	
	@Override
	public Boolean call() throws Exception {
		if (CDRestClient.getInstance().getIsTaskCanceled()) {
			return false;
		}
		String rawMemberList = getRawMemberList();
		String memberList = getCommaDelimitedMemberList(rawMemberList);
		String taskId = CDRestClient.getInstance().postCDData(_algorithm.getName(),
			_customParameters, memberList);
		if (taskId == null) {
			return false;
		}
		CommunityDetectionResult cdResult = CDRestClient.getInstance().getCDResult(taskId,
				PropertiesHelper.getInstance().getFunctionalEnrichmentTimeoutMillis());
		String name = AppUtils.TYPE_NONE_VALUE;
		StringBuilder annotatedList = new StringBuilder();
		int counter = 0;
		
		// Set pvalue to 1.0 and jaccard to 0.0 since NaNs 
		// are not allowed in CX or CX2 
		// Fix for UD-2650
		double pvalue = 1.0;
		double jaccard = 0.0;
		boolean jaccardSet = false;
		String sourcedb = null;
		String sourceterm = null;
		String term = null;
		HashSet<String> intersectedTermsHash = null;
		if (cdResult != null && cdResult.getResult() != null && cdResult.getResult().size() > 0) {
			name = cdResult.getResult().get("name").asText(name);
			pvalue = cdResult.getResult().get("p_value").asDouble();
			if (cdResult.getResult().has("jaccard")){
				jaccard = cdResult.getResult().get("jaccard").asDouble();
				jaccardSet = true;
			}
			if (cdResult.getResult().has("source")){
				sourcedb = cdResult.getResult().get("source").asText();
			}
			if (cdResult.getResult().has("sourceTermId")){
				sourceterm = cdResult.getResult().get("sourceTermId").asText();
			}
			intersectedTermsHash = new HashSet<>();
			if (cdResult.getResult().get("intersections").size() > 0) {
				Iterator<JsonNode> iterator = cdResult.getResult().get("intersections").elements();
				while (iterator.hasNext()) {
					if (counter > 0) {
						annotatedList.append(" ");
					}
					term = iterator.next().asText();
					annotatedList.append(term);
					intersectedTermsHash.add(term);
					counter++;
				}
			}
		}
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_SOURCE, sourcedb);
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_SOURCE_TERM, sourceterm);
		_network.getRow(_node).set(AppUtils.COLUMN_CD_COMMUNITY_NAME, name);
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_ALGORITHM, getAnnotatedAlgorithmString());
		_network.getRow(_node).set(AppUtils.COLUMN_CD_NONANNOTATED_MEMBERS, 
				getNonIntersectingMemberList(intersectedTermsHash, rawMemberList));
		
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS, annotatedList.toString());
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_MEMBERS_SIZE, counter);
		_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_PVALUE, pvalue);
		double overlap = 0.0;
		double inputGeneSize = _network.getRow(_node).get(AppUtils.COLUMN_CD_MEMBER_LIST_SIZE,
			Integer.class);
		
		if (inputGeneSize > 0){
		    overlap = (double)counter/inputGeneSize;
		}
		if (jaccardSet == true){
			_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP, jaccard);
		} else {
			BigDecimal bd = new BigDecimal(Double.toString(overlap));
			BigDecimal roundbd = bd.setScale(3, RoundingMode.HALF_UP);
			_network.getRow(_node).set(AppUtils.COLUMN_CD_ANNOTATED_OVERLAP, roundbd.doubleValue());
		}
		if (name != AppUtils.TYPE_NONE_VALUE) {
			_network.getRow(_node).set(AppUtils.COLUMN_CD_LABELED, true);
		}
		return true;
	}

}
