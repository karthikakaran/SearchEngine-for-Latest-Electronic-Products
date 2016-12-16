package com.ir.project.search.services;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Service;

import com.ir.project.search.SearchProps;
import com.ir.project.search.engine.queryexpansion.QueryExpansion;

@Service
public class QueryExpansionService {

	private static String indexDir = "/home/vaishthiru/Downloads/lucene_indexer";
	private Logger logger = Logger.getLogger(QueryExpansionService.class);

	private Set<Term> suggestedTermsSet;

	public List<String> fetchNextQueries(String queryString) {
		List<String> queries = expandQuery(queryString);

		if (queries != null) {
			return queries;
		} else {
			return new ArrayList<String>();
		}
	}

	private List<String> expandQuery(String queryString) {
		List<String> resultList = new ArrayList<String>();
		int numSuggestions = 0;

		logger.info("Query is: " + queryString);

		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(indexDir))));
			QueryParser parser = new QueryParser(Version.LUCENE_48, "data", new StandardAnalyzer(Version.LUCENE_48));
			Query query = parser.parse(queryString);
			TopDocs collector = searcher.search(query, 10);

			QueryExpansion queryExpansion = new QueryExpansion(new StandardAnalyzer(Version.LUCENE_48), searcher,
					(TFIDFSimilarity) searcher.getSimilarity(), SearchProps.properties);
			query = queryExpansion.expandQuery(queryString, collector, SearchProps.properties);

			suggestedTermsSet = new LinkedHashSet<Term>();
			query.extractTerms(suggestedTermsSet);
			Scanner stopWordFile = new Scanner(new File("/home/vaishthiru/Downloads/stopwords"));
			ArrayList<String> stopWords = new ArrayList<>();
			
			while (stopWordFile.hasNext()) {
				stopWords.add(stopWordFile.next());
			}
			for (Term term : suggestedTermsSet) {
				if (numSuggestions > 5) {
					break;
				} else {
					if (!term.text().equals(queryString) && !stopWords.contains(term.text())) {
						resultList.add(queryString + " " + term.text());
						numSuggestions++;
					}
				}
				
				
			}
			stopWordFile.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Exception occured while query expansion:" + ex.getMessage());
		}

		return resultList;
	}
}
