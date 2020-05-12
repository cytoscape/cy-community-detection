package org.cytoscape.app.communitydetection.util;

import org.cytoscape.model.CyTable;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;


/**
 * Contains utility methods to interact with {@link org.cytoscape.model.CyNetwork}
 * objects
 * @author churas
 */
public class CyNetworkUtil {

	/**
	 * Creates column in table passed in if it does <b>NOT</b> already exist
	 * @param <T> type of column
	 * @param table where column will be added to
	 * @param colName name of column
	 * @param type column type
	 * @param isImmutable denotes if values in new column can be changed
	 * @param defaultValue the default values for cells in new column
	 * @throws CommunityDetectionException if table or column name is {@code null}
	 */
	public <T> void createTableColumn(CyTable table,
			String colName, Class<? extends T> type, boolean isImmutable,
			T defaultValue) throws CommunityDetectionException {
		if (table == null){
			throw new CommunityDetectionException("table is null");
		}
		if (colName == null){
			throw new CommunityDetectionException("column name is null");
		}
		if (type == null){
			throw new CommunityDetectionException("type is null");
		}
		if (table.getColumn(colName) == null) {
			table.createColumn(colName, type, isImmutable, defaultValue);
		}
	}
}
