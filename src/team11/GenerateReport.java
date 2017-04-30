package team11;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Random;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class GenerateReport {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		generate(args);
	}
	
	public static void generate(String[] option) {
		Connection conn = null;
		PrintStream outReport = null;
		CsvWriter writer = null;
		try {
			
//			String sql = "SELECT * FROM cs_project_swqc.classified";
//			if (option.length != 0) {
//				sql += " WHERE classified_id IN ("+option[0]+")";
//			} 
//			sql +=";";
			ArrayList<String> GPA = new ArrayList<String>();
			CsvReader reader = new CsvReader(
					"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/studentToGPA.csv");
			while (reader.readRecord()) {
				// save header of cvs
				if (reader.getCurrentRecord() == 0) {
					System.out.println("Its 0 row");
					reader.getValues();
				} else {
					GPA.add(reader.getValues()[1]);
				}
			}
			reader.close();
			
			Random rand = new Random(); 
	        ArrayList<Integer> list = new ArrayList<Integer>();
	        for (int i = 0; i <= 4000; i++)
			{
				int no = rand.nextInt(116432) + 1;
	            
	            if(!list.contains(no)){
	                list.add(no);
	            } else {
	            	i--;
	            }
			}
	        
			String sql = "SELECT * FROM cs_project_swqc.classified_new WHERE classified_id IN (";
			for (int i = 0; i < list.size(); i++)
			{
				if (i == 0)
					sql += list.get(i);
				else
					sql += "," + list.get(i);
			}
			
			sql += ");";
			
			writer = new CsvWriter(
					"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/reportCSV.csv");
			String[] header = {"", "student_id", "search_query", "domain", "time", "count", "related", "GPA"};
			writer.writeRecord(header);
			
			outReport = new PrintStream(new File("reportFile.txt"));
			conn = DBUtil.getConnection();
			ArrayList<String[]> in = new ArrayList<String[]>();
			String[] column = {"student_id", "search_query", "domain", "time", "count", "related" };
			String[] query = {};
			in = DBUtil.get(conn, sql, column, query );
			
			ArrayList<String> outStudentList = new ArrayList<String>();
			int[] outCountDomain = {0,0,0,0,0,0};
			int[] outSchoolRelated = {0,0,0};
			ArrayList<String> outMaxQuery = new ArrayList<String>();
			int outItem = in.size();
			
			int currentMaxCount = 0;
			String currentMaxQuery = "";
			
			String currentStudent = "";
			for (int i = 0; i < outItem; i++)
			{
				String[] currentRow = in.get(i);
				int intCount = new Integer(currentRow[4]);
				
				int intSid = new Integer(currentRow[0].substring(1, currentRow[0].length()));
				
				String[] tmp = { ""+(i+1), currentRow[0], currentRow[1], currentRow[2], currentRow[3], currentRow[4], currentRow[5], GPA.get(intSid-1)};
				writer.writeRecord(tmp);
				
				switch (currentRow[2]) {
				case "bing":
					outCountDomain[0]++;
					break;
				case "google":
					outCountDomain[1]++;
					break;
				case "mm":
					outCountDomain[2]++;
					break;
				case "search":
					outCountDomain[3]++;
					break;
				case "yahoo":
					outCountDomain[4]++;
					break;
				case "youtube":
					outCountDomain[5]++;
					break;
				}
				
				switch (currentRow[5]) {
				case "School related":
					outSchoolRelated[0]++;
					break;
				case "Non School related":
					outSchoolRelated[1]++;
					break;
				case "Unknown":
					outSchoolRelated[2]++;
					break;
				}
				
				if (!currentRow[0].equals(currentStudent)){
					outStudentList.add(currentStudent);
					outMaxQuery.add(currentMaxQuery);
					currentStudent = currentRow[0];
					currentMaxCount = 0;
					currentMaxQuery = "";
				} else {
					if (intCount > currentMaxCount) {
						currentMaxCount = intCount;
						currentMaxQuery = currentRow[1]; 
					}
				}
				
				
			}
		
			outStudentList.remove(0);
			outMaxQuery.remove(0);
			
			outReport.println("This is the report of student search query in list:");
			outReport.println(outStudentList.toString());
			outReport.println();
			outReport.println("Include: " + outItem + " queries.");
			outReport.println();
			outReport.println("School related: " + outSchoolRelated[0] + " queries.");
			outReport.println("Not school related: " + outSchoolRelated[1] + " queries.");
			outReport.println("Unknow: " + outSchoolRelated[2] + " queries.");
			outReport.println();
			outReport.println("Student search them on:");
			outReport.println("Bing: " + outCountDomain[0]);
			outReport.println("Google: " + outCountDomain[1]);
			outReport.println("MM: " + outCountDomain[2]);
			outReport.println("search: " + outCountDomain[3]);
			outReport.println("Yahoo: " + outCountDomain[4]);
			outReport.println("Youtube: " + outCountDomain[5]);
			outReport.println();
			outReport.println("=================Most times search query for each student=================");
			for (int i = 0; i < outStudentList.size(); i++)
			{
				outReport.println(outStudentList.get(i) + " " + outMaxQuery.get(i));
			}
			
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			DBUtil.closeConnection(conn);
			writer.close();
			outReport.close();
		}
	}

}
