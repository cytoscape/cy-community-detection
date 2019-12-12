package org.cytoscape.app.communitydetection;

public class PropertiesHelper {

	private String baseurl;
	private String threadcount;

	private PropertiesHelper() {
	}

	private static class SingletonHelper {
		private static final PropertiesHelper INSTANCE = new PropertiesHelper();
	}

	public static PropertiesHelper getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public String getBaseurl() {
		return baseurl;
	}

	public void setBaseurl(String baseurl) {
		this.baseurl = baseurl;
	}

	public String getThreadcount() {
		return threadcount;
	}

	public void setThreadcount(String threadcount) {
		this.threadcount = threadcount;
	}

}
