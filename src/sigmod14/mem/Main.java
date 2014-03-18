package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Scanner;

public class Main {
	// TODO The singleton design seems ugly. Consider change later
	public static void main(String[] args) {
		DataLoader loader = DataLoader.INSTANCE;
		QueryHandler qHandler = QueryHandler.INSTANCE;		
		loader.setDataDirectory("data/outputDir-1k");
		
		try {
			long time = System.currentTimeMillis();
			loader.loadData();
			System.out.print(System.currentTimeMillis() - time);
			System.out.println(" ( time reading data )");
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File not found");
			System.exit(-1);
		} catch (ParseException e) {
			System.err.println("ERROR: Problem Parsing date");
			System.exit(-1);
		}

		long time = System.currentTimeMillis();
		
		try {
			Scanner scanner = 
				new Scanner(new File("data/queries/1k-queries.txt"), 
						    "ISO-8859-1");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String queryType = line.substring(0, 6);
				String params[] = 
					line.substring(7, line.length() - 1).split(", ");
				if (queryType.equals("query1")) {
					long p1 = Long.parseLong(params[0]); 
					long p2 = Long.parseLong(params[1]);
					int x = Integer.parseInt(params[2]);
					System.out.println(qHandler.query1(p1, p2, x));
				} else if (queryType.equals("query2")) {
					int k = Integer.parseInt(params[0]);
					try {
						System.out.println(qHandler.query2(k, params[1]));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if (queryType.equals("query3")) {
					int k = Integer.parseInt(params[0]);
					int hops = Integer.parseInt(params[1]);
					System.out.println(qHandler.query3(k, hops, params[2]));
				} else if (queryType.equals("query4")) {
					int k = Integer.parseInt(params[0]);
					System.out.println(qHandler.query4(k, params[1]));
				}   
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File not found");
			System.exit(-1);
		}
		System.out.println("Queries: " + (System.currentTimeMillis() - time));
	}
}
