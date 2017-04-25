package team11;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.sql.Connection;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Enrichment {
	private String apiKey;
	private int offset;
	private int limit;

	public Enrichment(String apiKey, int offset, int limit) {
		this.apiKey = apiKey;
		this.offset = offset;
		this.limit = limit;
	}
	
	public void searchResults(Connection conn) throws Exception {
		
		ArrayList<String[]> in = new ArrayList<String[]>();

		in = jdbctest.getSpecificStudentsQuery(conn);
		
		PrintStream out = new PrintStream(new File("enrichedQueries.txt"));
		for (int i = 0; i < in.size(); i ++) {

			String[] query = in.get(i);
			
			out.println("\n"+ (i+1) + query[1]);
			System.out.println((i+1) + ". " + query[1]);
			String results = search(query[1], offset, limit);
			
			ArrayList<String> resultName = new ArrayList<>();
			ArrayList<String> resultSnippet = new ArrayList<>();
			
			String resultString = "";

			try {
				JSONObject searchResponseJson = new JSONObject(results);
				JSONArray resultArray = searchResponseJson.getJSONObject("webPages").getJSONArray("value");
				for (int j = 0; j < resultArray.length(); j++) {
					JSONObject result = resultArray.getJSONObject(j);
					resultName.add(result.getString("name"));
					resultSnippet.add(result.getString("snippet"));                
				}
			} catch (JSONException e) {
				//Debug.printLn("Error in Json reading");
				System.out.println(e);
				out.println("xxxxxxxxxxxxxxxx");
				out.println("xxxxxxxxxxxxxxxx");
			}
			for (int j = 0; j < resultName.size(); j++){
				resultString += resultName.get(j) + "\\n" + resultSnippet.get(j) + "\\n";
				out.println(resultName.get(j));
				out.println(resultSnippet.get(j));
			}
			out.println();
			String[] a = { resultString, query[0], query[1] };
			System.out.println(resultString);
			DBUtil.post(conn, "UPDATE `cs_project_swqc`.`enrich_query` SET `information` = ? WHERE `student_id` = ? AND `search_query` = ?;", a);
		}
		out.close();
		System.out.println("\nDone enriching!\n");
	}

	private String search(String keyString, int offset, int limit) {
		HttpClient httpclient = HttpClients.createDefault();
		try {
			URIBuilder builder = new URIBuilder("https://api.cognitive.microsoft.com/bing/v5.0/search");

			builder.setParameter("q", keyString);
			builder.setParameter("count", String.valueOf(limit));
			builder.setParameter("offset", String.valueOf(offset));
			builder.setParameter("mkt", "en-us");

			URI uri = builder.build();
			HttpGet request = new HttpGet(uri);
			request.setHeader("Ocp-Apim-Subscription-Key", this.apiKey);

			System.out.println(request);
			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();
			System.out.println(response);

			if (entity != null) {
				return (EntityUtils.toString(entity));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "{}";
	}
	
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			Enrichment bingSearch = new Enrichment("5ad4ae8a7635441fb2a6817f76904cf6", 0, 10);
			bingSearch.searchResults(conn);
		} catch(Exception e) {
			System.out.println(e);
		} finally {
			DBUtil.closeConnection(conn);
		}
		
	}	
}