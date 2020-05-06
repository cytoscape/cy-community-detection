package org.cytoscape.app.communitydetection.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AppUtils {

	public static final String APP_NAME = "CyCommunityDetection";
	public static final String PROP_NAME = "cycommunitydetection";
	public static final String PROP_APP_BASEURL = "app.baseurl";
	public static final String PROP_IQUERY_URL = "iquery.url";
	public static final String PROP_APP_THREADCOUNT = "app.threadcount";
	public static final String PROP_PROJECT_VERSION = "project.version";
	public static final String PROP_PROJECT_NAME = "project.name";
	public static final String PROP_CD_TASK_TIMEOUT = "communitydetection.timeout.millis";
	public static final String PROP_FE_TASK_TIMEOUT = "functionalenrichment.timeout.millis";
	public static final String PROP_SUBMIT_RETRY_COUNT = "submit.retry.count";
	
	public static final String PROP_HTTP_SOCKET_TIMEOUT = "http.socket.timeout.millis";
	public static final String PROP_HTTP_CONNECT_TIMEOUT = "http.connect.timeout.millis";
	public static final String PROP_HTTP_CONNECTION_REQUEST_TIMEOUT = "http.connection.request.timeout.millis";

	public static final String PROP_POLL_INTERVAL_TIME = "polling.interval.time.millis";
	
	public static final String TOP_MENU = "Apps.Community Detection";
	public static final String TOP_MENU_CD = TOP_MENU + ".Community Detection";
	public static final String TOP_MENU_TM = TOP_MENU + ".Functional Enrichment";
	public static final String CONTEXT_MENU_CD = "Apps.Community Detection";
	public static final String CONTEXT_MENU_TM = CONTEXT_MENU_CD + ".Functional Enrichment";

	public static final String WRITE_CX_MENU_ITEM = "Export Network to CX";
	public static final String READ_CX_MENU_ITEM = "Import Network from CX";
	public static final String EDGE_READER_ID = "edgeListReaderFactory";
	public static final String EDGE_WRITER_ID = "edgeListWriterFactory";

	public final static String COLUMN_SUID = "SUID";
	public final static String COLUMN_DESCRIPTION = "description";
	public final static String COLUMN_DERIVED_FROM = "prov:wasDerivedFrom";
	public final static String COLUMN_GENERATED_BY = "prov:wasGeneratedBy";
	public final static String COLUMN_NDEX_UUID_HIDDEN = "NDEx UUID";
	public final static String COLUMN_CD_MEMBER_LIST = "CD_MemberList";
	public final static String COLUMN_CD_MEMBER_LIST_SIZE = "CD_MemberList_Size";
	public final static String COLUMN_CD_MEMBER_LIST_LOG_SIZE = "CD_MemberList_LogSize";
	public final static String COLUMN_CD_ORIGINAL_NETWORK = "__CD_OriginalNetwork";
	public final static String COLUMN_CD_COMMUNITY_NAME = "CD_CommunityName";
	public final static String COLUMN_CD_ANNOTATED_ALGORITHM = "CD_AnnotatedAlgorithm";
	public final static String COLUMN_CD_ANNOTATED_MEMBERS = "CD_AnnotatedMembers";
	public final static String COLUMN_CD_ANNOTATED_MEMBERS_SIZE = "CD_AnnotatedMembers_Size";
	public final static String COLUMN_CD_ANNOTATED_OVERLAP = "CD_AnnotatedMembers_Overlap";
	public final static String COLUMN_CD_ANNOTATED_PVALUE = "CD_AnnotatedMembers_Pvalue";
	public final static String COLUMN_CD_NONANNOTATED_MEMBERS = "CD_NonAnnotatedMembers";
	public final static String COLUMN_CD_ANNOTATED_SOURCE = "CD_AnnotatedMembers_SourceDB";
	public final static String COLUMN_CD_ANNOTATED_SOURCE_TERM = "CD_AnnotatedMembers_SourceTerm";
	
	
	public final static String COLUMN_CD_LABELED = "CD_Labeled";

	public final static String EDGE_LIST_SPLIT_PATTERN = ",";
	public final static String CD_MEMBER_LIST_DELIMITER = "[\\s,]+";

	/**
	 * Query variable to send genes to iQuery web application
	 */
	public final static String CD_IQUERY_GENES_QUERY_PREFIX = "?genes=";
	
	public final static String CD_IQUERY_SPACE_DELIM="%20";

	public final static String TYPE_NONE = "none";
	public final static String TYPE_WEIGHTED = "weighted";
	public final static String TYPE_ABOUT = "about";
	public final static String TYPE_NONE_VALUE = "(none)";
	public final static String TYPE_WEIGHTED_VALUE = "Weighted";
	public final static String TYPE_ABOUT_VALUE = "About";

	public final static String CD_ALGORITHM_OUTPUT_EDGELIST_KEY = "communityDetectionResult";
	public final static String CD_ALGORITHM_INPUT_TYPE = "EDGELIST";
	public final static String CD_ALGORITHM_INPUT_TYPE_V2 = "EDGELISTV2";
	public final static Set<String> CD_ALGORITHM_INPUT_TYPES = new HashSet<>(Arrays.asList(CD_ALGORITHM_INPUT_TYPE,
		                                                                                   CD_ALGORITHM_INPUT_TYPE_V2));
	
	public final static String TM_ALGORITHM_INPUT_TYPE = "GENELIST";
	public final static Set<String> TM_ALGORITHM_INPUT_TYPES = new HashSet<>(Arrays.asList(TM_ALGORITHM_INPUT_TYPE));
	public final static String CANCEL = "Cancel";
	public final static String APPLY = "Apply";
	public final static String CLOSE = "Close";
	public final static String ABOUT = "About";
	public final static String UPDATE = "Update";
	public final static String RUN = "Run";
	public final static String RESET = "Reset to defaults";
}