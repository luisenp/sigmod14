package sigmod14.mem;

import java.io.FileNotFoundException;
import java.text.ParseException;

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
			e.printStackTrace();
			System.exit(-1);
		} catch (ParseException e) {
			System.err.println("ERROR: Parsing date");
			e.printStackTrace();
			System.exit(-1);
		}

		// query1 test
//		System.out.println(qHandler.query1(576, 400, -1) + " 3");
//		System.out.println(qHandler.query1(58, 402, 0) + " 3");
//		System.out.println(qHandler.query1(266, 106, -1) + " 3");
//		System.out.println(qHandler.query1(313, 523, -1) + " -1");
//		System.out.println(qHandler.query1(858, 587, 1) + " 4");
//		System.out.println(qHandler.query1(155, 355, -1) + " 3");
//		System.out.println(qHandler.query1(947, 771, -1) + " 2");
//		System.out.println(qHandler.query1(105, 608, 3) + " -1");
//		System.out.println(qHandler.query1(128, 751, -1) + " 3");
//		System.out.println(qHandler.query1(814, 641, 0) + " 3");

		// query2 test
//		try {
//			System.out.println(qHandler.query2(3, "1980-02-01"));
//			System.out.println(qHandler.query2(4, "1981-03-10"));
//			System.out.println(qHandler.query2(3, "1982-03-29"));
//			System.out.println(qHandler.query2(3, "1983-05-09"));
//			System.out.println(qHandler.query2(5, "1984-07-02"));
//			System.out.println(qHandler.query2(3, "1985-05-31"));
//			System.out.println(qHandler.query2(3, "1986-06-14"));
//			System.out.println(qHandler.query2(7, "1987-06-24"));
//			System.out.println(qHandler.query2(3, "1988-11-10"));
//			System.out.println(qHandler.query2(4, "1990-01-25"));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		
		//query3 test
//		qHandler.query3(3, 2, "Asia");
//		qHandler.query3(4, 3, "Indonesia");
//		qHandler.query3(3, 2, "Egypt");
//		qHandler.query3(3, 2, "Italy");
//		qHandler.query3(5, 4, "Chengdu");
//		qHandler.query3(3, 2, "Peru");
//		qHandler.query3(3, 2, "Democratic_Republic_of_the_Congo");
//		qHandler.query3(7, 6, "Ankara");
//		qHandler.query3(3, 2, "Luoyang");
//		qHandler.query3(4, 3, "Taiwan");
		// 10k dataset
//		qHandler.query3(3, 2, "Asia");
//		qHandler.query3(4, 3, "Dolgoprudny");
//		qHandler.query3(3, 2, "Yongkang_District");
		
		
		// query4 test
		// 1k dataset
		qHandler.query4(3, "Bill_Clinton");
		qHandler.query4(4, "Napoleon");
		qHandler.query4(3, "Chiang_Kai-shek");
		qHandler.query4(3, "Charles_Darwin");
		qHandler.query4(5, "Ronald_Reagan");
		qHandler.query4(3, "Aristotle");
		qHandler.query4(3, "George_W._Bush");
		qHandler.query4(7, "Tony_Blair");
		qHandler.query4(3, "William_Shakespeare");
		qHandler.query4(4, "Augustine_of_Hippo");
		// 10k dataset
//		qHandler.query4(4, "Franklin_D._Roosevelt");
	}

}
