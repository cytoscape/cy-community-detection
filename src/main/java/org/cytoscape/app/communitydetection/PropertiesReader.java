package org.cytoscape.app.communitydetection;

import java.util.Properties;

import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;

public class PropertiesReader extends AbstractConfigDirPropsReader implements PropertyUpdatedListener {

	private final String propName;

	public PropertiesReader(String name, String propFileName) {
		super(name, propFileName, CyProperty.SavePolicy.CONFIG_DIR);
		this.propName = name;
	}

	@Override
	public void handleEvent(PropertyUpdatedEvent e) {
		if (e.getSource().getName().equalsIgnoreCase(propName)) {
			String baseurl = ((Properties) e.getSource().getProperties()).getProperty(AppUtils.PROP_APP_BASEURL);
			PropertiesHelper.getInstance().setBaseurl(baseurl);
			String threadcount = ((Properties) e.getSource().getProperties())
					.getProperty(AppUtils.PROP_APP_THREADCOUNT);
			PropertiesHelper.getInstance().setThreadcount(threadcount);
		}
	}

}
