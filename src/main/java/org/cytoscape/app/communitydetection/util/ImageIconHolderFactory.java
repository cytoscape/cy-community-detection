package org.cytoscape.app.communitydetection.util;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create ImageIconHolder objects
 * @author churas
 */
public class ImageIconHolderFactory {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ImageIconHolderFactory.class);
	
	public ImageIconHolderFactory(){
		
	}
	
	/**
	 * Given a resource prefix {@code iconResourcePrefix} and suffix 
	 * {@code iconResourceSuffix} return an ImageIconHolder object
	 * properly loaded. If {@code smallSize} or {@code largeSize} is
	 * 0 or less or if there is an error 
	 * @param iconResourcePrefix Prefix of resource ie info_icon
	 * @param iconResourceSuffix Suffix of resource ie png
	 * @param smallSize size to scale small icon ie 20
	 * @param largeSize size to scale large icon ie 40
	 * @return ImageIconHolder upon success or {@code null} if 
	 *         {@code smallSize} or {@code largeSize} is 0 or
	 *         less or if there is an error
	 */
	public ImageIconHolder getImageIconHolder(final String iconResourcePrefix,
			final String iconResourceSuffix, int smallSize,
			int largeSize){
		if (iconResourcePrefix == null){
			LOGGER.error("iconResourcePrefix is null");
			return null;
		}
		if (iconResourceSuffix == null){
			LOGGER.error("iconResourcePrefix is null");
			return null;
		}
		if (smallSize <= 0){
			LOGGER.error("requested small size of icon is 0 or less");
			return null;
		}
		if (largeSize <= 0){
			LOGGER.error("requested large size of icon is 0 or less");
			return null;
		}
		try {
		    File imgFile = File.createTempFile(iconResourcePrefix, iconResourceSuffix);
	        InputStream imgStream = getClass().getClassLoader().getResourceAsStream(iconResourcePrefix + "." + iconResourceSuffix);
			if (imgStream == null){
				LOGGER.error("Image stream for icon is null");
				return null;
			}
			Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			ImageIcon small = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(smallSize, smallSize, Image.SCALE_SMOOTH));
			ImageIcon large = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(largeSize, largeSize, Image.SCALE_SMOOTH));
			return new ImageIconHolder(small, large);
		}
	    catch (IOException ex){
		    LOGGER.error("Caught exception trying to load icon: ", ex);
	    }
		return null;
	}
}
