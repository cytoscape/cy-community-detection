package org.cytoscape.app.communitydetection;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import org.cytoscape.app.communitydetection.hierarchy.JEditorPaneFactory;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * A Dummy task that just displays a dialog describing the Community Detection
 * Application
 * @author churas
 */
public class AboutTaskFactoryImpl implements NetworkTaskFactory {

    private CySwingApplication _swingApplication;
    private JEditorPaneFactory _editorPaneFactory;
	private ShowDialogUtil _dialogUtil;
    private JEditorPane _editorPane;
    private ImageIcon _aboutIcon;
    
    public AboutTaskFactoryImpl(CySwingApplication swingApplication,
	    JEditorPaneFactory editorPaneFactory, ShowDialogUtil dialogUtil){
		_swingApplication = swingApplication;
		_editorPaneFactory = editorPaneFactory;
		_dialogUtil = dialogUtil;
    }
    
    private void createAboutEditorPaneIfNeeded(){
	if (_editorPane != null){
	    return;
	}
	
	loadImageIcon();
	
	StringBuilder sb = new StringBuilder();
	String version = "Unknown";
	Properties properties = new Properties();
	try {
	    properties.load(getClass().getClassLoader().getResourceAsStream(AppUtils.PROP_NAME + ".props"));
	    version = properties.getProperty(AppUtils.PROP_PROJECT_VERSION, version);
	} catch(IOException ioex){
	    
	}
	sb.append("Community Detection (");
	sb.append(version);
	sb.append(") is a Cytoscape App that includes ");
	sb.append("a framework, and remote service, to enable access to<br/>");
	sb.append("other popular Community Detection (CD) algorithms ");
	sb.append("capable of hierarchical construction.<br/><br/>In addition, ");
	sb.append("this App offers access to Gene Ontology enrichment (functional enrichment) ");
	sb.append("for annotation of<br/>community nodes to gain ");
	sb.append("biological insight.<br/><br/>");
	sb.append("<b>NOTE:</b> This service is experimental. The interface is subject to change.<br/><br/>");
	sb.append("<a href=\"https://github.com/idekerlab/cy-community-detection\">Click here for details and to report issues</a>");
	
	_editorPane = _editorPaneFactory.getDescriptionFrame(sb.toString());
    }

    private void loadImageIcon(){
	    try {
		File imgFile = File.createTempFile("about_icon", "png");
	        InputStream imgStream = getClass().getClassLoader().getResourceAsStream("about_icon.png");
		Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		_aboutIcon = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
	    }
	    catch (IOException ex){
		    
	    }
	}
    
    /**
     * Brings up an about dialog and then calls a dummy task 
     * that does nothing
     * @param network
     * @return TaskIterator with a dummy task
     */
    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
		createAboutEditorPaneIfNeeded();
		_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
			_editorPane, AppUtils.APP_NAME,
			JOptionPane.INFORMATION_MESSAGE, _aboutIcon);
		return new TaskIterator(new DoNothingTask());
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
