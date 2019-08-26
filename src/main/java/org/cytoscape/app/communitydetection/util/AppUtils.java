package org.cytoscape.app.communitydetection.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AppUtils {

	public static final String MENU = "Community Detection";

	public static final String WRITE_CX_MENU_ITEM = "Export Network to CX";
	public static final String READ_CX_MENU_ITEM = "Import Network from CX";
	public static final String EDGE_READER_ID = "edgeListReaderFactory";
	public static final String EDGE_WRITER_ID = "edgeListWriterFactory";

	public final static String COLUMN_SUID = "SUID";
	public final static String COLUMN_CD_MEMBER_LIST = "CD_MemberList";
	public final static String COLUMN_CD_ORIGINAL_NETWORK = "CD_OriginalNetwork";

	public final static Pattern SPLIT_PATTERN = Pattern.compile(",");
	public final static String CD_MEMBER_LIST_DELIMITER = " ";

	public final static String TYPE_NONE = "none";
	public final static String TYPE_WEIGHTED = "weighted";

	public final static Map<String, String> ALGORITHMS = new HashMap<String, String>() {
		{
			put("louvain", "Louvain");
			put("infomap", "InfoMap");
		}
	};
}