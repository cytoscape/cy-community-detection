/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
