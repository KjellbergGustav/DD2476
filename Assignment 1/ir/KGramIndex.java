/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class KGramIndex {

    /** Mapping from term ids to actual term strings */
    HashMap<Integer,String> id2term = new HashMap<Integer,String>();

    /** Mapping from term strings to term ids */
    HashMap<String,Integer> term2id = new HashMap<String,Integer>();

    /** Index from k-grams to list of term ids that contain the k-gram */
    HashMap<String,List<KGramPostingsEntry>> index = new HashMap<String,List<KGramPostingsEntry>>();

    /** The ID of the last processed term */
    int lastTermID = -1;

    /** Number of symbols to form a K-gram */
    int K = 3;

    public KGramIndex(int k) {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can't be constructed for a negative K value");
            System.exit(1);
        }
    }

    /** Generate the ID for an unknown term */
    private int generateTermID() {
        return ++lastTermID;
    }

    public int getK() {
        return K;
    }


    /**
     *  Get intersection of two postings lists
     */
    public List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
        // 
        // YOUR CODE HERE
        //
        //System.err.println(p2);
        int lengthP1 = p1.size();
        //System.err.println(lengthP1);
        int lengthP2 = p2.size();
        int indexP1 = 0;
        int indexP2 = 0;
        //System.err.println(lengthP1);
        //System.err.println(lengthP2);
        List<KGramPostingsEntry> result = new ArrayList<KGramPostingsEntry>();

        // Exakt same reasoning as in lab 1....
        while (indexP1 <= lengthP1-1 && indexP2 <= lengthP2-1){
            if (p1.get(indexP1).tokenID== p2.get(indexP2).tokenID){
                result.add(new KGramPostingsEntry(p1.get(indexP1).tokenID));
                indexP1++;
                indexP2++;
            }
            else if (p1.get(indexP1).tokenID < p2.get(indexP2).tokenID){
                indexP1++;
            }else{
                indexP2++;
            }
        }
        return result;
    }


    /** Inserts all k-grams from a token into the index. */
    public void insert( String token ) {
        //
        // YOUR CODE HERE
        //

        // 1. fix termId to have in conversion hashmaps an as key in the index....
        // 2. split words according to the K
        // 3. add the '^' and the '$' (start and end of line for regex.....)

        // If we already handled the term were done with it and don't need to do it again....
        if (term2id.get(token) == null){
            int termId = generateTermID();
            term2id.put(token, termId);
            id2term.put(termId, token);

            token = '^' + token + '$';
            if (token.contains("sic")){
                int a = 1;
            }
            String subString;
            for (int i = 0; i<=token.length()-K; i++){
                subString = token.substring(i, i+K);
                if (subString.equals("c*")){
                    int a = 1;
                }
                if (index.get(subString) == null){
                    List<KGramPostingsEntry> kGramList = new ArrayList<KGramPostingsEntry>();
                    kGramList.add(new KGramPostingsEntry(termId));
                    index.put(subString,kGramList);

                }else{
                    // else the substring should aldready be processed.
                    // Then only add the the termId to the existing postingsList...
                    List<KGramPostingsEntry> kGramList = index.get(subString);
                    int kGramSize = kGramList.size()-1;
                    // Below ensures that we don't have repititions in the words...
                    if (kGramList.get(kGramSize).tokenID< termId){
                        index.get(subString).add(
                            new KGramPostingsEntry(termId)
                        );
                    }
                }
            }

        }else{
            //System.err.println(token);
        }

    }
    
    /** Get postings for the given k-gram */
    public List<KGramPostingsEntry> getPostings(String kgram) {
        //
        // YOUR CODE HERE
        //
        //System.err.println(kgram);
        return index.get(kgram);
    }

    /** Get id of a term */
    public Integer getIDByTerm(String term) {
        return term2id.get(term);
    }

    /** Get a term by the given id */
    public String getTermByID(Integer id) {
        return id2term.get(id);
    }

    private static HashMap<String,String> decodeArgs( String[] args ) {
        HashMap<String,String> decodedArgs = new HashMap<String,String>();
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("patterns_file", args[i++]);
                }
            } else if ( "-f".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("file", args[i++]);
                }
            } else if ( "-k".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("k", args[i++]);
                }
            } else if ( "-kg".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("kgram", args[i++]);
                }
            } else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }
        return decodedArgs;
    }

    public void runMain(String[] kgString){
        //String[] kgrams = kgString.split(" ");
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgString) {
            if (kgram.length() != K) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + K + "-gram");
                System.exit(1);
            }

            if (postings == null) {
                postings = getPostings(kgram);
            } else {
                postings = intersect(postings, getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 10) {
                System.err.println("The first 10 of them are:");
                resNum = 10;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println(getTermByID(postings.get(i).tokenID));
            }
        }
    }

    public static void main(String[] arguments) throws FileNotFoundException, IOException {
        HashMap<String,String> args = decodeArgs(arguments);

        int k = Integer.parseInt(args.getOrDefault("k", "3"));
        KGramIndex kgIndex = new KGramIndex(k);

        File f = new File(args.get("file"));
        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
        Tokenizer tok = new Tokenizer( reader, true, false, true, args.get("patterns_file") );
        while ( tok.hasMoreTokens() ) {
            String token = tok.nextToken();
            kgIndex.insert(token);
        }

        String[] kgrams = args.get("kgram").split(" ");
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != k) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + k + "-gram");
                System.exit(1);
            }

            if (postings == null) {
                postings = kgIndex.getPostings(kgram);
            } else {
                postings = kgIndex.intersect(postings, kgIndex.getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 10) {
                System.err.println("The first 10 of them are:");
                resNum = 10;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println(kgIndex.getTermByID(postings.get(i).tokenID));
            }
        }
    }
}
