package org.cytoscape.app.communitydetection.subnetwork;

import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class ParentNetworkFinderException extends CommunityDetectionException {
	
	public ParentNetworkFinderException(String message){
		super(message);
	}
}
