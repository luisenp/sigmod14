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
		String charset = args[3]; // "ISO-8859-1";// "UTF-8"; //                
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
//		Database.INSTANCE.printDatabaseInfo();
		
		long time = System.currentTimeMillis();

		QueryHandler.initDistancesCache(Database.INSTANCE);
		int nThreads = Integer.parseInt(args[2]);
		QueryHandler handlers[] = new QueryHandler[nThreads];
		for (int i = 0; i < nThreads; i++) {		
			handlers[i] = new QueryHandler(Database.INSTANCE);
		}
		LinkedList<String> queries = new LinkedList<String> ();
		try {
			int cnt = 0;
			Scanner scanner = new Scanner(new File(args[1]), charset);
			while (scanner.hasNextLine()) {
				String query = scanner.nextLine();
				queries.add(query);
				handlers[cnt % nThreads].addQuery(query);
				cnt++;
			}
			scanner.close();
			System.err.println("Total number of queries: " + cnt);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File not found");
			System.exit(-1);
		}

		Thread threads[] = new Thread[nThreads];
		for (int i = 0; i < nThreads; i++) {	
			threads[i] = new Thread(handlers[i]);
			threads[i].start();
		}

		HashMap<String, String> answers = new HashMap<String, String> (); 
		try {
			for (int i = 0; i < nThreads; i++) {
				threads[i].join();
				answers.putAll(handlers[i].getAnswers());
//				handlers[i].printV();  	//TODO testing
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (String query : queries) {
			System.out.println(answers.get(query));
		}
		System.out.flush();
		System.err.println("Queries: " + (System.currentTimeMillis() - time));
	}
}
