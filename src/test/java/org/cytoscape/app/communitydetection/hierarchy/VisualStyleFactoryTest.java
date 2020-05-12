package org.cytoscape.app.communitydetection.hierarchy;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author churas
 */
public class VisualStyleFactoryTest {
	
	@Test
	public void testGetVisualStyleAlreadyLoaded(){
		VisualMappingManager mapManager = mock(VisualMappingManager.class);
		Set<VisualStyle> visualStyleSet = new LinkedHashSet<>();
		VisualStyle mockVs = mock(VisualStyle.class);
		when(mockVs.getTitle()).thenReturn("ha");
		visualStyleSet.add(mockVs);
		
		VisualStyle mockVsTwo = mock(VisualStyle.class);
		when(mockVsTwo.getTitle()).thenReturn(VisualStyleFactory.DEFAULT_STYLE_NAME);
		visualStyleSet.add(mockVsTwo);
		
		when(mapManager.getAllVisualStyles()).thenReturn(visualStyleSet);
		
		VisualStyleFactory vsf = new VisualStyleFactory(mapManager, null);
		VisualStyle vs = vsf.getVisualStyle();
		assertEquals(vs, mockVsTwo);
		verify(mockVs, times(1)).getTitle();
		verify(mockVsTwo, times(1)).getTitle();
		verify(mapManager, times(1)).getAllVisualStyles();
	}
	
	@Test
	public void testGetVisualStyleLoadFromResource(){
		VisualMappingManager mapManager = mock(VisualMappingManager.class);
		Set<VisualStyle> visualStyleSet = new HashSet<>();
		VisualStyle mockVs = mock(VisualStyle.class);
		when(mockVs.getTitle()).thenReturn("ha");
		visualStyleSet.add(mockVs);
		when(mapManager.getAllVisualStyles()).thenReturn(visualStyleSet);
		
		LoadVizmapFileTaskFactory mockTaskFac = mock(LoadVizmapFileTaskFactory.class);
		
		VisualStyle mockVsTwo = mock(VisualStyle.class);
		when(mockVsTwo.getTitle()).thenReturn(VisualStyleFactory.DEFAULT_STYLE_NAME);
		Set<VisualStyle> newStyleSet = new HashSet<>();
		newStyleSet.add(mockVsTwo);
		when(mockTaskFac.loadStyles(any(File.class))).thenReturn(newStyleSet);
		
		VisualStyleFactory vsf = new VisualStyleFactory(mapManager, mockTaskFac);
		VisualStyle vs = vsf.getVisualStyle();
		assertEquals(vs, mockVsTwo);
		verify(mockVs, times(1)).getTitle();
		verify(mapManager, times(1)).getAllVisualStyles();
		
		
		
	}
}
