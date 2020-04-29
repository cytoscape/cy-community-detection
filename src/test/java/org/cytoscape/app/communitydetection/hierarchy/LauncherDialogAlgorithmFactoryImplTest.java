package org.cytoscape.app.communitydetection.hierarchy;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.cytoscape.app.communitydetection.rest.CDRestClient;
import org.cytoscape.app.communitydetection.rest.CDRestClientException;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import static org.mockito.Mockito.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithms;

/**
 *
 * @author churas
 */
public class LauncherDialogAlgorithmFactoryImplTest {
	
	@Test
	public void testGetAlgorithmsNullResponse() throws Exception {
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CDRestClient mockClient = mock(CDRestClient.class);
		
		when(mockClient.getAlgorithms(true)).thenReturn(null);
		LauncherDialogAlgorithmFactoryImpl fac = new LauncherDialogAlgorithmFactoryImpl(mockClient, mockDialog);
		
		List<CommunityDetectionAlgorithm> algos = fac.getAlgorithms(null, "hello", true);
		assertNull(algos);
		verify(mockClient).getAlgorithms(true);
	}
	
	@Test
	public void testGetAlgorithmsRaisesCDRestClientException() throws Exception {
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CDRestClient mockClient = mock(CDRestClient.class);
		
		when(mockClient.getAlgorithms(false)).thenThrow(new CDRestClientException("yo"));
		LauncherDialogAlgorithmFactoryImpl fac = new LauncherDialogAlgorithmFactoryImpl(mockClient, mockDialog);
		
		List<CommunityDetectionAlgorithm> algos = fac.getAlgorithms(null, "hello", false);
		assertNull(algos);
		verify(mockClient, times(2)).getAlgorithms(false);
		verify(mockDialog).showMessageDialog(null, "Unable to get list of algorithms from service: yo : ");
	}
	
	@Test
	public void testGetAlgorithmsRaisesIOException() throws Exception {
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CDRestClient mockClient = mock(CDRestClient.class);
		
		when(mockClient.getAlgorithms(true)).thenThrow(new IOException("yo"));
		LauncherDialogAlgorithmFactoryImpl fac = new LauncherDialogAlgorithmFactoryImpl(mockClient, mockDialog);
		
		List<CommunityDetectionAlgorithm> algos = fac.getAlgorithms(null, "hello", true);
		assertNull(algos);
		verify(mockClient, times(2)).getAlgorithms(true);
		verify(mockDialog).showMessageDialog(null, "Unable to get list of algorithms from service: yo");
	}
	
	@Test
	public void testGetAlgorithmsSuccess() throws Exception {
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CDRestClient mockClient = mock(CDRestClient.class);
		CommunityDetectionAlgorithms cda = new CommunityDetectionAlgorithms();
		HashMap<String, CommunityDetectionAlgorithm> cMap = new HashMap<>();
		CommunityDetectionAlgorithm algoOne = new CommunityDetectionAlgorithm();
		algoOne.setName("algo1");
		algoOne.setInputDataFormat("fooey");
		cMap.put(algoOne.getName(), algoOne);
		
		CommunityDetectionAlgorithm algoTwo = new CommunityDetectionAlgorithm();
		algoTwo.setName("algo2");
		algoTwo.setInputDataFormat("blah");
		cMap.put(algoTwo.getName(), algoTwo);
		cda.setAlgorithms(cMap);
		
		when(mockClient.getAlgorithms(false)).thenReturn(cda);
		LauncherDialogAlgorithmFactoryImpl fac = new LauncherDialogAlgorithmFactoryImpl(mockClient, mockDialog);
		
		List<CommunityDetectionAlgorithm> algos = fac.getAlgorithms(null, "fooey", false);
		assertEquals(1, algos.size());
		assertEquals("algo1", algos.get(0).getName());
		verify(mockClient, times(1)).getAlgorithms(false);
	}
	
	@Test
	public void testGetAlgorithmsSuccessNullPassedInForAlgoType() throws Exception {
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		CDRestClient mockClient = mock(CDRestClient.class);
		CommunityDetectionAlgorithms cda = new CommunityDetectionAlgorithms();
		HashMap<String, CommunityDetectionAlgorithm> cMap = new HashMap<>();
		CommunityDetectionAlgorithm algoOne = new CommunityDetectionAlgorithm();
		algoOne.setName("algo1");
		algoOne.setInputDataFormat("fooey");
		cMap.put(algoOne.getName(), algoOne);
		
		CommunityDetectionAlgorithm algoTwo = new CommunityDetectionAlgorithm();
		algoTwo.setName("algo2");
		algoTwo.setInputDataFormat("blah");
		cMap.put(algoTwo.getName(), algoTwo);
		cda.setAlgorithms(cMap);
		
		when(mockClient.getAlgorithms(false)).thenReturn(cda);
		LauncherDialogAlgorithmFactoryImpl fac = new LauncherDialogAlgorithmFactoryImpl(mockClient, mockDialog);
		
		List<CommunityDetectionAlgorithm> algos = fac.getAlgorithms(null, null, false);
		assertEquals(2, algos.size());
		verify(mockClient, times(1)).getAlgorithms(false);
	}
}
