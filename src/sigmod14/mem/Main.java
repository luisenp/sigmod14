package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import sigmod14.mem.QueryHandler.QueryType;

// TODO The singleton design seems ugly. Consider changing later
// TODO Consider dedicated nodes and edges depending on the type
public class Main {
	public static void main(String[] args) {
		String charset = "UTF-8"; // "ISO-8859-1"; //       
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
		
		ArrayList<LinkedList<String>> queries = 
				new ArrayList<LinkedList<String>> ();
		for (int i = 0; i < 4; i++) {
			queries.add(0, new LinkedList<String>());
		}
		try {
			Scanner scanner = new Scanner(new File(args[1]), charset);
			while (scanner.hasNextLine()) {
				String query = scanner.nextLine();
				QueryType type = QueryHandler.getQueryType(query);
				if (type == QueryType.TYPE1)
					queries.get(0).add(query);
				if (type == QueryType.TYPE2)
					queries.get(1).add(query);
				if (type == QueryType.TYPE3)
					queries.get(2).add(query);
				if (type == QueryType.TYPE4)
					queries.get(3).add(query);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File not found");
			System.exit(-1);
		}

		Thread threads[] = new Thread[4];
		QueryHandler handlers[] = new QueryHandler[4];
		for (int i = 0; i < 4; i++) {		
			handlers[i] = new QueryHandler(Database.INSTANCE, queries.get(i));
			threads[i] = new Thread(handlers[i]);
			threads[i].start();
		}
		
		try {
			for (int i = 0; i < 4; i++) {
				threads[i].join();
				handlers[i].printAnswers();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.err.println("Queries: " + (System.currentTimeMillis() - time));
	}
}
