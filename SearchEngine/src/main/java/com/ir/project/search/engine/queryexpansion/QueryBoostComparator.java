/*
 * QueryBoostComparator.java
 *
 * Created on February 23, 2005, 5:28 PM
 *
 * @author Neil O. Rouben
 */

package com.ir.project.search.engine.queryexpansion;

import java.util.Comparator;

import org.apache.lucene.search.Query;

public class QueryBoostComparator implements Comparator<Object>
{
    
    /** Creates a new instance of QueryBoostComparator */
    public QueryBoostComparator()
    {
    }
    
    /**
     * Compares queries based on their boost
     * Since want to be sorted in decending order; comparison will be reversed
     */
    public int compare(Object obj1, Object obj2)
    {
        Query q1 = (Query) obj1;
        Query q2 = (Query) obj2;
        
        if ( q1.getBoost() > q2.getBoost() )
            return -1;
        else if ( q1.getBoost() < q2.getBoost() ) 
        	return 1;
        else
        	return 0;
    }
    
}
