import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
//import java.util.Scanner;

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

public class Bing {
	private String apiKey;
	private int offset;
	private int limit;

	public Bing(String apiKey, int offset, int limit) {
		this.apiKey = apiKey;
		this.offset = offset;
		this.limit = limit;
	}

	 
	
	public void searchResults() throws FileNotFoundException {
		
			
		    PrintStream out = new PrintStream(new File("enrichedQueries.txt"));
		    ArrayList<String> queries = new ArrayList<String>();
		    int count=1;
		   try{
			  		
			   String statement1="Select Column1 from query where status='0';";// selects all queries which are not enriched( status is 0)
			   queries=get(statement1,"Column1");
			 
			  System.out.println(queries);
			   
			
			  if(queries.size()>0){
		      
			    	while(queries.size()>=count)
		    	   {
			
			String query = queries.get(count-1);
			out.println("\n"+count);
			System.out.println(count + ". " + query);
			
			String results = search(query, offset, limit);
			ArrayList<String> resultName = new ArrayList<>();
			ArrayList<String> resultSnippet = new ArrayList<>();      
			int count1=1;
			try {
				JSONObject searchResponseJson = new JSONObject(results);
				JSONArray resultArray = searchResponseJson.getJSONObject("webPages").getJSONArray("value");
				
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject result = resultArray.getJSONObject(i);
					resultName.add(result.getString("name"));
					resultSnippet.add(result.getString("snippet"));
					count1++;// increments number of results are add added for enrichment
				}
			} catch (JSONException e) {
				//Debug.println("Error in Json reading");
				out.println("xxxxxxxxxxxxxxxx");
				out.println("xxxxxxxxxxxxxxxx");
							
			}
			
			for (int i = 0; i < resultName.size(); i++)
			{
				out.println(resultName.get(i));
			    out.println(resultSnippet.get(i));
			
			    String statement ="UPDATE query SET enriched=concat(ifnull(enriched,' '),' ',?) where Column1=?";
				post(statement,resultName.get(i)+" "+resultSnippet.get(i)+" ",query);//updates enriched column
			}
			
			out.println();
			count++;
			if( count1>1)// when there is loss of internet connection or some problem count1=1,to make query enrich again we use this condition to change the status from 0 to 1
				{						
				 String statement="update query set status=? where Column1=?";
				 post(statement,"1",query); // if the query is enriched completely ,status is 1
				}
			else
			{  
				String statement="update query set enriched=null ,status=? where Column1=?";
				post(statement,"0",query);// if query is not enriched status is 0//                    
				
			}
		}
			    	
		
	
		System.out.println("\nDone enriching!\n");
		}
		else 
			System.out.println("\nEmpty File.\n");
		}
		catch(Exception e){
			System.out.println(e);
			}
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


			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				return (EntityUtils.toString(entity));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "{}";
	}

}
