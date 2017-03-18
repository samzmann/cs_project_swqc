package me.dataclean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import me.xdrop.fuzzywuzzy.FuzzySearch;

/**
 * @author Tim
 *
 */
public final class Levenshtein {

/**
 * Function: componentWillReceiveProps
 * Description: Calculating the distance between two string, return the steps required from string a to string b,
 *              Basically, it follows Levenshtein distance algorithm.
 * Calls:
 * Called by: handleStringArrayList()
 * Editor: Tim
 * Date: 03/12/2017
 * Input:
 *      String a
 *      String b
 */
    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(
                    1 + Math.min(costs[j], costs[j - 1]),
                    a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1 // comparing a[i-1] and b[i-1], if they are same character, get the nw, or nw++
                ); 
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

/**
 * Function: sortStringArray
 * Description: pretreat the String array list (arrayToSort), sort some muiltword to a single string, 
 *              for example: the distance of (aaaa bbbb cccc) and (bbbb aaaa cccc) should be 0, they are same,
 *              but if comparing directly, the distance will return 4, so I sorted them as same aaaabbbbcccc to compare.
 * Calls:
 * Called by: handleStringArrayList()
 * Editor: Tim
 * Date: 03/12/2017
 * Input:
 *      int index
 *      String[] arrayToSort
 */
    public static String sortStringArray(int index, String[] arrayToSort) {

        String callbackStr = "";

        System.out.println();
        System.out.println(index + ".String before sort:");

        for (int i = 0; i < arrayToSort.length; i++) {
            System.out.print(arrayToSort[i] + ",");
        }
        System.out.println();
        System.out.println(index + ".Sorted:");
        // 璋冪敤鏁扮粍鐨勯潤鎬佹帓搴忔柟娉晄ort,涓斾笉鍖哄垎澶у皬鍐�
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < arrayToSort.length; i++) {
            callbackStr += arrayToSort[i];
        }
        System.out.println(callbackStr);
        return callbackStr;
    }


/**
 * Function: handleStringArrayList
 * Description: the main entrence of the remove query as you type function.
 * Calls:
 * Called by:
 * Editor: Tim
 * Date: 03/12/2017
 * Input:
 *      ArrayList<String> arrayString
 */
    public static ArrayList<String> handleStringArrayList(ArrayList<String> arrayString) throws FileNotFoundException {
    	
    	PrintStream out = new PrintStream(new File("LevenshteinChecking.txt"));
    	long startTime=System.currentTimeMillis();   //Starting time

    	int size = arrayString.size();
        ArrayList<String> preparedString = new ArrayList<String>() ;
        ArrayList<String> originalString = arrayString;

        for (int i = 0; i < arrayString.size(); i ++) {
            // String split with space and get the arrayString
            String[] tempStringDividedWithSpace = arrayString.get(i).split(" ");
            // Sorted this String array order by case insensitive whatever lower and upper case letter
            String sortedString = sortStringArray(i, tempStringDividedWithSpace);
            preparedString.add(sortedString);
        }

        for (int i = 0; i < preparedString.size(); i++) {
            for (int j = i + 1; j < preparedString.size(); j++) {
                String a = preparedString.get(i);
                String b = preparedString.get(j);
                String oria = originalString.get(i);
                String orib = originalString.get(j);

                int distance = distance(a, b);
                double ratio = ((double) distance) / (Math.max(a.length(), b.length()));
                int rate = 100 - new Double(ratio*100).intValue();
                int fuzzRate = FuzzySearch.tokenSortPartialRatio(oria, orib);
                System.out.println("A string: " + a + ", B string: "+ b);
                System.out.println("A string: " + oria + ", B string: "+ orib);
                out.println("A string: " + oria + ", B string: "+ orib);
                System.out.println("my rate is " + rate + ", and Fuzzy rate is " + fuzzRate);
                out.println("my rate is " + rate + ", and Fuzzy rate is " + fuzzRate);
                
                if (rate > 85 || fuzzRate > 85) {
                	if (a.length() > b.length()) {
                		preparedString.remove(j);
                		originalString.remove(j);
                		j --;
                	}
                	else if (a.length() < b.length()) {
                		preparedString.remove(i);
                		originalString.remove(i);
                		i --;
                		j = i;
                	}
                }
            }
        }
        
    	long endTime=System.currentTimeMillis(); 
    	System.out.println("runnning time: "+(endTime-startTime)+"ms"); 
    	out.println("running time: "+(endTime-startTime)+"ns for " + size + " elements" );
        out.close();
        return originalString;
    }
    
    /**
     * Function: handleStringArrayList2
     * Description: modified handleStringArrayList function, added reading and writing csv part, in order to handling dataset
     * Calls:
     * Called by:
     * Editor: Tim
     * Date: 03/12/2017
     * Input:
     *      ArrayList<String> arrayString
     */
        public static ArrayList<String> handleStringArrayList2(ArrayList<String> user, ArrayList<String> data, ArrayList<String> time, ArrayList<String> domain) throws FileNotFoundException {
        	
        	PrintStream out = new PrintStream(new File("csvParsingResults.txt"));
        	long startTime=System.currentTimeMillis();   //Starting time

        	int size = data.size();
            ArrayList<String> originalData = data;

            int i = 0;
            
            while(i < originalData.size() - 1) {
            	System.out.println(originalData.size());
                String flagData = originalData.get(i);
                String nextData = originalData.get(i+1);
                
                String[] preparedNow = flagData.split(" ");
                String[] preparedNext = nextData.split(" ");
                // Sorted this String array order by case insensitive whatever lower and upper case letter
                String sortedNow = sortStringArray(i, preparedNow);
                String sortedNext = sortStringArray(i, preparedNext);

                int distance = distance(sortedNow, sortedNext);
                double ratio = ((double) distance) / (Math.max(sortedNow.length(), sortedNext.length()));
                int rate = 100 - new Double(ratio*100).intValue();
                int fuzzRate = FuzzySearch.tokenSortPartialRatio(flagData, nextData);
                System.out.println("A string: " + sortedNow + ", B string: "+ sortedNext);
                System.out.println("A string: " + flagData + ", B string: "+ nextData);
                out.println("A string: " + flagData + ", B string: "+ nextData);
                System.out.println("my rate is " + rate + ", and Fuzzy rate is " + fuzzRate);
                out.println("my rate is " + rate + ", and Fuzzy rate is " + fuzzRate);
                
                	if ((rate > 85 || fuzzRate > 85) && (rate != 100)) {
                    	if (flagData.length() >= nextData.length()) {
                    		user.remove(i + 1);
                    		originalData.remove(i + 1);
                    		time.remove(i + 1);
                    		domain.remove(i + 1);
                    	}
                    	else if (flagData.length() < nextData.length()) {
                    		user.remove(i);
                    		originalData.remove(i);
                    		time.remove(i);
                    		domain.remove(i);
                    	}
                    }
                    else i++;
                }
                	
            
        	long endTime=System.currentTimeMillis(); 
        	System.out.println("running time: "+(endTime-startTime)+"ms"); 
        	out.println("running time: "+(endTime-startTime)+"ns for " + size + " elements" );
            out.close();
            return originalData;
        }
    
        
    public static void main(String[] args) throws IOException {
    	
    	String[] header = {};
		
    	ArrayList<String> user = new ArrayList<String>();
    	ArrayList<String> data = new ArrayList<String>();
    	ArrayList<String> domain = new ArrayList<String>();
    	ArrayList<String> timestamp = new ArrayList<String>();
		CsvReader reader = new CsvReader("C:/Users/worfx/Desktop/data/finaldatasetpart1(cleanedSam).csv");
		while (reader.readRecord()) {
	        if (reader.getCurrentRecord()==0){                
	        	System.out.println("Its 0 row");
	        	header = reader.getValues();
	        }
	        else {
	        	user.add(reader.getValues()[0]);
	        	data.add(reader.getValues()[1]);
	        	domain.add(reader.getValues()[2]);
	        	timestamp.add(reader.getValues()[3]);
	        }
        }
		reader.close();
		
		data = Levenshtein.handleStringArrayList2(user, data, timestamp, domain);
		
		CsvWriter writer = new CsvWriter("C:/Users/worfx/Desktop/data/finaldatasetpart1(tim).csv");
		writer.writeRecord(header);
		for(int i = 0; i < data.size(); i ++) {
			String[] tmp = {user.get(i), data.get(i), domain.get(i), timestamp.get(i)};
			writer.writeRecord(tmp);
			
        }
		writer.close();
	}	
}
