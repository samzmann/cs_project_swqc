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

import com.csvreader.CsvReader;

/**
 * Searching enrichment information of each queries which load from dataset table in database.
 * A modified Class of Bing.java
 * 
 * @author Shijian(Tim) Xu, Vikas Matcha
 * @version 1.0
 */


public class Enrichment {
	private String apiKey;
	private int offset;
	private int limit;

	public Enrichment(String apiKey, int offset, int limit) {
		this.apiKey = apiKey;
		this.offset = offset;
		this.limit = limit;
	}
	
	
/**
 * The original function of searching enriched message by using Bing api.
 * 
 * @param conn
 * 			   Connection object of current connection to database.
 * 
 * @throws Exception
 * 			   The error of processing database.
 * 
 */
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
	
	/**
	 * A edited way of merging enriched information from database.zz
	 * 
	 */
	public static void loadEnrichment() {
		Connection conn = null;
		PrintStream outFailed = null;
		try {
			outFailed = new PrintStream(new File("failed.txt"));
			
			String[] header = {};
			ArrayList<String> uid = new ArrayList<String>();
			ArrayList<String> gpa = new ArrayList<String>();
			CsvReader reader = new CsvReader(
					"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/studentToGPA.csv");
			while (reader.readRecord()) {
				// save header of cvs
				if (reader.getCurrentRecord() == 0) {
					header = reader.getValues();
					System.out.println("Its 0 row "+header[0]+" "+header[1]);
				} else {
					uid.add(reader.getValues()[0]);
					gpa.add(reader.getValues()[1]);
				}
			}
			reader.close();
			
			for (int i = 0; i < uid.size(); i ++)
			{
				String studentid = uid.get(i);
				String strGpa = gpa.get(i);
				System.out.println(studentid);
				
				ArrayList<String> inQueries = new ArrayList<String>();
				ArrayList<String> inEnrichedQuery = new ArrayList<String>();
				
				conn = DBUtil.getConnection();
				
				ArrayList<String[]> tableDataset = new ArrayList<String[]>();
				String[] columnDataset = {"dataset_id", "student_id", "search_query",  "domain", "time", "count"};
				//					[0]dataset table id,	[1]"s011"		[2]query  	 [3],	   [4],		[5]
				String[] sqlquery = {studentid};
				tableDataset = DBUtil.get(conn, "SELECT * FROM cs_project_swqc.dataset WHERE dataset.student_id = ? ;", columnDataset, sqlquery);
				
				int j;
				for (j = 0; j < tableDataset.size(); j ++)
				{
					ArrayList<String[]> tableEnrich = new ArrayList<String[]>();
					
					String[] query = {tableDataset.get(j)[2]};
					String[] columnEnriched = {"enriched", "status"};
					//							[0],		[1]				
					tableEnrich = DBUtil.get(conn, "SELECT * FROM cs_project_swqc.enriched WHERE enriched.`query` = ? ;", columnEnriched, query);
					
					String enrichedQuery = "";
					String cleaned = "";
					if (tableEnrich.size() != 0 && tableEnrich.get(0)[0] != null) {
						enrichedQuery = tableEnrich.get(0)[0].replace("\\n", " ");
						cleaned = enrichedQuery.replaceAll("[^a-zA-Z ]"," ").toLowerCase();
					} else {
						outFailed.println(tableDataset.get(j)[0]+" "+tableDataset.get(j)[1]+" "+tableDataset.get(j)[2]);
					}
					
					inQueries.add(query[0]);
					inEnrichedQuery.add(enrichedQuery);
				}
				
				for (j = 0; j < inQueries.size(); j++)
				{
					String[] newClassified = {studentid, inQueries.get(j), inEnrichedQuery.get(j), strGpa};
					DBUtil.post(conn, "INSERT INTO `cs_project_swqc`.`enrich_query15` (`student_id`, `search_query`, `information`, `gpa`) VALUES (?,?,?,?);", newClassified);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			DBUtil.closeConnection(conn);
			outFailed.close();
		}
		
	}
//	
//	public static void temp() {
//		Connection conn = null;
//		
//		try {
//			
//			conn = DBUtil.getConnection();
//			for (int i = 2; i <= 15; i++)
//			{
//				ArrayList<String[]> tableDataset = new ArrayList<String[]>();
//				String[] columnDataset = {"student_id", "search_query",  "information", "gpa"};
//				String[] sqlquery = {};
//				tableDataset = DBUtil.get(conn, "SELECT * FROM cs_project_swqc.enrich_query"+i+";", columnDataset, sqlquery);
//	
//				for (int j = 0; j < tableDataset.size(); j++)
//				{
//					String[] newClassified = {tableDataset.get(j)[0], tableDataset.get(j)[1], tableDataset.get(j)[2], tableDataset.get(j)[3]};
//					DBUtil.post(conn, "INSERT INTO `cs_project_swqc`.`enrich_query` (`student_id`, `search_query`, `information`, `gpa`) VALUES (?,?,?,?);", newClassified);
//				}
//			}
//			
//		} catch (Exception e) {
//			System.out.println(e);
//		} finally {
//			DBUtil.closeConnection(conn);
//		}
//		
//	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			Enrichment bingSearch = new Enrichment("5ad4ae8a7635441fb2a6817f76904cf6", 0, 10);
			bingSearch.searchResults(conn);
			loadEnrichment();
		} catch(Exception e) {
			System.out.println(e);
		} finally {
			DBUtil.closeConnection(conn);
		}
		
	}	
}