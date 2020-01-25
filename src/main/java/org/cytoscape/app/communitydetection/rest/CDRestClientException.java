package org.cytoscape.app.communitydetection.rest;

import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;

/**
 * The {@link #CDRestClientException} class is used to
 * indicate that an exceptional condition has occurred
 * with interaction with the Community Detection REST 
 * service
 * 
 * @author churas
 */
public class CDRestClientException extends Exception {
	
	private ErrorResponse errorResponse;
	private CommunityDetectionResult result;
	
	public CDRestClientException(String message){
		super(message);
	}
	
	public CDRestClientException(String message, ErrorResponse errorResponse){
		super(message);
		this.errorResponse = errorResponse;
	}
	
	public CDRestClientException(String message, CommunityDetectionResult result){
		super(message);
		this.result = result;
	}
	
	public ErrorResponse getErrorResponse(){
		return this.errorResponse;
	}
	
	public CommunityDetectionResult getResult(){
		return this.result;
	}
	
}
