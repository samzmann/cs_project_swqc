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
		
			
			//Scanner in = new Scanner(new File("randomSamplingOfQueries.txt"));
		    PrintStream out = new PrintStream(new File("enrichedQueries.txt"));
		    ArrayList<String> queries = new ArrayList<String>();
		    int count=1;
		   try{
			   Connection con = getConnection();
			   PreparedStatement statement  = con.prepareStatement("Select queries from query;");
			   ResultSet result1  = statement.executeQuery();
			    while(result1.next())
			    	{
			    	 queries.add(result1.getString("queries"));
			    	}
			  System.out.println(queries);
			   
			//   if(in.hasNextLine()){
			  if(queries.size()>0){
		      // while(in.hasNextLine())
			    	while(queries.size()>0)
		    	   {
			//String query = in.nextLine();
			String query = queries.get(count-1);
			out.println("\n"+count);
			System.out.println(count + ". " + query);
			
			String results = search(query, offset, limit);
			ArrayList<String> resultName = new ArrayList<>();
			ArrayList<String> resultSnippet = new ArrayList<>();      

			try {
				JSONObject searchResponseJson = new JSONObject(results);
				JSONArray resultArray = searchResponseJson.getJSONObject("webPages").getJSONArray("value");
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject result = resultArray.getJSONObject(i);
					resultName.add(result.getString("name"));
					resultSnippet.add(result.getString("snippet"));                
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
				
			    String query1 ="UPDATE query SET enriched=concat(ifnull(enriched,' '),' ',?,' ',?) where queries=?";
				PreparedStatement posted=con.prepareStatement(query1);
				posted.setString(1,resultName.get(i)+"\n");
				posted.setString(2,resultSnippet.get(i)+"\n");
				posted.setString(3,query);
				posted.executeUpdate();
			}
			out.println();
			count++;
		}
		//in.close();
		System.out.println("\nDone enriching!\n");
		}
		else 
			System.out.println("\nEmpty File.\n");
		}
		catch(Exception e){System.out.println(e);}
	}
	
	public static Connection getConnection() throws Exception{
		try{
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/student";// create a new database with name "us"
		String username = "root";
		String password = "Mba@2016";
		Class.forName(driver); 
		Connection conn = DriverManager.getConnection(url+"?useSSL=false",username,password);
		System.out.println("Connected");
		return conn;
		} catch(Exception e){System.out.println(e);
		} 
		return null;
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
