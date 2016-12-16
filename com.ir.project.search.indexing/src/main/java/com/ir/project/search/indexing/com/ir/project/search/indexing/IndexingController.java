package com.ir.project.search.indexing.com.ir.project.search.indexing;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ir.project.search.domain.ErrorObject;
import com.ir.project.search.domain.SearchResult;
import com.ir.project.search.domain.SuccessObject;

@RestController
public class IndexingController{
	 String indexDir = "/home/vaishthiru/Downloads/datafinal/collection";
	 String dataDir = "/home/vaishthiru/Downloads/lucene_indexer";

	 //private final AtomicLong counter = new AtomicLong();
	 //private static final String PATH = "/error";
	 
	 @RequestMapping(method = RequestMethod.GET, value = "createIndex")
		public ResponseEntity<Object> createIndex() throws IOException, org.json.simple.parser.ParseException {
			HttpStatus httpStatus = HttpStatus.OK;
			Object responseObj = null;
			Indexer indexer = new Indexer();
			Object resultObj = indexer.createIndex();
			if (resultObj instanceof Exception) {
				httpStatus = HttpStatus.BAD_REQUEST;
				ErrorObject errorObj = new ErrorObject();
				errorObj.setCode(httpStatus.name());
				errorObj.setMessage(((Exception) resultObj).getMessage());
				responseObj = errorObj;
			} else {
				SuccessObject successObj = (SuccessObject) resultObj;
				successObj.setCode(httpStatus.name());
				successObj.setMessage("Indexing of Docs has completed");
				responseObj = successObj;
			}
			
			return new ResponseEntity<Object>(responseObj, httpStatus);
		}
	 
	 @RequestMapping(method = RequestMethod.GET, value = "search")
		public ResponseEntity<Object> search(@RequestParam(required = true) String query, @RequestParam(required = false, defaultValue = "false") boolean pageRank) throws IOException, ParseException, RestClientException, URISyntaxException, org.json.simple.parser.ParseException {
			HttpStatus httpStatus = HttpStatus.OK;
			Object responseObj = null;

			if (StringUtils.isEmpty(query)) {
				httpStatus = HttpStatus.BAD_REQUEST;
				ErrorObject errorObj = new ErrorObject();
				errorObj.setCode(httpStatus.name());
				errorObj.setMessage("Query is blank");
				responseObj = errorObj;
			} else {
				//Searcher searcher = new Searcher(dataDir);
				List<SearchResult> resultObj = searchFromSolr(query);//searcher.search(query, pageRank);
				responseObj = resultObj;
			}
			
			return new ResponseEntity<Object>(responseObj, httpStatus);
		}
		
		public List<SearchResult> searchFromSolr(String searchQuery) throws IOException, ParseException, RestClientException, URISyntaxException, org.json.simple.parser.ParseException{
			RestTemplate restTemplate = new RestTemplate();
			List<SearchResult> searchResults = new ArrayList<>();
		    String url = "http://localhost:8983/solr/electronic/select?indent=on&q="+URLEncoder.encode(searchQuery)+"&wt=json";
			ResponseEntity<String> jsonResponse = restTemplate.exchange(url, HttpMethod.GET, new RequestEntity<Object>(HttpMethod.GET, new URI(url)),
					String.class);
			String jsonBody = jsonResponse.getBody();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonBody);
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject jsonObj =  (JSONObject) parser.parse(jsonObject.get("response").toString());
			JSONArray jObj =  (JSONArray) parser.parse(jsonObj.get("docs").toString());
			Iterator<JSONObject> iterator = jObj.iterator();
			while (iterator.hasNext()) {
				JSONObject item = iterator.next();
				String urlOfItem = item.get("url").toString().trim();
		    	String title = item.get("title").toString().trim();
		    	String data = item.get("content").toString().substring(0, 500).replaceAll("\\s+", " ").trim();
		    	  if (title == null || title.isEmpty()) {
						title = url;
				  }
		    	  SearchResult searchResult = new SearchResult(urlOfItem, title, data); 
		    	  searchResults.add(searchResult);
			}
			return searchResults;
	   }
}
