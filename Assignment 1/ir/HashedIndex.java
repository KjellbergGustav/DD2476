/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    //String = Token
    //PostingList = Inverted index containing DocId
    private final HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( final String token, final int docID, final int offset ) {
        //
        // YOUR CODE HERE
        // putIfAbsent can be used to put a value in the map if the key does not exists, then returns null, else, returns value.
        final PostingsEntry newPostingEntry = new PostingsEntry(docID);
        // adds a new list if the token is absent in the hashmap.
        //this.index.putIfAbsent(token,new PostingsList());
        if (this.index.get(token) == null){
            this.index.put(token, new PostingsList());
        }
        // get the empty or existing postingsList
        PostingsList postingsList = index.get(token);
        postingsList.addNewEntry(newPostingEntry, offset);
        
        //newPostingEntry.addOffset(offset);


        //
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( final String token ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        return this.index.get(token);
    }

    public void computeScores(int n) {
        
    }
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
