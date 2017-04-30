import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
public class db{
	
public static Connection getConnection() throws Exception{
	try{
	String driver = "com.mysql.jdbc.Driver";
	
	String url = "jdbc:mysql://localhost:3306/student";// connecting to a database "student"
	
	String username="root";
	String password="Mba@2016";
	Class.forName(driver);	 
	Connection conn = DriverManager.getConnection(url+"?useSSL=false",username,password);
	//System.out.println("Connected");
	return conn;
	} 
	catch(Exception e){System.out.println();
	}
		return null;
	}

public static ArrayList<String> get( String query,String Column) throws Exception
{    Connection con=null;
	 try{
		 
		 //Connection
		 con=getConnection();
		 PreparedStatement statement= con.prepareStatement(query);
		 ResultSet result=statement.executeQuery();
		 ArrayList<String> array= new ArrayList<String>();
		 while(result.next())
	    	{
	    	 array.add(result.getString(Column));
	    	}
		  return array;
		 
	 }
	 catch(Exception e){System.out.println();
	 }
	 finally
	 {
		 try{
			 con.close();
		        
		 }catch(Exception e){System.out.println();}
	 }
return null;
}
public static void post(String statement,int a1,String a2,String a3) throws Exception{
	 Connection con=null;
	try{
		 con=getConnection();
		 PreparedStatement posted=con.prepareStatement(statement);
			posted.setInt(1,a1);
			posted.setString(2,a2);	
			posted.setString(3,a3);
			posted.executeUpdate();
		 }catch(Exception e){System.out.println(e);}
	      finally { try{
	    	  con.close();
	      }catch(Exception e)
	      {
	    	  System.out.println();
	      }
		 		}
}
public static void update(String statement,String a1,String a2) throws Exception{
	 Connection con=null;
	try{
		 con=getConnection();
		 PreparedStatement posted=con.prepareStatement(statement);
			posted.setString(1,a1);
			posted.setString(2,a2);	
			//posted.setString(3,a3);
			posted.executeUpdate();
		 }catch(Exception e){System.out.println(e);}
	      finally { try{
	    	  con.close();
	      }catch(Exception e)
	      {
	    	  System.out.println();
	      }
		 		}
}

}
