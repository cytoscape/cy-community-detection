package org.cytoscape.app.communitydetection.util;

import java.util.regex.Pattern;

public class AppUtils {

	public final static String SHARED_NAME_COL = "shared name";
	public final static String SHARED_INTERACTION = "shared interaction";

	public final static Pattern SPLIT_PATTERN = Pattern.compile(",");
	public final static String SOURCE = "source.SUID";
	public final static String TARGET = "target.SUID";
	public final static String NEW_NODE_NAME = "Community";
}