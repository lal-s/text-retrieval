# text-retrieval

This is a simple text retrieval system which parses wikipedia documents and builds the inverted index using [Lucene](http://lucene.apache.org/core/) library.
 
The aim is reconstruct parts of [IBM's Watson](http://www.aaai.org/Magazine/Watson/watson.php) which is a question answering system developed for a quiz show 'Jeopardy'
  
The system takes in a query as input and processes it using Lucene's Query Parser. The scoring algorithm uses tf-idf term weighting to get the result
where: 
  
  ```
  tf(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
  idf(t) = log_e(Total number of documents / Number of documents with term t in it).
  score= tf * idf
  ```
  
 The system then returns the top 10 matching documents .
 On the evaluated data set, the precision for the system for 
 
 ```
 precision@1 = 62%
 precision@10 = 79%
 ```
  
 
