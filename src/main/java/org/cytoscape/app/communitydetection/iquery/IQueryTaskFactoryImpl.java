package org.cytoscape.app.communitydetection.iquery;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.cytoscape.app.communitydetection.DoNothingTask;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.DesktopUtil;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link NetworkViewTaskFactory} and
 * {@link AbstractNodeViewTaskFactory} to send members of specified
 * node to iQuery
 *
 */
public class IQueryTaskFactoryImpl extends AbstractNodeViewTaskFactory implements NetworkViewTaskFactory {

	private final static Logger LOGGER = LoggerFactory.getLogger(IQueryTaskFactoryImpl.class);
	private final CySwingApplication _swingApplication;
	private final ShowDialogUtil _dialogUtil;
	private DesktopUtil _deskTopUtil;
 

	public IQueryTaskFactoryImpl(CySwingApplication swingApplication,
			ShowDialogUtil dialogUtil) {
		_swingApplication = swingApplication;
		_dialogUtil = dialogUtil;
		_deskTopUtil = new DesktopUtil();
	}
	
	protected void setAlternateDesktopUtil(DesktopUtil deskTopUtil){
		this._deskTopUtil = deskTopUtil;
	}
	
	/**
	 * Creates URL with genes appended to end in format: 
	 * http://search.ndexbio.org/?genes=GENE1%20GENE2%20GENE3
	 * @param networkView
	 * @return 
	 */
	private URI getIQueryURI(CyNetworkView networkView){
		try {
			String termList = getTermList(networkView);
			if (termList == null){
				_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
						"No terms to send to iQuery");
				return null;
			}
			return new URI(PropertiesHelper.getInstance().getiQueryurl() +
					AppUtils.CD_IQUERY_GENES_QUERY_PREFIX + termList);
		} catch(URISyntaxException e){
			LOGGER.error("Unable to build URL to send terms to iQuery", e);
			_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
					"Unable to generate URL for iQuery: " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Creates term list by getting data from node column named 
	 * {@literal org.cytoscape.app.communitydetection.util.AppUtils#COLUMN_CD_MEMBER_LIST}
	 * and replacing the default delimiter with {@literal %20} so it can be put in
	 * a web link
	 * @param networkView
	 * @return 
	 */
	private String getTermList(CyNetworkView networkView){
		CyNetwork network = networkView.getModel();
		List<CyNode> selectedNodes = CyTableUtil.getSelectedNodes(network);
		String termlist = network.getRow(selectedNodes.get(0)).get(AppUtils.COLUMN_CD_MEMBER_LIST, String.class);
		if (termlist == null || termlist.trim().isEmpty()){
			return null;
		}
		return termlist.replaceAll(AppUtils.CD_MEMBER_LIST_DELIMITER, AppUtils.CD_IQUERY_SPACE_DELIM);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		URI iQueryURI = getIQueryURI(networkView);
		if (iQueryURI == null){
			return new TaskIterator(new DoNothingTask());
		}
		URL iQueryURL = null;
		try {
			iQueryURL = iQueryURI.toURL();
			LOGGER.debug("Opening " + iQueryURL + " in default browser");
			_deskTopUtil.getDesktop().browse(iQueryURI);
		} catch (Exception e) {
			LOGGER.info("Unable to open default browser window to pass terms to iQuery", e);
			_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
					"Default browser window could not be opened. Please copy/paste this link to your browser: "
						+ (iQueryURL == null ? "NA" : iQueryURL));
		}
		return new TaskIterator(new DoNothingTask());
	}

	/**
	 * Lets caller know if this task can be invoked via 
	 * {@link #createTaskIterator(org.cytoscape.view.model.CyNetworkView) }
	 * @param networkView
	 * @return true if one and only one node is selected and 
	 *         {@value org.cytoscape.app.communitydetection.util.AppUtils#COLUMN_CD_MEMBER_LIST}
	 *         column exists in node table of network
	 */
	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (networkView != null && networkView.getModel() != null) {
			if (CyTableUtil.getSelectedNodes(networkView.getModel()).size() != 1) {
				return false;
			}
			if (networkView.getModel().getDefaultNodeTable()
					.getColumn(AppUtils.COLUMN_CD_MEMBER_LIST) != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return this.createTaskIterator(networkView);
	}

	/**
	 * Just calls {@link #isReady(org.cytoscape.view.model.CyNetworkView) } ignoring
	 * {@code nodeView}
	 * @param nodeView This is ignored
	 * @param networkView
	 * @return See {@link #isReady(org.cytoscape.view.model.CyNetworkView) } for 
	 *         return information
	 */
	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return this.isReady(networkView);
	}
}
