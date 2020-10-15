package org.cytoscape.app.communitydetection.tally;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.model.CyColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a JPanel that shows a list of Cytoscape Columns/Attributes
 * for the parent network. The user can then select column(s) they 
 * would like to tally in the hierarchy network
 * 
 * @author churas
 */
public class TallyDialog extends JPanel {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(TallyDialog.class);

	private ShowDialogUtil _dialogUtil;
	private boolean _guiLoaded = false;
	private DefaultListModel _listModel;
	private Map<String, CyColumn> _columnMap;
	private JList _jList;
	
	public TallyDialog(ShowDialogUtil dialogUtil){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		_dialogUtil = dialogUtil;
	}
	
	/**
	 * Creates the GUI and populates the GUI with list of 
	 * Cytoscape Columns/Attributes
	 * that can be tallied
	 * @param columns Map of column name to {@code CyColumn}
	 * @return {@code true} if success or false if {@code columns} is {@code null} or empty
	 */
	public boolean createGUI(Map<String, CyColumn> columns){
		if (columns == null || columns.isEmpty()){
			return false;
		}
		if (_guiLoaded == false){
			_listModel = new DefaultListModel();
			this.add(getListPanel());
			_guiLoaded = true;
		}
		_listModel.clear();
		_columnMap = columns;		
		_listModel.addAll(_columnMap.keySet());
		
		return true;
	}
	
	/**
	 * Gets the columns the user selected, if any.
	 * @return {@code null} if no columns were shown to user otherwise empty list or list of selected
	 *         columns 
	 */
	public List<CyColumn> getColumnsToTally(){
		if (_listModel == null){
			return null;
		}
		if (_columnMap == null){
			return null;
		}
		List<CyColumn> selectedCols = new ArrayList<>();
		for (Object selectedVal : _jList.getSelectedValuesList()){
			if (_columnMap.containsKey((String)selectedVal)){
				selectedCols.add(_columnMap.get((String)selectedVal));
			}
		}
		return selectedCols;
	}
	
	/**
	 * Creates the JPanel
	 * @return 
	 */
	private JPanel getListPanel(){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(new JLabel("Select Attributes/Columns(s) from parent network"
				+ "to tally on hierarchy network."));
		
		_jList = new JList(_listModel);
		_jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane listScrollPane = new JScrollPane(_jList);
		mainPanel.add(listScrollPane);
		return mainPanel;
	}
}
