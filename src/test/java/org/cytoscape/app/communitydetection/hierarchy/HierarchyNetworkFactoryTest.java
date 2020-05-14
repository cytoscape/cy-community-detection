package org.cytoscape.app.communitydetection.hierarchy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.HashMap;
import java.util.Map;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class HierarchyNetworkFactoryTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();
	
	@Test
	public void testGetHierarchyNetworkWithNullArgs(){
		CyNetworkFactory networkFactory = _nts.getNetworkFactory();
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNetworkNaming networkNaming = mock(CyNetworkNaming.class);
		CyNetworkManager networkManager = _nts.getNetworkManager();
		CyRootNetworkManager rootNetworkManager = mock(CyRootNetworkManager.class);
		
		HierarchyNetworkFactory hnf = new HierarchyNetworkFactory(networkFactory,
		networkNaming,rootNetworkManager, networkManager);
		CommunityDetectionResult cdResult = new CommunityDetectionResult();
		
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		Map<String, String> customParameters = new HashMap<>();
		try {
			hnf.getHierarchyNetwork(null, cdResult,
					"weight", cda, customParameters);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("parent network is null", cde.getMessage());
		}
		
		try {
			hnf.getHierarchyNetwork(parentNetwork, null,
					"weight", cda, customParameters);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("community detection object is null", cde.getMessage());
		}
		
		try {
			hnf.getHierarchyNetwork(parentNetwork, cdResult,
					"weight", null, customParameters);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("algorithm is null", cde.getMessage());
		}
	}
	
	@Test
	public void testGetHierarchyNetworkNullResult() throws CommunityDetectionException {
		CyNetworkFactory networkFactory = _nts.getNetworkFactory();
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNetworkNaming networkNaming = mock(CyNetworkNaming.class);
		CyNetworkManager networkManager = _nts.getNetworkManager();
		CyRootNetworkManager rootNetworkManager = mock(CyRootNetworkManager.class);
		
		HierarchyNetworkFactory hnf = new HierarchyNetworkFactory(networkFactory,
		networkNaming,rootNetworkManager, networkManager);
		CommunityDetectionResult cdResult = new CommunityDetectionResult();
		
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		Map<String, String> customParameters = new HashMap<>();
		try {
			hnf.getHierarchyNetwork(parentNetwork, cdResult,
					"weight", cda, customParameters);
			fail("Expected exception");
		} catch(CommunityDetectionException cde){
			assertEquals("community detection result is null", cde.getMessage());
		}
	}
	
	@Test
	public void testGetHierarchyNetworkOldEdgeListFormat() throws CommunityDetectionException {
		CyNetworkFactory networkFactory = _nts.getNetworkFactory();
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNetworkNaming networkNaming = mock(CyNetworkNaming.class);
		CyNetworkManager networkManager = _nts.getNetworkManager();
		CyRootNetworkManager rootNetworkManager = mock(CyRootNetworkManager.class);
		
		HierarchyNetworkFactory hnf = new HierarchyNetworkFactory(networkFactory,
		networkNaming,rootNetworkManager, networkManager);
		CommunityDetectionResult cdResult = new CommunityDetectionResult();
		TextNode tn = new TextNode("11022,10030,c-m;11022,1005,c-m;");
		cdResult.setResult(tn);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		Map<String, String> customParameters = new HashMap<>();
		
		String edgeStr = tn.asText().trim();
		EdgeStringNetworkUpdator mockEdgeUpdator = mock(EdgeStringNetworkUpdator.class);
		when(mockEdgeUpdator.updateNetworkWithEdgeString(eq(parentNetwork), any(CyNetwork.class), eq(edgeStr))).thenReturn(null);
		hnf.setAlternateEdgeStringNetworkUpdator(mockEdgeUpdator);

		MemberListNetworkUpdator mockMemberUpdator = mock(MemberListNetworkUpdator.class);
		hnf.setAlternateMemberListNetworkUpdator(mockMemberUpdator);
		
		AttributeNetworkUpdator mockAttribUpdator = mock(AttributeNetworkUpdator.class);
		hnf.setAlternateAttributeNetworkUpdator(mockAttribUpdator);
		
		CyNetwork newNet = hnf.getHierarchyNetwork(parentNetwork, cdResult,
				              	"weight", cda, customParameters);
	}
	
	@Test
	public void testGetHierarchyNetworkV2Format() throws Exception {
		CyNetworkFactory networkFactory = _nts.getNetworkFactory();
		CyNetwork parentNetwork = _nts.getNetwork();
		CyNetworkNaming networkNaming = mock(CyNetworkNaming.class);
		CyNetworkManager networkManager = _nts.getNetworkManager();
		CyRootNetworkManager rootNetworkManager = mock(CyRootNetworkManager.class);
		
		HierarchyNetworkFactory hnf = new HierarchyNetworkFactory(networkFactory,
		networkNaming,rootNetworkManager, networkManager);
		CommunityDetectionResult cdResult = new CommunityDetectionResult();
		String jsonStr = "{\"communityDetectionResult\": \"11022\", \"nodeAttributesAsCX2\": {\"attributeDeclarations\": [{\"nodes\": { \"col\": { \"d\": \"integer\", \"a\": \"p1\", \"v\": 0}}}],\"nodes\": [{\"id\": 11022,\"v\": { \"p1\": 0}}]}}";
		ObjectMapper om = new ObjectMapper();
		JsonNode res = om.readTree(jsonStr);
		cdResult.setResult(res);
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		Map<String, String> customParameters = new HashMap<>();
		
		CustomDataNetworkUpdator mockCustomUpdator = mock(CustomDataNetworkUpdator.class);
		hnf.setAlternateCustomDataNetworkUpdator(mockCustomUpdator);
		
		EdgeStringNetworkUpdator mockEdgeUpdator = mock(EdgeStringNetworkUpdator.class);
		when(mockEdgeUpdator.updateNetworkWithEdgeString(eq(parentNetwork), any(CyNetwork.class), eq("11022"))).thenReturn(null);
		hnf.setAlternateEdgeStringNetworkUpdator(mockEdgeUpdator);

		MemberListNetworkUpdator mockMemberUpdator = mock(MemberListNetworkUpdator.class);
		hnf.setAlternateMemberListNetworkUpdator(mockMemberUpdator);
		
		AttributeNetworkUpdator mockAttribUpdator = mock(AttributeNetworkUpdator.class);
		hnf.setAlternateAttributeNetworkUpdator(mockAttribUpdator);
		
		CyNetwork newNet = hnf.getHierarchyNetwork(parentNetwork, cdResult,
				              	"weight", cda, customParameters);
	}
}
