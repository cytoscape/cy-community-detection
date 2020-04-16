package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.rest.CDRestClientException;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class LauncherDialogAlgorithmFactoryImpl implements LauncherDialogAlgorithmFactory  {

	private final static Logger LOGGER = LoggerFactory.getLogger(LauncherDialogAlgorithmFactoryImpl.class);
	private ShowDialogUtil _dialogUtil;
	private CDRestClient _client;
	
	public LauncherDialogAlgorithmFactoryImpl(CDRestClient client,
			ShowDialogUtil dialogUtil){
		this._client = client;
		this._dialogUtil = dialogUtil;
	}
	/**
	 * Queries REST service for list of Community Detection Algorithms
	 * @param parentWindow
	 * @return list of algorithms or null if there was an error. Also displays
	 *         a dialog to the user describing the issue
	 */
	@Override
	public List<CommunityDetectionAlgorithm> getAlgorithms(Component parentWindow,
			String algorithmType, boolean refresh) {
		
		CommunityDetectionAlgorithms result = null;
		
		try {
			result = this.getAlgorithmsFromService(refresh);
			if (result == null){
				return null;
			}
			ArrayList<CommunityDetectionAlgorithm> algorithms = new ArrayList<>();
			for (CommunityDetectionAlgorithm algo : result.getAlgorithms().values()) {
				if (algo.getInputDataFormat().equalsIgnoreCase(algorithmType)){
					algorithms.add(algo);
				}
			}
			return algorithms;
		} catch(CDRestClientException ce){
			_dialogUtil.showMessageDialog(parentWindow,
					"Unable to get list of algorithms from service: " + ce.getMessage() + " : " +
							(ce.getErrorResponse() == null ? "" : ce.getErrorResponse().asJson()));
		} catch(IOException io){
			_dialogUtil.showMessageDialog(parentWindow,
					"Unable to get list of algorithms from service: " + io.getMessage());
		}
		return null;
	}
	
	/**
	 * Gets the algorithms from CD Service by trying twice. The second time
	 * the value is just returned or any exception is passed on.
	 * @return Algorithms found in service or null if there are none
	 * @throws CDRestClientException thrown if there is a problem with service call on 2nd
	 *                               try
	 * @throws IOException thrown if there is a problem with service call on 2nd
	 *                     try
	 */
	private CommunityDetectionAlgorithms getAlgorithmsFromService(boolean refresh) throws CDRestClientException, IOException{
		CDRestClient client = CDRestClient.getInstance();
		try {
			return client.getAlgorithms(refresh);
		} catch(CDRestClientException ce){
			LOGGER.warn("Got error trying to get algorithm list from "
					+ "CD service, trying again",
					ce);
		} catch(IOException ie){
			LOGGER.warn("Got error trying to get algorithm list from "
					+ "CD service, trying again",
					ie);
		}
		return client.getAlgorithms(false);
	}
	
}