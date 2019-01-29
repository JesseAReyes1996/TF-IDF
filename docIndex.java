import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

import org.apache.commons.validator.routines.*;

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
	public static boolean isStopWord(String word){
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
		Arrays.sort(directoryListing);

		List<String> tempDocument;

		//to check whether the words are URLs, we don't want to include URLs
		UrlValidator validator = new UrlValidator(){
	        @Override
	        public boolean isValid(String value) {
	            return super.isValid(value) || super.isValid("http://" + value);
	        }
		};

  		if(directoryListing != null){
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
							//don't include URLs
							if(validator.isValid(currLine[i])){
								continue;
							}

							//split hyphenated words
							if(currLine[i].contains("-")){
								String[] hyphenSplit = currLine[i].split("-");
								if(!isStopWord(hyphenSplit[0])){
									tempDocument.add(hyphenSplit[0]);
								}
								if(!isStopWord(hyphenSplit[1])){
									tempDocument.add(hyphenSplit[1]);
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

	private static void createDocIndex(List<List<String>> documents){
		//iterate through the documents
		for(int i = 0; i < documents.size(); ++i){
			//get the number of terms in each document
			int numTerms = 0;
			numTerms = documents.get(i).size();
			docIndex.put(i+1, numTerms);
		}
		System.out.println(docIndex);
	}

    public static void main(String[] args){

		//get the stop words
		readStopWords("stoplist.txt");

		//get the documents
		readDocuments("data");

		//create a document index
		createDocIndex(documents);
    }
}