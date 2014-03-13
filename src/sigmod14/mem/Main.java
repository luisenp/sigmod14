package sigmod14.mem;

import java.io.FileNotFoundException;

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
		}

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
	}

}
