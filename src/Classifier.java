import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Classifier {
	public static void main(String[] args) throws FileNotFoundException {
		Bing bingSearch = new Bing("5ad4ae8a7635441fb2a6817f76904cf6", 0, 10);
		bingSearch.searchResults();
		classify();
	}	
	public static void classify(){
		Scanner enrichedQuery,queryFile,nonSchoolWords;		
		try {
			enrichedQuery = new Scanner(new File("enrichedQueries.txt"));
			queryFile = new Scanner(new File("randomSamplingOfQueries.txt"));
			nonSchoolWords = new Scanner(new File("corpusNonschool.txt"));
			PrintStream outCorpusFinal = new PrintStream(new File("cleanedQueries.txt"));
			PrintStream classifiedQueries = new PrintStream(new File("classified.txt"));
			Scanner inStopWords = new Scanner(new File("stopwords.txt"));
			Scanner corpusFinal = new Scanner(new File("corpusSchool.txt"));
			int i = 2;
			String query = "";
			Set<String> stopWords = new HashSet<String>();
			Set<String> corpusWords = new HashSet<String>();
			Set<String> words = new HashSet<String>();
			Set<String> badwords = new HashSet<String>();			
			StringBuffer temp = new StringBuffer();
			ArrayList<String> bagOfWords = new ArrayList<String>();			

			while(nonSchoolWords.hasNext()){
				badwords.add(nonSchoolWords.next());
			}
			while(inStopWords.hasNext()){
				stopWords.add(inStopWords.next());
			}			
			while(corpusFinal.hasNext()){
				corpusWords.add(corpusFinal.next());
			}			
			while(enrichedQuery.hasNextLine()){				
				String  nxt = enrichedQuery.nextLine();
				if(nxt.equals(Integer.toString(i))){
					String cleaned = temp.toString().replaceAll("[^a-zA-Z ]", " ").toLowerCase();
					bagOfWords.add(cleaned);
					temp.delete(0, temp.length());
					i++;
				}
				else{
					temp.append(nxt+ " ");
				}				 	
			}
			String cleaned = temp.toString().replaceAll("[^a-zA-Z ]"," ").toLowerCase();
			bagOfWords.add(cleaned);
			
			System.out.println(bagOfWords);
			int match_count = 0, match_count_bad = 0;
			int j = 0;
			for(j=0;j<bagOfWords.size(); j++){
				String res = " ";
				String res_bad = " ";
				if(bagOfWords.size() != 0){
					String data = bagOfWords.get(j);
					query = queryFile.nextLine().toLowerCase();
					classifiedQueries.print(query + " ^ ");
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
						if(map.get(key) >= 1)
						{ words.add(key); }
					}
					outCorpusFinal.println(map.toString()+"\n"+ words.toString() +"\n\n");
					for(String word:words){
						if(badwords.contains(word)){
							res_bad += word+" ";
							match_count_bad++;
							query = "bad";
						}
					}
					for(String word:words){
						if(corpusWords.contains(word)){
							res += word+" ";
							match_count++;
						}
					}
					if(match_count_bad == 0  && match_count == 0){
						classifiedQueries.println(res_bad+ "^"+ res + "^"+match_count+"^Non School");
					}
					else if(match_count_bad < match_count){
						classifiedQueries.println(res_bad+ "^"+ res + "^"+match_count+"^School");
					}
					else{
						classifiedQueries.println(res_bad + "^"+ res+ "^"+ match_count_bad+"^Non School");
					}
					match_count = 0;
					match_count_bad = 0;
					words.clear();
				}
			}			
			outCorpusFinal.close();
			enrichedQuery.close();
			inStopWords.close();
			corpusFinal.close();
			System.out.println("Done classifying!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}

