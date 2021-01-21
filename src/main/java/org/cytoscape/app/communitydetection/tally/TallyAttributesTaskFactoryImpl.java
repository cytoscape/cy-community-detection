package org.cytoscape.app.communitydetection.tally;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.DoNothingTask;
import org.cytoscape.app.communitydetection.subnetwork.ParentNetworkChooserDialog;
import org.cytoscape.app.communitydetection.subnetwork.ParentNetworkFinder;
import org.cytoscape.app.communitydetection.subnetwork.ParentNetworkFinderException;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.CyNetworkUtil;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Creates task to take column(s) from parent network that are of type Integer or 
 * Boolean and uses them to tally number of nodes in member list of hierarchy
 * where those given column(s) have value of {@code true} or non zero number. 
 * @author churas
 */
public class TallyAttributesTaskFactoryImpl implements NetworkTaskFactory {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(TallyAttributesTaskFactoryImpl.class);

	private TallyDialog _dialog;
	private CySwingApplication _swingApplication;
	private ParentNetworkFinder _parentNetworkFinder;
	private ParentNetworkChooserDialog _parentNetworkDialog;
	private ShowDialogUtil _dialogUtil;
	private CyNetworkUtil _cyNetworkUtil;
	private final CyNetworkManager _networkManager;

	/**
	 * Constructor
	 * 
	 * @param swingApplication
	 * @param dialogUtil
	 * @param dialog
	 * @param parentNetworkFinder
	 * @param parentNetworkDialog
	 * @param cyNetworkUtil
	 * @param networkManager 
	 */
	public TallyAttributesTaskFactoryImpl(CySwingApplication swingApplication,
		ShowDialogUtil dialogUtil,
		TallyDialog dialog, 
		ParentNetworkFinder parentNetworkFinder,
		ParentNetworkChooserDialog parentNetworkDialog,
		CyNetworkUtil cyNetworkUtil,
		CyNetworkManager networkManager) {
		this._dialog = dialog;
		this._dialogUtil = dialogUtil;
		this._swingApplication = swingApplication;
		this._parentNetworkFinder = parentNetworkFinder;
		this._parentNetworkDialog = parentNetworkDialog;
		this._cyNetworkUtil = cyNetworkUtil;
		this._networkManager = networkManager;
	}
	
	/**
	 * Gets the parent network by first checking the network attribute on the
	 * hierarchy and if that fails, displays a dialog to the user
	 * @param hierarchyNetwork 
	 * @return parent network or {@code null} if none selected or found
	 */
	private CyNetwork getParentNetwork(CyNetwork hierarchyNetwork){
		try {
			List<CyNetwork> parentNetworks =  _parentNetworkFinder.findParentNetworks(_networkManager.getNetworkSet(), 
			                                                                          hierarchyNetwork);
			if (parentNetworks != null && parentNetworks.size() == 1){
				return parentNetworks.get(0);
			} 

			if (_parentNetworkDialog.createGUI(parentNetworks) == false){
				LOGGER.error("No parent network selected via GUI");
				return null;
			}
			Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
			int res = _dialogUtil.showOptionDialog(_swingApplication.getJFrame(),
				                               this._parentNetworkDialog,
									"Parent Network Chooser",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, 
									null, 
									options,
									options[0]);
			if (res == 0){
				CyNetwork selectedParentNetwork = _parentNetworkDialog.getSelection();
				if (_parentNetworkDialog.rememberChoice() == true){
					try {
						_cyNetworkUtil.updateHierarchySUID(hierarchyNetwork,
								selectedParentNetwork);
					} catch(CommunityDetectionException cde){
						LOGGER.error("Error updating parent "
								+ "network id on hierarchy network: " + cde.getMessage());
					}
				}
				return selectedParentNetwork;
			}
			return null;
		}
		catch(ParentNetworkFinderException pe){
			LOGGER.error("Caught exception trying to find parent network", pe);
			return null;
		}
	}
	
	/**
	 * Gets the columns that can be tallied. These are columns that
	 * must be of type Integer
	 * @param parentNetwork 
	 */
	private Map<String, CyColumn> getColumnsThatCanBeTallied(CyNetwork parentNetwork){
		Map<String, CyColumn> cyColumns = new HashMap<>();
		for (CyColumn col : parentNetwork.getDefaultNodeTable().getColumns()){
			if (col.getName() == null || 
					col.getName().equals("selected")){
				continue;
			}

			LOGGER.debug("Checking type for column: " + col.getName());
			if (col.getType() == null){
				continue;
			}
			if (col.getType() == Integer.class || 
				col.getType() == Boolean.class ||
					col.getType() == Double.class){
				LOGGER.debug("Found attribute/column that can be tallied: "
						+ col.getName());
				cyColumns.put(col.getName(), col);
			}
		}
		return cyColumns;
	}

	/**
	 * Creates the task to tally columns(s) from parent network on hierarchy network
	 * If 'network' is not a hierarchy network or is missing AppUtils.COLUMN_CD_MEMBER_LIST
	 * a dialog will be displayed and a no op task will be returned.
	 * 
	 * @param network hierarchy network
	 * @return Task to run
	 */
	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
	    if (network == null){
		_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
			"A network must be selected in Cytoscape to run "
				+ "Tally Attributes.\n\n"
				+ "For more information visit About menu item under Apps => Community Detection",
			AppUtils.APP_NAME, JOptionPane.ERROR_MESSAGE);
		 return new TaskIterator(new DoNothingTask());
	    }
		
	    if (network.getDefaultNodeTable().getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) == null){
		_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
			"A Community Detection hierarchy with a node column named\n" 
					+ AppUtils.COLUMN_CD_MEMBER_LIST + " ("
				+ "type String with node names delimited by spaces)\n" 
				+ "needs to exist on network to run "
				+ "Tally Attributes on hierarchy.\n\n"
				+ "For more information click About menu item under Apps => Community Detection",
			AppUtils.APP_NAME, JOptionPane.ERROR_MESSAGE);
		 return new TaskIterator(new DoNothingTask());
	    }
	    
		CyNetwork selectedParentNetwork = this.getParentNetwork(network);
		if (selectedParentNetwork == null){
			LOGGER.error("No parent network selected/found");
			return new TaskIterator(new DoNothingTask());
		}
		
		Map<String, CyColumn> columnsThatCanBeTallied = getColumnsThatCanBeTallied(selectedParentNetwork);
		
	    if (_dialog.createGUI(columnsThatCanBeTallied) == false){
			LOGGER.error("Unable to create tally GUI");
			return new TaskIterator(new DoNothingTask());
		}
	    Object[] options = {AppUtils.TALLY, AppUtils.CANCEL};
	    int res = _dialogUtil.showOptionDialog(_swingApplication.getJFrame(),
		                                   this._dialog,
					           "Tally Attributes",
						   JOptionPane.YES_NO_OPTION,
						   JOptionPane.PLAIN_MESSAGE, 
						   null, 
						   options,
						   options[0]);
	    if (res == 0){
			// user wants to run job
			LOGGER.info("User wants to run task");
			List<CyColumn> tallyCols = _dialog.getColumnsToTally();
			if (tallyCols == null || tallyCols.isEmpty()){
				LOGGER.error("No columns selected to tally");
				_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
			"No columns selected to tally",  
			AppUtils.APP_NAME, JOptionPane.ERROR_MESSAGE);
			    return new TaskIterator(new DoNothingTask());
			}
			return new TaskIterator(new TallyTask(_cyNetworkUtil,
					selectedParentNetwork, network, tallyCols));
		} else {
		   LOGGER.info("User canceled operation");
		}
	    return new TaskIterator(new DoNothingTask());
	}

	/**
	 * Will always return true to denote this task can be run
	 * @param network
	 * @return 
	 */
	@Override
	public boolean isReady(CyNetwork network) {
	    return true;
	}
}
