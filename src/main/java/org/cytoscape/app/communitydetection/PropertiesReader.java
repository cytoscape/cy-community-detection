package org.cytoscape.app.communitydetection;

import java.util.Properties;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;

public class PropertiesReader extends AbstractConfigDirPropsReader implements PropertyUpdatedListener {

	private final String propName;

	public PropertiesReader(String name, String propFileName) {
		super(name, propFileName, CyProperty.SavePolicy.CONFIG_DIR);
		this.propName = name;
	}

	@Override
	public void handleEvent(PropertyUpdatedEvent e) {
		if (e.getSource().getName().equalsIgnoreCase(propName)) {
		    
			Properties props = (Properties)e.getSource().getProperties();
			String baseurl = props.getProperty(AppUtils.PROP_APP_BASEURL);
			
			PropertiesHelper pHelper = PropertiesHelper.getInstance();
			pHelper.setBaseurl(baseurl);
			
			String threadcount = props.getProperty(AppUtils.PROP_APP_THREADCOUNT);
			pHelper.setThreadcount(threadcount);
			
			// these properties are NOT exposed by default in the properties file
			// should they be?
			int httpSocketTimeout = getPropertyAsInt(props, AppUtils.PROP_HTTP_SOCKET_TIMEOUT, 10000); 
			pHelper.setHttpSocketTimeoutMillis(httpSocketTimeout);
			
			int httpConnectTimeout = getPropertyAsInt(props, AppUtils.PROP_HTTP_CONNECT_TIMEOUT, 10000); 
			pHelper.setHttpConnectTimeoutMillis(httpConnectTimeout);
			
			int httpConnectionRequestTimeout = getPropertyAsInt(props, AppUtils.PROP_HTTP_CONNECTION_REQUEST_TIMEOUT, 10000); 
			pHelper.setHttpConnectionRequestTimeoutMillis(httpConnectionRequestTimeout);
			
			int pollIntervalTime = getPropertyAsInt(props, AppUtils.PROP_POLL_INTERVAL_TIME, 1000); 
			pHelper.setPollingIntervalTimeMillis(pollIntervalTime);
			
			int cdTimeout = getPropertyAsInt(props, AppUtils.PROP_CD_TASK_TIMEOUT, 1800000);
			pHelper.setCommunityDetectionTimeoutMillis(cdTimeout);
			
			int feTimeout = getPropertyAsInt(props, AppUtils.PROP_FE_TASK_TIMEOUT, 1800000);
			pHelper.setFunctionalEnrichmentTimeoutMillis(feTimeout);
			
			int submitRetryCount = getPropertyAsInt(props, AppUtils.PROP_SUBMIT_RETRY_COUNT, 2);
			pHelper.setSubmitRetryCount(submitRetryCount);
		}
	}
	
	protected int getPropertyAsInt(Properties props, final String propertyName, int defaultValue){
		String propVal = props.getProperty(propertyName);
		if (propVal == null || propVal.trim().isEmpty()){
			return defaultValue;
		}	
		try {
			return Integer.parseInt(propVal);
		}
		catch(NumberFormatException nfe){
		}
		return defaultValue;
    }
}
