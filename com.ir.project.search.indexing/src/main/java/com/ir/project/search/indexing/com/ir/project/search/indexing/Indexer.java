package com.ir.project.search.indexing.com.ir.project.search.indexing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ir.project.search.domain.SuccessObject;

public class Indexer {

   public IndexWriter indexWriter;
   private Logger logger = Logger.getLogger(Indexer.class);
   String indexDir = "/home/vaishthiru/Downloads/lucene_indexer";
   String dataDir =  "/home/vaishthiru/Downloads/datafinal/collection";
   ArrayList<JSONObject> JSONObjectList = new ArrayList<>();
   HashMap<String, Double> pagerank = new HashMap<>();	
   FileWriter writer = null;
   Scanner reader = null;
   
   public JSONObject parseJSONFile(File jsonFile) throws IOException, ParseException{
       //Get the JSON file, in this case is in ~/resources/test.json
       /*InputStream jsonFile =  getClass().getResourceAsStream(dataDir + "/" + jsonFileName);
       Reader readerJson = new InputStreamReader(jsonFile);
       //Parse the json file using simple-json library
       Object fileObjects = JSONValue.parse(readerJson);*/
	   FileReader fr = new FileReader(jsonFile);
	   //logger.info(jsonFile + " read...");
	   JSONParser parser = new JSONParser();
	   Object object = parser.parse(fr);
	   JSONObject jsonObject = (JSONObject) object;
       return jsonObject;
   }
   
   public Object createIndex() throws IOException, ParseException{
	   int count = 0;
	   //Pagerank code
	   //writer = new FileWriter(new File("/home/vaishthiru/Downloads/pageGraph.data"));
	   //Pagerank code ends
	   //index code - begins
	   //reader = new Scanner(new File("/home/vaishthiru/Downloads/jsonPageRank.data"));
	   //index code - ends
	   openIndex();
       File folder = new File(dataDir);
       if (folder != null) {
			File[] fileList = folder.listFiles();
			for (File file : fileList) {
				if (file.isFile()) {
					JSONObject jsonObject  = parseJSONFile(file);
					//Pagerank code
					//writeFilePageRank((String) jsonObject.get("url"), jsonObject.get("urlOutgoingLinks") );
					//Pagerank code ends
					//Index code starts																					
					//JSONObjectList.add(jsonObject);
					//Index code ends
					addDocuments(jsonObject);
					count++;
				}
			}
		}
       logger.info("Number of Documents Indexed: " + count);
       //JSONParser parser = new JSONParser();
       //Index code starts																									
       /*while(reader.hasNextLine()) {
    	   JSONObject pagerankJson = (JSONObject) parser.parse(reader.nextLine());
    	   pagerank.put(pagerankJson.get("source ").toString(), Double.parseDouble(pagerankJson.get("target").toString()));
       }
       for (JSONObject object : JSONObjectList) {
    	  addDocuments(object, pagerank.get(object.get("url")));
       }
       reader.close();*/
       //index code ends
       //Pagerank code
       //writer.close();
       //Pagerank code - ends
       finish();
       return new SuccessObject();
   }
   
   private void writeFilePageRank(String string, Object object) throws IOException {
	   JSONArray arrayObjects = (JSONArray)object;
	   writer.write(string);
	   Iterator<JSONObject> it = arrayObjects.iterator();
	   while (it.hasNext()) {
		   writer.write("\t"+it.next());
	   }
	   writer.write("\n");
   }

public boolean openIndex(){
       try {
    	   Directory dir = FSDirectory.open(new File(indexDir));
           File folder = new File(indexDir);
           if (folder.exists()) {
				FileUtils.deleteDirectory(folder);
		   }
           IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48,
					new StandardAnalyzer(Version.LUCENE_48));
          // IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		   //Always overwrite the directory
           config.setOpenMode(OpenMode.CREATE);
           indexWriter = new IndexWriter(dir, config);
           return true;
       } catch (Exception e) {
           System.err.println("Error opening the index. " + e.getMessage());
       }
       return false;
   }
   
   public void addDocuments(JSONObject jsonObject){//, Double pagerank
	   Document doc = new Document();
	   doc.add(new TextField("data",
				(String) jsonObject.get("data") == null ? "" : (String) jsonObject.get("data"), Field.Store.YES));
	   doc.add(new StringField("name",
				(String) jsonObject.get("name") == null ? "" : (String) jsonObject.get("name"), Field.Store.YES));
	   doc.add(new StringField("id", (Long) jsonObject.get("id") + "", Field.Store.YES));
	   doc.add(new StringField("outgoingLinks", (Long) jsonObject.get("outgoingLinks") + "", Field.Store.YES));
	   doc.add(new StringField("title",
				(String) jsonObject.get("title") == null ? "" : (String) jsonObject.get("title"), Field.Store.YES));
	   doc.add(new StringField("url", (String) jsonObject.get("url") == null ? "" : (String) jsonObject.get("url"),
				Field.Store.YES));
	  // doc.add(new StringField("pagerank", (Double) pagerank + "", Field.Store.YES));
       try {
           indexWriter.addDocument(doc);
       } catch (IOException ex) {
           System.err.println("Error adding documents to the index. " +  ex.getMessage());
       }
   }
   
   public void finish(){
       try {
           indexWriter.commit();
           indexWriter.close();
       } catch (IOException ex) {
           System.err.println("We had a problem closing the index: " + ex.getMessage());
       }
   }
}