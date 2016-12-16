package com.ir.project.search.services;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.sandbox.queries.DuplicateFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Service;

import com.ir.project.search.domain.SearchResult;
import com.ir.project.search.engine.indexer.lucene.pagerank.PageRanker;

@Service
public class SearchService {

	private static String indexDir = "/home/vaishthiru/Downloads/lucene_indexer";
	private Logger logger = Logger.getLogger(SearchService.class);

	public List<SearchResult> fetchSearchResults(String queryString, Boolean pageRank) {
		List<SearchResult> results;

		if (pageRank) {
			results = searchIndexWithPageRank(queryString);
		} else {
			results = searchIndexWithHits(queryString);
		}

		/*RestTemplate restTemplate = new RestTemplate();
		try {
			String url = "http://localhost:3001/cluster?query=" + URLEncoder.encode(queryString);
			restTemplate.exchange(url, HttpMethod.GET, new RequestEntity<Object>(HttpMethod.GET, new URI(url)),
					String.class);
		} catch (RestClientException | URISyntaxException e) {
			e.printStackTrace();
		}*/

		if (results != null) {
			return results;
		} else {
			return new ArrayList<SearchResult>();
		}
	}

	private List<SearchResult> searchIndexWithHits(String queryString) {

		List<SearchResult> resultList = new ArrayList<SearchResult>();

		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(indexDir))));
			QueryParser parser = new QueryParser(Version.LUCENE_48, "data", new StandardAnalyzer(Version.LUCENE_48));
			Query query = parser.parse(queryString);
			DuplicateFilter duplicateFilter = new DuplicateFilter("title");
			//duplicateFilter.setKeepMode(DuplicateFilter.KeepMode.KM_USE_FIRST_OCCURRENCE);
			//duplicateFilter.setProcessingMode(DuplicateFilter.ProcessingMode.PM_FAST_INVALIDATION);
			TopDocs collector = searcher.search(query, duplicateFilter, 10);
			
			
			ScoreDoc[] hits = collector.scoreDocs;
			int times =  Math.min(hits.length, 10);
			for (int i = 0; i < times; i++) {
				Document document = searcher.doc(hits[i].doc);
				
				String url = document.get("url").trim();
				String title = cleanupData(document.get("title")).trim();
				String data = cleanupData(document.get("data"));

				if (title == null || title.isEmpty()) {
					title = url;
				}

				SearchResult result = new SearchResult(url, title, data);
				String url1 = document.get("data");
				assertFalse("No duplicate urls should be returned",result.getUrl().contains(url1));
				resultList.add(result);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Exception occured while searching index:" + ex.getMessage());
		}

		return resultList;
	}

	private List<SearchResult> searchIndexWithPageRank(String queryString) {

		List<SearchResult> resultList = new ArrayList<SearchResult>();

		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(indexDir))));
			QueryParser parser = new QueryParser(Version.LUCENE_48, "data", new StandardAnalyzer(Version.LUCENE_48));
			Query query = parser.parse(queryString);
			DuplicateFilter duplicateFilter = new DuplicateFilter("title");
			//duplicateFilter.setKeepMode(DuplicateFilter.KeepMode.KM_USE_FIRST_OCCURRENCE);
			//duplicateFilter.setProcessingMode(DuplicateFilter.ProcessingMode.PM_FAST_INVALIDATION);
			TopDocs collector = searcher.search(query, duplicateFilter, 10);
			ArrayList<Document> pageRankedDocs = new ArrayList<Document>();
			pageRankedDocs = PageRanker.sortDocsByPageRank(collector, searcher);

			for (Document document : pageRankedDocs) {

				String url = document.get("url").trim();
				String title = cleanupData(document.get("title")).trim();
				String data = cleanupData(document.get("data"));

				if (title == null || title.isEmpty()) {
					title = url;
				}

				SearchResult result = new SearchResult(url, title, data);
				String url1 = document.get("data");
				assertFalse("No duplicate urls should be returned",result.getUrl().contains(url1));
				resultList.add(result);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception occured while searching index:" + ex.getMessage());
		}

		return resultList;
	}

	private String cleanupData(String data) {
	    data = data.replaceAll("\\?", "");
		if (data.length() > 500) {
			data = data.substring(0, 150);
		}

		return data.replaceAll("\\s+", " ").trim();
	}
}
