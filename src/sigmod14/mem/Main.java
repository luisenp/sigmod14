package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

// TODO The singleton design seems ugly. Consider changing later
public class Main {
	public static void main(String[] args) {
		String charset = args[3];                
		DataLoader loader = DataLoader.INSTANCE;
		loader.setCharset(charset);				
		loader.setDataDirectory(args[0]);
		try {
			long time = System.currentTimeMillis();
			loader.loadData();
			System.err.print(System.currentTimeMillis() - time);
			System.err.println(" ( time reading data )");
		} catch (IOException e) {
			System.err.println("ERROR: I/O problem");
			e.printStackTrace();
			System.exit(-1);
		} catch (ParseException e) {
			System.err.println("ERROR: Problem Parsing date");
			e.printStackTrace();
			System.exit(-1);
		}
		
		long time = System.currentTimeMillis();

		int numThreads = Integer.parseInt(args[2]);
		LinkedList<String> queries = new LinkedList<String> ();
		try {
			Scanner scanner = new Scanner(new File(args[1]), charset);
			while (scanner.hasNextLine()) {
				String query = scanner.nextLine();
				queries.add(query);
			}
			scanner.close();
			System.err.println("Total number of queries: " + queries.size());
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File not found");
			System.exit(-1);
		}		
		
		QueryHandler handler = new QueryHandler(Database.INSTANCE, queries);
		handler.solveQueries(numThreads);
		HashMap<String, String> answers = handler.getAnswers();
		for (String query : queries) {
			System.out.println(answers.get(query));
		}
		System.out.flush();
		System.err.println("Queries: " + (System.currentTimeMillis() - time));
	}
}
