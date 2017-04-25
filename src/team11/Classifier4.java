package team11;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import java.sql.Connection;

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
	}	
	public static void classify(){
		Connection conn = null;
		
		try {
			int[] domainCount = {0,0,0,0,0,0};
			ArrayList<Integer> checkSid = new ArrayList<Integer>();
			
		//Read randomSid.txt file to load picked student id
			ArrayList<Integer> sid = new ArrayList<Integer>();
			Scanner inRandomSid = new Scanner(new File("randomSid.txt"));
			while(inRandomSid.hasNext()) {
				sid.add(new Integer(inRandomSid.nextLine()));
			}
			Collections.sort(sid);
			inRandomSid.close();
			
		//read words list from txt file, including school related word, stop word, non school related word.
			Scanner inNonschoolCorpus = new Scanner(new File("corpusNonschool.txt"));
			Scanner inStopWords = new Scanner(new File("stopwords.txt"));
			Scanner inSchoolCorpus = new Scanner(new File("corpusSchool.txt"));
			
		//read out put file.
			PrintStream outCorpusFinal = new PrintStream(new File("cleanedQueries.txt"));
			PrintStream outClassifiedQueries = new PrintStream(new File("classified.txt"));
			
		
		
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
			
		//Based on student from list to get his/her data set and enriched queries.
			for (int i = 0; i < sid.size(); i ++)
			{
		//initial in attribute for each student
				ArrayList<String> inQueries = new ArrayList<String>();
				ArrayList<String> inEnrichedQuery = new ArrayList<String>();
				ArrayList<String> inDomain = new ArrayList<String>();
				ArrayList<String> inTime = new ArrayList<String>();
				ArrayList<String> inSearchCount = new ArrayList<String>();
				
				Set<String> enrichedWords = new HashSet<String>();
				ArrayList<String> bagOfWords = new ArrayList<String>();	
			
				int j;
				String studentid = sid.get(i).toString();
				switch (studentid.length()){
    			case 1:
    				studentid = "s00"+studentid;
    				break;
    			case 2:
    				studentid = "s0"+studentid;
    				break;
    			case 3:
    				studentid = "s"+studentid;
    			default:
    				break;
				}
				System.out.println(studentid);
				
		//Connect database, load data from table dataset and enrich_query
				conn = DBUtil.getConnection();
				ArrayList<String[]> tableEnrich = new ArrayList<String[]>();
				ArrayList<String[]> tableDataset = new ArrayList<String[]>();
				String[] columnEnrich = {"enrich_query_id", "student_id", "search_query", "information"};
				//					[0]enriched table id,	[1]"s011"		[2]query  	[3]enriched
				String[] columnDataset = {"dataset_id", "student_id", "search_query",  "domain", "time", "count"};
				//					[0]dataset table id,	[1]"s011"		[2]query  	 [3],	   [4],		[5]
				String[] sqlquery = {studentid};
				tableEnrich = DBUtil.get(conn, "SELECT * FROM cs_project_swqc.enrich_query WHERE enrich_query.student_id = ? ;", columnEnrich, sqlquery);
				tableDataset = DBUtil.get(conn, "SELECT * FROM cs_project_swqc.dataset WHERE dataset.student_id = ? ;", columnDataset, sqlquery);

				
				for (j = 0; j < tableEnrich.size(); j ++) 
				{
					if ((tableEnrich.get(j)[1].equals(tableDataset.get(j)[1])) && (tableEnrich.get(j)[2].equals(tableDataset.get(j)[2]))) {
						inQueries.add(tableEnrich.get(j)[2]);
						inEnrichedQuery.add(tableEnrich.get(j)[3].replace("\\n", " "));
						inDomain.add(tableDataset.get(j)[3]);
						inTime.add(tableDataset.get(j)[4]);
						inSearchCount.add(tableDataset.get(j)[5]);
					} else {
						System.out.println(tableEnrich.get(j)[1] +" "+ tableDataset.get(j)[1]);
						System.out.println(tableEnrich.get(j)[2] + " " + tableDataset.get(j)[2]);
					}
				}
				checkSid.add(inQueries.size());
				System.out.println(inQueries.size());
				
		//start processing for each enriched query 
				String query = "";
				for (j = 0; j < inEnrichedQuery.size(); j++)
				{
					String cleaned = inEnrichedQuery.get(j).replaceAll("[^a-zA-Z ]"," ").toLowerCase();
					bagOfWords.add(cleaned);
				}
				System.out.println(bagOfWords);
				System.out.println(bagOfWords.size());
				System.out.println("===================================================");

		//based on enriched query to compare with words list(school related, non school related, stop, etc...)
				int match_count = 0, match_count_bad = 0;
				
				for(j = 0; j < bagOfWords.size(); j++)
				{
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
					if(match_count_bad == 0  && match_count == 0){
						related = "Non School related";
						outClassifiedQueries.println(res_bad + "^" + res + "^" + match_count + "^Non School");
					}
					else if(match_count_bad == match_count){
						if (inDomain.get(j) == "youtube") {
							related = "Non School related";
							outClassifiedQueries.println(res_bad+ "^"+ res + "^" + match_count + "^Non School");
						}
						else {
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
					String[] newClassified = {studentid, inQueries.get(j), inDomain.get(j), inTime.get(j), inSearchCount.get(j), related};
					DBUtil.post(conn, "INSERT INTO `cs_project_swqc`.`classified` (`student_id`, `search_query`, `domain`, `time`, `count`, `related`) "+
					"VALUES (?, ?, ?, ?, ?, ?);", newClassified);
	        	}
			}
			for (int i = 0; i < 6 ;i++)
				System.out.println(domainCount[i]);
			
			for (int i = 0; i < checkSid.size(); i++)
				System.out.print(checkSid.get(i) + " ");
			System.out.println();
			
			outCorpusFinal.close();
			
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

