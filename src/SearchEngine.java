/**
 * Created by sl049811 on 1/16/15.
 */
// reference: http://www.lucenetutorial.com/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class SearchEngine {

  public static void main(String[] args) throws IOException, ParseException {

    StandardAnalyzer analyzer = new StandardAnalyzer();
    Directory index = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    IndexWriter w = new IndexWriter(index, config);

    System.out.println("Enter path for folder name and location for wikipedia files");

    BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
    String file_location = br2.readLine();
    File folder = new File(file_location);
    File[] listoffiles_in_folder = folder.listFiles();

    for (int i = 0; i < listoffiles_in_folder.length; i++) {
      String file2 = listoffiles_in_folder[i].getName();
      System.out.println("Parsing file: " + i);
      FileReader freader = new FileReader(file_location + "/" + file2);
      BufferedReader breader = new BufferedReader(freader);
      String current_line = null;
      String current_title = "";
      String data = "";

      while ((current_line = breader.readLine()) != null) {
        current_line = current_line.trim();
        current_line = current_line.toLowerCase();
        int len = current_line.length();
        int c = 0;
        int c2 = 0;
        if (Pattern.matches("#redirect(.*?)", current_line) || Pattern
            .matches("image:(.*?)", current_line) ||
            Pattern.matches("\\|(.*?)", current_line) || Pattern.matches("==(.*?)==", current_line)
            || Pattern.matches("file:(.*?)", current_line)) {
          c++;
        }
        if (Pattern.matches("==references==", current_line)) {
          current_line = breader.readLine();
          if (current_line == null) {
            break;
          }
          while (!(Pattern.matches("\\[\\[(.*?)\\]\\]", current_line)) && !(Pattern
              .matches("==(.*?)==", current_line))) {
            current_line = breader.readLine();
            if (current_line == null)

            {
              break;
            }
          }
          if (current_line == null) {
            break;
          }
        }
        if (Pattern.matches("\\[\\[(.*?)\\]\\]", current_line)) {
          c2++;
          len = current_line.length();
          if (current_title != "") {
            data = data.replaceAll("\\[tpl\\](.*?)\\[/tpl\\]", "");
            addDoc(w, data, current_title);
            data = "";
          }
          current_title = current_line.substring(2, len - 2);
        }

        current_line = current_line.replaceAll("[^\\[\\]a-zA-Z0-9/]", " ");
        if (c2 == 0 && c == 0) {
          data = data + "\n" + current_line;
        }
      }
      data = data.replaceAll("\\[tpl\\](.*?)\\[/tpl\\]", "");
      addDoc(w, data, current_title);
    }
    w.close();
    System.out.println("Enter file location");
    BufferedReader br23 = new BufferedReader(new InputStreamReader(System.in));
    String file_location2 = br2.readLine();
    FileReader freader2 = new FileReader(file_location2);
    BufferedReader breader = new BufferedReader(freader2);
    String current_line = null;
    int count_pre = 0;
    int count_any_pre = 0;
    while ((current_line = breader.readLine()) != null) {
      String categ = current_line;
      String text = breader.readLine();
      String answer = breader.readLine();
      text = text.toLowerCase();
      String input_query = categ + " " + text;
      input_query = input_query.toLowerCase();
      System.out.println();
      System.out.println("Query: " + text);
      System.out.println("Expected Answer: " + answer);
      categ = categ.toLowerCase();
      text = text.toLowerCase();
      input_query = input_query.replaceAll("[^\\[\\]a-zA-Z0-9/]", " ");
      categ = categ.replaceAll("[^\\[\\]a-zA-Z0-9/]", " ");
      text = text.replaceAll("[^\\[\\]a-zA-Z0-9/]", " ");
      String querystr1 = args.length > 0 ? args[0] : input_query;
      String querystr2 = args.length > 0 ? args[0] : text;
      Query q1 = new QueryParser("title", analyzer).parse(querystr1);
      Query q2 = new QueryParser("title", analyzer).parse(querystr2);
      int hitsPerPage = 10;
      IndexReader reader = DirectoryReader.open(index);
      IndexSearcher searcher = new IndexSearcher(reader);
      BM25Similarity custom = new BM25Similarity(0.5f, 0.1f);
      searcher.setSimilarity(custom);
      TopScoreDocCollector collector1 = TopScoreDocCollector.create(hitsPerPage);
      TopScoreDocCollector collector2 = TopScoreDocCollector.create(hitsPerPage);
      searcher.search(q1, collector1);
      searcher.search(q2, collector2);
      ScoreDoc[] hits1 = collector1.topDocs().scoreDocs;
      ScoreDoc[] hits2 = collector2.topDocs().scoreDocs;
      int any_pre = 0;
      String[] categ_words = categ.split(" ");
      for (int i = 0; i < hits1.length; ++i) {
        int docId1 = hits1[i].doc;
        int docId2 = hits2[i].doc;
        boolean categ_in_title = false;
        Document d = searcher.doc(docId1);
        String[] title_words = d.get("isbn").toLowerCase().split(" ");
        for (int x = 0; x < categ_words.length; x++) {
          for (int y = 0; y < title_words.length; y++) {
            title_words[y] = title_words[y].replaceAll("[^\\[\\]a-zA-Z0-9/]", " ");
            categ_words[x] = categ_words[x].trim();
            title_words[y] = title_words[y].trim();
            if (categ_words[x].equalsIgnoreCase(title_words[y])) {
              categ_in_title = true;
              d = searcher.doc(docId2);//changing the searcher to only question excluding category
            }
          }
        }
        if (d.get("isbn").toLowerCase().matches(answer.toLowerCase())) {
          any_pre = 1;
        }
        if (i == 0 && d.get("isbn").toLowerCase().matches(answer.toLowerCase())) {
          count_pre++;
        }
        if (!categ_in_title) {
          System.out.println((i + 1) + ". " + d.get("isbn") + "\t Score: " + hits1[i].score);
        } else {
          int kk = i;
          if (i < 9) {
            kk++;
          }
          docId2 = hits2[kk].doc;
          d = searcher.doc(docId2);

          System.out.println((i + 1) + ". " + d.get("isbn") + "\t Score: " + hits2[i].score);
        }
      }
      if (any_pre == 1) {
        count_any_pre++;
      }
      reader.close();
    }
    System.out.println("precision at 1 " + count_pre + "%");
    System.out.println("precision at 10 " + count_any_pre + "%");
  }

  private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("title", title, Field.Store.YES));
    doc.add(new StringField("isbn", isbn, Field.Store.YES));
    w.addDocument(doc);
  }

 /* String lemmatize(String text)
  {
	  String lemma=text;
	  Properties props = new Properties();
	  props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
	  StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	  Annotation document = new Annotation(text);
	  pipeline.annotate(document);
	  List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
	  int sentCount = 1;
	  for(CoreMap sentence: sentences) {
	       for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
	          String word = token.get(CoreAnnotations.TextAnnotation.class);
	        }
	        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
	           lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
	        }
	        sentCount ++;
	      }
	      return lemma;
  }*/
}

