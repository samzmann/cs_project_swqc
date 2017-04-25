package team11;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Database connection util class.
 * 
 * @author Vikas Matcha, Shijian(Tim) Xu
 * @version 1.0
 */

public class DBUtil {
	
	static Properties pros = null;
    //static initial class of loading prop from db.properties
	
/**
  * Constructor of DBUtil
  * <p>
  * Load database config from configuration file db.properties(src/db.properties).
  */
    static {  
        pros = new Properties();
        try {
            //trying loading dbconfig from db.properties
        	ClassLoader loader = Thread.currentThread().getContextClassLoader();   
        	//System.out.println(loader.getResource("db.properties"));
        	InputStream in = loader.getResourceAsStream("db.properties");
            if(in == null) {
                throw new FileNotFoundException("Configure file unavailable");
            }
            pros.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
/**
 * 
 * 
 */ 
    public static Connection getConnection() {
        Connection conn = null;
        try {
            //loading database driver
            Class.forName(pros.getProperty("mysqlDriver"));
            //connect database
            conn = DriverManager.getConnection(pros.getProperty("awsmysqlURL"),
                    pros.getProperty("awsmysqlUser"),pros.getProperty("awsmysqlPwd"));
             
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

	public static ArrayList<String[]> get(Connection conn, String statement, String[] column, String[] query) throws Exception// receives data from table
	{	
		try {
			PreparedStatement ps = conn.prepareStatement(statement);
			
			for (int i = 1; i <= query.length; i++)
				ps.setString(i, query[i-1]);
			System.out.println(ps);
			ResultSet rs = ps.executeQuery();
			ArrayList<String[]> array = new ArrayList<String[]>();
			while (rs.next()) {
				String[] row = new String[column.length];
				for (int i = 0; i < column.length; i++)
					row[i] = rs.getString(column[i]);
				array.add(row);
			}
			return array;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public static void post(Connection conn, String statement, String[] a) throws Exception {// update table contents
		try {
			PreparedStatement ps = conn.prepareStatement(statement);
			for (int i = 1; i <= a.length; i++)
				ps.setString(i, a[i-1]);
			ps.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
		}
	}

	
	public static void closeConnection(Connection conn) {// update table contents
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
