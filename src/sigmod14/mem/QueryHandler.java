package sigmod14.mem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import sigmod14.mem.graph.Edge;
import sigmod14.mem.graph.KnowsEdge;
import sigmod14.mem.graph.Node;
import sigmod14.mem.graph.NotFoundException;
import sigmod14.mem.graph.Person;
import sigmod14.mem.graph.Tag;

public class QueryHandler implements Runnable {
	private Database db;
	private LinkedList<String> queries;
	private HashMap<String,String> answers;
	private final SimpleDateFormat sdf =
			new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");

	private static short distances[][];
	
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
	
	private class TagComparator implements Comparator<Integer> {
		private long date;
		private HashMap<Integer, Integer> ranges;
		
		TagComparator(long date) {
			this.date = date;
			ranges = new HashMap<Integer, Integer>();
		}
		

		private int getRangeTag(int tagID) {
			if (ranges.containsKey(tagID)) return ranges.get(tagID);
			HashSet<Integer> vertices = new HashSet<Integer>();
			Tag tag = db.getTag(tagID);
			
			// getting all the persons in the induced graph
			for (Person person : tag.getInterested()) {				
				long birthday = 0;
				try {
					birthday = person.getBirthday();
				} catch (NotFoundException e) {
					System.err.println("ERROR: Birthday should be defined.");
					e.printStackTrace();
					System.exit(-1);
				}
				if (birthday < date) continue;
				vertices.add(person.getId());
			}

			// finding size of largest component using multiple DFS
			int score = 0;
			HashSet<Integer> visited = new HashSet<Integer>();
			for (Integer id : vertices) {
				if (visited.contains(id)) continue;
				int sizeComp = 0;
				LinkedList<Integer> stack = new LinkedList<Integer> ();
				stack.add(id);
				while (!stack.isEmpty()) {
					int personID = stack.removeFirst();
					if (visited.contains(personID)) continue;
					visited.add(personID);
					sizeComp++;
					Person person = db.getPerson(personID);
					for (Edge ae : person.getKnows()) {
						KnowsEdge edge = (KnowsEdge) ae;
						int idAdjPerson = edge.getOtherNode(person).getId();
						if (!vertices.contains(idAdjPerson)) continue;
						stack.addFirst(idAdjPerson);
					}
				}
				if (sizeComp > score) score = sizeComp;
			}
			ranges.put(tagID, score);
			return score;
		}
		
		public int compare(Integer tag1ID, Integer tag2ID) {		
			Integer score1 = getRangeTag(tag1ID);
			Integer score2 = getRangeTag(tag2ID);
			int comp = score1.compareTo(score2);
			if (comp == 0) {
				try {
					String name1 = db.getTagName(tag1ID);
					String name2 = db.getTagName(tag2ID);
					return name2.compareTo(name1);
				} catch (NotFoundException e) {
					System.err.println("ERROR: All tags should have a name");
					e.printStackTrace();
					System.exit(-1);
				}
			}
			return comp;
		}
	}

	private class PersonPair {
		Person person1;
		Person person2;
		
		PersonPair(Person person1, Person person2) {
			this.person1 = person1;
			this.person2 = person2;
		}
		
		public String toString() {
			String ret = person1.getId() + "|" + person2.getId();
			return ret;
		}
	}
	
	private class PersonPairComparator implements Comparator<PersonPair> {
		// finds the number of common tags between the two persons in the pair
		private int getSimilarity(PersonPair pp) {
			int similarity = 0;
			Person person1 = pp.person1;
			Person person2 = pp.person2;
			for (Tag tag : person1.getInterests()) {
				if (tag.getInterested().contains(person2)) {
						similarity++;
				}
			}
			return similarity;
		}
		public int compare(PersonPair pp1, PersonPair pp2) {
			int score1 = getSimilarity(pp1);
			int score2 = getSimilarity(pp2);
			if (score1 == score2) {
				long id11 = pp1.person1.getId();
				long id12 = pp1.person2.getId();
				long id21 = pp2.person1.getId();
				long id22 = pp2.person2.getId();
				if (id11 < id21) return 1;
				if (id11 > id21) return -1;
				if (id12 < id22) return 1;
				if (id12 > id22) return -1;
				return 0;
			}
			return score1 < score2 ? -1 : 1;
		}
	}
	
	private class Centrality {
		long num;
		long den;
		
		Centrality(long num, long den) {
			if (den == 0) {	// centrality = 0 if den = 0
				num = 0;
				den = 1;
			}
			this.num = num;
			this.den = den;
		}
		
		int compare(Centrality r) {
			long a = num*r.den;
			long b = r.num*den;
			if (a < b) return -1;
			if (a > b) return 1;
			return 0;
		}
		
		public String toString() {
			double d = (double) num/den;
			return String.valueOf(d);
		}
	}
	
	private class PersonCentrality {
		private Person person;
		private Centrality centrality;
		
		PersonCentrality(Person person, Centrality centrality) {
			this.person = person;
			this.centrality = centrality;
		}
		
		public String toString() {
			return person.toString() + " " + centrality.toString();
		}
	}
	
	private class 
	PersonCentralityComparator implements Comparator<PersonCentrality> {		
		public int compare(PersonCentrality pc1, PersonCentrality pc2) {
			int cmp = pc1.centrality.compare(pc2.centrality); 
			if (cmp == 0) {
				if (pc1.person.getId() > pc2.person.getId()) return -1;
				if (pc1.person.getId() < pc2.person.getId()) return 1;
				return 0;
			}
			return cmp;
		}
		
	}

	private static class 
	PersonDegreeComparator implements Comparator<Person> {		
		public int compare(Person p1, Person p2) {
			return -1*Integer.compare(p1.getKnows().size(), p2.getKnows().size());
		}
		
	}
		
	// does a BFS on the induced graph, starting from p1 and counting the
	// steps to reach p2
	public String query1(int p1, int p2, int x) {
		Person goal = db.getPerson(p2);
		if (goal == null) return "-1";
		LinkedList<Person> queue = new LinkedList<Person> ();
		LinkedList<Short> dist = new LinkedList<Short> ();
		HashSet<Integer> visited = new HashSet<Integer> ();
		queue.addFirst(db.getPerson(p1));
		dist.addFirst((short) 0);
		short bestDistance = 10000;   
		while (!queue.isEmpty()) {
			Person person = queue.removeFirst();
			int pid = person.getId();
			short d = dist.removeFirst();
			if (visited.contains(pid)) 
				continue;
			visited.add(pid);
			if (x == -123) {
				if (pid == p2) {
					if (d < bestDistance) 
						bestDistance = d;
					return String.valueOf(bestDistance);
				}
				if (d >= bestDistance) 
					return String.valueOf(bestDistance);
				if (distances[pid] != null && distances[pid][p2] != -1) {
					short dgoal = (short) (d + distances[pid][p2]);
					if (dgoal < bestDistance)
						bestDistance = dgoal;
					continue;
				}
			} else {
				if (person.equals(goal)) 
					return String.valueOf(d);				
			}
			for (Edge ae : person.getKnows()) {
				KnowsEdge edge = (KnowsEdge) ae;
				Person adjPerson = (Person) edge.getOtherNode(person);
				short replyOut = edge.getRepOut();
				short replyIn = edge.getRepIn();
				if (replyIn > x && replyOut > x) {
					queue.add(adjPerson);
					dist.add((short) (d + 1));
				}
			}
		}
		return bestDistance == 10000? "-1" : String.valueOf(bestDistance);
	}
	
	public String query2(int k, String d) throws ParseException {
		Date date = sdf.parse(d + ":00:00:00");
		
		// priority queue according to the tag range
		PriorityQueue<Integer> pq = 
			new PriorityQueue<Integer> (k + 1, new TagComparator(date.getTime()));
		for (int tagID : db.getAllTags()) {
			pq.add(tagID);
			if (pq.size() > k) pq.poll();
		} 
		
		// returning tag names in the correct order
		String topTags = "";
		while (!pq.isEmpty()) {
			try {				
				String tagName = db.getTagName(pq.poll());
				topTags = tagName + " " + topTags;
			} catch (NotFoundException e) {
				System.err.println("ERROR: All tags should have a name");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		return topTags;
	}
	
	private boolean personIsLocatedAt(int personID, int placeID) {
		Person person = db.getPerson(personID);
		for (Node location : person.getLocations()) {
			Long tmpPlace = (long) location.getId();
			do {
				if (tmpPlace == placeID) return true;
				tmpPlace = db.getPlaceLocation(tmpPlace);
			} while (tmpPlace != null);
		}
		return false;
	}	
	
	public String query3(int k, int hops, String placeName) {
		// finding all persons at placeName
		String placeIDs[] = db.getPlacesNamed(placeName).split(" ");
		HashSet<Integer> personsAtPlace = new HashSet<Integer> ();
		for (String s: placeIDs) {
			Integer placeID = Integer.parseInt(s);
			Person persons[] = db.getAllPersons();
			for (int personID = 0; personID < db.getNumPersons(); personID++) {
				if (persons[personID] == null) continue;
				if (personIsLocatedAt(personID, placeID)) 
					personsAtPlace.add(personID);
			}
		}
		
		// computing similarities between all persons at place less than 
		// hops + 1 steps away
		PriorityQueue<PersonPair> pq = 
			new PriorityQueue<PersonPair>(k + 1, new PersonPairComparator()); 
		for (Integer idP1 : personsAtPlace) {
			Person p1 = db.getPerson(idP1);
			LinkedList<Person> queue = new LinkedList<Person>();
			LinkedList<Integer> dist = new LinkedList<Integer>();
			HashSet<Integer> visited = new HashSet<Integer>();
			queue.addFirst(p1);
			dist.addFirst(0);
			// does a BFS with depth-limit = hops 
			// and computes similarities of each reachable person to p1
			while (!queue.isEmpty()) {
				Person p2 = queue.removeFirst();
				int d = dist.removeFirst();
				if (visited.contains(p2.getId())) continue;
				visited.add(p2.getId());
				if (p1.getId() != p2.getId() && p1.getId() < p2.getId()
					&& personsAtPlace.contains(p2.getId())) {
					// prioritized according to their (p1 p2) similarity
					pq.add(new PersonPair(p1, p2));
					if (pq.size() > k) pq.poll();
				}
				if (d == hops) continue;
				for (Edge ae : p2.getKnows()) {
					KnowsEdge edge = (KnowsEdge) ae;
					queue.add((Person) edge.getOtherNode(p2));
					dist.add(d + 1);
				}
			}
		}

		String topPairs = "";
		while (!pq.isEmpty()) 
			topPairs = pq.poll().toString() + " " + topPairs;
		
		return topPairs;
	}

	// returns false if the maximum possible centrality with the 
	// given parameters is guaranteed to be lower or equal than c. 
	// returns true otherwise
	//
	// rp - current rp (reachable nodes from p)
	// sp - current sp (sum geod. distances of reachable nodes)
	// n - number of vertices on the graph
	// d - minimum geod. distances of reamining nodes
	// c - centrality to compare to
	private 
	boolean checkCentrality(long rp, long sp, long n, long d, Centrality c) {
		long x = n - rp;
		if (2*x*d <= d - 2*sp) return true;
		Centrality maxC = new Centrality(n, sp + x*d);
		if (maxC.compare(c) < 0) return false;
		return true;
	}
	
	public String query4(int k, String tagName) {
		// finding the tag with the given name
		Tag tag = null;
		for (int idTag : db.getAllTags()) {
			Tag node = db.getTag(idTag);
			try {
				if (db.getTagName(idTag).equals(tagName)) {
					tag = node;
				}
			} catch (NotFoundException e) {
				System.err.println("ERROR: Tag should have a name");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		// finding all vertices on the induced graph
		HashSet<Person> vertices = new HashSet<Person>();
		for (Person person : tag.getMembersForums()) {
			vertices.add(person);
		}
		Person sortedVertices[] = vertices.toArray(new Person[vertices.size()]);		
		Arrays.sort(sortedVertices, new PersonDegreeComparator());
		
		int n1 = vertices.size() - 1;
	
		// from each node p on the graph, do a BFS and compute centrality
		PriorityQueue<PersonCentrality> pq =
			new PriorityQueue<PersonCentrality> 
				(k + 1, new PersonCentralityComparator());
		int cnt = 0;
		for (Person p : sortedVertices) {
			if (3*cnt++ > vertices.size()) 
				break;
			LinkedList<Person> queue = new LinkedList<Person> ();
			LinkedList<Integer> dist = new LinkedList<Integer> ();
			HashSet<Person> visited = new HashSet<Person> ();
			queue.add(p);
			dist.add(0);
			long rp = -1, sp = 0;
			// do a BFS to compute relevant quantities rp, sp				
			while (!queue.isEmpty()) {
				Person p2 = queue.removeFirst();
				int d = dist.removeFirst();
				// visit only vertices with the given forum tag
				if (!vertices.contains(p2) || visited.contains(p2)) 
					continue;
				visited.add(p2);
				rp++;
				sp += d;
				if (pq.size() >= k
					&& !checkCentrality(rp, sp, n1, d, pq.peek().centrality)) {
					// stops if max. possible centrality is lower than the
					// worst one in the priority queue
					break;
				}
				for (Edge ae : p2.getKnows()) {
					KnowsEdge edge = (KnowsEdge) ae;
					queue.add((Person) edge.getOtherNode(p2));
					dist.add(d + 1);
				}
			}			
			pq.add(new PersonCentrality(p, new Centrality(rp*rp, n1*sp)));
			if (pq.size() > k) pq.poll();
		}
		String queryAns = "";
		while (!pq.isEmpty()) {
			PersonCentrality pc = pq.poll();
			queryAns = String.valueOf(pc.person.getId()) + " " + queryAns;
		}
		return queryAns;
	}

	public void solveQueries() {
		for (String query : queries) {
			QueryType type = getQueryType(query);
			String params[] = 
				query.substring(7, query.length() - 1).split(", ");
			if (type.equals(QueryType.TYPE1)) {
				int p1 = Integer.parseInt(params[0]); 
				int p2 = Integer.parseInt(params[1]);
				int x = Integer.parseInt(params[2]);
				answers.put(query, query1(p1, p2, x));
			} else if (type.equals(QueryType.TYPE2)) {
				int k = Integer.parseInt(params[0]);
				try {
					answers.put(query, query2(k, params[1]));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (type.equals(QueryType.TYPE3)) {
				int k = Integer.parseInt(params[0]);
				int hops = Integer.parseInt(params[1]);
				answers.put(query, query3(k, hops, params[2]));
			} else if (type.equals(QueryType.TYPE4)) {
				int k = Integer.parseInt(params[0]);
				answers.put(query, query4(k, params[1]));
			}			
		}		
	}
	
	public void run() {
		solveQueries();
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
	
	public static void initDistancesCache(Database db) {
		int numPersons = db.getNumPersons();
		distances = new short[numPersons][];
		Person sorted[] = new Person[numPersons];
		for (int i = 0; i < numPersons; i++) {
			sorted[i] = db.getPerson(i);
		}
		Arrays.sort(sorted, new PersonDegreeComparator());
		HashSet<Integer> visited = new HashSet<Integer> ();		
		for (int i = 0; i*i < 0; i++) {
			int cnt = 0;
			Person p = sorted[i];
			LinkedList<Person> queue = new LinkedList<Person> (); 
			LinkedList<Short> dist = new LinkedList<Short> ();
			distances[p.getId()] = new short[numPersons];
			Arrays.fill(distances[p.getId()], (short) -1);
			visited.clear();
			queue.addFirst(p);
			dist.addFirst((short) 0);
			while (!queue.isEmpty()) {
				if (10*cnt > numPersons) break;
				Person cur = queue.removeFirst();
				short d = dist.removeFirst();
				if (visited.contains(cur.getId())) continue;
				visited.add(cur.getId());
				distances[p.getId()][cur.getId()] = d;
				for (KnowsEdge edge : cur.getKnows()) {
					short replyOut = edge.getRepOut();
					short replyIn = edge.getRepIn();
					if (replyIn > -1 && replyOut > -1) {
						queue.add((Person) edge.getOtherNode(cur));
						dist.add((short) (d + 1));
					}
				}
			}
		}
	}
}
