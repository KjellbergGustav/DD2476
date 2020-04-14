/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import ir.PersistentHashedIndex.Entry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.charset.*;
import java.io.*;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm implements Comparable<QueryTerm>{
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    
    /* 
    Normalize the qury term
    */
    private void normalize(double length){
        weight /= length;
    }

    public int compareTo(QueryTerm other){
        return Double.compare(other.weight, weight);
    }

    }

    
    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.5;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
        }
    }
    
    /*
    Normalize the entire query
    */
    public void normalizeQuery(){
        int countTerms = this.queryterm.size();
        double queryLength = length();
        for (int idx = 0; idx < countTerms; idx++){
            this.queryterm.get(idx).normalize(queryLength);
        }

    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryterm ) {
            len += t.weight; 
        }
        return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryterm ) {
            queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
        }
        return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
        //
        //  YOUR CODE HERE
        //

        // 1.1 Extract all relevant docIds
        ArrayList<Integer> relevantDocIds = new ArrayList<Integer>();
        for (int idx = 0; idx < docIsRelevant.length; idx++){
            if (docIsRelevant[idx]){
                relevantDocIds.add(results.get(idx).docID);
            }
        }
        
        // If there are no relavant documents, break early.
        if (relevantDocIds.size() == 0){ return; } 

        //1.2 Normalize the query
        this.normalizeQuery();

        //2.1 q_m hashmap

        HashMap<String,Double> newQ = new HashMap<String, Double>();

        /*
        2.2 compute the first term:
        q_m(token) = alpha*q.weight*ln(N_docs/df)

        Note: the weights have already been normalized hence that's missing in eq 2.1
        */
        for (QueryTerm qt: this.queryterm){
            Index index = engine.index;
            Double val = alpha*qt.weight*Math.log((double)index.docLengths.size()/index.getPostings(qt.term).size());
            newQ.put(qt.term, val);
        }

        /* 
        3.2 Compute v(d)
        */
        
        double beta_normalized = this.beta/relevantDocIds.size();


        for (int docId: relevantDocIds){
            try{
            Reader reader = new InputStreamReader( new FileInputStream(engine.index.docNames.get(docId)), StandardCharsets.UTF_8 );
            Tokenizer tok = new Tokenizer( reader, true, false, true, "patterns.txt" );
            //ArrayList<String> tokensInDoc = new ArrayList<String>();

            Index index = engine.index;
            while ( tok.hasMoreTokens() ){
                String token = tok.nextToken();
                //tokensInDoc.add(token);
                // beta*1/|D_r|*tf-idf/len(relevantDoc)
                // unfourtunatly we can't use our previous calculated score here, at first glance at least, as it's already tampered with in diffrent ways...
                PostingsList pl = index.getPostings(token);
                int plSize = pl.size();
                double secondTerm = beta_normalized*Math.log((double)index.docLengths.size()/plSize)/index.docLengths.get(docId);
                /*3.3 add to q_m */
                if (newQ.containsKey(token)){
                    double firstTerm = newQ.get(token);
                    newQ.put(token, firstTerm+secondTerm);
                }else{
                    newQ.put(token, secondTerm);
                }
            }
            reader.close();
        }
        catch (Exception e){
            System.err.println("msg" + e);
        }
        }
        // Sort the hashmap by value, source of information: https://stackoverflow.com/a/19671853
        Map<String, Double> sortedQ = newQ.entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect
        (Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2)->e1, LinkedHashMap::new));
        System.out.println("Done!");
        queryterm.clear();

        //int amountOfQueryTerms = 20;
        int count = 0;
/*        for(Map.Entry<String,Double> entry: sortedQ.entrySet()){
            queryterm.add(new QueryTerm(entry.getKey(),entry.getValue()));
            //amountOfQueryTerms--;
            if (count < 50){
                System.out.println(entry.getKey() + " " + entry.getValue());
                count++;
            }*/
            //if (amountOfQueryTerms == 0) {break;}
      //}
    }
}


