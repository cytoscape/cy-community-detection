package org.cytoscape.app.communitydetection.util;

import java.util.HashMap;
import java.util.Map;

public class AppUtils {

	public static final String TOP_MENU = "Tools.Community Detection";
	public static final String CONTEXT_MENU = "Community Detection";

	public static final String WRITE_CX_MENU_ITEM = "Export Network to CX";
	public static final String READ_CX_MENU_ITEM = "Import Network from CX";
	public static final String EDGE_READER_ID = "edgeListReaderFactory";
	public static final String EDGE_WRITER_ID = "edgeListWriterFactory";

	public final static String COLUMN_SUID = "SUID";
	public final static String COLUMN_CD_MEMBER_LIST = "CD_MemberList";
	public final static String COLUMN_CD_MEMBER_LIST_SIZE = "CD_MemberList_Size";
	public final static String COLUMN_CD_MEMBER_LIST_LOG_SIZE = "CD_MemberList_LogSize";
	public final static String COLUMN_CD_ORIGINAL_NETWORK = "CD_OriginalNetwork";
	public final static String COLUMN_CD_COMMUNITY_NAME = "CD_CommunityName";
	public final static String COLUMN_CD_ANNOTATED_MEMBERS = "CD_AnnotatedMembers";
	public final static String COLUMN_CD_LABELED = "CD_Labeled";

	public final static String EDGE_LIST_SPLIT_PATTERN = ",";
	public final static String CD_MEMBER_LIST_DELIMITER = "[\\s,]+";

	public final static String TYPE_NONE = "none";
	public final static String TYPE_WEIGHTED = "weighted";
	public final static String TYPE_NONE_VALUE = "(none)";

	public final static Map<String, String> HIERARCHY_ALGORITHMS = new HashMap<String, String>() {
		{
			put("louvain", "Louvain");
			put("infomap", "InfoMap");
			put("clixo", "CliXO");
		}
	};

	public final static Map<String, String> TERM_MAPPING_ALGORITHMS = new HashMap<String, String>() {
		{
			put("gprofilersingletermv2", "GProfiler term Mapping");
		}
	};
}