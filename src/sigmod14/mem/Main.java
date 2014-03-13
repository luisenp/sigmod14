package sigmod14.mem;

import java.io.FileNotFoundException;
import java.text.ParseException;

public class Main {
	
	public static void main(String[] args) {
		Database DB = Database.INSTANCE;
		DB.setDataDirectory("data/outputDir-1k");
		try {
			long time = System.currentTimeMillis();
			DB.readData();
			System.out.print(System.currentTimeMillis() - time);
			System.out.println(" ( time reading data )");
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File not found");
			e.printStackTrace();
			System.exit(-1);
		} catch (ParseException e) {
			System.err.println("ERROR: Parsing date");
			e.printStackTrace();
			System.exit(-1);
		}

		// query1 test
		System.out.println(DB.query1(576, 400, -1) + " 3");
		System.out.println(DB.query1(58, 402, 0) + " 3");
		System.out.println(DB.query1(266, 106, -1) + " 3");
		System.out.println(DB.query1(313, 523, -1) + " -1");
		System.out.println(DB.query1(858, 587, 1) + " 4");
		System.out.println(DB.query1(155, 355, -1) + " 3");
		System.out.println(DB.query1(947, 771, -1) + " 2");
		System.out.println(DB.query1(105, 608, 3) + " -1");
		System.out.println(DB.query1(128, 751, -1) + " 3");
		System.out.println(DB.query1(814, 641, 0) + " 3");

		// query2 test
		try {
			System.out.println(DB.query2(3, "1980-02-01"));
			System.out.println(DB.query2(4, "1981-03-10"));
			System.out.println(DB.query2(3, "1982-03-29"));
			System.out.println(DB.query2(3, "1983-05-09"));
			System.out.println(DB.query2(5, "1984-07-02"));
			System.out.println(DB.query2(3, "1985-05-31"));
			System.out.println(DB.query2(3, "1986-06-14"));
			System.out.println(DB.query2(7, "1987-06-24"));
			System.out.println(DB.query2(3, "1988-11-10"));
			System.out.println(DB.query2(4, "1990-01-25"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
