/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */

package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    KGramIndex kgIndex;

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
     OVERLOAD!!!
     I guess all the this code should be places in the kgIndex instead, but my searcher wasn't really formatted so that it would
     be suitable.... Instead, lets overload.
     */
    public PostingsList getPostings( final String token, KGramIndex kgIndex ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        this.kgIndex = kgIndex;

        int index_wildcard = token.indexOf('*');
        if (index_wildcard == -1){
            return this.index.get(token);
        }else{
            return getPLWildcard(token, index_wildcard);
        }
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
    private Boolean verifyWords(String matchingTerm,String token,int index_wildcard){
        Boolean result = false;
        if (index_wildcard == 0){
            String subS = matchingTerm.substring(matchingTerm.length()-(token.length()-1));
            if (matchingTerm.substring(matchingTerm.length()-(token.length()-1)).equals(token.substring(1))){
                result = true;
            }
            return result;
        }else if( index_wildcard == token.length()-1){
            if (matchingTerm.substring(0,index_wildcard).equals(token.substring(0,index_wildcard))){
                result = true;
            }
            return result;
        }else if(matchingTerm.substring(0, index_wildcard).equals(token.substring(0,index_wildcard))){
            int tokenLen = token.length();
            int tokenEndStringLen = tokenLen-index_wildcard-1;
            int termLen = matchingTerm.length();
            if (matchingTerm.substring(termLen-tokenEndStringLen, termLen).equals(token.subSequence(index_wildcard+1, tokenLen))){
                return true;
            }
        }
        
        return false;
    }

    private PostingsEntry unionDocId(PostingsEntry pe1, PostingsEntry pe2){
        PostingsEntry result = new PostingsEntry(pe1.docID);
        result.score = pe1.score+pe2.score;
        int p1Idx = 0;
        int p2Idx = 0;

        while (pe1.offset.size()>p1Idx && pe2.offset.size()>p2Idx){
            if (pe1.getOffset().get(p1Idx)>pe2.getOffset().get(p2Idx)){
                result.addOffset(pe2.getOffset().get(p2Idx));
                p2Idx++;
            }else if (pe1.getOffset().get(p1Idx)<pe2.getOffset().get(p2Idx)){
                result.addOffset(pe1.getOffset().get(p1Idx));
                p1Idx++;
            }else{
                result.addOffset(pe1.getOffset().get(p1Idx));
                p1Idx++;
                p2Idx++;
            }
        }

        // If they are not equally finished we dont wanna miss anything....
        while (pe1.offset.size()>p1Idx){
            result.addOffset(pe1.getOffset().get(p1Idx));
            p1Idx++;
        }
        while (pe2.offset.size()>p2Idx){
            result.addOffset(pe2.getOffset().get(p2Idx));
            p2Idx++;
        }

        return result;
    }

    private PostingsList union(PostingsList p1, PostingsList p2){
        PostingsList result = new PostingsList();
        int p1Idx = 0;
        int p2Idx = 0;

        while (p1.size() > p1Idx && p2.size() > p2Idx){
            if (p1.get(p1Idx).docID == p2.get(p2Idx).docID){
                if (p2.get(p2Idx).docID == 9820){
                    int hold = 1;
                }
                // merge the offsets so that they are in order.... Look @ discussion forum...
                result.addNewEntry(unionDocId(p1.get(p1Idx), p2.get(p2Idx)));
                p1Idx++;
                p2Idx++;

            }else if (p1.get(p1Idx).docID < p2.get(p2Idx).docID){
                result.addNewEntry(p1.get(p1Idx));
                p1Idx++;
            }else{
                result.addNewEntry(p2.get(p2Idx));
                p2Idx++;
            }
        }

        while (p1.size() > p1Idx){
            result.addNewEntry(p1.get(p1Idx));
            p1Idx++;
        }
        while (p2.size() > p2Idx){
            result.addNewEntry(p2.get(p2Idx));
            p2Idx++;
        }
        return result;
    }

    private PostingsList intersectMatchedWords(List<KGramPostingsEntry> matchingWords, String token, int index_wildcard){
        /*
        Time to check if the extracted words are actually matching the real ones...

        1. Loop over all matching words.
        2. Check that the words actually match with our token.
        3. Get the real postingsEntry from the verified result and make sure it's sorted...

        */
        PostingsList result = null;
        for (KGramPostingsEntry matchedWord: matchingWords){
            String matchingTerm = kgIndex.getTermByID(matchedWord.tokenID);

            Boolean verified = false;
            verified = verifyWords(matchingTerm, token, index_wildcard);
            if (verified){
                if(result != null){
                    result = union(result, index.get(matchingTerm));
                }else{
                    result = index.get(matchingTerm);
                }
            }

        }
        return result;
    }

    // TODO: This is so bad it's embarrassing, rewrite this even if it works....
    private List<KGramPostingsEntry> matchEnd(String token){
        List<KGramPostingsEntry> result;
        result = kgIndex.getPostings(token.substring(token.length()-kgIndex.K+1, token.length())+'$');
        if (token.length()<=kgIndex.K){
            return result;
        }
                int size = token.length()-kgIndex.K;
                for (int i = 0; i <= token.length()-kgIndex.K; i++){
                    result = kgIndex.intersect(result, kgIndex.getPostings(token.substring(i, i+kgIndex.K)));
                }
                return result;
        
    }
    private List<KGramPostingsEntry> matchStart(String token){
        List<KGramPostingsEntry> result;
        result = kgIndex.getPostings('^' + token.substring(0, kgIndex.K-1));
        if (token.length()<=kgIndex.K){
            return result;
        }
            int size = token.length()-(kgIndex.K+1);
            for (int i = 0; i <= token.length()-(kgIndex.K+1); ++i){
                String tStr = token.substring(i, i+kgIndex.K);
                List<KGramPostingsEntry> test = kgIndex.getPostings(token.substring(i, i+kgIndex.K));
                System.err.println(token.substring(i, i+kgIndex.K));
                result = kgIndex.intersect(result, kgIndex.getPostings(token.substring(i, i+kgIndex.K)));
            }
            return result;
    }
    private List<KGramPostingsEntry> matchWildcard(String token, int index_wildcard){
        List<KGramPostingsEntry> result;
        int tokenLength = token.length()-1;
        if (index_wildcard == tokenLength) {
            // Word begins with *, meaning that we can't match with ^ but can eliminate words with $
            result = matchStart(token);
            return result;
        }else if (index_wildcard == 0){
            // Word ends with * meaning that we cant match with $ but can word with ^
            result = matchEnd(token);
            return result;
        }else{
            // The default case would lead to the star being centered in the word and we can this match start and end.
            result = kgIndex.intersect(matchStart(token.substring(0, index_wildcard)), matchEnd(token.substring(index_wildcard+1, token.length())));
            return result;
        }
    }    
    private PostingsList getPLWildcard(String token, int index_wildcard){
        
        /*
        1. Get all the words that would suite the wildcard and store these in a list.
        2. Ensure that the relevant words start or end the correct way
        3. Merge the postings entries that we'll get. that being:
        3.1 for every term that a wildcard term has yielded, merge their postingsList, sorted after docId
        3.2 Special case!- if two terms exist in the same docId, then we need to merge the offsets so that they are in order..
        4. return
        */


        // TODO: Something happens in here... It gets stuck, maybe some bad return....
        List<KGramPostingsEntry> matchingWords = matchWildcard(token, index_wildcard);
        
        PostingsList result = intersectMatchedWords(matchingWords, token, index_wildcard);
        
        return result;
    }
    public void computeScores(int n) {
        // For token in this.index.keys()
        // documentFrequency = (this.index(token).size)
        // inverted Df = n/documentFrequency
        // postingsLists = this.index(token)
        // for docId in postingsLists.keys():
        for (String token: this.index.keySet()){
            int df = this.index.get(token).size();
            
            double idf = Math.log((double)n/(double)df);
            if (token.equals("frowning")){
                System.err.println(token+" "+ idf);
            }
            if (token.equals("food")|| token.equals("residence") || token.equals("#redirect") || token.equals("redirect") || token.equals("davis") || token.equals("coop") || token.equals("resident") || token.equals("movein") || token.equals("hall") || token.equals("recycling") || token.equals("drive")){
                System.err.println(token + " " + idf);
            }
            for (PostingsEntry entry: this.index.get(token).getList()){
                if (entry.docID == 9820){
                    int hold = 0;
                }
                int tf = entry.offset.size();
                // Remove this line for now and let us do our normaliziation later on....
                double tf_idf = (tf*idf)/docLengths.get(entry.docID);
                
                // Instead we'll use this line without normalization
                //double tf_idf = (tf*idf);
                entry.score = tf_idf;
            }
        }
    }

    public void computeEucDocLen(){
        Set<Integer> docIds = documentTerms.keySet();
        // IterateOverAllTerms
        /*for (Integer key: docIds){
            double docLenEuc = 0.0;
            ArrayList<String> terms = documentTerms.get(key);
            for (String term: terms){
                docLenEuc += Math.pow(getPostings(term).get(key).score,2);
            }
            docLengthsEuc.put(key, Math.sqrt(docLenEuc));        
        }*/
        Set<String> terms = this.index.keySet();
        for (String termKey: terms){
            PostingsList pl = this.index.get(termKey);
            for( PostingsEntry pe: pl.getList()){
                if (docLengthsEuc.containsKey(pe.docID)){
                    docLengthsEuc.put(pe.docID, docLengthsEuc.get(pe.docID)+ Math.pow(pe.score,2));
                }
                else{
                    docLengthsEuc.put(pe.docID, Math.pow(pe.score,2));
                }
            }
        }
        Set<Integer> docLenKey = docLengthsEuc.keySet();
        for (Integer key: docLenKey){
            docLengthsEuc.put(key, Math.sqrt(docLengthsEuc.get(key)));
        }
    }
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
