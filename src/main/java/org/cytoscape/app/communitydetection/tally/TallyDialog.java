package org.cytoscape.app.communitydetection.tally;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
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
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.IconJLabelDialogFactory;
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

	private boolean _guiLoaded = false;
	private DefaultListModel _listModel;
	private Map<String, CyColumn> _columnMap;
	private JList _jList;
	private IconJLabelDialogFactory _iconFactory;

	final static String DESC_MESSAGE = "<b>Tally Attributes on Hierarchy</b> provides a way<br/>"
			+ "to count the number of nodes in each hierarchy cluster that have<br/>"
			+ "a <b><i>true</i></b> or <b><i>positive</i></b> value for a specified set of attributes/columns<br/>"
			+ "in the parent network.<br/><br/>These counts are stored as new columns/attributes<br/>"
			+ "on the hierarchy with the same name as seen in the parent network, but prefixed<br/>"
			+ "with <b><i>" + AppUtils.COLUMN_CD_TALLY_NAMESPACE + "</i></b> namespace.<br/><br/>"
			+ "In addition, any nodes in the hierarchy cluster that do <b>NOT</b> match any of the specified<br/>"
			+ "set of attributes/columns are counted in the <b><i>" + AppUtils.COLUMN_CD_UNMATCHED + "</i></b><br/>"
			+ "column/attribute.<br/><br/>"
			+ "<b>WARNING:</b> For attribute(s)/column(s) of type <b><i>Double</i></b>, the value<br/>"
			+ "is rounded to nearest integer before checking to see if the value is <b><i>positive</i></b><br/><br/>"
			+ "<a href=\"https://cdaps.readthedocs.io\">Click here for information about Tally Attributes on Hierarchy</a><br/><br/>";
			
	
	public TallyDialog(IconJLabelDialogFactory iconFactory){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		_iconFactory = iconFactory;
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
		mainPanel.setName("mainPanel");
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints descConstraints = new GridBagConstraints();
		descConstraints.gridy = 0;
		descConstraints.gridx = 0;
		descConstraints.anchor = GridBagConstraints.LINE_START;
		descConstraints.fill = GridBagConstraints.NONE;
		descConstraints.insets = new Insets(20, 5, 20, 10);
		descriptionPanel.add(new JLabel("Select Attribute(s)/Columns(s) to tally"),
				descConstraints);
		
		GridBagConstraints infoConstraints = new GridBagConstraints();
		infoConstraints.gridy = 0;
		infoConstraints.gridx = 1;
		infoConstraints.insets = new Insets(30, 10, 20, 10);
		descriptionPanel.add(getInfoIcon());
		mainPanel.add(descriptionPanel);
		
		_jList = new JList(_listModel);
		_jList.setName("columnList");
		_jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_jList.setToolTipText("Attributes/Column(s) from parent network");
		JScrollPane listScrollPane = new JScrollPane(_jList);
		listScrollPane.setName("columnListScrollPane");
		mainPanel.add(listScrollPane);
		return mainPanel;
	}
	
	/**
	 * Creates a {@link javax.swing.JLabel} with an info icon that when clicked
	 * displays a small dialog that displays information about the parameter
	 * passed in
	 * @param parameter The parameter
	 * @return 
	 * @throws IOException 
	 */
	private JLabel getInfoIcon(){
		JLabel restUrlLabel = _iconFactory.getJLabelIcon(this,"info_icon", "png",
				"Tally Attributes on hierarchy description", 
				DESC_MESSAGE, 20, 40);
		restUrlLabel.setName("infoIcon");
		restUrlLabel.setToolTipText("Click here for more information about "
				+ "Tallying Attributes on Hierarchy");
	
		return restUrlLabel;
	}
}
