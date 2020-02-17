/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;
    public ArrayList<Integer> offset = new ArrayList<Integer>();

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */


    public PostingsEntry(int docID){
        this.docID = docID;
    }

    public int getDocId(){
        return this.docID;
    }

    /*@Override
    public int compareTo(PostingsEntry compareEntry){
        int compareDocId  = ((PostingsEntry)compareEntry).getDocId();
        return this.docID-compareDocId;
    }*/

    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, score );
    }

    public void addOffset(int offsetPos){
        this.offset.add(offsetPos);
    }

    public ArrayList<Integer> getOffset(){
        return this.offset;
    }
    //
    // YOUR CODE HERE
    //
}

