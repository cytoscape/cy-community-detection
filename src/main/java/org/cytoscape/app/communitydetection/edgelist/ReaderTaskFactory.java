package org.cytoscape.app.communitydetection.edgelist;

import java.io.InputStream;
import org.cytoscape.work.TaskIterator;

/**
 *
 * @author churas
 */
public interface ReaderTaskFactory {

	TaskIterator createTaskIterator(InputStream inputStream, String collectionName,
			Long originalNetSUID);
	
}
