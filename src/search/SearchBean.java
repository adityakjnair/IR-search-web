package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.print.Doc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import index.Token;
import index.TokenDAO;

/**
 *
 * @author aditya
 */
@ManagedBean(name = "searchBean")
@SessionScoped
public class SearchBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static List<String> STOP_WORDS = Arrays.asList("a", "able", "about", "across", "after", "all", "almost",
			"also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "brought", "but",
			"by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for",
			"from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if",
			"in", "into", "is", "it", "its", "just", "know", "known", "least", "let", "like", "likely", "may", "made",
			"make", "me", "might", "more", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often",
			"on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so",
			"some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to",
			"too", "twas", "us", "wants", "was", "we", "well", "were", "what", "when", "where", "which", "while", "who",
			"whom", "why", "will", "with", "would", "yet", "you", "your");

	private static final int PAGE_SIZE = 5;
	private static final String PARENT_FOLDER = "C:\\Users\\adity\\Desktop\\IR\\WEBPAGES_RAW\\";

	private Map<String, String> urlMap;
	private List<DocSearchResult> resultObjs;
	private int page;
	private int totalPages;
	private int totalResults;

	private double timeTaken;
	private String query;
	private boolean doPaginate;
	private boolean fromNavigate;
	private boolean noResults;

	public SearchBean() {
		urlMap = setUrlFromFile();
		query = "";
		resultObjs = new ArrayList<>();
		page = 0;
		totalPages = 0;
		totalResults = 0;
		timeTaken = 0L;
		doPaginate = false;
		fromNavigate = false;
		noResults = false;
		
	}

	public void searchQuery() {
		if(!isFromNavigate())
			setPage(0);
		Long start = System.nanoTime();
		setResultObjs(new ArrayList<>());
		List<String> queryWords = tokenizeQuery(this.query);
		List<DocSearchResult> displayResults = queryWords.size() == 1 ? searchForSingleWord(queryWords.get(0))
				: searchForMultipleWords(queryWords);
		if(displayResults.size() == 0)
			setNoResults(true);
		else
			setNoResults(false);
		setTotalResults(displayResults.size());
		setResultObjects(displayResults);
		setFromNavigate(false);
		Long end = System.nanoTime();
		Long timeInNano = end - start;
		setTimeTaken(timeInNano.doubleValue() / 1E9);
	}

	private void setResultObjects(List<DocSearchResult> resultObjects) {
		List<DocSearchResult> results = getResultObjs();
		// TODO: Get title and snippet
		setDoPaginate(resultObjects.size() > PAGE_SIZE);
		setTotalPages(resultObjects.size() / PAGE_SIZE);
		int paginate = getPage();
		if (isDoPaginate()) {
			resultObjects = resultObjects.subList(paginate, paginate + PAGE_SIZE);
		}
		for (DocSearchResult resultObj : resultObjects) {
			resultObj.setUrl(getUrlMap().get(resultObj.getDocID()));
			String[] resultMeta = getResultMeta(new File(PARENT_FOLDER + resultObj.getDocID().replace("/", "\\")));
			String title = resultMeta[0];
			String snippet = resultMeta[1];
			resultObj.setTitle(title);
			resultObj.setSnippet(snippet.replace(this.query, "<b>" + this.query + "</b>"));
			results.add(resultObj);
			setResultObjs(results);
		}
	}

	private String[] getResultMeta(File htmlFile) {
		String titleText = "Title of Result Page";
		String snippet = "";
		try {
			if (htmlFile != null) {
				// Parse the file into a JSoup Document
				Document doc = Jsoup.parse(htmlFile, "utf-8");
				// Remove listed tags from the document
				doc.select("a, script, style, .hidden, option").remove();
				titleText = doc.getElementsByTag("title").text();
				int occurance = doc.toString().indexOf(this.query);
				if(occurance != -1) {
					int start = Math.max(occurance-10, 0);
					int end = Math.min(occurance+10, doc.toString().length());
					snippet = doc.toString().substring(start,end);
				}
			}
			String[] queryWords = this.query.split(" ");
			List<String> queryVariations = new ArrayList<>(Arrays.asList(queryWords));
			for(String word: queryWords) {
				String cap = capitalize(word);
				queryVariations.add(cap);
			}
			for (String word : queryVariations) {
				if(titleText.contains(word))
					titleText = titleText.replace(word, "<b>" + word + "</b>");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { titleText, snippet };
	}

	private String capitalize(String line) {
		char cap = Character.toUpperCase(line.charAt(0));
		return cap+line.substring(1);
	}

	private List<String> tokenizeQuery(String query) {
		List<String> wordsInQuery = new ArrayList<>();
		for (String word : query.split("[\\W_]+")) {
			if (isValidWord(word)) {
				wordsInQuery.add(word.toLowerCase());
			}
		}
		return wordsInQuery;
	}

	private static boolean isValidWord(String word) {
		return !(STOP_WORDS.contains(word) || word.length() < 3 || word.length() > 45 || word.matches(".*\\d+.*"));
	}

	private List<DocSearchResult> searchForSingleWord(String word) {
		List<Token> tknList = getTokenDao().getTokensOrdered(word);
		if (tknList.isEmpty()) {
			return Collections.emptyList();
		}

		List<DocSearchResult> resultList = new ArrayList<>();
		for (Token t : tknList) {
			resultList.add(new DocSearchResult(t.getTokenPK().getDocumentID(), t.getTfIdf(), t.getWeight()));
		}
		return resultList;
	}

	private List<DocSearchResult> searchForMultipleWords(List<String> words) {
		Set<String> docIDSetForAllWords = new HashSet<>();
		Map<String, List<Token>> docIDTokenMap = new HashMap<>();

		// iterate over the words array
		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			List<Token> tknList = getTokenDao().getTokens(word);
			List<String> allDocIDsForCurrentWord = new ArrayList<>();
			for (Token t : tknList) {
				String currentDocID = t.getTokenPK().getDocumentID();
				List<Token> tkns = docIDTokenMap.getOrDefault(currentDocID, new ArrayList<>());
				tkns.add(t);
				docIDTokenMap.put(currentDocID, tkns);
				allDocIDsForCurrentWord.add(currentDocID);
			}
			if (i == 0) {
				docIDSetForAllWords.addAll(allDocIDsForCurrentWord);
			} else {
				docIDSetForAllWords.retainAll(allDocIDsForCurrentWord);
			}
		}

		if (docIDSetForAllWords.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> docIDs = new ArrayList<>(docIDTokenMap.keySet());

		for (String docID : docIDs) {
			if (!docIDSetForAllWords.contains(docID)) {
				docIDTokenMap.remove(docID);
			}
		}
		return buildReturnList(docIDTokenMap);
	}

	private List<DocSearchResult> buildReturnList(Map<String, List<Token>> docIDTokenMap) {

		Map<String, DocSearchResult> docIDSearchResultMap = new HashMap<>();
		for (String docID : docIDTokenMap.keySet()) {
			List<Token> correspondingTokens = docIDTokenMap.get(docID);
			for (Token t : correspondingTokens) {
				double currentTfIdfVal = t.getTfIdf();
				double currentWeight = (double) t.getWeight() / correspondingTokens.size();
				if (docIDSearchResultMap.containsKey(docID)) {
					DocSearchResult existingDSR = docIDSearchResultMap.get(docID);
					double resultTfIDfVal = existingDSR.getTfIdf() + currentTfIdfVal;
					double resultWeight = existingDSR.getWeight() + currentWeight;
					existingDSR.setTfIdf(resultTfIDfVal);
					existingDSR.setWeight(resultWeight);
					docIDSearchResultMap.put(docID, existingDSR);
				} else {
					docIDSearchResultMap.put(docID, new DocSearchResult(docID, currentTfIdfVal, currentWeight));
				}
			}
		}

		List<DocSearchResult> resultList = new ArrayList<>(docIDSearchResultMap.values());

		resultList.sort(Comparator.comparingDouble(DocSearchResult::getWeight).reversed()
				.thenComparing(Comparator.comparingDouble(DocSearchResult::getTfIdf).reversed()));

		return resultList;

	}

	private Map<String, String> setUrlFromFile() {
		Map<String, String> urlMap = new HashMap<>();

		File file = new File("C:\\Users\\adity\\Desktop\\IR\\bookkeeping.json");
		try {

			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {
				if (st.contains("{") || st.contains("}"))
					continue;
				String[] docURL = st.split(":");
				String docId = docURL[0].replace("\"", "").trim();
				String url = "http://" + docURL[1].replace("\"", "").replace(",", "").trim();
				urlMap.put(docId, url);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urlMap;
	}

	public void goToPrev() {
		setFromNavigate(true);
		int paginate = getPage();
		if (page > 0) {
			setPage(paginate - 1);
		}
		searchQuery();
	}

	public void goToNext() {
		setFromNavigate(true);
		int paginate = getPage();
		if (page < getTotalPages()) {
			setPage(paginate + 1);
		}
		searchQuery();
	}

	public Map<String, String> getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(Map<String, String> urlMap) {
		this.urlMap = urlMap;
	}

	public List<DocSearchResult> getResultObjs() {
		return resultObjs;
	}

	public void setResultObjs(List<DocSearchResult> resultObjs) {
		this.resultObjs = resultObjs;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public boolean isDoPaginate() {
		return doPaginate;
	}

	public void setDoPaginate(boolean doPaginate) {
		this.doPaginate = doPaginate;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}
	
	public boolean isFromNavigate() {
		return fromNavigate;
	}

	public void setFromNavigate(boolean fromNavigate) {
		this.fromNavigate = fromNavigate;
	}

	public boolean isNoResults() {
		return noResults;
	}

	public void setNoResults(boolean noResults) {
		this.noResults = noResults;
	}

	
	private TokenDAO getTokenDao() {
		return new TokenDAO();
	}

	public double getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(double timeTaken) {
		this.timeTaken = timeTaken;
	}
}
