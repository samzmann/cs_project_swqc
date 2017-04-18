package team11;
 
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
 
/**
 * JDBC数据库操作工具类  简化数据库操作
 * @author wxisme
 *
 */
public class jdbcutilnoused {
    //资源文件
    static Properties pros = null;
    //静态初始化  当加载JDBCUtil类时调用
    static {  
        pros = new Properties();
        try {
            //加载资源文件
        	
        	ClassLoader loader = Thread.currentThread().getContextClassLoader();   
//        	System.out.println(loader.getResource("db.properties"));
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
     * 获取数据库连接
     * @return Connection
     */
    public static Connection getMySQLConnection() {
        Connection conn = null;
        try {
            //加载数据库驱动
            Class.forName(pros.getProperty("mysqlDriver"));
            //获取数据库连接
            conn = DriverManager.getConnection(pros.getProperty("mysqlURL"),
                    pros.getProperty("mysqlUser"),pros.getProperty("mysqlPwd"));
             
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
     
    /**
     * 关闭io资源
     * @param io
     */
    public static void closeFile(Closeable ... io) {
        for(Closeable temp : io) {
            if(temp != null) {
                try {
                    temp.close();
                } catch (IOException e) {
                    System.out.println("文件关闭失败");
                    e.printStackTrace();
                }
            }
        }
    }
     
    public static void close(ResultSet rs,Statement ps,Connection conn) {
        try {
            if(rs!=null){
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(ps!=null){
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(conn!=null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     
    public static void close(Statement ps,Connection conn){
        try {
            if(ps!=null){
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(conn!=null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     
    public static void close(Connection conn){
        try {
            if(conn!=null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     
 
}