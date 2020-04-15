package org.cytoscape.app.communitydetection;

import java.util.Properties;

import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;

public class PropertiesReader extends AbstractConfigDirPropsReader implements PropertyUpdatedListener {

	private final String propName;

	public PropertiesReader(String name, String propFileName) {
		super(name, propFileName, CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
		this.propName = name;
	}

	@Override
	public void handleEvent(PropertyUpdatedEvent e) {
		if (e.getSource().getName().equalsIgnoreCase(propName)) {
			Properties props = (Properties)e.getSource().getProperties();			
			PropertiesHelper.getInstance().updateViaProperties(props);
		}
	}
}
