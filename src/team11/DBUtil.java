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

import com.csvreader.CsvReader;


/**
 * Database connection utility class, in order to easy connect database and close connection.
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
 * Connect database by configuration file 'db.properties' setting.
 * 
 * @return Connection
 * 
 */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            //loading database driver
            Class.forName(pros.getProperty("mysqlDriver"));
            //connect database
            conn = DriverManager.getConnection(pros.getProperty("mysqlURL"),
                    pros.getProperty("mysqlUser"),pros.getProperty("mysqlPwd"));
             
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

/**
 * Reading data from database.
 * 
 * @param conn
 *            Connection object of current connection to database.
 * @param statement
 * 			  Basic SQL statement, reminds ? as input part.
 * @param column
 * 			  Requirement column of data table.
 * @param query
 * 			  Value which fill into ? in SQL statement.
 * 
 * @return ArrayList
 * 
 * @throws Exception
 * 			  Throw connection error.
 */
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
	
/**
 * Post statement to database.
 * 
 * @param conn
 *            Connection object of current connection to database.
 * @param statement
 * 			  Basic SQL statement, reminds ? as input part.
 * @param a
 * 			  Value which fill into ? in SQL statement.
 * 
 * @throws Exception
 * 			  Throw connection error.
 */
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

/**
 * Closing connection.
 * 
 * @param conn
 *            Connection object of current connection to database.
 * 
 */
	public static void closeConnection(Connection conn) {// update table contents
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
