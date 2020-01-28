package org.cytoscape.app.communitydetection.util;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 * 
 * @author churas
 */
public class ShowDialogUtil {
	
	
	public void showMessageDialog(Component parentComponent,
			Object message, String title, int messageType, Icon icon){
		JOptionPane.showMessageDialog(parentComponent, message, title, messageType, icon);
	}
	
	public void showMessageDialog(Component parentComponent, Object message){
		JOptionPane.showMessageDialog(parentComponent, message);
	}
}
