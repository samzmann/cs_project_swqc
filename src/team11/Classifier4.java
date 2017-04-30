package team11;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.sql.Connection;
import java.text.SimpleDateFormat;

/**
 * Forth Edition of Classifier created by team 11.
 * Change original queries from txt file sampling data to dataset from database.
 * 
 * @author Shijian(Tim) Xu, Samuel Mann, 
 * @version 1.0
 */


public class Classifier4 {
	public static void main(String[] args){
		classify();
//		accuracyTest();
	}	
	public static void accuracyTest() {
		try {
			PrintStream outQueries = new PrintStream(new File("accuracyQueries.txt"));
		
		//read gpa from csv			
			String[] header = {};
			ArrayList<String> queries = new ArrayList<String>();
			ArrayList<String> text1 = new ArrayList<String>();
			ArrayList<String> text2 = new ArrayList<String>();
			CsvReader reader = new CsvReader(
					"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/AccuracyTest.csv");
			while (reader.readRecord()) {
				// save header of cvs
				int row = (int) reader.getCurrentRecord();
				if (reader.getCurrentRecord() == 0) {
					header = reader.getValues();
					System.out.println("Its 0 row "+header[0]+" "+header[1]);
				} 
				else if (row >= 3){
					queries.add(reader.getValues()[2]);
					text1.add(reader.getValues()[3]);
					text2.add(reader.getValues()[7]);
				}
			}
			reader.close();
			text1.size();
			CsvWriter writer = new CsvWriter(
					"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/testTemp.csv");
			
			for (int i = 0; i < text1.size(); i++) {
				outQueries.println(queries.get(i));
				if(
					(text1.get(i).equals("Non School related") && text2.get(i).equals("Non school")) || 
					(text1.get(i).equals("School related") && text2.get(i).equals("School")) ||
					(text1.get(i).equals("Unknown") && text2.get(i).equals("Unknown"))
				) {
					String[] tmp = {text1.get(i), text2.get(i), ""};
					writer.writeRecord(tmp);
				} 
				else {
					String[] tmp = {text1.get(i), text2.get(i), "Diff"};
					writer.writeRecord(tmp);
				}
				
			}
			writer.close();
			outQueries.close();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			
		}
	}
	public static void classify(){
		Connection conn = null;
		
		try {
			int[] domainCount = {0,0,0,0,0,0};
			ArrayList<Integer> checkSid = new ArrayList<Integer>();
			
		//read words list from txt file, including school related word, stop word, non school related word.
			Scanner inNonschoolCorpus = new Scanner(new File("corpusNonschool2.txt"));
			Scanner inStopWords = new Scanner(new File("stopwords.txt"));
			Scanner inSchoolCorpus = new Scanner(new File("corpusSchool2.txt"));
			
		//read out put file.
			PrintStream outCorpusFinal = new PrintStream(new File("cleanedQueries.txt"));
			PrintStream outClassifiedQueries = new PrintStream(new File("classified.txt"));
			PrintStream outFailed = new PrintStream(new File("failed.txt"));
			
		//initial hash table and set for words lists
			HashMap<String, Integer> schoolCorpusWords = new HashMap<String, Integer>();
			HashMap<String, Integer> nonSchoolCorpusWords = new HashMap<String, Integer>();
			Set<String> stopWords = new HashSet<String>();			

			while(inNonschoolCorpus.hasNext()){
				String word = inNonschoolCorpus.next().toLowerCase();
				if(nonSchoolCorpusWords.containsKey(word)){
					int count = nonSchoolCorpusWords.get(word);
					nonSchoolCorpusWords.put(word, count+1);
				}
				else
					nonSchoolCorpusWords.put(word, 1);
			}
			while(inStopWords.hasNext()){
				stopWords.add(inStopWords.next());
			}			
			while(inSchoolCorpus.hasNext()){
				String word = inSchoolCorpus.next().toLowerCase();
				if(schoolCorpusWords.containsKey(word)){
					int count = schoolCorpusWords.get(word);
					schoolCorpusWords.put(word, count+1);
				}
				else
					schoolCorpusWords.put(word, 1);
			}
			
		//read gpa from csv			
			String[] header = {};
			ArrayList<String> uid = new ArrayList<String>();
			ArrayList<String> gpa = new ArrayList<String>();
			CsvReader reader = new CsvReader(
					"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/studentToGPA.csv");
			while (reader.readRecord()) {
				// save header of cvs
				if (reader.getCurrentRecord() == 0) {
					header = reader.getValues();
					System.out.println("Its 0 row "+header[0]+" "+header[1]);
				} else {
					uid.add(reader.getValues()[0]);
					gpa.add(reader.getValues()[1]);
				}
			}
			reader.close();
			
		//Based on student from list to get his/her data set and enriched queries.

			{
		//initial in attribute for each student
				ArrayList<String> inSid = new ArrayList<String>();
				ArrayList<String> inQueries = new ArrayList<String>();
				ArrayList<String> inEnrichedQuery = new ArrayList<String>();
				ArrayList<String> inDomain = new ArrayList<String>();
				ArrayList<String> inTime = new ArrayList<String>();
				ArrayList<String> inSearchCount = new ArrayList<String>();
				ArrayList<String> inGPA = new ArrayList<String>();
				
				Set<String> enrichedWords = new HashSet<String>();
				ArrayList<String> bagOfWords = new ArrayList<String>();	
			
				int j, k;
				

				
		//Connect database, for each sid load his/her queries
				conn = DBUtil.getConnection();
				ArrayList<String[]> tableDataset = new ArrayList<String[]>();
				String[] columnDataset = {"dataset_id", "student_id", "search_query",  "domain", "time", "count"};
				//					[0]dataset table id,	[1]"s011"		[2]query  	 [3],	   [4],		[5]
				String[] sqlquery = {};
				tableDataset = DBUtil.get(conn, "SELECT * FROM cs_project_swqc.dataset WHERE student_id != '';", columnDataset, sqlquery);
				
				ArrayList<String[]> tableEnrichQuery = new ArrayList<String[]>();
				String[] columnEnrichQuery = {"enrich_query_id", "student_id", "search_query",  "information", "gpa"};
				//									[0]				[1]"s011"		[2]query  	 	[3],	   	[4]	
				tableEnrichQuery = DBUtil.get(conn, "SELECT * FROM cs_project_swqc.enrich_query;", columnEnrichQuery, sqlquery);
				
				
		//for each query of a specific student, read enriched information from enriched table.
				for (j = 0; j < tableDataset.size(); j ++)
				{
					
					if (tableEnrichQuery.get(j)[1].equals(tableDataset.get(j)[1]) && tableEnrichQuery.get(j)[2].equals(tableDataset.get(j)[2])) {
						String cleaned = tableEnrichQuery.get(j)[3].replaceAll("[^a-zA-Z ]"," ").toLowerCase();
						
						inSid.add(tableDataset.get(j)[1]);
						inQueries.add(tableDataset.get(j)[2]);
						inEnrichedQuery.add(tableEnrichQuery.get(j)[3].replace("\\n", " "));
						inDomain.add(tableDataset.get(j)[3]);
						inTime.add(tableDataset.get(j)[4]);
						inSearchCount.add(tableDataset.get(j)[5]);
						inGPA.add(tableEnrichQuery.get(j)[4]);
						
						bagOfWords.add(cleaned);
					} else {
						System.out.println(tableEnrichQuery.get(j)[2] + " " + tableDataset.get(j)[2]);
					}
				}
				checkSid.add(inQueries.size());
				System.out.println(inQueries.size());

				System.out.println(bagOfWords.size());
				System.out.println("===================================================");

		//based on enriched query to compare with words list(school related, non school related, stop, etc...)
				String query = "";
				int match_count = 0, match_count_bad = 0;
				
				for(j = 0; j < bagOfWords.size(); j++)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YY HH:mm");
					Date date =sdf.parse(inTime.get(j));
					Calendar cal=Calendar.getInstance();
					cal.setTime(date);
					
					switch(inDomain.get(j))
					{
						case "bing":
							domainCount[0]++;
							break;
						case "google":
							domainCount[1]++;
							break;
						case "mm":
							domainCount[2]++;
							break;
						case "search":
							domainCount[3]++;
							break;
						case "yahoo":
							domainCount[4]++;
							break;
						case "youtube":
							domainCount[5]++;
							break;
						default:
							break;
					}
					String res = " ";
					String res_bad = " ";
					String data = bagOfWords.get(j);
					query = inQueries.get(j).toLowerCase();
					outClassifiedQueries.print(query + " ^ ");
					for(String s:stopWords){ 
						data = data.replaceAll("\\b"+s+"\\b", "");
					}
					data = data.replaceAll("\\s+", " ");
					String[] words_str = data.split("\\s+");

					Map<String, Integer> map = new HashMap<>();
					for (String w : words_str){
						Integer n = map.get(w);
						n = (n == null) ? 1 : ++n;
						map.put(w, n);
					}

					for(String key : map.keySet()){
						if(map.get(key) >= 1){ 
							enrichedWords.add(key);
						}
					}
//					System.out.println(map.toString()+"\n"+ enrichedWords.toString() +"\n\n");
					outCorpusFinal.println(map.toString()+"\n"+ enrichedWords.toString() +"\n\n");
					
					for(String enrichedWord:enrichedWords){
						if(schoolCorpusWords.containsKey(enrichedWord)){							
							int count = schoolCorpusWords.get(enrichedWord);
							res += enrichedWord + "(" + count + ")" +" ";
							match_count += count;
						}

						if(nonSchoolCorpusWords.containsKey(enrichedWord)){
							int count = nonSchoolCorpusWords.get(enrichedWord);
							res_bad += enrichedWord + "(" + count + ")" +" ";							
							match_count_bad += count;
						}
					}
					
					System.out.println(inQueries.get(j)+" "+match_count+" "+match_count_bad);
					String related = "";
					
			// comparing the number of match count and non match count to conclude the queries is school related or not.
					if(match_count_bad == 0  && match_count == 0){
						related = "Non School related";
						outClassifiedQueries.println(res_bad + "^" + res + "^" + match_count + "^Non School");
					}
			// if match count equal non match, load the domain and searching time to judge.
					else if(match_count_bad == match_count){
						if (inDomain.get(j) == "youtube") {
							related = "Non School related";
							outClassifiedQueries.println(res_bad+ "^"+ res + "^" + match_count + "^Non School");
						} else if (cal.HOUR_OF_DAY > 8 && cal.HOUR_OF_DAY < 17) {
							related = "School related";
							outClassifiedQueries.println(res_bad + "^" + res + "^" + match_count + "^School");
						} else {
							related = "Unknown";
							outClassifiedQueries.println(res_bad + "^" + res + "^" + match_count + "^Unknown");
						}
					}
					else if(match_count_bad < match_count){
						related = "School related";
						outClassifiedQueries.println(res_bad + "^" + res + "^" + match_count + "^School");
					}
					else{
						related = "Non School related";
						outClassifiedQueries.println(res_bad + "^" + res + "^" + match_count_bad + "^Non School");
					}
					match_count = 0;
					match_count_bad = 0;
					enrichedWords.clear();
					String[] newClassified = {inSid.get(j), inQueries.get(j), inDomain.get(j), inTime.get(j), inSearchCount.get(j), related};
					DBUtil.post(conn, "INSERT INTO `cs_project_swqc`.`classified_new` (`student_id`, `search_query`, `domain`, `time`, `count`, `related`) "+
					"VALUES (?, ?, ?, ?, ?, ?);", newClassified);
	        	}
			}
			for (int i = 0; i < 6 ;i++)
				System.out.println(domainCount[i]);
			
			for (int i = 0; i < checkSid.size(); i++)
				System.out.print(checkSid.get(i) + " ");
			System.out.println();
			
			outCorpusFinal.close();
			outFailed.close();
			
			inNonschoolCorpus.close();
			inStopWords.close();
			inSchoolCorpus.close();
			System.out.println("Done classifying!");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			DBUtil.closeConnection(conn);
		}
	}
}

