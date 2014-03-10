package sigmod14;

import java.io.FileNotFoundException;

public class Main {

	public static void main(String[] args) {
		String data_path = "data/outputDir-1k";
		String query_path = "queries";
		Query q = new Query1();
		try {
			q.setup(data_path, query_path);
			q.run(data_path, query_path);
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: " + e.getMessage());
		}
		q.teardown();
	}

}
