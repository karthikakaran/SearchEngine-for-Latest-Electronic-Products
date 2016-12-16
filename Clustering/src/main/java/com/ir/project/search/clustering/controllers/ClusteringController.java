package com.ir.project.search.clustering.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ir.project.search.clustering.cluster.ClusteringDataFromLuceneWithCustomFields;
import com.ir.project.search.clustering.cluster.LuceneIndexer;
import com.ir.project.search.clustering.domain.*;

@RestController
@RequestMapping("/cluster")
public class ClusteringController {

	@Autowired
	private LuceneIndexer luceneIndexer;

	@CrossOrigin(origins = "http://localhost:3000")
	@RequestMapping(method = RequestMethod.GET, value = "")
	public ResponseEntity<Object> cluster(@RequestParam String query) throws IOException {
		//ResponseEntity<Object> responseObject;
		Object responseObject = null;
		ClusteringDataFromLuceneWithCustomFields cluster = new ClusteringDataFromLuceneWithCustomFields();
		List<SearchResult> searchResults = cluster.cluster(query);
		responseObject = searchResults;
		return new ResponseEntity<Object>(responseObject, HttpStatus.OK);
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
