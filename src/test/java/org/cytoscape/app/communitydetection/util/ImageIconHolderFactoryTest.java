package org.cytoscape.app.communitydetection.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class ImageIconHolderFactoryTest {
	
	@Test
	public void testNullPrefix(){
		ImageIconHolderFactory iconFac = new ImageIconHolderFactory();
		ImageIconHolder iih = iconFac.getImageIconHolder(null, "png", 20, 40);
		assertNull(iih);
	}
	
	@Test
	public void testNullSuffix(){
		ImageIconHolderFactory iconFac = new ImageIconHolderFactory();
		ImageIconHolder iih = iconFac.getImageIconHolder("about_icon", null, 20, 40);
		assertNull(iih);
	}
	
	@Test
	public void testSmallSizeEqualsZero(){
		ImageIconHolderFactory iconFac = new ImageIconHolderFactory();
		ImageIconHolder iih = iconFac.getImageIconHolder("about_icon", "png", 0, 40);
		assertNull(iih);
	}
	
	@Test
	public void testLargeSizeEqualsZero(){
		ImageIconHolderFactory iconFac = new ImageIconHolderFactory();
		ImageIconHolder iih = iconFac.getImageIconHolder("about_icon", "png", 20, 0);
		assertNull(iih);
	}
	
	@Test
	public void testSuccess(){
		ImageIconHolderFactory iconFac = new ImageIconHolderFactory();
		ImageIconHolder iih = iconFac.getImageIconHolder("info_icon", "png", 20, 40);
		assertEquals(20, iih.getSmallIcon().getIconHeight());
		assertEquals(20, iih.getSmallIcon().getIconWidth());
		assertEquals(40, iih.getLargeIcon().getIconHeight());
		assertEquals(40, iih.getLargeIcon().getIconWidth());
		assertNotNull(iih.getSmallIcon().getImage());
		assertNotNull(iih.getLargeIcon().getImage());
	}
	
	@Test
	public void testNonExistantIcon(){
		ImageIconHolderFactory iconFac = new ImageIconHolderFactory();
		ImageIconHolder iih = iconFac.getImageIconHolder("doesnotexist", "png", 20, 40);
		assertNull(iih);
	}
}
