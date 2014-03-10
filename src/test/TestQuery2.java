package test;

import java.io.FileNotFoundException;

import sigmod14.Query2;

public class TestQuery2 {
	public static void main(String args[]) {
		Query2 q = new Query2();
		long time = System.currentTimeMillis();
		try {
			q.setup(null, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis() - time);
		
		time = System.currentTimeMillis();
		q.run(null, "query2(3, 1980-02-01)");
		System.out.println(System.currentTimeMillis() - time);
	}
}
