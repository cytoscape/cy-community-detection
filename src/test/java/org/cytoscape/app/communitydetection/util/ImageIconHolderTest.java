package org.cytoscape.app.communitydetection.util;

import javax.swing.ImageIcon;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class ImageIconHolderTest {
	
	@Test
	public void testGettersAndSetters(){
		ImageIconHolder iih = new ImageIconHolder(null, null);
		assertEquals(null, iih.getSmallIcon());
		assertEquals(null, iih.getLargeIcon());
		ImageIcon small = new ImageIcon();
		ImageIcon large = new ImageIcon();
		
		iih = new ImageIconHolder(small, large);
		assertEquals(small, iih.getSmallIcon());
		assertEquals(large, iih.getLargeIcon());
	}
}
