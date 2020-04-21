package org.cytoscape.app.communitydetection.util;

import javax.swing.ImageIcon;

/**
 *
 * @author churas
 */
public class ImageIconHolder {
	
	private ImageIcon _small;
	private ImageIcon _large;
	
	public ImageIconHolder(ImageIcon small, ImageIcon large){
		_small = small;
		_large = large;
	}
	public ImageIcon getSmallIcon(){
		return _small;
	}
	
	public ImageIcon getLargeIcon(){
		return _large;
	}
}
