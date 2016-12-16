package com.ir.project.search.indexing.com.ir.project.search.indexing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.ir.project.search.domain.SearchResult;

public class Searcher {
	
   IndexSearcher indexSearcher;
   QueryParser queryParser;
   Query query;
   
   public Searcher(String indexDirectoryPath) throws IOException{
     //Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
     Directory dir = FSDirectory.open(new File(indexDirectoryPath));
     IndexReader indexReader = DirectoryReader.open(dir);
     indexSearcher = new IndexSearcher(indexReader);
     queryParser = new QueryParser(Version.LUCENE_48, "data", new StandardAnalyzer(Version.LUCENE_48));
     //queryParser = new QueryParser(Version.LUCENE_6_3_0, "data", new StandardAnalyzer());
   }
   
   public List<SearchResult> search( String searchQuery, boolean pageRank) throws IOException, ParseException{
	  
	  List<SearchResult> searchResults = new ArrayList<>();
      query = queryParser.parse(searchQuery);
      TopDocs topDocs = indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
      ScoreDoc[] hits = topDocs.scoreDocs;
      //HITS based
      if (!pageRank) {
	      for(ScoreDoc hitDoc : hits) {
	    	  Document doc = getDocument(hitDoc);
	    	  String url = doc.get("url").trim();
	    	  String title = doc.get("title").trim();
	    	  String data = doc.get("data").substring(0, 500).replaceAll("\\s+", " ").trim();
	    	  if (title == null || title.isEmpty()) {
					title = url;
			  }
	    	  SearchResult searchResult = new SearchResult(url, title, data); 
	    	  searchResults.add(searchResult);
	      }
      }
	  return searchResults;
   }

   public Document getDocument(ScoreDoc scoreDoc) 
      throws CorruptIndexException, IOException{
      return indexSearcher.doc(scoreDoc.doc);	
   }

   public void close() throws IOException{
	   //indexSearcher.close();
   }
}