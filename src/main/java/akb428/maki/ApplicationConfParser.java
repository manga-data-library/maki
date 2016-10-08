package akb428.maki;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import akb428.maki.model.HbaseConfModel;
import akb428.maki.model.MediaConfModel;

public class ApplicationConfParser {

	public JsonNode rootNode;

	public ApplicationConfParser(String confFilename)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		rootNode = mapper.readValue(new File(confFilename), JsonNode.class);

	}

	public HbaseConfModel getHbaseConfModel() {

		HbaseConfModel hbaseConfModel = new HbaseConfModel();
		JsonNode node = rootNode.get("HBase");

		hbaseConfModel.setExecute(node.get("execute").asBoolean());
		// resource
		JsonNode resouceNode = node.get("resource");

		List<String> resouceArray = new ArrayList<String>();

		for (int i = 0; i < resouceNode.size(); i++) {
			resouceArray.add(resouceNode.path(i).asText());
		}

		hbaseConfModel.setResource(resouceArray);

		return hbaseConfModel;
	}
	
	public MediaConfModel getMediaConfModel () {
		MediaConfModel mediaConf = new MediaConfModel();
		
		JsonNode node = rootNode.get("Media");

		mediaConf.setExecute(node.get("execute").asBoolean());
		
		return mediaConf;
	}
}