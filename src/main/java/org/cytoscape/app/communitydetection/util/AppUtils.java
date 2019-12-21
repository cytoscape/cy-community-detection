package org.cytoscape.app.communitydetection.util;

public class AppUtils {

	public static final String APP_NAME = "CyCommunityDetection";
	public static final String PROP_APP_BASEURL = "app.baseurl";
	public static final String PROP_APP_THREADCOUNT = "app.threadcount";
	public static final String PROP_PROJECT_VERSION = "project.version";
	public static final String PROP_PROJECT_NAME = "project.name";

	public static final String TOP_MENU = "Apps.Community Detection";
	public static final String TOP_MENU_CD = TOP_MENU + ".Community Detection";
	public static final String TOP_MENU_TM = TOP_MENU + ".Functional Enrichment";
	public static final String CONTEXT_MENU_CD = "Community Detection";
	public static final String CONTEXT_MENU_TM = CONTEXT_MENU_CD + ".Functional Enrichment";

	public static final String WRITE_CX_MENU_ITEM = "Export Network to CX";
	public static final String READ_CX_MENU_ITEM = "Import Network from CX";
	public static final String EDGE_READER_ID = "edgeListReaderFactory";
	public static final String EDGE_WRITER_ID = "edgeListWriterFactory";

	public final static String COLUMN_SUID = "SUID";
	public final static String COLUMN_DESCRIPTION = "Description";
	public final static String COLUMN_DERIVED_FROM = "prov:wasDerivedFrom";
	public final static String COLUMN_GENERATED_BY = "prov:wasGeneratedBy";
	public final static String COLUMN_CD_MEMBER_LIST = "CD_MemberList";
	public final static String COLUMN_CD_MEMBER_LIST_SIZE = "CD_MemberList_Size";
	public final static String COLUMN_CD_MEMBER_LIST_LOG_SIZE = "CD_MemberList_LogSize";
	public final static String COLUMN_CD_ORIGINAL_NETWORK = "CD_OriginalNetwork";
	public final static String COLUMN_CD_COMMUNITY_NAME = "CD_CommunityName";
	public final static String COLUMN_CD_ANNOTATED_MEMBERS = "CD_AnnotatedMembers";
	public final static String COLUMN_CD_ANNOTATED_MEMBERS_SIZE = "CD_AnnotatedMembers_Size";
	public final static String COLUMN_CD_ANNOTATED_OVERLAP = "CD_AnnotatedMembers_Overlap";
	public final static String COLUMN_CD_ANNOTATED_PVALUE = "CD_AnnotatedMembers_Pvalue";
	
	public final static String COLUMN_CD_LABELED = "CD_Labeled";

	public final static String EDGE_LIST_SPLIT_PATTERN = ",";
	public final static String CD_MEMBER_LIST_DELIMITER = "[\\s,]+";

	public final static String TYPE_NONE = "none";
	public final static String TYPE_WEIGHTED = "weighted";
	public final static String TYPE_ABOUT = "about";
	public final static String TYPE_NONE_VALUE = "(none)";
	public final static String TYPE_WEIGHTED_VALUE = "Weighted";
	public final static String TYPE_ABOUT_VALUE = "About";

	public final static String CD_ALGORITHM_INPUT_TYPE = "EDGELIST";
	public final static String TM_ALGORITHM_INPUT_TYPE = "GENELIST";
	public final static String CANCEL = "Cancel";
	public final static String APPLY = "Apply";
	public final static String CLOSE = "Close";
	public final static String RUN = "Run";
	public final static String RESET = "Reset to defaults";
}