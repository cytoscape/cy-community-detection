package org.cytoscape.app.communitydetection.event;

/**
 * Event to denote that the Baseurl in PropertiesHelper
 * has changed. Interested objects should implement
 * this interface
 * @author churas
 */
public interface BaseurlUpdatedListener {
	
	
	/**
	 * Fired when REST server URL is updated
	 * @param event 
	 */
	public void urlUpdatedEvent(BaseurlUpdatedEvent event);
}
