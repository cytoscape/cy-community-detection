package org.cytoscape.app.communitydetection.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.cytoscape.app.communitydetection.PropertiesHelper;
import org.cytoscape.work.TaskMonitor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithms;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;
import org.ndexbio.communitydetection.rest.model.Task;

/**
 *
 * @author churas
 */
public class CDRestClientTest {
    

	@Test
	public void testGetBaseurlIsNull() throws IOException, CDRestClientException {
		CDRestClient client = CDRestClient.getInstance();
		PropertiesHelper.getInstance().setBaseurl(null);
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.deleteTask("12345");
			fail("Expected exception");
		} catch(NullPointerException npe){
			assertEquals("REST Service URL is null", npe.getMessage());
		}
	}
	
	@Test
	public void testGetClientWithNullHttpClient() throws IOException, CDRestClientException {
		CDRestClient client = CDRestClient.getInstance();
		PropertiesHelper.getInstance().setBaseurl("http://foo");
		client.setAlternateHttpClientFactory(null);
		try {
			client.deleteTask("12345");
			fail("Expected exception");
		} catch(NullPointerException npe){
			assertEquals("HttpClientFactory is null", npe.getMessage());
		}
	}
	
	@Test
	public void testDeleteTaskNullPassedIn() throws Exception {
		CDRestClient client = CDRestClient.getInstance();
		try {
			client.deleteTask(null);
			fail("Expected IllegalArgumentException");
		}
		catch(IllegalArgumentException iae){
			assertEquals("task id cannot be null", iae.getMessage());
		}
	}
	
    @Test
    public void testDeleteTaskSuccess() throws Exception {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");

		
		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		when(mockClient.execute(any(HttpDelete.class))).thenReturn(mockRes);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		client.deleteTask("mytask");
		ArgumentCaptor<HttpDelete> hdel = ArgumentCaptor.forClass(HttpDelete.class);
		verify(mockClient).execute(hdel.capture());
		assertEquals("http://foo/mytask", hdel.getValue().getURI().toString());

    }
	
	@Test
    public void testDeleteTaskFail() throws Exception {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");

		
		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		when(mockClient.execute(any(HttpDelete.class))).thenReturn(mockRes);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		client.deleteTask("mytask");
		ArgumentCaptor<HttpDelete> hdel = ArgumentCaptor.forClass(HttpDelete.class);
		verify(mockClient).execute(hdel.capture());
		assertEquals("http://foo/mytask", hdel.getValue().getURI().toString());
    }
 
	@Test
    public void testGetAlgorithmsTaskFailNoErrorMessage() throws Exception {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");

		
		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockRes);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.getAlgorithms(true);
			fail("Expected Exception");
		} catch(CDRestClientException ce){
			assertEquals("Unable to get list of "
					+ "algorithms from CD Service (HTTP Status Code: 500)", ce.getMessage());
		}
		ArgumentCaptor<HttpGet> hget = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockClient).execute(hget.capture());
		assertEquals("http://foo/algorithms", hget.getValue().getURI().toString());

    }
	
	@Test
    public void testGetAlgorithmsTaskFailWithErrorResponse() throws Exception {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		HttpEntity mockEntity = mock(HttpEntity.class);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");

		
		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockRes);
		ErrorResponse foo = new ErrorResponse();
		foo.setDescription("desc");
		foo.setMessage("msg");
		InputStream iStream = new ByteArrayInputStream(foo.asJson().getBytes());
		when(mockEntity.getContent()).thenReturn(iStream);
		when(mockRes.getEntity()).thenReturn(mockEntity);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.getAlgorithms(true);
			fail("Expected Exception");
		} catch(CDRestClientException ce){
			assertEquals("Unable to get list of "
					+ "algorithms from CD Service (HTTP Status Code: 500)", ce.getMessage());
			assertNotNull(ce.getErrorResponse());
			assertEquals("desc", ce.getErrorResponse().getDescription());
		}
		ArgumentCaptor<HttpGet> hget = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockClient).execute(hget.capture());
		assertEquals("http://foo/algorithms", hget.getValue().getURI().toString());
    }
	
	@Test
    public void testGetAlgorithmsTaskNoBytesInResponse() throws Exception {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		HttpEntity mockEntity = mock(HttpEntity.class);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");

		
		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockRes);
		ErrorResponse foo = new ErrorResponse();
		foo.setDescription("desc");
		foo.setMessage("msg");
		InputStream iStream = new ByteArrayInputStream(foo.asJson().getBytes());
		when(mockEntity.getContent()).thenReturn(iStream);
		when(mockRes.getEntity()).thenReturn(mockEntity);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.getAlgorithms(true);
			fail("Expected Exception");
		} catch(CDRestClientException ce){
			assertEquals("Unable to get list of "
					+ "algorithms from CD Service (HTTP Status Code: 500)", ce.getMessage());
			assertNotNull(ce.getErrorResponse());
			assertEquals("desc", ce.getErrorResponse().getDescription());
		}
		ArgumentCaptor<HttpGet> hget = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockClient).execute(hget.capture());
		assertEquals("http://foo/algorithms", hget.getValue().getURI().toString());
    }
	
	@Test
    public void testGetAlgorithmsSuccess() throws Exception {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		HttpEntity mockEntity = mock(HttpEntity.class);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");

		
		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockRes);
		CommunityDetectionAlgorithms cdAlgos = new CommunityDetectionAlgorithms();
		CommunityDetectionAlgorithm cd = new CommunityDetectionAlgorithm();
		cd.setName("hello");
		HashMap<String, CommunityDetectionAlgorithm> algoMap = new HashMap<>();
		algoMap.put(cd.getName(), cd);
		cdAlgos.setAlgorithms(algoMap);

		ObjectMapper mapper = new ObjectMapper();
		
		InputStream iStream = new ByteArrayInputStream(mapper.writeValueAsBytes(cdAlgos));
		when(mockEntity.getContent()).thenReturn(iStream);
		when(mockRes.getEntity()).thenReturn(mockEntity);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		CommunityDetectionAlgorithms algos = client.getAlgorithms(true);
		assertEquals(1, algos.getAlgorithms().size());
		assertEquals("hello", algos.getAlgorithms().get("hello").getName());
		
		// check caching works
		algos = client.getAlgorithms(false);
		assertEquals(1, algos.getAlgorithms().size());
		assertEquals("hello", algos.getAlgorithms().get("hello").getName());
		
		ArgumentCaptor<HttpGet> hget = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockClient).execute(hget.capture());
		assertEquals("http://foo/algorithms", hget.getValue().getURI().toString());
    }
	
    @Test
    public void testGetIsTaskCanceled(){
		CDRestClient client = CDRestClient.getInstance();
		try {
			client.setTaskCanceled(false);
			assertFalse(client.getIsTaskCanceled());
			client.setTaskCanceled(true);
			assertTrue(client.getIsTaskCanceled());
		} finally {
			client.setTaskCanceled(false);
		}
    }
    @Test
    public void testgetErrorMessageFromResultWithNullRequest(){
	CDRestClient client = CDRestClient.getInstance();
	assertEquals("", client.getErrorMessageFromResult(null));
    }
    
    @Test
    public void testgetErrorMessageFromResultWithEmptyResultInRequest(){
	CommunityDetectionResult cdr = new CommunityDetectionResult();
	CDRestClient client = CDRestClient.getInstance();
	assertEquals("", client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals("mymessage", client.getErrorMessageFromResult(cdr));
    }
    
    @Test
    public void testgetErrorMessageFromResultWithTextResult(){
	CommunityDetectionResult cdr = new CommunityDetectionResult();
	CDRestClient client = CDRestClient.getInstance();
	cdr.setResult(new TextNode("hello"));
	assertEquals(" : hello", client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals("mymessage : hello", client.getErrorMessageFromResult(cdr));
    }
    
    @Test
    public void testgetErrorMessageFromResultWithNonTextResult(){
	CommunityDetectionResult cdr = new CommunityDetectionResult();
	CDRestClient client = CDRestClient.getInstance();
	cdr.setResult(BooleanNode.TRUE);
	assertEquals("", client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals("mymessage", client.getErrorMessageFromResult(cdr));
    }
    
    @Test
    public void testgetErrorMessageFromResultWithTruncatedTextResult(){
	String tooLongStr = String.join("",
		Collections.nCopies(CDRestClient.TRUNCATE_ERROR_MESSAGE_RESULT_LEN+5,
			"x"));
	String truncStr = String.join("",
		Collections.nCopies(CDRestClient.TRUNCATE_ERROR_MESSAGE_RESULT_LEN,
			"x"));
	CommunityDetectionResult cdr = new CommunityDetectionResult();
	CDRestClient client = CDRestClient.getInstance();
	cdr.setResult(new TextNode(tooLongStr));
	assertEquals(" : " + truncStr + "...", client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals("mymessage : " + truncStr + "...", client.getErrorMessageFromResult(cdr));
    }
	
	@Test
	public void testGetErrorResponseNull(){
		CDRestClient client = CDRestClient.getInstance();
		assertNull(client.getErrorResponse(null));
	}
	
	@Test
	public void testGetErrorResponseNullEntity(){
		HttpResponse mockRes = mock(HttpResponse.class);
		CDRestClient client = CDRestClient.getInstance();
		assertNull(client.getErrorResponse(mockRes));
		verify(mockRes, times(1)).getEntity();
	}
	
	@Test
	public void testGetErrorResponseIOException() throws IOException{
		HttpResponse mockRes = mock(HttpResponse.class);
		HttpEntity mockEntity = mock(HttpEntity.class);
		when(mockEntity.getContent()).thenThrow(new IOException("error"));
		when(mockRes.getEntity()).thenReturn(mockEntity);
		CDRestClient client = CDRestClient.getInstance();
		assertNull(client.getErrorResponse(mockRes));
		verify(mockRes, times(1)).getEntity();
		verify(mockEntity, times(1)).getContent();
	}
	
	@Test
	public void testGetErrorResponseSuccess() throws IOException{
		HttpResponse mockRes = mock(HttpResponse.class);
		HttpEntity mockEntity = mock(HttpEntity.class);
		ErrorResponse foo = new ErrorResponse();
		foo.setDescription("desc");
		foo.setMessage("msg");
		InputStream iStream = new ByteArrayInputStream(foo.asJson().getBytes());
		when(mockEntity.getContent()).thenReturn(iStream);
		when(mockRes.getEntity()).thenReturn(mockEntity);
		CDRestClient client = CDRestClient.getInstance();
		ErrorResponse res = client.getErrorResponse(mockRes);
		assertEquals("desc", res.getDescription());
		assertEquals("msg", res.getMessage());
		verify(mockRes, times(1)).getEntity();
		verify(mockEntity, times(1)).getContent();
	}
	
	@Test
	public void testGetCDResultWithNullTaskId(){
		CDRestClient client = CDRestClient.getInstance();
		try{
			client.getCDResult(null, 0);
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException iae){
			assertEquals("task id cannot be null", iae.getMessage());
		} catch(CDRestClientException ce){
			fail("Did not expect this CDRestClientException: " + ce.getMessage());
		} catch(IOException io){
			fail("Did not expect this IOException: " + io.getMessage());
		}
	}
	
	@Test
	public void testGetCDResultTaskIsCanceled() throws CDRestClientException, IOException {
		CDRestClient client = CDRestClient.getInstance();
		try {
			HttpClientFactory mockFac = mock(HttpClientFactory.class);
			HttpClient mockClient = mock(HttpClient.class);
			HttpResponse mockRes = mock(HttpResponse.class);
			StatusLine mockStatus = mock(StatusLine.class);
		
			PropertiesHelper.getInstance().setBaseurl("http://foo");
			when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
			when(mockClient.execute(any(HttpDelete.class))).thenReturn(mockRes);
			when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		
			client.setAlternateHttpClientFactory(mockFac);
			client.setTaskCanceled(true);
			assertNull(client.getCDResult("testGetCDResultTaskIsCanceled", 0));
		} finally {
			client.setTaskCanceled(false);
		}
	}
	
	@Test
	public void testGetCDResultTaskIsSuccessfulFirstCall() throws CDRestClientException, IOException {
		CDRestClient client = CDRestClient.getInstance();
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);


		HttpResponse mockGetRes = mock(HttpResponse.class);
		StatusLine mockGetStatus = mock(StatusLine.class);
		HttpEntity mockGetEntity = mock(HttpEntity.class);
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		cdRes.setStatus(CommunityDetectionResult.COMPLETE_STATUS);
		cdRes.setMessage("some message");
		cdRes.setProgress(50);
		ObjectMapper mapper = new ObjectMapper();
		InputStream iStream = new ByteArrayInputStream(mapper.writeValueAsBytes(cdRes));
		when(mockGetEntity.getContent()).thenReturn(iStream);
		when(mockGetRes.getEntity()).thenReturn(mockGetEntity);
		when(mockGetStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		HttpResponse mockDeleteRes = mock(HttpResponse.class);
		StatusLine mockDeleteStatus = mock(StatusLine.class);

		PropertiesHelper.getInstance().setBaseurl("http://foo");
		when(mockDeleteStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockDeleteRes.getStatusLine()).thenReturn(mockDeleteStatus);
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockGetRes);
		when(mockClient.execute(any(HttpDelete.class))).thenReturn(mockDeleteRes);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);

		client.setAlternateHttpClientFactory(mockFac);
		CommunityDetectionResult res = client.getCDResult("testGetCDResultTaskIsSuccessfulFirstCall", 0);
		assertEquals(50, res.getProgress());
		assertEquals(CommunityDetectionResult.COMPLETE_STATUS, res.getStatus());
		assertEquals("some message", res.getMessage());
		
	}
	
	@Test
	public void testGetCDResultTaskFailsFirstCall() throws CDRestClientException, IOException {
		CDRestClient client = CDRestClient.getInstance();
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);


		HttpResponse mockGetRes = mock(HttpResponse.class);
		StatusLine mockGetStatus = mock(StatusLine.class);
		HttpEntity mockGetEntity = mock(HttpEntity.class);
		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		cdRes.setStatus(CommunityDetectionResult.FAILED_STATUS);
		cdRes.setMessage("i failed");
		cdRes.setProgress(88);
		ObjectMapper mapper = new ObjectMapper();
		InputStream iStream = new ByteArrayInputStream(mapper.writeValueAsBytes(cdRes));
		when(mockGetEntity.getContent()).thenReturn(iStream);
		when(mockGetRes.getEntity()).thenReturn(mockGetEntity);
		when(mockGetStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		HttpResponse mockDeleteRes = mock(HttpResponse.class);
		StatusLine mockDeleteStatus = mock(StatusLine.class);

		PropertiesHelper.getInstance().setBaseurl("http://foo");
		when(mockDeleteStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockDeleteRes.getStatusLine()).thenReturn(mockDeleteStatus);
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockGetRes);
		when(mockClient.execute(any(HttpDelete.class))).thenReturn(mockDeleteRes);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);

		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.getCDResult("testGetCDResultTaskFailsFirstCall", 0);
			fail("Expected Exception");
		} catch(CDRestClientException ce){
			CommunityDetectionResult res = ce.getResult();
			assertEquals(client.getErrorMessageFromResult(res), ce.getMessage());
			assertEquals(88, res.getProgress());
			assertEquals(CommunityDetectionResult.FAILED_STATUS, res.getStatus());
			assertEquals("i failed", res.getMessage());
		}
	}
	
	private HttpResponse getMockHttpResponseWithResult(CommunityDetectionResult cdRes,
			int httpStatus) throws IOException{
		HttpResponse mockGetRes = mock(HttpResponse.class);
		StatusLine mockGetStatus = mock(StatusLine.class);
		HttpEntity mockGetEntity = mock(HttpEntity.class);
		
		ObjectMapper mapper = new ObjectMapper();
		InputStream iStream = new ByteArrayInputStream(mapper.writeValueAsBytes(cdRes));
		when(mockGetEntity.getContent()).thenReturn(iStream);
		when(mockGetRes.getEntity()).thenReturn(mockGetEntity);
		when(mockGetStatus.getStatusCode()).thenReturn(httpStatus);
		return mockGetRes;
	}
	
	@Test
	public void testGetCDResultTaskTimesOut() throws CDRestClientException, IOException {
		CDRestClient client = CDRestClient.getInstance();
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);

		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		cdRes.setStatus(CommunityDetectionResult.SUBMITTED_STATUS);
		cdRes.setMessage("some message");
		cdRes.setProgress(50);
		
		HttpResponse mockGetResOne = getMockHttpResponseWithResult(cdRes, HttpStatus.SC_OK);
		HttpResponse mockGetResTwo = getMockHttpResponseWithResult(cdRes, HttpStatus.SC_OK);

		HttpResponse mockDeleteRes = mock(HttpResponse.class);
		StatusLine mockDeleteStatus = mock(StatusLine.class);

		PropertiesHelper.getInstance().setBaseurl("http://foo");
		PropertiesHelper.getInstance().setPollingIntervalTimeMillis(1);
		when(mockDeleteStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockDeleteRes.getStatusLine()).thenReturn(mockDeleteStatus);
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockGetResOne, mockGetResTwo);
		when(mockClient.execute(any(HttpDelete.class))).thenReturn(mockDeleteRes);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);

		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.getCDResult("testGetCDResultTaskTimesOut", 2);
			fail("Expected CDRestClientException");
		} catch(CDRestClientException ce){
			CommunityDetectionResult res = ce.getResult();
			assertEquals(50, res.getProgress());
			assertEquals(CommunityDetectionResult.SUBMITTED_STATUS, res.getStatus());
			assertEquals("some message", res.getMessage());
		}
	}
	
	@Test
	public void testGetCDResultTaskTimesOutWithTaskMonitor() throws CDRestClientException, IOException {
		CDRestClient client = CDRestClient.getInstance();
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);

		CommunityDetectionResult cdRes = new CommunityDetectionResult();
		cdRes.setStatus(CommunityDetectionResult.SUBMITTED_STATUS);
		cdRes.setMessage("some message");
		cdRes.setProgress(20);
		
		HttpResponse mockGetResOne = getMockHttpResponseWithResult(cdRes, HttpStatus.SC_OK);
		cdRes.setProgress(60);
		HttpResponse mockGetResTwo = getMockHttpResponseWithResult(cdRes, HttpStatus.SC_OK);

		HttpResponse mockDeleteRes = mock(HttpResponse.class);
		StatusLine mockDeleteStatus = mock(StatusLine.class);

		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");
		PropertiesHelper.getInstance().setPollingIntervalTimeMillis(1);
		when(mockDeleteStatus.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		when(mockDeleteRes.getStatusLine()).thenReturn(mockDeleteStatus);
		when(mockClient.execute(any(HttpGet.class))).thenReturn(mockGetResOne, mockGetResTwo);
		when(mockClient.execute(any(HttpDelete.class))).thenReturn(mockDeleteRes);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);

		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.getCDResult("testGetCDResultTaskTimesOutWithTaskMonitor",
					mockMonitor, 0.1f, 0.8f, 2);
			fail("Expected CDRestClientException");
		} catch(CDRestClientException ce){
			CommunityDetectionResult res = ce.getResult();
			assertEquals(60, res.getProgress());
			assertEquals(CommunityDetectionResult.SUBMITTED_STATUS, res.getStatus());
			assertEquals("some message", res.getMessage());
		}
		verify(mockMonitor).setProgress(AdditionalMatchers.eq(0.24f, 0.1f));
		verify(mockMonitor).setProgress(AdditionalMatchers.eq(0.52f, 0.1f));
	}
	
	@Test
	public void testPostCDDataWithNullAlgo() throws CDRestClientException, IOException{
		CDRestClient client = CDRestClient.getInstance();
		try {
			client.postCDData(null, null, "hi");
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException iae){
			assertEquals("algorithm cannot be null", iae.getMessage());
		}
	}
	
	@Test
	public void testPostCDDataFailNoRetry() throws IOException, CDRestClientException {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");
		PropertiesHelper.getInstance().setSubmitRetryCount(1);

		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		when(mockClient.execute(any(HttpPost.class))).thenReturn(mockRes);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.postCDData("foo", null, "somedata");
			fail("Expected CDRestClientException");
		} catch(CDRestClientException ce){
			assertEquals("POST call to http://foo failed. Error code: " +
					Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR), ce.getMessage());
		}
	}
	
	@Test
	public void testPostCDDataTaskCanceled() throws IOException, CDRestClientException {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		PropertiesHelper.getInstance().setBaseurl("http://foo");
		PropertiesHelper.getInstance().setSubmitRetryCount(1);
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		try {
			client.setTaskCanceled(true);
			assertNull(client.postCDData("foo", null, "somedata"));
		} finally {
			client.setTaskCanceled(true);			
		}
	}
	
	@Test
	public void testPostCDDataSuccessFirstTimeWithCustomParams() throws IOException, CDRestClientException {
		HttpClientFactory mockFac = mock(HttpClientFactory.class);
		HttpClient mockClient = mock(HttpClient.class);
		HttpResponse mockRes = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		
		HttpEntity mockEntity = mock(HttpEntity.class);
		
		Task myTask = new Task();
		myTask.setId("taskid");
		ObjectMapper mapper = new ObjectMapper();
		InputStream iStream = new ByteArrayInputStream(mapper.writeValueAsBytes(myTask));
		when(mockEntity.getContent()).thenReturn(iStream);
		when(mockRes.getEntity()).thenReturn(mockEntity);
		
		PropertiesHelper.getInstance().setBaseurl("http://foo");
		PropertiesHelper.getInstance().setSubmitRetryCount(1);

		when(mockStatus.getStatusCode()).thenReturn(HttpStatus.SC_ACCEPTED);
		when(mockFac.getHttpClient(any(RequestConfig.class))).thenReturn(mockClient);
		when(mockClient.execute(any(HttpPost.class))).thenReturn(mockRes);
		when(mockRes.getStatusLine()).thenReturn(mockStatus);
		
		CDRestClient client = CDRestClient.getInstance();
		client.setAlternateHttpClientFactory(mockFac);
		HashMap<String, String> customParams = new HashMap<>();
		customParams.put("-x", "someval");
		String taskId = client.postCDData("foo", customParams, "somedata");
		assertEquals("taskid", taskId);
	}
}
