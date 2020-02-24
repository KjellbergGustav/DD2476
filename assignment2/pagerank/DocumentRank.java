import java.util.*;

import java.io.*;

public class DocumentRank implements Comparable<DocumentRank>{
    public int docId;
    public double rank;
    public String name;

    public DocumentRank(){
    }

    public void setDocId(int docId){
        this.docId = docId;
    }

    public void setRank(double rank){
        this.rank = rank;
    }

    public void setName(String name){
        this.name = name;
    }

    public int compareTo(DocumentRank other){
        return Double.compare(other.rank, rank);
    }
}