package org.cytoscape.app.communitydetection.hierarchy;

import java.util.LinkedHashMap;
import java.util.Map;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class AttributeNetworkUpdatorTest {
	
	private NetworkTestSupport _nts = new NetworkTestSupport();

	@Test
	public void setNetworkAttributesNoWeightNoNDExUUIDNoCustomParams() throws CommunityDetectionException {
		CyNetwork parentNetwork = _nts.getNetwork();
		parentNetwork.getRow(parentNetwork).set(CyNetwork.NAME, "parent");
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		CyRootNetworkManager rootNetworkManager = _nts.getRootNetworkFactory();
		CyNetworkNaming networkNaming = mock(CyNetworkNaming.class);

		when(networkNaming.getSuggestedNetworkTitle("foo_parent")).thenReturn("foo_parent");
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		AttributeNetworkUpdator updator = new AttributeNetworkUpdator(rootNetworkManager, networkNaming);
		
		CommunityDetectionResult cdResult = new CommunityDetectionResult();
		
		updator.setNetworkAttributes(parentNetwork, hierarchyNetwork, null, cda, cdResult, null);
		
		assertEquals("foo_parent", hierarchyNetwork.getRow(hierarchyNetwork).get(CyNetwork.NAME,
				String.class));
		
		String desc = hierarchyNetwork.getRow(hierarchyNetwork).get(AppUtils.COLUMN_DESCRIPTION, String.class);
		assertEquals("Original network: parent\n"
				   + "Algorithm used for community detection: foo\n"
				   + "Edge table column used as weight: no column used\n"
				   + "CustomParameters: {}", desc);
		
		String derivedFrom = hierarchyNetwork.getRow(hierarchyNetwork).get(AppUtils.COLUMN_DERIVED_FROM,
				String.class);
		
		assertEquals("parent", derivedFrom);
		String generatedBy = hierarchyNetwork.getRow(hierarchyNetwork).get(AppUtils.COLUMN_GENERATED_BY,
				String.class);
		assertTrue(generatedBy.startsWith("App: CyCommunityDetection ("));
		assertTrue(generatedBy.endsWith(") Docker Image: null"));

	}
	
	@Test
	public void setNetworkAttributesWithWeightAndNDExUUID() throws CommunityDetectionException {
		CyNetwork parentNetwork = _nts.getNetwork();
		parentNetwork.getRow(parentNetwork).set(CyNetwork.NAME, "parent");
		
		CyNetwork hierarchyNetwork = _nts.getNetwork();
		CyRootNetworkManager rootNetworkManager = _nts.getRootNetworkFactory();
		CyNetworkNaming networkNaming = mock(CyNetworkNaming.class);

		when(networkNaming.getSuggestedNetworkTitle("foo_weightcol_parent")).thenReturn("foo_weightcol_parent");
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		AttributeNetworkUpdator updator = new AttributeNetworkUpdator(rootNetworkManager, networkNaming);
		
		CommunityDetectionResult cdResult = new CommunityDetectionResult();
		
		Map<String, String> cParams = new LinkedHashMap<>();
		cParams.put("-a", "val");
		cParams.put("-b", "val2");
		updator.setNetworkAttributes(parentNetwork, hierarchyNetwork, "weightcol", cda, cdResult, cParams);
		
		assertEquals("foo_weightcol_parent", hierarchyNetwork.getRow(hierarchyNetwork).get(CyNetwork.NAME,
				String.class));
		
		String desc = hierarchyNetwork.getRow(hierarchyNetwork).get(AppUtils.COLUMN_DESCRIPTION, String.class);
		assertEquals("Original network: parent\n"
				   + "Algorithm used for community detection: foo\n"
				   + "Edge table column used as weight: weightcol\n"
				   + "CustomParameters: {-a=val, -b=val2}", desc);
		
		String derivedFrom = hierarchyNetwork.getRow(hierarchyNetwork).get(AppUtils.COLUMN_DERIVED_FROM,
				String.class);
		
		assertEquals("parent", derivedFrom);
		String generatedBy = hierarchyNetwork.getRow(hierarchyNetwork).get(AppUtils.COLUMN_GENERATED_BY,
				String.class);
		assertTrue(generatedBy.startsWith("App: CyCommunityDetection ("));
		assertTrue(generatedBy.endsWith(") Docker Image: null"));

	}
}
