package team11;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.csvreader.CsvReader;
 
 
public class jdbctest {
 
    public static void main(String[] args) throws IOException, Exception {
    	Connection conn = DBUtil.getConnection();
		String[] header = {};

		ArrayList<String> uid = new ArrayList<String>();
		ArrayList<String> data = new ArrayList<String>();
		ArrayList<String> domain = new ArrayList<String>();
		ArrayList<String> timestamp = new ArrayList<String>();
		ArrayList<String> count = new ArrayList<String>();
    	CsvReader reader = new CsvReader(
				"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/finaldatasetpart2(Tim)_onlyone.csv");
    	while (reader.readRecord()) {
			// save header of cvs
			if (reader.getCurrentRecord() == 0) {
				System.out.println("Its 0 row");
				header = reader.getValues();
			} else {
				uid.add(reader.getValues()[0]);
				data.add(reader.getValues()[1]);
				domain.add(reader.getValues()[2]);
				timestamp.add(reader.getValues()[3]);
				count.add(reader.getValues()[4]);
				try {
					DBUtil.post(conn, "INSERT INTO `cs_project_swqc`.`dataset` (`studentID`, `searchQuery`, `domain`, `time`, `count`) VALUES (?, ?, ?, ?, ?);", reader.getValues());
				} catch (SQLException e) {
		            e.printStackTrace();
		        }
				
			}
		}
    	DBUtil.closeConnection(conn);
		reader.close();
        
        
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        String sql = "select * from dataset where id = ?";
//        
//        try {
//            ps = conn.prepareStatement(sql);
//            ps.setInt(1, 1);
//            rs = ps.executeQuery();
//             
//            while(rs.next()) {
//                System.out.println(rs.getString("name"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
 
}