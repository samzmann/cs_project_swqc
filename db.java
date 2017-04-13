import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
public class db{
	
public static Connection getConnection() throws Exception{// creates a connection between database and eclipse
	try{
	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/student";// connecting to a database "student", first create your own database and replace student with your database name
	String username = "";//enter ur username
	String password = "";//enter ur password
	Class.forName(driver); 
	Connection conn = DriverManager.getConnection(url+"?useSSL=false",username,password);
	//System.out.println("Connected");
	return conn;
	} 
	catch(Exception e){System.out.println(e);
	} 
	return null;
	}

public static ArrayList<String> get( String query, String Column) throws Exception// receives data from table
{
	 try{
		 
		 Connection con=getConnection();
		 PreparedStatement statement= con.prepareStatement(query);
		 ResultSet result=statement.executeQuery();
		 ArrayList<String> array= new ArrayList<String>();
		 while(result.next())
	    	{
	    	 array.add(result.getString(Column));
	    	}
		  return array;
		 
	 }
	 catch(Exception e){System.out.println(e);
	 }
return null;
}
public static void post(String statement,String a1,String a2) throws Exception{// update table contents
	 try{
		 Connection con=getConnection();
		 PreparedStatement posted=con.prepareStatement(statement);
			posted.setString(1,a1);
			posted.setString(2,a2);			
			posted.executeUpdate();
		 }catch(Exception e){System.out.println(e);}
	      finally { 
		 		}
}
}
