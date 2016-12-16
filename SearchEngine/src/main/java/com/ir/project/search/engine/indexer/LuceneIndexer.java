package com.ir.project.search.engine.indexer;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
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
import org.apache.lucene.util.Version;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;

import com.ir.project.search.domain.SuccessObject;

@Component
public class LuceneIndexer {

	private static String dirPath = "/home/vaishthiru/Downloads/data_2";
	private static String indexDir = "/home/vaishthiru/Downloads/lucene_indexer";

	private Logger logger = Logger.getLogger(LuceneIndexer.class);

    
    
	public Object indexDocs() {
		int count = 0;
		try {
			File folder = new File(dirPath);
			File indexFolder = new File(indexDir);

			if (indexFolder.exists()) {
				FileUtils.deleteDirectory(indexFolder);
			}

			Directory dir = FSDirectory.open(new File(indexDir));
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48,
					new StandardAnalyzer(Version.LUCENE_48));
			IndexWriter indexWriter = new IndexWriter(dir, config);
			if (folder != null) {
				File[] fileList = folder.listFiles();
				for (File file : fileList) {
					if (file.isFile()) {
						FileReader fr = new FileReader(file);
						logger.info(file);
						JSONParser parser = new JSONParser();
						Object object = parser.parse(fr);
						JSONObject jsonObject = (JSONObject) object;
						createIndex(file.getPath(), jsonObject, indexWriter);
						count++;
					}
				}
			}

			logger.info("Number of Documents Indexed: " + count);

			//indexWriter.close();
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
			doc.add(new StringField("Outlinks", (Long)jsonObject.get("Outlinks") + "", Field.Store.YES));
			doc.add(new StringField("title",
					(String) jsonObject.get("title") == null ? "" : (String) jsonObject.get("title"), Field.Store.YES));
			doc.add(new StringField("url", (String) jsonObject.get("url") == null ? "" : (String) jsonObject.get("url"),
					Field.Store.YES));

			indexWriter.addDocument(doc);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Exception occured in retrieving tokens!!" + ex.getMessage() + "\n\n" + ex.getStackTrace());
		}
	}

}
