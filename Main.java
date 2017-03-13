package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Main{
	
 public static void main(String [] args) throws Exception{
	 
creatTable();
post();
get();
 }
 
 public static void post() throws Exception{
	 try{
		 final String var1="damu2";
		 Connection con=getConnection();
		 PreparedStatement posted=con.prepareStatement("insert into student (Rollno, NAME) values(102,'"+var1+"');");
		 posted.executeUpdate();
		 }catch(Exception e){System.out.println(e);}
	      finally { 
		 System.out.println("Insert Completed.");
	 		}
 }
 
 public static ArrayList<String> get() throws Exception{
	 try{
		 Connection con=getConnection();
		 PreparedStatement statement= con.prepareStatement("Select Rollno,NAME from student where Rollno='102';");
		 ResultSet result=statement.executeQuery();
		 ArrayList<String> array= new ArrayList<String>();
		 while(result.next()){
			 System.out.print(result.getString("Rollno"));
			 System.out.print("  ");
			 System.out.println(result.getString("NAME"));
			 array.add(result.getString("NAME"));
			 
		 }
		 System.out.println("all records have been selected");
		 return array;
		 
	 }catch(Exception e){System.out.println(e);}
 return null;}
 public static void creatTable() throws Exception {
	 try{
		 Connection con = getConnection();
		 PreparedStatement create =con.prepareStatement("create table IF NOT EXISTS student(Rollno int(6), NAME varchar(25)); ");
		 create.executeUpdate();
		 	 }catch(Exception e){System.out.println(e);}
	     finally{
	    	 System.out.println("function complete.");
	     }
 } 
public static Connection getConnection() throws Exception{
try{
String driver = "com.mysql.jdbc.Driver";
String url = "jdbc:mysql://localhost:3306/us";
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
}
