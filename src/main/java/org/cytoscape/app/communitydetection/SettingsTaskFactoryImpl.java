package org.cytoscape.app.communitydetection;

import java.util.Properties;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Dummy task that just displays a dialog describing the Community Detection
 * Application
 * @author churas
 */
public class SettingsTaskFactoryImpl implements NetworkTaskFactory {

	private final static Logger LOGGER = LoggerFactory.getLogger(SettingsTaskFactoryImpl.class);

    private CySwingApplication _swingApplication;
	private SettingsDialog _settingsDialog;
	private ShowDialogUtil _dialogUtil;
	private CyProperty<Properties> _cyProperties;
	private PropertiesHelper _pHelper;
    
    public SettingsTaskFactoryImpl(CySwingApplication swingApplication,
			SettingsDialog settingsDialog, ShowDialogUtil dialogUtil,
			CyProperty<Properties> cyProperties){
		_swingApplication = swingApplication;
		_settingsDialog = settingsDialog;
		_dialogUtil = dialogUtil;
		_cyProperties = cyProperties;
    }
    
  
    
    /**
     * Brings up an about dialog and then calls a dummy task 
     * that does nothing
     * @param network
     * @return TaskIterator with a dummy task
     */
    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
		if (_settingsDialog.createGUI() == false){
			LOGGER.error("SettingsDialog.createGUI() returned false");
			return new TaskIterator(new DoNothingTask());
		}
		Object[] options = {AppUtils.UPDATE, AppUtils.CANCEL};
	    int res = _dialogUtil.showOptionDialog(_swingApplication.getJFrame(),
		                                   this._settingsDialog,
					           "Community Detection Settings",
						   JOptionPane.YES_NO_OPTION,
						   JOptionPane.PLAIN_MESSAGE, 
						   null, 
						   options,
						   options[0]);
		if (res == 0){
			LOGGER.debug("User requested settings be updated");
			updateBaseURL(_settingsDialog.getBaseurl());
		}
		return new TaskIterator(new DoNothingTask());
    }
	
	private void updateBaseURL(final String newBaseUrl){
		String newURL = null;
		if (newBaseUrl == null || newBaseUrl.trim().isEmpty() == true){
			newURL = PropertiesHelper.DEFAULT_BASEURL;
		} else {
			if (!newBaseUrl.startsWith("http://") && ! newBaseUrl.startsWith("https://")){
				newURL = PropertiesHelper.BASEURL_PREFIX + newBaseUrl;
			} else {
				newURL = newBaseUrl;
			}
			
			newURL = newURL + PropertiesHelper.BASEURL_SUFFIX;
		}
		LOGGER.info("Setting app.baseurl to " + newURL);
		_cyProperties.getProperties().setProperty(AppUtils.PROP_APP_BASEURL, newURL);
		PropertiesHelper.getInstance().updateViaProperties(_cyProperties.getProperties());
	}

    /**
     * This is always ready
     * @param network
     * @return true
     */
    @Override
    public boolean isReady(CyNetwork network) {
		return true;
    }
    
}
