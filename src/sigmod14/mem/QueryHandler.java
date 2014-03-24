package sigmod14.mem;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import sigmod14.mem.graph.AbstractEdge;
import sigmod14.mem.graph.Edge;
import sigmod14.mem.graph.NotFoundException;
import sigmod14.mem.graph.Person;
import sigmod14.mem.graph.Tag;

public class QueryHandler {
	public static final QueryHandler INSTANCE = 
				new QueryHandler(Database.INSTANCE);
	
	private Database db;
	
	private QueryHandler(Database db) {
		this.db = db;
	}
	
	private class TagComparator implements Comparator<Long> {
		private long date;
		private HashMap<Long, Integer> ranges;
		
		TagComparator(long date) {
			this.date = date;
			ranges = new HashMap<Long, Integer>();
		}
		

		private int getRangeTag(long tagID) {
			if (ranges.containsKey(tagID)) return ranges.get(tagID);
			HashSet<Long> vertices = new HashSet<Long>();
			Tag tag = db.getTag(tagID);
			
			// getting all the persons in the induced graph
			for (AbstractEdge edge : tag.getInterested()) {
				Person person = (Person) edge.getIn();				
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
			HashSet<Long> visited = new HashSet<Long>();
			for (Long id : vertices) {
				if (visited.contains(id)) continue;
				int sizeComp = 0;
				LinkedList<Long> stack = new LinkedList<Long> ();
				stack.add(id);
				while (!stack.isEmpty()) {
					Long personID = stack.removeFirst();
					if (visited.contains(personID)) continue;
					visited.add(personID);
					sizeComp++;
					Person person = db.getPerson(personID);
					for (AbstractEdge ae : person.getKnows()) {
						Edge edge = (Edge) ae;
						Long idAdjPerson = edge.getOtherNode(person).getId();
						if (!vertices.contains(idAdjPerson)) continue;
						stack.addFirst(idAdjPerson);
					}
				}
				if (sizeComp > score) score = sizeComp;
			}
			ranges.put(tagID, score);
			return score;
		}
		
		public int compare(Long tag1ID, Long tag2ID) {		
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
			for (AbstractEdge e1 : person1.getInterests()) {
				Tag tag = (Tag) e1.getOut();
				for (AbstractEdge e2 : tag.getInterested()) {
					if (e2.getIn().equals(person2)) {
						similarity++;
						break;
					}
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
		
	// does a BFS on the induced graph, starting from p1 and counting the
	// steps to reach p2
	public String query1(long p1, long p2, int x) {
		Person goal = db.getPerson(p2);
		if (goal == null) return "-1";
		LinkedList<Person> queue = new LinkedList<Person> ();
		LinkedList<Integer> dist = new LinkedList<Integer> ();
		HashSet<Person> visited = new HashSet<Person> ();
		queue.addFirst(db.getPerson(p1));
		dist.addFirst(0);
		while (!queue.isEmpty()) {
			Person person = queue.removeFirst();			
			int d = dist.removeFirst();
			if (visited.contains(person)) continue;
			if (person.equals(goal)) return String.valueOf(d);
			visited.add(person);
			for (AbstractEdge ae : person.getKnows()) {
				Edge edge = (Edge) ae;
				Person adjPerson = (Person) edge.getOtherNode(person);
				int replyOut = -1, replyIn = -1;
				try {
					replyOut = (Integer) edge.getPropertyValue("repOut");
					replyIn = (Integer) edge.getPropertyValue("repIn");
				} catch (NotFoundException e) {
					System.err.println("ERROR: Property should had been defined");
					e.printStackTrace();
					System.exit(-1);
				}
				if (replyIn > x && replyOut > x) {
					queue.add(adjPerson);
					dist.add(d + 1);
				}
			}
		}
		return "-1";
	}
	
	public String query2(int k, String d) throws ParseException {
		Date date = DataLoader.INSTANCE.sdf.parse(d + ":00:00:00");
		
		// priority queue according to the tag range
		PriorityQueue<Long> pq = 
			new PriorityQueue<Long> (k + 1, new TagComparator(date.getTime()));
		for (Long tagID : db.getAllTags()) {
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
	
	private boolean personIsLocatedAt(long personID, long placeID) {
		Person person = db.getPerson(personID);
		for (AbstractEdge edge : person.getLocations()) {
			Long tmpPlace = edge.getIn().getId();
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
		HashSet<Long> personsAtPlace = new HashSet<Long> ();
		for (String s: placeIDs) {
			Long placeID = Long.parseLong(s);
			for (Long personID : db.getAllPersons()) {
				if (personIsLocatedAt(personID, placeID)) 
					personsAtPlace.add(personID);
			}
		}
		
		// computing similarities between all persons at place less than 
		// hops + 1 steps away
		PriorityQueue<PersonPair> pq = 
			new PriorityQueue<PersonPair>(k + 1, new PersonPairComparator()); 
		for (Long idP1 : personsAtPlace) {
			Person p1 = db.getPerson(idP1);
			LinkedList<Person> queue = new LinkedList<Person>();
			LinkedList<Integer> dist = new LinkedList<Integer>();
			HashSet<Long> visited = new HashSet<Long>();
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
				for (AbstractEdge ae : p2.getKnows()) {
					Edge edge = (Edge) ae;
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
	// c- centrality to compare to
	private 
	boolean checkCentrality(long rp, long sp, long n, long d, Centrality c) {
		long x = n - rp;
		if (2*x*d <= d - 2*sp) return true;
		Centrality maxC = new Centrality(n, sp + x*d);
		if (maxC.compare(c) <= 0) return false;
		return true;
	}
	
	public String query4(int k, String tagName) {
		// finding the tag with the given name
		Tag tag = null;
		for (Long idTag : db.getAllTags()) {
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
		for (AbstractEdge edge : tag.getMembersForums()) {
			vertices.add((Person) edge.getIn());
		}
		int n1 = vertices.size() - 1;

		// from each node p on the graph, do a BFS and compute centrality
		PriorityQueue<PersonCentrality> pq =
			new PriorityQueue<PersonCentrality> 
				(k + 1, new PersonCentralityComparator());
		for (Person p : vertices){
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
				if (!vertices.contains(p2)) continue;
				if (visited.contains(p2)) continue;
				visited.add(p2);
				rp++;
				sp += d;
				if (!pq.isEmpty()
					&& !checkCentrality(rp, sp, n1, d, pq.peek().centrality)) {
					// stops if max. possible centrality is lower than the
					// worst one in the priority queue
					break;
				}
				for (AbstractEdge ae : p2.getKnows()) {
					Edge edge = (Edge) ae;
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
	
}
