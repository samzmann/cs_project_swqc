package team11.tim.removequery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import me.xdrop.fuzzywuzzy.FuzzySearch;

/**
 * Class Levenshtein is the abstract base class for data cleaning in 
 * <i>Preprocessing and Enrichment of Student Web Search Query Catalogs</i>
 * Research Project.
 * <p>
 * Program based on Levenshtein Algorithm, calculating the distance of queries(String)
 * in order to delete duplicate string and keep the longest one.
 * 
 * @author Shijian(Tim) Xu
 * @version 1.0
 */
public final class Levenshtein {

	/**
	 * Comparing query a and query b, return Distance of 2 queries.
	 * <p>
	 * The core function of calculating the distance between pair of queries,
	 * based on Levenshtein algorithm.
	 * 
	 * @param a
	 *            Based query.
	 * @param b
	 *            Comparing query.
	 * @return the numeral steps from query a to query b.
	 * @see String
	 * @see Math
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
				// comparing a[i-1] and= b[i-1], if they are same character, get the nw, or nw++
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1 );
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];
	}

	/**
	 * Dictionary sorting a single query which included multiple words.
	 * <p>
	 * Considering the query is combine with multiple words, and the result will be affected by 
	 * the order of words. It also the pre-load queries function from original Dataset csv.
	 *
	 * @param index
	 *            The ordinal number of queries loaded from original data.
	 * @param arrayToSort
	 *            The single query which Split by ' '.
	 * @return A single String query sorted by dictionary order.
	 * @see Arrays
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
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		for (int i = 0; i < arrayToSort.length; i++) {
			callbackStr += arrayToSort[i];
		}
		System.out.println(callbackStr);
		return callbackStr;
	}

	/**
	 * Main processing function.
	 * 
	 *
	 * @param uid
	 *            Student id column.
	 * @param data
	 *            Search query column.
	 * @param time
	 *            Search time column.
	 * @param domain
	 *            The domain of query.
	 * @return An arraylist of cleaned query list.
	 * @throws FileNotFoundException  If an open or output 
	 *                      		  exception occurred
	 * @see #sortStringArray(int, String[])
	 * @see #distance(String, String)
	 * @see me.xdrop.fuzzywuzzy
	 */
	public static ArrayList<String> handleStringArrayList(ArrayList<String> uid, ArrayList<String> data,
			ArrayList<String> time, ArrayList<String> domain) throws FileNotFoundException {

		PrintStream out = new PrintStream(new File("csvParsingResults.txt"));
		long startTime = System.currentTimeMillis(); // Starting time

		int size = data.size();
		ArrayList<String> originalData = data;

		int i = 0;
		while (i < originalData.size() - 1) {
			for (int j = 1; j <= 5; j++) {
				if (i + j >= originalData.size())
					continue;
				System.out.println(originalData.size());
				String flagData = originalData.get(i);
				String nextData = originalData.get(i + j);

				String[] preparedNow = flagData.split(" ");
				String[] preparedNext = nextData.split(" ");
				// Sorted this String array order by case insensitive whatever
				// lower and upper case letter
				String sortedNow = sortStringArray(i, preparedNow);
				String sortedNext = sortStringArray(i, preparedNext);

				int distance = distance(sortedNow, sortedNext);
				double ratio = ((double) distance) / (Math.max(sortedNow.length(), sortedNext.length()));
				int rate = 100 - new Double(ratio * 100).intValue();
				int fuzzRate = FuzzySearch.tokenSortPartialRatio(flagData, nextData);
				System.out.println("A string: " + sortedNow + ", B string: " + sortedNext);
				System.out.println("A string: " + flagData + ", B string: " + nextData);
				out.println("A string: " + flagData + ", B string: " + nextData);
				System.out.println("my rate is " + rate + ", and Fuzzy rate is " + fuzzRate);
				out.println("my rate is " + rate + ", and Fuzzy rate is " + fuzzRate);

				if (rate != 100) {
					if (rate > 85 || fuzzRate > 85) {
						if (flagData.length() >= nextData.length()) {
							uid.remove(i + j);
							originalData.remove(i + j);
							time.remove(i + j);
							domain.remove(i + j);
							j--;
						} else if (flagData.length() < nextData.length()) {
							uid.remove(i);
							originalData.remove(i);
							time.remove(i);
							domain.remove(i);
							j = 0;
						}
					} else if (flagData.contains(nextData)) {
						uid.remove(i + j);
						originalData.remove(i + j);
						time.remove(i + j);
						domain.remove(i + j);
						j--;
					} else if (nextData.contains(flagData)) {
						uid.remove(i);
						originalData.remove(i);
						time.remove(i);
						domain.remove(i);
						j = 0;
					}
				}
			}
			i++;
		}

		long endTime = System.currentTimeMillis(); // get the ending time
		System.out.println("Running time： " + (endTime - startTime) + "ms");
		out.println("running time: " + (endTime - startTime) + "ns for " + size + " elements");
		out.close();
		return originalData;
	}
	
	/**
     * Entering runnable function.
     *
     * @param args
	 *            
     * @throws IOException  If an open or output 
	 *                     	exception occurred
     * @see		  Levenshtein#handleStringArrayList(ArrayList, ArrayList, ArrayList, ArrayList)
     * @see       com.csvreader.CsvReader
     * @see		  com.csvreader.CsvWriter
     */
	public static void main(String[] args) throws IOException {

		String[] header = {};

		ArrayList<String> uid = new ArrayList<String>();
		ArrayList<String> data = new ArrayList<String>();
		ArrayList<String> domain = new ArrayList<String>();
		ArrayList<String> timestamp = new ArrayList<String>();
		CsvReader reader = new CsvReader(
				"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/finaldatasetpart1(cleanedSam).csv");
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
			}
		}
		reader.close();

		data = Levenshtein.handleStringArrayList(uid, data, timestamp, domain);

		CsvWriter writer = new CsvWriter(
				"/Users/Tim/Desktop/research paper/CapstoneCourseSpring2017/data/finaldatasetpart1(Tim).csv");
		writer.writeRecord(header);
		for (int i = 0; i < data.size(); i++) {
			String[] tmp = { uid.get(i), data.get(i), domain.get(i), timestamp.get(i) };
			writer.writeRecord(tmp);
		}
		writer.close();
	}
}
