
/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2016, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package com.ir.project.search.clustering.cluster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.Cluster;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.ProcessingComponentConfiguration;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;
import org.carrot2.shaded.guava.common.collect.Maps;
import org.carrot2.source.lucene.IFieldMapper;
import org.carrot2.source.lucene.LuceneDocumentSource;
import org.carrot2.source.lucene.LuceneDocumentSourceDescriptor;
import org.carrot2.source.lucene.SimpleFieldMapper;
import org.carrot2.util.annotations.ThreadSafe;
import org.carrot2.util.attribute.IObjectFactory;

import com.ir.project.search.clustering.domain.SearchResult;

/**
 * This example shows how to apply custom processing to documents returned by the
 * {@link LuceneDocumentSource}.
 * <p>
 * It is assumed that you are familiar with {@link ClusteringDocumentList},
 * {@link UsingCachingController} and {@link ClusteringDataFromLucene} examples.
 * 
 * @see ClusteringDataFromLucene
 * @see ClusteringDocumentList
 * @see UsingCachingController
 */
public class ClusteringDataFromLuceneWithCustomFields
{
    /**
     * Entry point. 
     * @return 
     */
    public ArrayList<SearchResult> cluster(String query) throws IOException
    {
        /*
         * We will use the CachingController for this example. Running
         * LuceneDocumentSource within the CachingController will let us open the index
         * once per component initialization and not once per query, which would be the
         * case with SimpleController. We will also use this opportunity to show how
         * component-specific attribute values can be passed during CachingComponent
         * initialization.
         */

        /*
         * Create a caching controller that will reuse processing component instances, but
         * will not perform any caching of results produced by components. We will leave
         * caching of documents from Lucene index to Lucene and the operating system
         * caches.
         */
        final Controller controller = ControllerFactory.createPooling();
        ArrayList<SearchResult> resultToUI = new ArrayList<SearchResult>();
        /*
         * Prepare a map with component-specific attributes. Here, this map will contain
         * the index location and names of fields to be used to fetch document title and
         * summary.
         */
        final Map<String, Object> luceneGlobalAttributes = new HashMap<String, Object>();

        String indexPath = "/home/vaishthiru/Downloads/lucene_indexer";

        // Sanity check.
        if (!new File(indexPath).isDirectory()) {
            System.err.println("Index directory does not exist: " + indexPath);
            return resultToUI;
        }

        LuceneDocumentSourceDescriptor
            .attributeBuilder(luceneGlobalAttributes)
            .directory(FSDirectory.open(Paths.get(indexPath)));

        /*
         * In ClusteringDataFromLucene we used a simple configuration of
         * LuceneDocumentSource whereby we only provided the names of Lucene fields to be
         * used for titles and summaries. If more advanced mapping of Lucene documents is
         * required, you can implement your own version of IFieldMapper as below.
         * 
         * Note that we could also provide here an instance of the mapper rather than
         * its class. The differences are summarized below:
         * 
         * > Class: Class has to have a no-parameter constructor. Instances of the
         *   class will not be shared between processing threads, which means the
         *   implementation does not have to be thread-safe. Recommended in most 
         *   situations unless the instances are expensive to create.
         *   
         * > Instance: The provided instance will be shared across processing threads,
         *   which means the implementation MUST be thread-safe.
         */
        LuceneDocumentSourceDescriptor
            .attributeBuilder(luceneGlobalAttributes)
            .fieldMapper(new CustomFieldMapper());

        /*
         * The Analyzer used by Lucene while searching can also be provided via factory
         * because it does not have a parameterless constructor.
         */
        LuceneDocumentSourceDescriptor
            .attributeBuilder(luceneGlobalAttributes)
            .analyzer(StandardAnalyzerFactory.class);

        /*
         * Initialize the controller passing the above attributes as component-specific
         * for Lucene. The global attributes map will be empty. Note that we've provided
         * an identifier for our specially-configured Lucene component, we'll need to use
         * this identifier when performing processing.
         */
        controller.init(
            new HashMap<String, Object>(),
            new ProcessingComponentConfiguration(
                LuceneDocumentSource.class, "lucene", luceneGlobalAttributes));

        /*
         * Perform processing.
         */
        final Map<String, Object> processingAttributes = Maps.newHashMap();
        CommonAttributesDescriptor.attributeBuilder(processingAttributes)
            .query(query);

        /*
         * We need to refer to the Lucene component by its identifier we set during
         * initialization. As we've not assigned any identifier to the
         * LingoClusteringAlgorithm we want to use, we can its fully qualified class name.
         */
        ProcessingResult process = controller.process(
            processingAttributes, "lucene", LingoClusteringAlgorithm.class.getName());

        ConsoleFormatter.displayResults(process);
        /*List<Cluster> cluster = process.getClusters();
        Double maxScore = 0.0; 
        Cluster maxCluster = new Cluster();
        for (Cluster clust : cluster) {
        	if(maxScore < clust.getScore()) {
        		maxScore = clust.getScore();
        		maxCluster = clust;
        	}
        }
        int count = 0;
        for (org.carrot2.core.Document document : maxCluster.getAllDocuments())
        {
            String title = document.getField(org.carrot2.core.Document.TITLE);
            String url = document.getField(org.carrot2.core.Document.CONTENT_URL);
            SearchResult searchResult = new SearchResult(url, title, "");
            resultToUI.add(searchResult);
            count++;
            if (count == 10)
            	break;
        }*/
        int count = 0;
       /* List<Cluster> cluster = process.getClusters();
        for (Cluster clust : cluster) {
        	 org.carrot2.core.Document document = clust.getDocuments().get(0);
        	 String title = document.getField(org.carrot2.core.Document.TITLE);
        	 title = title.replaceAll("\\?", "");
             String url = document.getField(org.carrot2.core.Document.CONTENT_URL);
             SearchResult searchResult = new SearchResult(url, title, "");
             resultToUI.add(searchResult);
             count++;
             if (count == 10)
             	break;
        }*/
       // if (resultToUI.size() < 10) {
        	for (org.carrot2.core.Document document : process.getDocuments()) {
        		String title = document.getField(org.carrot2.core.Document.TITLE);
        		title = title.replaceAll("\\?", "");
                String url = document.getField(org.carrot2.core.Document.CONTENT_URL);
                SearchResult searchResult = new SearchResult(url, title, "");
                resultToUI.add(searchResult);
                count++;
                if (count == 10)
                	break;
        	}
       // }
        /*List<Cluster> cluster = process.getClusters();
        for (Cluster clust : cluster) {
        	for (org.carrot2.core.Document document : clust.getDocuments()) {
        		String title = document.getField(org.carrot2.core.Document.TITLE);
                String url = document.getField(org.carrot2.core.Document.CONTENT_URL);
                SearchResult searchResult = new SearchResult(url, title, "");
                resultToUI.add(searchResult);
                count++;
                if (count == 10)
                	break;
        	}
        	if (count == 10)
        		break;
        }*/
        
        return resultToUI;
    }
    
    /**
     * A wrapper class producing {@link StandardAnalyzer} instances.
     */
    public static final class StandardAnalyzerFactory implements IObjectFactory<Analyzer> {
        @Override
        public Analyzer create()
        {
            return new StandardAnalyzer();
        }
    }

    /**
     * Our custom Lucene -> Carrot2 content mapper. You can {@link SimpleFieldMapper}
     * source code for the default implementation.
     */
    @ThreadSafe
    public static final class CustomFieldMapper implements IFieldMapper
    {
        public void map(Query luceneQuery, Analyzer analyzer, Document luceneDoc,
            org.carrot2.core.Document carrot2Doc)
        {
            /*
             * Here we need to transfer the desired content from the provided Lucene
             * document to the provided Carrot2 document.
             */
            carrot2Doc.setContentUrl(luceneDoc.get("url"));
            carrot2Doc.setTitle(luceneDoc.get("title"));
            carrot2Doc.setField("data", luceneDoc.get("data"));
        }

        public String [] getSearchFields()
        {
            /*
             * Here we need to return the names of Lucene fields that should be searched.
             * Note that these fields don't necessarily have to be the same as the fields
             * used in the map() method.
             */
            return new String []
            {
                "data","title"
            };
        }
    }
}
