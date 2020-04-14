/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */

package ir;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.*;

import ir.Query.QueryTerm;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;
    
    HashMap<String,Double> pageRankScore = new HashMap<String, Double>();

    Double pageRankWeight = 1.0;

    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType, NormalizationType normalizationType ) { 
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        PostingsList result = new PostingsList();
        
        /*switch(queryType){
            case INTERSECTION_QUERY:
                result = Search_insterseaction(query);
                break;
            case PHRASE_QUERY:
                result = Search_phrase(query, 1);
                break;
            case RANKED_QUERY:
                break;
        }*/

        if (query.queryterm.size() == 1){
            result = this.index.getPostings(query.queryterm.get(0).term, kgIndex);
            switch(queryType){
                case RANKED_QUERY:
                    readPageRankFile("ranks.txt");
                    result = rankedSearch(query,result, rankingType, normalizationType);
            }
        }else{
            if (queryType == QueryType.RANKED_QUERY){
                readPageRankFile("ranks.txt");
                result = rankedSearch(query,null, rankingType, normalizationType);
                return result;
            }
            for (int queryTermIndex = 0; queryTermIndex < query.queryterm.size()-1; queryTermIndex++){
                // Ground case when we compare with non-result list
                PostingsList p2 = this.index.getPostings(query.queryterm.get(queryTermIndex+1).term, kgIndex);
                if (queryTermIndex == 0){
                    result = this.index.getPostings(query.queryterm.get(queryTermIndex).term, kgIndex);
                }
                p2.getList().sort(Comparator.comparing(PostingsEntry::getDocId));
                //p2.getList().sort((pe1, pe2) -> pe1.getDocId().compareTo(pe2.getDocId()));
                result.getList().sort(Comparator.comparing(PostingsEntry::getDocId));
                switch(queryType){
                    case INTERSECTION_QUERY:
                        result = IntersectSearch(result,p2);
                        break;
                    case PHRASE_QUERY:
                        result = PhraseSearch(result, p2, 1);
                        break;
                    case RANKED_QUERY:
                        break;
                }
            }
        }
        return result;
    }

    private void readPageRankFile(String filename){
        // Code from skeleton in persistentHashedIndex
        try{
            FileReader freader = new FileReader(filename);
            BufferedReader br = new BufferedReader(freader);
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(" ");
                String fileName = data[0];
                Double score = Double.parseDouble(data[1]);
                pageRankScore.put(fileName, score);
            }
        freader.close();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }

    }

    private double calcCombinedScore(PostingsEntry pe, double score, RankingType rankingType){
        // Gets the docName that we'll remove the path from and the use as key for our own hashMap
        // TF_IDF, PAGERANK, COMBINATION
        if (rankingType == RankingType.TF_IDF){return score;}
        else if (rankingType == RankingType.COMBINATION || rankingType == RankingType.PAGERANK){
            String docNameUnstripped = this.index.docNames.get(pe.docID);
            int stripIndex = docNameUnstripped.lastIndexOf("/") + 1;
            String docNameStripped = docNameUnstripped.substring(stripIndex, docNameUnstripped.length());
            double prScore = pageRankScore.get(docNameStripped);
            if (rankingType == RankingType.PAGERANK){return prScore;}
            return combinationScoreFunction(prScore, score, pe);
        }
        return score;
    }

    private double combinationScoreFunction(double prScore, double tfidfScore, PostingsEntry pe){
        return prScore*pageRankWeight+tfidfScore/this.index.docLengths.get(pe.docID);
    }
    private double normalizeScore(PostingsEntry pe, NormalizationType normalizationType){
        if (normalizationType == NormalizationType.EUCLIDEAN){
            double s = pe.score/this.index.docLengthsEuc.get(pe.docID);
            return s;
        }else{
            double s = pe.score/this.index.docLengths.get(pe.docID);
            return s;
        }
    }

    private PostingsList rankedSearch(Query query, PostingsList p1, RankingType rankingType, NormalizationType normalizationType){
        if (p1 != null){
            PostingsList res = new PostingsList();
            for (PostingsEntry pe: p1.getList()){
                //TODO: pe.score = query.queryterm.get(0).weight*normalizeScore(pe, normalizationType);
                pe.score = query.queryterm.get(0).weight*pe.score;
            }
            Collections.sort(p1.getList());
            return p1;
        }
        else{
            PostingsList result = new PostingsList();
            for (int queryTermIndex = 0; queryTermIndex <= query.queryterm.size()-1; queryTermIndex++){
                PostingsList pl = this.index.getPostings(query.queryterm.get(queryTermIndex).term, kgIndex);
                for (PostingsEntry pe: pl.getList()){
                    if (result.entryExist(pe.docID) == null){
                        if (pe.docID == 9820){
                            int holds = 1;
                        }
                        PostingsEntry peRes = new PostingsEntry(pe.docID);
                        //TODO: Double normalizedScore = normalizeScore(pe, normalizationType);
                        Double normalizedScore = pe.score;
                        peRes.score = normalizedScore*query.queryterm.get(queryTermIndex).weight;
                        peRes.score = calcCombinedScore(peRes, peRes.score, rankingType);
                        result.addNewEntry(peRes);
                    }
                    else{
                        if (pe.docID == 9820){
                            int holds = 1;
                        }
                        // TODO: Double normalizedScore = normalizeScore(pe, normalizationType);
                        Double normalizedScore = pe.score;

                        double score = normalizedScore*query.queryterm.get(queryTermIndex).weight;
                        pe = result.getEntryByDocId(pe.docID);
                        //result.getEntryByDocId(pe.docID).score += pe.score*query.queryterm.get(queryTermIndex).weight;
                        result.getEntryByDocId(pe.docID).score +=calcCombinedScore(pe, score, rankingType);

                    }
                }
            }
            for (int idx = 0; idx<result.getList().size()-1; idx++){

                int len = this.index.docLengths.get(result.get(idx).docID);
                double score = result.get(idx).score;
                
                Collections.sort(result.getList());
            }
            return result;
        }

    }
    
    private PostingsList PhraseSearch(PostingsList p1, PostingsList p2, int distance){
        PostingsList phrasePostingsList = new PostingsList();
        //TOD: You need to sort the  lists
        int lengthP1 = p1.size()-1;
        int lengthP2 = p2.size()-1;
        int indexP1 = 0;
        int indexP2 = 0;

        while (indexP1 <= lengthP1 && indexP2 <= lengthP2){
            p1.get(indexP1).getOffset().size();
            p2.get(indexP2).getOffset().size();
            if (p1.get(indexP1).docID == p2.get(indexP2).docID){
                ArrayList<Integer> offsetList = new ArrayList<Integer>();
                ArrayList<Integer> p1Offset = p1.get(indexP1).getOffset();
                ArrayList<Integer> p2Offset = p2.get(indexP2).getOffset();
                int lengthOffsetP1 = p1Offset.size()-1;
                int lengthoffsetP2 = p2Offset.size()-1;
                int offsetIndexP1 = 0;
                int offsetIndexP2 = 0;
                
                /*for (int os1: p1Offset){
                    for (int os2: p2Offset){
                        if (Math.abs(os2-os1)  <= 1){
                            offsetList.add(os2);
                        }else if (os1 < os2){
                            break;
                        }
                    }
                    for (int res: offsetList){
                        phrasePostingsList.addNewEntry(p2.get(indexP2), res);
                    }
                }*/
                while (offsetIndexP1 <= lengthOffsetP1){
                    while (offsetIndexP2 <= lengthoffsetP2){
                        if ((p2Offset.get(offsetIndexP2)-p1Offset.get(offsetIndexP1)) == distance){
                            offsetList.add(p2Offset.get(offsetIndexP2));
                        }else if (p1Offset.get(offsetIndexP1)<p2Offset.get(offsetIndexP2)){
                            break;
                        }
                        offsetIndexP2++;
                    }
                
                while (!offsetList.isEmpty() && Math.abs(offsetList.get(0)-p1Offset.get(offsetIndexP1))>distance){
                    offsetList.remove(0);
                }
                // We should add p2 here maybe??
                // We should also return the Saved offsets as that is all we need to look at???
                for (int savedOffset: offsetList){
                    PostingsEntry newEntry = new PostingsEntry(p2.get(indexP2).getDocId());

                    phrasePostingsList.addNewEntry(newEntry, savedOffset);
                }
                offsetIndexP1++;
                }
                indexP1++;
                indexP2++;
            }else if (p1.get(indexP1).docID < p2.get(indexP2).docID){
                indexP1++;
            }else{
                indexP2++;
            }
        }
        return phrasePostingsList;
    }
    
    /*private PostingsList Search_insterseaction(Query query){
        PostingsList result = new PostingsList();
        if (query.queryterm.size() == 1){
            result = this.index.getPostings(query.queryterm.get(0).term);
            System.out.println("The term");
            System.out.println(query.queryterm.get(0).term);
            System.out.println("The resulting PostingsList");
            System.out.println(result);
        }else{
            System.out.println("Query length");
            System.out.println(query.queryterm.size());
            for (int queryTermIndex = 0; queryTermIndex < query.queryterm.size()-1; queryTermIndex++){
                // Ground case when we compare with non-result list
                System.out.println(String.format("Loop nr %d", queryTermIndex+1));
                PostingsList p2 = this.index.getPostings(query.queryterm.get(queryTermIndex+1).term);
                if (queryTermIndex == 0){
                    result = this.index.getPostings(query.queryterm.get(queryTermIndex).term);
                }
                result = IntersectSearch(result,p2);
            }
        }
        return result;
    }*/

    // Compares two posting lists and returns one on where they instersect
    // Input: Two posting lists p1 and p2
    // Return: A instersect<p1,p2>
    private PostingsList IntersectSearch(PostingsList p1, PostingsList p2){
        PostingsList instersecPostingsList = new PostingsList();

        int lengthP1 = p1.size()-1;
        int lengthP2 = p2.size()-1;
        int indexP1 = 0;
        int indexP2 = 0;

        while (indexP1 <= lengthP1 && indexP2 <= lengthP2){
            if (p1.get(indexP1).docID == p2.get(indexP2).docID){
                instersecPostingsList.addNewEntry(p1.get(indexP1));
                indexP1 ++;
                indexP2 ++;
            }else if(p1.get(indexP1).docID<p2.get(indexP2).getDocId()) {
                indexP1 ++;
            }else{
                indexP2++;
            }
        }
        return instersecPostingsList;
    }
}