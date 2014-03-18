package sigmod14.mem;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import sigmod14.mem.Database.RelTypes;

public class QueryHandler {
	public static final QueryHandler INSTANCE = new QueryHandler();

	// pointers to Database storage
	private HashMap<Long,Node> persons;
	private HashMap<Long,Node> tags;
	private HashMap<Long,Long> placeLocatedAtPlace;
	private HashMap<String,Long> namePlaces;
		
	private class TagComparator implements Comparator<Long> {
		private Date date;
		private HashMap<Long, Integer> ranges;
		
		TagComparator(Date date) {
			this.date = date;
			ranges = new HashMap<Long, Integer>();
		}
		

		private int getRangeTag(long tagID) {
			if (ranges.containsKey(tagID)) return ranges.get(tagID);
			HashSet<Long> vertices = new HashSet<Long>();
			Node tag = tags.get(tagID);
			
			// getting all the persons in the induced graph
			for (Edge edge : tag.getIncident()) {
				if (!edge.getRelType().equals(RelTypes.INTERESTED)) continue;
				Node person = edge.getIn();
				Date birthday;
				try {
					birthday = (Date) person.getPropertyValue("birthday");
				} catch (NotFoundException e) {
					continue;	// this person is not defined in persons.csv 
				}
				if (birthday.before(date)) continue;
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
					Long idPerson = stack.removeFirst();
					if (visited.contains(idPerson)) continue;
					visited.add(idPerson);
					sizeComp++;
					Node person = persons.get(idPerson);
					for (Edge edge : person.getIncident()) {
						if (!edge.getRelType().equals(RelTypes.KNOWS)) continue;
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
					String name1 = 
						(String) tags.get(tag1ID).getPropertyValue("name");
					String name2 = 
						(String) tags.get(tag2ID).getPropertyValue("name");
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
		Node person1;
		Node person2;
		
		PersonPair(Node person1, Node person2) {
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
			Node person1 = pp.person1;
			Node person2 = pp.person2;
			for (Edge e1 : person1.getIncident()) {
				if (e1.getRelType() != RelTypes.INTERESTED) continue;
				Node tag = e1.getOut();
				for (Edge e2 : tag.getIncident()) {
					if (e2.getRelType() == RelTypes.INTERESTED
							&& e2.getIn().equals(person2)) {
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
		private Node person;
		private Centrality centrality;
		
		PersonCentrality(Node person, Centrality centrality) {
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
	
	private QueryHandler() {
		persons = Database.INSTANCE.getPersons();
		tags = Database.INSTANCE.getTags();
		placeLocatedAtPlace = Database.INSTANCE.getPlaceLocatedAtPlace();
		namePlaces = Database.INSTANCE.getNamePlaces();
	}
	
	// does a BFS on the induced graph, starting from p1 and counting the
	// steps to reach p2
	public String query1(long p1, long p2, int x) {
		Node goal = persons.get(p2);
		if (goal == null) return "-1";
		LinkedList<Node> queue = new LinkedList<Node> ();
		LinkedList<Integer> dist = new LinkedList<Integer> ();
		HashSet<Node> visited = new HashSet<Node> ();
		queue.addFirst(persons.get(p1));
		dist.addFirst(0);
		while (!queue.isEmpty()) {
			Node person = queue.removeFirst();			
			int d = dist.removeFirst();
			if (visited.contains(person)) continue;
			if (person.equals(goal)) return String.valueOf(d);
			visited.add(person);
			for (Edge edge: person.getIncident()) {
				if (edge.getRelType() != RelTypes.KNOWS) continue;
				Node adjPerson = edge.getOtherNode(person);
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
		Date date = DataLoader.sdf.parse(d + ":00:00:00");
		
		// priority queue according to the tag range
		PriorityQueue<Long> pq = 
			new PriorityQueue<Long> (k + 1, new TagComparator(date));
		for (Long tagID : tags.keySet()) {
			pq.add(tagID);
			if (pq.size() > k) pq.poll();
		} 
		
		// returning tag names in the correct order
		String topTags = "";
		while (!pq.isEmpty()) {
			try {				
				String tagName = (String) tags.get(pq.poll())
					                          .getPropertyValue("name");
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
		Node person = persons.get(personID);
		for (Edge edge : person.getIncident()) {
			if (edge.getRelType() != RelTypes.LOCATEDAT) continue;
			Long place = edge.getIn().getId();
			do {
				if (place == placeID) return true;
				if (placeLocatedAtPlace.containsKey(place)) 
					place = placeLocatedAtPlace.get(place);
				else place = null;
			} while (place != null);
		}
		return false;
	}	
	
	public String query3(int k, int hops, String placeName) {
		// finding all persons at placeName
		Long placeID = namePlaces.get(placeName);
		HashSet<Long> personsAtPlace = new HashSet<Long> ();
		for (Long personID : persons.keySet()) {
			if (personIsLocatedAt(personID, placeID)) 
				personsAtPlace.add(personID);
		}
		
		int cnt = 0;	//TODO debug
		
		// computing similarities between all persons at place less than 
		// hops + 1 steps away
		PriorityQueue<PersonPair> pq = 
			new PriorityQueue<PersonPair>(k + 1, new PersonPairComparator()); 
		for (Long idP1 : personsAtPlace) {
			System.out.println(cnt++);	// TODO debug
			Node p1 = persons.get(idP1);
			LinkedList<Node> queue = new LinkedList<Node>();
			LinkedList<Integer> dist = new LinkedList<Integer>();
			HashSet<Long> visited = new HashSet<Long>();
			queue.addFirst(p1);
			dist.addFirst(0);
			// does a BFS with depth-limit = hops 
			// and computes similarities of each reachable person to p1
			while (!queue.isEmpty()) {
				Node p2 = queue.removeFirst();
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
				for (Edge edge : p2.getIncident()) {
					if (edge.getRelType() != RelTypes.KNOWS) continue;
					queue.add(edge.getOtherNode(p2));
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
		Node tag = null;
		for (Long idTag : tags.keySet()) {
			Node node = tags.get(idTag);
			try {
				if (node.getPropertyValue("name").equals(tagName)) {
					tag = node;
				}
			} catch (NotFoundException e) {
				System.err.println("ERROR: Tag should have a name");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		// finding all vertices on the induced graph
		HashSet<Node> vertices = new HashSet<Node>();
		for (Edge edge : tag.getIncident()) {
			if (edge.getRelType() != RelTypes.MEMBERFORUMTAG) continue;
			vertices.add(edge.getIn());
		}
		int n1 = vertices.size() - 1;

		// from each node p on the graph, do a BFS and compute centrality
		PriorityQueue<PersonCentrality> pq =
			new PriorityQueue<PersonCentrality> 
				(k + 1, new PersonCentralityComparator());
		for (Node p : vertices){
			LinkedList<Node> queue = new LinkedList<Node> ();
			LinkedList<Integer> dist = new LinkedList<Integer> ();
			HashSet<Node> visited = new HashSet<Node> ();
			queue.add(p);
			dist.add(0);
			long rp = -1, sp = 0;
			// do a BFS to compute relevant quantities rp, sp
			while (!queue.isEmpty()) {
				Node p2 = queue.removeFirst();
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
				for (Edge edge : p2.getIncident()) {
					if (edge.getRelType() != RelTypes.KNOWS) continue;
					queue.add(edge.getOtherNode(p2));
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
