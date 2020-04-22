package org.cytoscape.app.communitydetection;

import java.util.Properties;
import org.cytoscape.app.communitydetection.hierarchy.LauncherDialog;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHelper {

	public final static String BASEURL_PREFIX = "http://";
	public final static String BASEURL_SUFFIX = "/cd/communitydetection/v1";
	public final static String DEFAULT_BASEURL = BASEURL_PREFIX
			+ "cdservice.cytoscape.org" + BASEURL_SUFFIX;
	private final static Logger LOGGER = LoggerFactory.getLogger(PropertiesHelper.class);

	private String baseurl;
	private String iQueryurl;
	private String threadcount;
	private String appName;
	private String appVersion;
	private int httpSocketTimeoutMillis;
	private int httpConnectTimeoutMillis;
	private int httpConnectionRequestTimeoutMillis;
	private int pollingIntervalTimeMillis;
	private int communityDetectionTimeoutMillis;
	private int functionalEnrichmentTimeoutMillis;
	private int submitRetryCount;

	private PropertiesHelper() {
	}

	private static class SingletonHelper {
		private static final PropertiesHelper INSTANCE = new PropertiesHelper();
	}

	/**
	 * Gets instance of this object
	 * @return 
	 */
	public static PropertiesHelper getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public void updateViaProperties(Properties props){
		if (props == null){
			LOGGER.warn("Properties passed in was null");
			props = new Properties();
		}
		LOGGER.debug("Updating properties");
		appName = props.getProperty(AppUtils.PROP_PROJECT_NAME, AppUtils.APP_NAME);
		appVersion = props.getProperty(AppUtils.PROP_PROJECT_VERSION, "Unknown");
		setBaseurl(props.getProperty(AppUtils.PROP_APP_BASEURL,
				PropertiesHelper.DEFAULT_BASEURL));
		setiQueryurl(props.getProperty(AppUtils.PROP_IQUERY_URL,
				"http://iquery.ndexbio.org"));
		setThreadcount(props.getProperty(AppUtils.PROP_APP_THREADCOUNT, "4"));
		setCommunityDetectionTimeoutMillis(getPropertyAsInt(props,
								AppUtils.PROP_CD_TASK_TIMEOUT, 1800000));
		setFunctionalEnrichmentTimeoutMillis(getPropertyAsInt(props,
								AppUtils.PROP_FE_TASK_TIMEOUT, 1800000));
		setSubmitRetryCount(getPropertyAsInt(props,
								AppUtils.PROP_SUBMIT_RETRY_COUNT, 2));
		setHttpSocketTimeoutMillis(getPropertyAsInt(props,
								AppUtils.PROP_HTTP_SOCKET_TIMEOUT, 10000));
		setHttpConnectTimeoutMillis(getPropertyAsInt(props,
								AppUtils.PROP_HTTP_CONNECT_TIMEOUT, 10000));
		setHttpConnectionRequestTimeoutMillis(getPropertyAsInt(props,
								AppUtils.PROP_HTTP_CONNECTION_REQUEST_TIMEOUT, 10000));
		setPollingIntervalTimeMillis(getPropertyAsInt(props,
								AppUtils.PROP_POLL_INTERVAL_TIME, 1000));		
	}
	
	/**
	 * Gets REST endpoint for CD Service
	 * @return 
	 */
	public String getBaseurl() {
		return baseurl;
	}

	/**
	 * Gets REST endpoint for CD Service with http?:// prefix and
	 * /.* suffix removed
	 * @return 
	 */
	public String getBaseurlHostNameOnly(){
		if (baseurl == null){
			return "";
		}
		return baseurl.replaceAll("^https?://", "").replaceAll("/.*$", "");
	}
	
	/**
	 * Sets REST endpoint for CD Service and sets
	 * LauncherDialog.ALGORITHM_ENDPOINT_UPDATED to true if previous
	 * or new baseurl is null or if the previous value is different then
	 * the current
	 * @param baseurl 
	 */
	public void setBaseurl(String baseurl) {
		if (this.baseurl == null || baseurl == null || !this.baseurl.equals(baseurl)){
			LauncherDialog.ALGORITHM_ENDPOINT_UPDATED.set(true);
		} 
		this.baseurl = baseurl;
	}

	/**
	 * Gets base URL of iQuery web application
	 * @return URL as string ie http://iquery.ndexbio.org
	 */
	public String getiQueryurl() {
		return iQueryurl;
	}

	/**
	 * Sets base URL of iQuery web application
	 * @param iQueryurl 
	 */
	public void setiQueryurl(String iQueryurl) {
		this.iQueryurl = iQueryurl;
	}

	
	/**
	 * Gets number of parallel term mapping tasks to invoke
	 * when running functional enrichment tasks
	 * @return 
	 */
	public String getThreadcount() {
		return threadcount;
	}

	/**
	 * Sets number of parallel term mapping tasks to invoke
	 * when running functional enrichment tasks
	 * @param threadcount 
	 */
	public void setThreadcount(String threadcount) {
		this.threadcount = threadcount;
	}

	public int getHttpSocketTimeoutMillis() {
		return httpSocketTimeoutMillis;
	}

	public void setHttpSocketTimeoutMillis(int httpSocketTimeoutMillis) {
		this.httpSocketTimeoutMillis = httpSocketTimeoutMillis;
	}

	public int getHttpConnectTimeoutMillis() {
		return httpConnectTimeoutMillis;
	}

	public void setHttpConnectTimeoutMillis(int httpConnectTimeoutMillis) {
		this.httpConnectTimeoutMillis = httpConnectTimeoutMillis;
	}

	public int getHttpConnectionRequestTimeoutMillis() {
		return httpConnectionRequestTimeoutMillis;
	}

	public void setHttpConnectionRequestTimeoutMillis(int httpConnectionRequestTimeoutMillis) {
		this.httpConnectionRequestTimeoutMillis = httpConnectionRequestTimeoutMillis;
	}

	/**
	 * Gets time to wait in milliseconds before checking status of task
	 * running on CD Service
	 * @return 
	 */
	public int getPollingIntervalTimeMillis() {
		return pollingIntervalTimeMillis;
	}

	/**
	 * Sets time to wait in milliseconds before checking status of task
	 * running on CD Service
	 * @param pollingIntervalTimeMillis 
	 */
	public void setPollingIntervalTimeMillis(int pollingIntervalTimeMillis) {
		this.pollingIntervalTimeMillis = pollingIntervalTimeMillis;
	}

	public int getCommunityDetectionTimeoutMillis() {
		return communityDetectionTimeoutMillis;
	}

	public void setCommunityDetectionTimeoutMillis(int communityDetectionTimeoutMillis) {
		this.communityDetectionTimeoutMillis = communityDetectionTimeoutMillis;
	}

	public int getFunctionalEnrichmentTimeoutMillis() {
		return functionalEnrichmentTimeoutMillis;
	}

	public void setFunctionalEnrichmentTimeoutMillis(int functionalEnrichmentTimeoutMillis) {
		this.functionalEnrichmentTimeoutMillis = functionalEnrichmentTimeoutMillis;
	}

	public int getSubmitRetryCount() {
		return submitRetryCount;
	}

	public void setSubmitRetryCount(int submitRetryCount) {
		this.submitRetryCount = submitRetryCount;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	
	
	/**
	 * Gets property as {@code int} from {@code props} passed in
	 * @param props {@link java.util.Properties} to extract the value from using {@code propertyName} as key
	 * @param propertyName Name of Property
	 * @param defaultValue Value to return if there is an error parsing the value, 
	 *                     no property is found, or there was an error converting the value to
	 * @return value of property
	 */
	protected int getPropertyAsInt(Properties props, final String propertyName, int defaultValue){
		String propVal = null;
		if (props != null){
			propVal = props.getProperty(propertyName);
		}
		if (propVal == null || propVal.trim().isEmpty()){
			return defaultValue;
		}	
		try {
			return Integer.parseInt(propVal);
		}
		catch(NumberFormatException nfe){
			LOGGER.warn("Caught format exception attempting to "
					+ "get integer from property", nfe);
		}
		return defaultValue;
    }
}
