import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Classifier3 {
	public static void main(String[] args) throws FileNotFoundException {
		//Bing bingSearch = new Bing("ad687097e5b743e1beff7f9987072310", 0, 10);
		//bingSearch.searchResults();
		classify();
	}	
	public static void classify(){
		Scanner inEnrichedQuery,inRandomQueries,inNonschoolCorpus;		
		try {
			inEnrichedQuery = new Scanner(new File("enrichedQueries.txt"));
			inRandomQueries = new Scanner(new File("randomSamplingOfQueries.txt"));
			inNonschoolCorpus = new Scanner(new File("corpusNonschool.txt"));
			Scanner inStopWords = new Scanner(new File("stopwords.txt"));
			Scanner inSchoolCorpus = new Scanner(new File("corpusSchool.txt"));

			PrintStream outCorpusFinal = new PrintStream(new File("cleanedQueries.txt"));
			PrintStream outClassifiedQueries = new PrintStream(new File("classified.txt"));

			HashMap<String, Integer> schoolCorpusWords = new HashMap<String, Integer>();
			HashMap<String, Integer> nonSchoolCorpusWords = new HashMap<String, Integer>();
			Set<String> stopWords = new HashSet<String>();			
			Set<String> enrichedWords = new HashSet<String>();
			ArrayList<String> bagOfWords = new ArrayList<String>();		
			String query = "";

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
			
			StringBuffer temp = new StringBuffer();
			int i=2;
			while(inEnrichedQuery.hasNextLine()){				
				String  nxt = inEnrichedQuery.nextLine();
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
			System.out.println(cleaned);
			bagOfWords.add(cleaned);
			System.out.println(bagOfWords.size());
			int match_count = 0, match_count_bad = 0;
			int j = 0;
			for(j=0;j<bagOfWords.size(); j++){
				String res = " ";
				String res_bad = " ";
				if(inRandomQueries.hasNextLine()){				
					String data = bagOfWords.get(j);
					query = inRandomQueries.nextLine().toLowerCase();
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
						if(map.get(key) >= 1)
						{ enrichedWords.add(key); }
					}

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

					if(match_count_bad == 0  && match_count == 0){
						outClassifiedQueries.println(res_bad+ "^"+ res + "^"+match_count+"^Non School");
					}
					else if(match_count_bad == match_count){
						outClassifiedQueries.println(res_bad+ "^"+ res + "^"+match_count+"^Unknown");
					}
					else if(match_count_bad < match_count){
						outClassifiedQueries.println(res_bad+ "^"+ res + "^"+match_count+"^School");
					}
					else{
						outClassifiedQueries.println(res_bad + "^"+ res+ "^"+ match_count_bad+"^Non School");
					}
					match_count = 0;
					match_count_bad = 0;
					enrichedWords.clear();
				}
			}			
			outCorpusFinal.close();
			inEnrichedQuery.close();
			inStopWords.close();
			inSchoolCorpus.close();
			System.out.println("Done classifying!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

