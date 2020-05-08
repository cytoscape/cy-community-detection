package org.cytoscape.app.communitydetection.cx2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@code CX2NodeAttributes} from JSON
 * @author churas
 */
public class CX2NodeAttributesFactory {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CX2NodeAttributesFactory.class);

	public CX2NodeAttributes getCX2NodeAttributes(JsonNode nodeAttrsAsCX2){
		if (nodeAttrsAsCX2 == null){
			LOGGER.error("nodeAttrsAsCX2 is null");
			return null;
		}
		CX2NodeAttributes nodeAttrs = null;
		ObjectMapper om = new ObjectMapper();
		try {
			return om.readValue(nodeAttrsAsCX2.traverse(), CX2NodeAttributes.class);
		} catch(IOException io){
			LOGGER.error("caught io exception " + io.getMessage(), io);
		}
		return nodeAttrs;
	}
}
