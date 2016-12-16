package com.ir.project.search.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import com.ir.project.search.domain.ErrorObject;
import com.ir.project.search.domain.SearchResult;
import com.ir.project.search.domain.SuccessObject;
import com.ir.project.search.engine.indexer.LuceneIndexer;
import com.ir.project.search.services.QueryExpansionService;
import com.ir.project.search.services.SearchService;
@RestController
@RequestMapping("/search")
public class SearchController {

	@Autowired
	private SearchService searchService;
	
	@Autowired
	private QueryExpansionService queryExpansionService;

	@Autowired
	private LuceneIndexer luceneIndexer;

	@CrossOrigin(origins = "http://localhost:4000")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<Object> search(@RequestParam(required = true) String query, @RequestParam(required = false, defaultValue = "false") Boolean pagerank) throws IOException, ParseException, URISyntaxException, org.json.simple.parser.ParseException {
		ResponseEntity<Object> response;
		HttpStatus httpStatus = HttpStatus.OK;
		Object responseObj = null;

		if (StringUtils.isEmpty(query)) {
			httpStatus = HttpStatus.BAD_REQUEST;
			ErrorObject errorObj = new ErrorObject();
			errorObj.setCode(httpStatus.name());
			errorObj.setMessage("Query is blank");
			responseObj = errorObj;
		} else {
			try {
				List<SearchResult> searchResults = searchService.fetchSearchResults(query, pagerank);
				responseObj = searchResults;
				//resultObj = searchFromSolr(query);
			} catch (RestClientException e) {
				e.printStackTrace();
			}
		}

		response = new ResponseEntity<Object>(responseObj, httpStatus);
		return response;
	}
	
	
	
	@CrossOrigin(origins = "http://localhost:4000")
	@RequestMapping(method = RequestMethod.GET, value = "autocomplete")
	public ResponseEntity<Object> autocomplete(@RequestParam(required = true) String query) {
		ResponseEntity<Object> response;
		HttpStatus httpStatus = HttpStatus.OK;
		Object responseObj = null;

		if (StringUtils.isEmpty(query)) {
			httpStatus = HttpStatus.BAD_REQUEST;
			ErrorObject errorObj = new ErrorObject();
			errorObj.setCode(httpStatus.name());
			errorObj.setMessage("Query is blank");
			responseObj = errorObj;
		} else {
			List<String> queries = queryExpansionService.fetchNextQueries(query);
			responseObj = queries;
		}

		response = new ResponseEntity<Object>(responseObj, httpStatus);
		return response;
	}

	@CrossOrigin(origins = "http://localhost:4000")
	@RequestMapping(method = RequestMethod.GET, value = "indexDocs")
	public ResponseEntity<Object> indexDocs() {
		HttpStatus httpStatus = HttpStatus.OK;
		Object responseObj = null;
		Object resultObj = luceneIndexer.indexDocs();

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
}
