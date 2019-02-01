import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.lang.Math;

import org.apache.commons.validator.routines.UrlValidator;
import javafx.util.Pair;

public class docIndex {

	//static array list to store stop words in
	private static ArrayList<String> stopWords = new ArrayList<String>();

	//get the stop words and store them in stopWords(ArrayList)
	private static void readStopWords(String fileName){
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
    		for(String line; (line = br.readLine()) != null;){
        		stopWords.add(line);
    		}
		}catch(Exception e){
			System.out.println(e);
		}
	}

	//check if a given word is a stop word
	private static boolean isStopWord(String word){
		word = word.toLowerCase();
		if(stopWords.contains(word)){
			return true;
		}
		return false;
	}

	//list of documents without stop words
	private static List<List<String>> documents = new ArrayList<List<String>>();

	//read in the documents and store them in a list of lists
	private static void readDocuments(String dirName){
		File dir = new File(dirName);
  		File[] directoryListing = dir.listFiles();

		List<String> tempDocument;

		//to check whether the words are URLs, we don't want to include URLs
		UrlValidator validator = new UrlValidator(){
	        @Override
	        public boolean isValid(String value) {
	            return super.isValid(value) || super.isValid("http://" + value);
	        }
		};

  		if(directoryListing != null){
			Arrays.sort(directoryListing);

			//iterate through the directory
			for(File child : directoryListing){

				//instantiate a new temp document
				tempDocument = new ArrayList<String>();

				try(BufferedReader br = new BufferedReader(new FileReader(child))) {
					//iterate through the document
					for(String line; (line = br.readLine()) != null;){

						//remove the zero width space character
						line = line.replaceAll("[\\p{C}]", "");

						//split the current line using whitespace as a delimiter
						String[] currLine = line.split("\\s+");

						//iterate through the current line and add any valid words to tempDocument(ArrayList<String>)
						for(int i = 0; i < currLine.length; ++i){
							currLine[i] = currLine[i].toLowerCase();
							//don't include URLs
							if(validator.isValid(currLine[i])){
								continue;
							}

							//split hyphenated words
							if(currLine[i].contains("-")){
								String[] hyphenSplit = currLine[i].split("-");

								//split words with a forward slash
								if(hyphenSplit[0].contains("/")){
									String[] forwardSlashSplit = hyphenSplit[0].split("/");
									if(!isStopWord(forwardSlashSplit[0])){
										tempDocument.add(forwardSlashSplit[0]);
									}
									if(!isStopWord(forwardSlashSplit[1])){
										tempDocument.add(forwardSlashSplit[1]);
									}
								}
								else if(!isStopWord(hyphenSplit[0])){
									tempDocument.add(hyphenSplit[0]);
								}

								//split words with a forward slash
								if(hyphenSplit[1].contains("/")){
									String[] forwardSlashSplit = hyphenSplit[1].split("/");
									if(!isStopWord(forwardSlashSplit[0])){
										tempDocument.add(forwardSlashSplit[0]);
									}
									if(!isStopWord(forwardSlashSplit[1])){
										tempDocument.add(forwardSlashSplit[1]);
									}
								}
								else if(!isStopWord(hyphenSplit[1])){
									tempDocument.add(hyphenSplit[1]);
								}
								continue;
							}

							//split words with a forward slash
							if(currLine[i].contains("/")){
								String[] forwardSlashSplit = currLine[i].split("/");
								if(!isStopWord(forwardSlashSplit[0])){
									tempDocument.add(forwardSlashSplit[0]);
								}
								if(!isStopWord(forwardSlashSplit[1])){
									tempDocument.add(forwardSlashSplit[1]);
								}
								continue;
							}

							//check that the current word isn't a stop word
							if(!isStopWord(currLine[i])){
								tempDocument.add(currLine[i]);
							}
						}
		    		}
				}catch(Exception e){
					System.out.println(e);
				}
				documents.add(tempDocument);
    		}
  		}
		else{
			System.out.println("ERROR: " + dirName + " is not a directory");
  		}
	}

	//key: docID, value: # terms in document
	public static HashMap<Integer, Integer> docIndex = new HashMap<Integer, Integer>();

	public static void createDocIndex(List<List<String>> documents){
		int numTerms;
		//iterate through the documents
		for(int i = 0; i < documents.size(); ++i){
			//get the number of terms in each document
			numTerms = documents.get(i).size();
			//put the docID and number of terms into the index
			docIndex.put(i, numTerms);
		}
	}

	//key: term, value: # documents with a point to a linked list of postings with docID/frequency
	public static HashMap<String, Pair<Integer, LinkedList<Pair<Integer,Integer>>>> termIndex = new HashMap<String, Pair<Integer, LinkedList<Pair<Integer,Integer>>>>();

	public static void createTermIndex(List<List<String>> documents){
		for(int i = 0; i < documents.size(); ++i){
			for(int j = 0; j < documents.get(i).size(); ++j){

				String term = documents.get(i).get(j);

				//check whether the term is already in the term index
				Pair<Integer, LinkedList<Pair<Integer,Integer>>> hit = termIndex.get(term);

				//term is not in the index
				if(hit == null){
					//instantiate a new linked list and add the docID and docFreq pair
					int docID = i;
					int docFreq = 1; //since this is the first time the term is hitting, docFreq is one
					Pair<Integer,Integer> docIDFreqPair = new Pair<>(docID, docFreq);
					LinkedList<Pair<Integer,Integer>> postings = new LinkedList<Pair<Integer,Integer>>();
					postings.add(docIDFreqPair);

					int numDocs = 1; //since this is the first time the term is hitting the doc, numDocs is one

					//instantiate a new pair with # docs and the linked list postings
					hit = new Pair<Integer, LinkedList<Pair<Integer,Integer>>>(numDocs, postings);
					//place the term and index into the term index
					termIndex.put(term, hit);
				}

				//term is in the index
				else{
					int currDocID = i;

					boolean inPostings = false;
					LinkedList<Pair<Integer,Integer>> postings = hit.getValue();

					//iterate through the postings and check if we already have the document
					for(int k = 0; k < postings.size(); ++k){
    					int postingID = postings.get(k).getKey();
						//the term appears again in the same document
						if(currDocID == postingID){
							inPostings = true;
							//increase the document frequency by one
							int docFreq = postings.get(k).getValue() + 1;
							Pair<Integer,Integer> docIDFreqPair = new Pair<>(postingID, docFreq);
							postings.set(k, docIDFreqPair);
						}
					}
					//if the document is not apart of postings
					if(!inPostings){
						int numDocs = hit.getKey() + 1; //increase the number of docs the term is in
						int docFreq = 1; //since this is the first time the term is hitting, docFreq is one
						Pair<Integer,Integer> docIDFreqPair = new Pair<>(currDocID, docFreq);
						postings.add(docIDFreqPair);
						//update the number of documents
						Pair<Integer, LinkedList<Pair<Integer,Integer>>> hitUpdate = new Pair<Integer, LinkedList<Pair<Integer,Integer>>>(numDocs, postings);
						termIndex.put(term, hitUpdate);
					}
				}
			}
		}
	}

    public static void main(String[] args){

		//get the stop words
		readStopWords("stoplist.txt");

		//get the documents
		readDocuments("data");

		//create a document index
		createDocIndex(documents);

		//create a term index
		createTermIndex(documents);

		//test our indexes
		test tester = new test();
		tester.runTest(termIndex, docIndex);
    }
}

class test {
	public static void computeTermWeightings(String term, LinkedList<Pair<Integer,Integer>> postings){}

	public static void computeTF(LinkedList<Pair<Integer,Integer>> postings, HashMap<Integer, Integer> docIndex){
		for(int i = 0; i < postings.size(); ++i){
			System.out.println("numerator " + postings.get(i).getValue());
			System.out.println("denominator " + docIndex.get(postings.get(i).getKey()));
			double tf = (double) postings.get(i).getValue()/ (double) docIndex.get(postings.get(i).getKey());
			System.out.println("TF (document " + Integer.toString(postings.get(i).getKey()) + "): " + Double.toString(tf));
		}
	}

	public static void computeIDF(String term, LinkedList<Pair<Integer,Integer>> postings, HashMap<Integer, Integer> docIndex){
		//take the natural log of (number of docs/number of docs with term 't')
		System.out.println("numerator" + docIndex.size());
		System.out.println("denominator" + postings.size());
		double idf = Math.log( (double) docIndex.size() / (double) postings.size());

		System.out.println("IDF (" + term + "): " + Double.toString(idf));
	}

	public static void computeTF_IDF(String term, LinkedList<Pair<Integer,Integer>> postings){}

	public static void runTest(HashMap<String, Pair<Integer, LinkedList<Pair<Integer,Integer>>>> termIndex, HashMap<Integer, Integer> docIndex){

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String query;

		while(true){
			try{
				System.out.print("Enter a search term(\"QUIT\" to quit): ");
				query = in.readLine();
				if(query.equals("QUIT")){
					break;
				}
				query = query.toLowerCase();

				Pair<Integer, LinkedList<Pair<Integer,Integer>>> postings = termIndex.get(query);
				System.out.println(docIndex);
				System.out.println(postings.getValue());
				computeTF(postings.getValue(), docIndex);
				computeIDF(query, postings.getValue(), docIndex);

			}catch(Exception e){
				System.out.println("your input is invalid");
				continue;
			}
		}
	}
}
