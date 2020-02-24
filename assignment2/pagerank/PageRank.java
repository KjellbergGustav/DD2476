import java.util.*;

import javax.management.openmbean.KeyAlreadyExistsException;

import java.io.*;

import java.util.Collections;

public class PageRank {

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
	// c = 1-BORED
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

       
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );
	iterate( noOfDocs, 6);
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new HashMap<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
    void iterate( int numberOfDocs, int maxIterations ) {
	// YOUR CODE HERE
		// a is a vector we choose with size (1, numberOfDocs)
		// ap is the vector we compare with and multiply with P
		// P is our initial probability dist vector, this p[keyset] = 1/keyset.size()
		Double[] a = new Double[numberOfDocs];
		Double[] ap = new Double[numberOfDocs];
		Arrays.fill(a, 0.0);
		Arrays.fill(ap, 0.0);
		ap = randomProbVector(ap);
		
		int no_of_iterations = 0;
		//ap[0] = 1.0;
		while (difference_of_vectors(a, ap, numberOfDocs) >= EPSILON && no_of_iterations <= maxIterations){
			a = ap.clone();
			double sumAp = 0.0;
			for (int row = 0; row < numberOfDocs; row++){
				for (int col = 0; col < numberOfDocs; col++){
					//ap[col] = BORED/numberOfDocs;
					double G = BORED/numberOfDocs;

					HashMap<Integer, Boolean> outwards_links = link.get(row);
					if (outwards_links != null){
						if (outwards_links.get(col) != null){
							//ap[col] += (a[row]/(outwards_links.size())) *(1-BORED);
							//sumAp += ap[row];
							//ap[row] += a[col]*(1.0/outwards_links.size());
							G += ((1.0-BORED)/out[row]);
						}
					}else{
						//ap[row] += a[col]*(1.0/numberOfDocs);
						//ap[col] += (a[row]/numberOfDocs) * (1-BORED);
						//sumAp += ap[row];
						G += (1.0-BORED)/numberOfDocs;
					}
					double aVal = a[row];
					ap[col] += aVal*G;
				}
			}
			double sum_ap = 0.0;
			double sum_a = 0.0;
			for (int i = 0; i < numberOfDocs; i++){
			//	ap[i] = ap[i]/sumAp;
				sum_ap += ap[i];
				sum_a += a[i];
			}
			double new_sum = 0.0;
			for (int i = 0; i < numberOfDocs; i++){
					ap[i] = ap[i]/sum_ap;
					a[i] = a[i]/sum_a;
				}
			no_of_iterations++;
		}
		print_result(ap);
		System.out.println(no_of_iterations);
    }

	private void print_result(Double[] result){
		ArrayList<DocumentRank> docRanks = new ArrayList<DocumentRank>();
		double sum = 0;
		for (double d: result){
			sum += d;
		}
		for (int docId = 0; docId < result.length; docId ++){
			DocumentRank docRank = new DocumentRank();
			docRank.setDocId(docId);
			docRank.setRank(result[docId]);
			docRank.setName(docName[docId]);
			//System.out.println(docName[docId]);
			docRanks.add(docRank);
		}
		Collections.sort(docRanks);
		for (int iter = 0; iter < 30; iter++){
			DocumentRank doc = docRanks.get(iter);
			System.out.println(doc.name + " " + doc.rank);
		}

		try {
			FileWriter myWriter = new FileWriter("./ranks.txt");
			for (DocumentRank doc: docRanks){
				myWriter.write("" + doc.name + " " +doc.rank+ "\n");
			}
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		  } catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		  }

		int a = 1;
	}

	private double difference_of_vectors(Double[] vec1, Double[] vec2, int numberOfDocs){
		double sum_diff = 0.0;
		for (int i = 0; i< numberOfDocs; i++){
			sum_diff += Math.abs(vec1[i] - vec2[i]);
		}
		System.out.println("Diff: " + sum_diff);
		return sum_diff;
	}
    /* --------------------------------------------- */


	private Double[] randomProbVector(Double[] res){
		Random rand = new Random(); 
		
		double sum = 0.0;
		for (int idx = 0; idx < res.length; idx++){
			double random_num = rand.nextDouble();
			res[idx] = random_num;
			sum += random_num;
		}
		for (int idx = 0; idx < res.length; idx++){
			res[idx]= res[idx]/sum;
		}
		return res;
	}
    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}