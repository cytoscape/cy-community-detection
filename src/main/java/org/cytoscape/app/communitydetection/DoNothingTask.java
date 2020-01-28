package org.cytoscape.app.communitydetection;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author churas
 */
public class DoNothingTask  extends AbstractTask{

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
    }

    @Override
    public void cancel() {
	super.cancel();
    }

    @Override
    public TaskIterator getTaskIterator() {
	return super.getTaskIterator(); 
    }
    
}
