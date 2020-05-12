package org.cytoscape.app.communitydetection.hierarchy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to get and load CDAPS Hierarchy Style
 * @author churas
 */
public class VisualStyleFactory {
	private final static Logger LOGGER = LoggerFactory.getLogger(VisualStyleFactory.class);
	
	public final static String DEFAULT_STYLE_NAME = "CD_Hierarchy";
	public final static String DEFAULT_STYLE_PREFIX = "cd_hierarchy";
	public final static String DEFAULT_STYLE_SUFFIX = "xml";
	public final static String DEFAULT_STYLE_RESOURCEPATH = DEFAULT_STYLE_PREFIX + "." + DEFAULT_STYLE_SUFFIX;
	
	
	private VisualMappingManager _visualMappingManager;
	private LoadVizmapFileTaskFactory _vizmapFileTaskFactory;
	
	public VisualStyleFactory(VisualMappingManager visualMappingManager,
			LoadVizmapFileTaskFactory vizmapFileTaskFactory){
		_visualMappingManager = visualMappingManager;
		_vizmapFileTaskFactory = vizmapFileTaskFactory;
	}
	
	public VisualStyle getVisualStyle(){
		for (VisualStyle style : _visualMappingManager.getAllVisualStyles()) {
			if (style.getTitle().equalsIgnoreCase(DEFAULT_STYLE_NAME)) {
				return style;
			}
		}
		File styleFile = null;
		try {
			InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_STYLE_RESOURCEPATH);
			styleFile = File.createTempFile(DEFAULT_STYLE_PREFIX, DEFAULT_STYLE_SUFFIX);
			Files.copy(resourceStream, styleFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			LOGGER.debug("Style set size: " + _vizmapFileTaskFactory.loadStyles(styleFile).size());

			return _vizmapFileTaskFactory.loadStyles(styleFile).iterator().next();
		} catch(IOException io){
			LOGGER.error("Error loading style", io);
		}
		finally {
			if (styleFile != null){
				styleFile.delete();
			}
		}
		return null;
	}
}
