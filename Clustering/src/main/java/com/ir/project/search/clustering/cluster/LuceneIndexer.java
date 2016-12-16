package com.ir.project.search.clustering.cluster;

import java.io.File;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;

import com.ir.project.search.clustering.domain.SuccessObject;

@Component
public class LuceneIndexer {

	public static String dirPath = "/home/vaishthiru/workspace/Lucene/folder";
	public static String indexDir = "/home/vaishthiru/Downloads/lucene_indexer";

	private Logger logger = Logger.getLogger(LuceneIndexer.class);

	public Object indexDocs() {
		
		String docId = "";
		Path indexDirPath = FileSystems.getDefault().getPath(indexDir);
		try {
			File folder = new File(dirPath);
			Directory dir = FSDirectory.open(indexDirPath);
			IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
			IndexWriter indexWriter = new IndexWriter(dir, config);
			if (folder != null) {
				File[] fileList = folder.listFiles();
				for (File file : fileList) {
					if (file.isFile()) {
						String filename = file.getPath();
						docId = filename.substring(file.getParent().length() + "Doc".length() + 1,
								filename.length() - 4);
						logger.info("Processing Doc#" + docId + "...");

						FileReader fr = new FileReader(file);
						JSONParser parser = new JSONParser();
						Object object = parser.parse(fr);
						JSONObject jsonObject = (JSONObject) object;
						createIndex(file.getPath(), jsonObject, indexWriter);
					}
				}
			}
			indexWriter.close();
			return new SuccessObject();
		} catch (Exception ex) {
			ex.printStackTrace();
			return ex;
		}
	}

	public void createIndex(String filePath, JSONObject jsonObject, IndexWriter indexWriter) {

		try {

			Document doc = new Document();
			doc.add(new TextField("data",
					(String) jsonObject.get("data") == null ? "" : (String) jsonObject.get("data"), Field.Store.YES));
			doc.add(new StringField("name",
					(String) jsonObject.get("name") == null ? "" : (String) jsonObject.get("name"), Field.Store.YES));
			doc.add(new StringField("id", (Long) jsonObject.get("id") + "", Field.Store.YES));
			doc.add(new StringField("Outlinks", (Long) jsonObject.get("Outlinks") + "", Field.Store.YES));
			doc.add(new StringField("title",
					(String) jsonObject.get("title") == null ? "" : (String) jsonObject.get("title"), Field.Store.YES));
			doc.add(new StringField("url", (String) jsonObject.get("url") == null ? "" : (String) jsonObject.get("url"),
					Field.Store.YES));

			indexWriter.addDocument(doc);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("Exception occured in retrieving tokens!!" + ex.getMessage() + "\n\n" + ex.getStackTrace());
		}
	}

}
