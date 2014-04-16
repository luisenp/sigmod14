package sigmod14.mem;

import java.util.HashMap;
import java.util.LinkedList;

public class QueryHandler {
	private Database db;
	private LinkedList<String> queries;
	private HashMap<String,String> answers;
	
	public static enum QueryType {
		TYPE1,
		TYPE2,
		TYPE3,
		TYPE4
	}
	
	public QueryHandler(Database db, LinkedList<String> queries) {
		this.db = db;
		this.queries = queries;
		answers = new HashMap<String,String> ();
	}

	public QueryHandler(Database db) {
		this(db, new LinkedList<String> ());
	}
		
	public void solveQueries(int numThreads) {		
		Query123Solver solvers123[] = new Query123Solver[numThreads];
		Query4Solver solver4 = new Query4Solver(db, numThreads);
		
		for (int i = 0; i < numThreads; i++) {
			solvers123[i] = new Query123Solver(db);
		}
		int cnt = 0;
		for (String query : queries) {
			QueryType type = getQueryType(query);
			String params[] = 
					query.substring(7, query.length() - 1).split(", ");
			if (type.equals(QueryType.TYPE4)) {
				int k = Integer.parseInt(params[0]);
				answers.put(query, solver4.query4(k, params[1]));				
			} else {
				solvers123[cnt % numThreads].addQuery(query);
				cnt++;
			}
		}
		
		Thread threads[] = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread(solvers123[i]);
			threads[i].start();
		}

		for (int i = 0; i < numThreads; i++) {
			try {
				threads[i].join();
				answers.putAll(solvers123[i].getAnswers());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public HashMap<String,String> getAnswers() {
		return answers;
	}
	
	public static QueryType getQueryType(String query) {
		String type = query.substring(0, 6);
		if (type.equals("query1")) {
			return QueryType.TYPE1;
		} else if (type.equals("query2")) {
			return QueryType.TYPE2;
		} else if (type.equals("query3")) {
			return QueryType.TYPE3;
		} else if (type.equals("query4")) {
			return QueryType.TYPE4;
		}			
		return null;
	}
	
	public void addQuery(String query) {
		queries.add(query);
	}
}
