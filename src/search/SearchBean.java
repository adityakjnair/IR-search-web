package search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import index.TokenDAO;

/**
*
* @author aditya
*/
@ManagedBean(name = "searchBean")
@SessionScoped
public class SearchBean implements Serializable{

	private static final long serialVersionUID = 1L;

	private String query;
	private List<String> results;

	public SearchBean() {
		query = "";
		results = new ArrayList<>();
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public List<String> getResults() {
		return results;
	}

	public void setResults(List<String> results) {
		this.results = results;
	}

	
	public void doSearch() {
		List<String> searchRes = getTokenDao().getDocIDByToken(this.query);
		for(String result : searchRes) {
			this.results.add(result);
		}
	}
	
	private TokenDAO getTokenDao() {
		return new TokenDAO();
	}
	
}
