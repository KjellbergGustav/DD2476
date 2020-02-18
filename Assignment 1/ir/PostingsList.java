/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.HashMap;

public class PostingsList{
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();
    private HashMap<Integer, PostingsEntry> checkList = new HashMap<Integer, PostingsEntry>();
    // Constructor
    public PostingsList(){};

    /** Number of postings in this list. */
    public int size() {
    return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
    return list.get( i );
    }

    public PostingsEntry getEntryByDocId(int docId){
        return this.checkList.get(docId);
    }

    public ArrayList<PostingsEntry> getList(){
        return this.list;

    }

    // 
    //  YOUR CODE HERE
    //

    // addNewEntry - adds a new entry to the list
    // Parameters: PostingEntry
    // Return: bool - if entry already did exist return false, else true

    public void addNewEntry(PostingsEntry newEntry, int offset){
        if (this.list.isEmpty()){
            if (offset != -1){ newEntry.addOffset(offset);}
            this.list.add(newEntry);
            this.checkList.put(newEntry.docID, newEntry);
        }else{
            PostingsEntry existingEntry = entryExist(newEntry.docID);
            if ( existingEntry == null){
                if (offset != -1){ newEntry.addOffset(offset);}
                this.list.add(newEntry);
                this.checkList.put(newEntry.docID, newEntry);
            }else{
                if (offset != -1) {existingEntry.addOffset(offset);}
            }
        }
    }
    public void addNewEntry(PostingsEntry newEntry){
        addNewEntry(newEntry, -1);
        /*if (this.list.isEmpty()){
            this.list.add(newEntry);
        }else{
            if (!entryExist(newEntry.docID)){
                this.list.add(newEntry);
            }
        }*/
    }

    public String toString(){
        StringBuilder test = new StringBuilder();
        
        String returnString = "PostingsList;[";
        for (PostingsEntry pe: this.list){
            returnString += pe.getDocId() + ";" + pe.offset.toString() + ";";
            test.append(pe.docID);
            for (int os: pe.offset){
                test.append(";");
                test.append(os);
            }
            test.append(";" + pe.score);
            test.append(":");
        }
        returnString += "]";
        String testString = test.toString();
        byte[] testByte = testString.getBytes();
        int testSize = testByte.length;
        int currSize = returnString.getBytes().length;
        return testString;
    }

    public PostingsEntry entryExist(int docId){

        // eventually this will be sorted and then we can use a real search algo like bin search...
        
        /*for (PostingsEntry p: this.list){
            if (docId == p.docID){return p;}
        }
        return null;*/
        return this.checkList.get(docId);
    }
}

