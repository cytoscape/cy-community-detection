package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.Component;
import java.util.List;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;

/**
 * Interface for classes that wish to get algorithms 
 * for LauncherDialog
 * @author churas
 */
public interface LauncherDialogAlgorithmFactory {
	
	/**
	 * Given a type return algorithms from CDAPS service
	 * 
	 * @param algorithmType
	 * @return list of algorithms or null if there was a problem
	 */
	public List<CommunityDetectionAlgorithm> getAlgorithms(Component parentWindow,
			final String algorithmType);
	
}
