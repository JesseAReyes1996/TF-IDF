To compile

javac -cp .:commons-validator-1.6.jar:javafx.base-8.60.11.jar docIndex.java

To run

java -cp .:commons-validator-1.6.jar:javafx.base-8.60.11.jar docIndex

This program determines the term weighting, term frequency, inverse document frequency, and the TF-IDF of a given term in a collection of given documents.
It first reads in any stopwords provided by the user in a stoplist.txt file. It then reads in the document collection and builds two inverted indexes from
the given collection. One  being a term index and the other being a document index.

The program allows a user to search any term present in the term index. It will then use the two indexes to perform all necessary calculations to
determine the term weighting, term frequency, inverse document frequency, and the TF-IDF. The search is very efficient as it uses a hash map to perform
the query on the index, resulting in O(1) lookup time.
