package org.cytoscape.app.communitydetection.hierarchy;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import static org.mockito.Mockito.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author churas
 */
public class HierarchyNetworkViewFactoryTest {
	
	@Test
	public void testGetHierarchyNetworkViewNetworkIsNull(){
		HierarchyNetworkViewFactory fac = new HierarchyNetworkViewFactory(null,
		                                      null, null, null, null);
		
		VisualStyle mockStyle = mock(VisualStyle.class);
		CyLayoutAlgorithm mockLayoutAlgo = mock(CyLayoutAlgorithm.class);
		
		assertNull(fac.getHierarchyNetworkView(null, mockStyle, mockLayoutAlgo));
	}
	
	@Test
	public void testGetHierarchyNetworkViewStyleIsNull(){
		HierarchyNetworkViewFactory fac = new HierarchyNetworkViewFactory(null,
		                                      null, null, null, null);
		
		CyNetwork mockNetwork = mock(CyNetwork.class);
		CyLayoutAlgorithm mockLayoutAlgo = mock(CyLayoutAlgorithm.class);
		
		assertNull(fac.getHierarchyNetworkView(mockNetwork, null, mockLayoutAlgo));
	}
	
	@Test
	public void testGetHierarchyNetworkViewLayoutIsNull(){
		HierarchyNetworkViewFactory fac = new HierarchyNetworkViewFactory(null,
		                                      null, null, null, null);
		
		CyNetwork mockNetwork = mock(CyNetwork.class);
		VisualStyle mockStyle = mock(VisualStyle.class);

		assertNull(fac.getHierarchyNetworkView(mockNetwork, mockStyle, null));
	}
	
	@Test
	public void testGetHierarchyNetworkViewSuccess(){
		
		CyNetworkViewFactory mockNetworkViewFactory = mock(CyNetworkViewFactory.class);
		VisualMappingManager mockVisualMappingManager = mock(VisualMappingManager.class);
		SynchronousTaskManager mockTaskManager = mock(SynchronousTaskManager.class);
		CyLayoutAlgorithmManager mockLayoutAlgorithmManager = mock(CyLayoutAlgorithmManager.class);
		CyNetworkViewManager mockNetworkViewManager = mock(CyNetworkViewManager.class);
		
		HierarchyNetworkViewFactory fac = new HierarchyNetworkViewFactory(mockNetworkViewManager,
		                                      mockNetworkViewFactory,
				mockVisualMappingManager, mockLayoutAlgorithmManager, mockTaskManager);
		
		CyNetworkView mockView = mock(CyNetworkView.class);
		VisualStyle mockStyle = mock(VisualStyle.class);
		CyNetwork mockNetwork = mock(CyNetwork.class);
		CyLayoutAlgorithm mockLayout = mock(CyLayoutAlgorithm.class);
		
		when(mockNetworkViewFactory.createNetworkView(mockNetwork)).thenReturn(mockView);
		when(mockLayoutAlgorithmManager.getDefaultLayout()).thenReturn(mockLayout);
		CyLayoutAlgorithm mockLayoutAlgo = mock(CyLayoutAlgorithm.class);
		String layoutContext = "hi";
		when(mockLayout.createLayoutContext()).thenReturn(layoutContext);
		Task[] tList = new Task[1];
		TaskIterator mockTasks = new TaskIterator(0, tList);
		when(mockLayout.createTaskIterator(eq(mockView), eq(layoutContext),
				eq(CyLayoutAlgorithm.ALL_NODE_VIEWS), eq(null))).thenReturn(mockTasks);
		CyNetworkView view = fac.getHierarchyNetworkView(mockNetwork, mockStyle, mockLayoutAlgo);
		assertEquals(mockView, view);
		
		verify(mockVisualMappingManager, times(1)).setVisualStyle(mockStyle, mockView);
		verify(mockStyle, times(1)).apply(mockView);
		verify(mockTaskManager, times(1)).execute(mockTasks);
		verify(mockNetworkViewManager, times(1)).addNetworkView(mockView);
	}
	
	
}
