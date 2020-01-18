package org.cytoscape.app.communitydetection.rest;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Collections;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

/**
 *
 * @author churas
 */
public class CDRestClientTest {
    
 
    @Test
    public void testGetIsTaskCanceled(){
	CDRestClient client = CDRestClient.getInstance();
	client.setTaskCanceled(false);
	assertFalse(client.getIsTaskCanceled());
	client.setTaskCanceled(true);
	assertTrue(client.getIsTaskCanceled());
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
	assertEquals(" : ", client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals(" :  : mymessage", client.getErrorMessageFromResult(cdr));
    }
    
    @Test
    public void testgetErrorMessageFromResultWithTextResult(){
	CommunityDetectionResult cdr = new CommunityDetectionResult();
	CDRestClient client = CDRestClient.getInstance();
	cdr.setResult(new TextNode("hello"));
	assertEquals(" : hello", client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals(" : hello : mymessage", client.getErrorMessageFromResult(cdr));
    }
    
    @Test
    public void testgetErrorMessageFromResultWithNonTextResult(){
	CommunityDetectionResult cdr = new CommunityDetectionResult();
	CDRestClient client = CDRestClient.getInstance();
	cdr.setResult(BooleanNode.TRUE);
	assertEquals(" : ", client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals(" :  : mymessage", client.getErrorMessageFromResult(cdr));
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
	assertEquals(" : " + truncStr, client.getErrorMessageFromResult(cdr));
	cdr.setMessage("mymessage");
	assertEquals(" : " + truncStr + " : mymessage", client.getErrorMessageFromResult(cdr));
    }
}
