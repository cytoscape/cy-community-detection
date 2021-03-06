package org.cytoscape.app.communitydetection.termmap;


import java.util.Map;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.hierarchy.LauncherDialog;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NetworkTaskFactory} implementation to create {@link TermMappingTask}
 * for Menu Bar.
 *
 */
public class NetworkTermMappingTaskFactoryImpl implements NetworkTaskFactory {

	private final static Logger LOGGER = LoggerFactory.getLogger(NetworkTermMappingTaskFactoryImpl.class);

	private LauncherDialog _dialog;
	private CySwingApplication _swingApplication;

	public NetworkTermMappingTaskFactoryImpl(CySwingApplication swingApplication,
		LauncherDialog dialog) {
		this._dialog = dialog;
		this._swingApplication = swingApplication;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
	    if (network == null){
		JOptionPane.showMessageDialog(_swingApplication.getJFrame(),
			"A network must be selected in Cytoscape to run "
				+ "Functional Enrichment.\n"
				+ "For more information visit About menu item under Apps => Community Detection",
			AppUtils.APP_NAME, JOptionPane.ERROR_MESSAGE);
		 return new TaskIterator(new TermMappingTask(null, null, null, false));
	    }
		
	    if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) == null){
		JOptionPane.showMessageDialog(_swingApplication.getJFrame(),
			"A node column named " + AppUtils.COLUMN_CD_MEMBER_LIST + " ("
				+ "type String with genes delimited by spaces)\n" 
				+ "needs to exist on network to run "
				+ "Functional Enrichment.\n"
				+ "For more information click About menu item under Apps => Community Detection",
			AppUtils.APP_NAME, JOptionPane.ERROR_MESSAGE);
		 return new TaskIterator(new TermMappingTask(null, null, null, false));
	    }
	    
	    if (_dialog.createGUI(_swingApplication.getJFrame()) == false){
			return new TaskIterator(new TermMappingTask(null, null, null, false));
		}
	    _dialog.updateNodeSelectionButtons(network);
	    Object[] options = {AppUtils.RUN, AppUtils.CANCEL};
	    int res = JOptionPane.showOptionDialog(_swingApplication.getJFrame(),
		                                   this._dialog,
					           "Run Functional Enrichment",
						   JOptionPane.YES_NO_OPTION,
						   JOptionPane.PLAIN_MESSAGE, 
						   null, 
						   options,
						   options[0]);
	    if (res == 0){
		// user wants to run job
		CommunityDetectionAlgorithm cda = this._dialog.getSelectedCommunityDetectionAlgorithm();
		if (cda != null){   
		    Map<String, String> customParameters = this._dialog.getAlgorithmCustomParameters(cda.getName());
		    LOGGER.debug("User wants to run: " + cda.getName() +
			    (customParameters == null ? "" : " with " +
				    customParameters.toString()));

		    return new TaskIterator(new TermMappingTask(network, cda, customParameters,
			    _dialog.runOnSelectedNodes()));
		} else {
		   LOGGER.error("Couldnt get algorithm from dialog...");
		}
	    }
	    return new TaskIterator(new TermMappingTask(null, null, null, false));
	}

	@Override
	public boolean isReady(CyNetwork network) {
	    return true;
	}
}
