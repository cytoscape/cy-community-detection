package org.cytoscape.app.communitydetection.event;

/**
 * Event denoting a change in REST server URL
 * @author churas
 */
public class BaseurlUpdatedEvent {
	
	private String _oldURL;
	private String _newURL;
	
	/**
	 * Constructor
	 * @param oldURL the old REST server URL
	 * @param newURL the new REST server URL
	 */
	public BaseurlUpdatedEvent(final String oldURL, final String newURL){
		_oldURL = oldURL;
		_newURL = newURL;
	}

	/**
	 * Gets old REST server URL
	 * @return 
	 */
	public String getOldURL() {
		return _oldURL;
	}

	/**
	 * Gets new REST server URL
	 * @return 
	 */
	public String getNewURL() {
		return _newURL;
	}
}
