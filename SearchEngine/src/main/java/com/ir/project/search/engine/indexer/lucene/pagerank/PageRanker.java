package com.ir.project.search.engine.indexer.lucene.pagerank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class PageRanker {
	public static ArrayList<Document> sortDocsByPageRank(TopDocs collector, IndexSearcher searcher) throws IOException {
		ArrayList<Document> rankedDocs = new ArrayList<Document>();
		ScoreDoc[] pageRanks = collector.scoreDocs;
		
		for (ScoreDoc scoreDoc : pageRanks) {
			rankedDocs.add(searcher.doc(scoreDoc.doc));
		}
		if (pageRanks.length > 0) {
			Collections.sort(rankedDocs, new Comparator<Document>() {
				@Override
				public int compare(Document obj1, Document obj2){
					Double i1 = Double.parseDouble(obj1.get("outgoingLinks"));
					Double i2 = Double.parseDouble(obj2.get("outgoingLinks"));
					return i2.compareTo(i1);
				}
			});
		}
		return rankedDocs;
	}
}
