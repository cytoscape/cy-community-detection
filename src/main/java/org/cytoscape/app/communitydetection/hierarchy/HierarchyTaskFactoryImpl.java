package org.cytoscape.app.communitydetection.hierarchy;


import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.rest.CDRestClientException;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Task factory impl to create {@link HierarchyTask}.
 *
 */
public class HierarchyTaskFactoryImpl implements NetworkTaskFactory {

	private final static Logger LOGGER = LoggerFactory.getLogger(HierarchyTaskFactoryImpl.class);
	private LauncherDialog _dialog;
	private CySwingApplication _swingApplication;

	public HierarchyTaskFactoryImpl(CySwingApplication swingApplication, LauncherDialog dialog) {
		this._dialog = dialog;
		this._swingApplication = swingApplication;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
	    if (network == null){
		JOptionPane.showMessageDialog(_swingApplication.getJFrame(),
			"A network must be selected in Cytoscape to Run "
				+ "Community Detection. "
				+ "For more information click About menu item",
			AppUtils.APP_NAME, JOptionPane.ERROR_MESSAGE);
		return new TaskIterator(new HierarchyTask(network, null, null, null));
	    }
		if (_dialog.createGUI(_swingApplication.getJFrame())== false){
			return new TaskIterator(new HierarchyTask(network, null, null, null));
		} 
		
	    _dialog.updateWeightColumnCombo(getNumericColumns(network.getDefaultEdgeTable()));
	    Object[] options = {AppUtils.RUN, AppUtils.CANCEL};
	    int res = JOptionPane.showOptionDialog(_swingApplication.getJFrame(),
		                                   this._dialog,
					           "Run Community Detection",
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
			    customParameters == null ? "" : " with " +
				    customParameters.toString());
		    return new TaskIterator(new HierarchyTask(network, cda, customParameters,
			    _dialog.getWeightColumn()));
		} else {
		   LOGGER.error("Couldnt get algorithm from dialog...");
		}
	    }
	    
	    return new TaskIterator(new HierarchyTask(network, null, null, null));
	}

	/**
	 * Always true cause the {@link #createTaskIterator(org.cytoscape.model.CyNetwork) }
	 * will handle case where there is NOT a network
	 * @param network
	 * @return true 
	 */
	@Override
	public boolean isReady(CyNetwork network) {
		return true;
	}

	/**
	 * Return a list of columns with numeric values which the user could
	 * potentially use as the weight column for clustering
	 * @param table
	 * @return string list with column names
	 */
	private Set<String> getNumericColumns(CyTable table) {
	
		Set<String> columnNames = new HashSet<String>();
		for (CyColumn column : table.getColumns()) {
			if (Number.class.isAssignableFrom(column.getType())) {
				columnNames.add(column.getName());
			}
		}
		if (columnNames.contains(CyNetwork.SUID)) {
			columnNames.remove(CyNetwork.SUID);
		}
		return columnNames;
	}
}
