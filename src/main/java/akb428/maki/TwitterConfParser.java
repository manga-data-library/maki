package akb428.maki;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import akb428.maki.model.TwitterModel;

public class TwitterConfParser {
	public static TwitterModel readConf(String confFilename)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readValue(new File(confFilename), JsonNode.class);

		TwitterModel twitterModel = new TwitterModel();
		twitterModel.setAccessToken(rootNode.get("access_token").asText());
		twitterModel.setAccessToken_secret(rootNode.get("access_token_secret").asText());
		twitterModel.setConsumerKey(rootNode.get("consumer_key").asText());
		twitterModel.setConsumerSecret(rootNode.get("consumer_secret").asText());

		return twitterModel;
	}
}
