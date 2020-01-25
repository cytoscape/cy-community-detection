package org.cytoscape.app.communitydetection;

public class PropertiesHelper {

	private String baseurl;
	private String threadcount;
	private int httpSocketTimeoutMillis;
	private int httpConnectTimeoutMillis;
	private int httpConnectionRequestTimeoutMillis;
	private int pollingIntervalTimeMillis;
	private int pollingMaxRetryCount;
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

	/**
	 * Gets REST endpoint for CD Service
	 * @return 
	 */
	public String getBaseurl() {
		return baseurl;
	}

	/**
	 * Sets REST endpoint for CD Service
	 * @param baseurl 
	 */
	public void setBaseurl(String baseurl) {
		this.baseurl = baseurl;
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

	/**
	 * Gets number of retries to perform when checking for completion of a 
	 * task
	 * @return 
	 */
	public int getPollingMaxRetryCount() {
		return pollingMaxRetryCount;
	}

	/**
	 * Sets number of retries to perform when checking for completion of a 
	 * task
	 * @param pollingMaxRetryCount
	 */
	public void setPollingMaxRetryCount(int pollingRetryCount) {
		this.pollingMaxRetryCount = pollingRetryCount;
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
}
