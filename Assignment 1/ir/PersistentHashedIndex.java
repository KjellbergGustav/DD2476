/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.*;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The dictionary file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    // 611953 is a prime 
    public static final long TABLESIZE = 1007939L;

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */ 
    public class Entry {
        //
        //  YOUR CODE HERE
        //

        // This is a representation of the data written, that being:
        // 1. The key (word) so that we ensure that we retrive the correct line!
        // 2. The size of the data written so that we know how much we should fetch.
        // 3. The pointer to the line.
        long dataDocLocation;
        String word;
        int postingsListSize;
        public Entry(long dataDocLocation, String word, int postingsListSize){
            this.dataDocLocation = dataDocLocation;
            this.word = word;
            this.postingsListSize = postingsListSize;
        }

        public long getPointer(){
            return this.dataDocLocation;
        }
        public String getWord(){
            return this.word;
        }
        private int getSizeOfPL(){
            return this.postingsListSize;
        }

        public String toString(){
            return this.getWord() +";" + this.getSizeOfPL() + ";"+this.getPointer() +";" + "\n";
        }

        public String pointerToString(){
            return this.getPointer() + ";";
        }
    }


    // ==================================================================

    
    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created. 
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
        } catch ( FileNotFoundException e ) {
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek( ptr );
            byte[] data = dataString.getBytes();
            dataFile.write( data );
            return data.length;
        } catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file. 
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
        //
        //  YOUR CODE HERE
        //
        try {
            ptr*=14;
            dictionaryFile.seek(ptr);
            //byte[] entryData = entry.toString().getBytes();
            //String test = new String(entryData);
            //System.err.println("Size of entry" + entryData.length);
            dictionaryFile.writeChar(entry.getWord().charAt(0));
            dictionaryFile.writeLong(entry.getPointer());
            dictionaryFile.writeInt(entry.getSizeOfPL());

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry( long ptr, String searchWord) {   
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE 
        //

        /*return this.getWord() +";" + this.getSizeOfPL() + ";"+this.getPointer() + "\n";*/
        try {
            ptr *=14;
            dictionaryFile.seek(ptr);
            //dictionaryFile.seek(ptr*100 + (searchWord.hashCode()%91));
            
            //dictionaryFile.seek( ptr ); 

            //char readChar = dictionaryFile.readChar();

            if (dictionaryFile.readChar() != searchWord.charAt(0)){return null;}
            /*if (searchWord.charAt(0) != readChar){return null;}*/
            /*if (fethedWord != searchWord){
                return null;
            }*/
            long fetchedPointer = dictionaryFile.readLong();
            int fetchedPLsize = Math.abs(dictionaryFile.readInt());
            //int test = Integer.parseInt(fetchedPLsize);
            Entry returnedEntry = new Entry(fetchedPointer, searchWord, fetchedPLsize);
            return returnedEntry;
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for (Map.Entry<Integer,String> entry : docNames.entrySet()) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write(docInfoEntry.getBytes());
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put(new Integer(data[0]), data[1]);
                docLengths.put(new Integer(data[0]), new Integer(data[2]));
            }
        }
        freader.close();
    }

    private long hashFunc(String key, Boolean recusion){
        // Inspo from: https://www.strchr.com/hash_functions
        /*Kernighan and Ritchie's function uses INITIAL_VALUE of 0 and M of 31.*/
        
        long hashValue = 7; // Fits most, see link above (should also be prime)
        for(int i = 0; i < key.length(); i++){
            hashValue = hashValue*31 + key.charAt(i);
            hashValue = (hashValue%TABLESIZE);
        }

        // As words can have the same code, we reverse to get more uniqueness
        // trying to obatin a so called Avalanche effect.
        if (!recusion){
            String reversedString = new StringBuilder(key).reverse().toString(); // this should yield another hash val. (flipping bits)
            long reversedHash = hashFunc(reversedString, true); // The hash val for flipped bits
            hashValue *= 100; // make the last bits 0, this basically increase our avalanche effect...
            reversedHash = (reversedHash%70); // we only care about a couple of digits now to make it unique and we get another 
            // type of combination compared to % TABLESIZE
            hashValue += reversedHash;
        }
        return hashValue%TABLESIZE;
        
        // This was obiously shit.
        //long hashValue = (Math.abs(key.hashCode()) % TABLESIZE);
        //return Math.abs(hashValue);
    }
    private long hashFunc(String key){
        return hashFunc(key, false);
    }

    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        try {

            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings list
            // 
            //  YOUR CODE HERE
            //
            Set<String> keys = this.index.keySet();
            HashMap<Long,Integer> tempHashMap = new HashMap<Long,Integer>();
            String stringRepOfClass;
            long hashValue;
            int iter = 0;
            for (String key: keys){
                PostingsList postingsList = this.index.get(key);
                stringRepOfClass = postingsList.toString();
                stringRepOfClass = key + ":" + stringRepOfClass; // prepend the word

                hashValue = hashFunc(key); // Returns the hashvalue (pntr in dictFile)

                // This will double the hashValue if there's a collision
                // char = 2, int = 4, long = 8
                int sizeOfEntry = 14;
                //while ((!(tempHashMap.putIfAbsent(hashValue, 1) == null))){
                while ((!(tempHashMap.putIfAbsent(hashValue, 1) == null) &&!(tempHashMap.putIfAbsent(hashValue+sizeOfEntry, 1) == null) )){
                    hashValue = Math.abs(hashValue+1) % TABLESIZE;
                    //System.err.println("hashValue:" + hashValue);
                    collisions++;
                }

                //Integer a = tempHashMap.putIfAbsent(Math.abs(hashValue), 1);
                //if (a == null){collisions++;};

                int returnedSizeOfData = writeData(stringRepOfClass, free); // write the data to the file and increase the pntr in the datafile.
                writeEntry(new Entry(free, key, returnedSizeOfData), hashValue); // write the entry to the dict file at pos hashValue

                free += returnedSizeOfData;
            }

                
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }


    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        long tokenHash = hashFunc(token);
        Entry entry = readEntry(tokenHash, token);
        while (entry == null){
            tokenHash = (Math.abs(tokenHash+1) % TABLESIZE);
            entry = readEntry(tokenHash, token);
        }

        String data = readData(entry.getPointer(), entry.getSizeOfPL());
        PostingsList postingsList = new PostingsList();
        String[] dataSplit = data.split(":"); // this will contain a postingListObj
        boolean notMatch = true;
        //while (notMatch){
        for (int c = 0; c <=token.length()-1; c++){
            if (token.charAt(c) == dataSplit[0].charAt(c)){notMatch = false;}
            else{notMatch = true;}
            }/*
            tokenHash = (Math.abs(tokenHash+1) % TABLESIZE);
            entry = readEntry(tokenHash, token);
            data = readData(entry.getPointer(), entry.getSizeOfPL());
            postingsList = new PostingsList();
            dataSplit = data.split(":"); // this will contain a postingListObj
        }*/
        for (int i = 1; i<=dataSplit.length-1; i++){
            // TODO: There are multiple offsets, you'll need to iterate over them and add, though doesn't feel efficient...
            String[] splittedPostings = dataSplit[i].split(";");
            String docId = splittedPostings[0];
            Double score = Double.parseDouble(splittedPostings[splittedPostings.length-1]);
            //docId = docId.replace("[", "");
            PostingsEntry postingsEntry = new PostingsEntry(Integer.parseInt(docId));
            postingsEntry.score = score;
            //String[] offsetSplit = offsetString.split(";");
            //offsetString = offsetString.replace("[", "");
            //offsetString = offsetString.replace("]", "");
            for (int l = 1; l<=splittedPostings.length-2; l++){
                String offsetVal = splittedPostings[l];
                offsetVal = offsetVal.replace(" ", "");
                postingsList.addNewEntry(postingsEntry, Integer.parseInt(offsetVal));
            }
        }
        return postingsList;
    }


    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert( String token, int docID, int offset ) {
            //
            // YOUR CODE HERE
            // putIfAbsent can be used to put a value in the map if the key does not exists, then returns null, else, returns value.
            final PostingsEntry newPostingEntry = new PostingsEntry(docID);
            // adds a new list if the token is absent in the hashmap.
            this.index.putIfAbsent(token,new PostingsList());
            // get the empty or existing postingsList
            PostingsList postingsList = index.get(token);
            postingsList.addNewEntry(newPostingEntry, offset);
            
            //newPostingEntry.addOffset(offset);
    
    
            //
    }

    public void computeScores(int n){
        // For token in this.index.keys()
        // documentFrequency = (this.index(token).size)
        // inverted Df = n/documentFrequency
        // postingsLists = this.index(token)
        // for docId in postingsLists.keys():
        for (String token: this.index.keySet()){
            int df = this.index.get(token).size();
            double idf = Math.log(n/df);
            for (PostingsEntry entry: this.index.get(token).getList()){
                int tf = entry.offset.size();
                double tf_idf = (tf*idf)/docLengths.get(entry.docID);
                entry.score = tf_idf;
            }
        }
    }

    /**
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        writeIndex();
        System.err.println( "done!" );
    }
}
