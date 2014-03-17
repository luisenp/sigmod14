package sigmod14.mem;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Database {
	public static final Database INSTANCE = new Database();
	
	// types
	public static enum NodeTypes {
		Person,
		Tag,
		Forum,
		Place,
	}
	
	public static enum EdgeTypes {
		Directed,
		Undirected,
	}

	public static enum RelTypes {
		Knows,
		Interested,
		LocationOf
	}	
		
	// data storage
	private HashMap<Long,Node> persons;
	private HashMap<Long,Node> tags;
	private HashMap<Long,Node> places;
	private HashMap<Long,Long> commentCreator;
	private HashMap<Long,Long> orgPlace;
	private HashMap<Long,Long> placePlace;
	private HashMap<Edge,Edge> edges;	

	private class TagComparator implements Comparator<Long> {
		private Date date;	
		public TagComparator(Date date) {
			this.date = date;
		}		
		public int compare(Long tag1ID, Long tag2ID) {		
			Integer score1 = getScoreTag(tag1ID, date);
			Integer score2 = getScoreTag(tag2ID, date);
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
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new HashMap<Long,Node> (100000);
		tags = new HashMap<Long,Node> (100000);
		places = new HashMap<Long,Node> (10000);

		commentCreator = new HashMap<Long,Long> (10000000);
		orgPlace = new HashMap<Long,Long> (10000);
		placePlace = new HashMap<Long,Long> (10000);
		
		edges = new HashMap<Edge,Edge> (500000);
	}
	

	public Edge findUndirectedEdge(Node n1, Node n2, RelTypes relType) 
			throws NotFoundException {
		Node out = n1.getId() < n2.getId() ? n1 : n2;
		Node in = n1.getId() < n2.getId() ? n2 : n1;
		Edge e = new Edge(out, in, EdgeTypes.Undirected, relType);
		if (edges.containsKey(e)) return edges.get(e);
		throw new NotFoundException();
	}	
	
	public int getScoreTag(long tagID, Date date) {
		HashSet<Long> vertices = new HashSet<Long>();
		Node tag = tags.get(tagID);
		
		// getting all the persons in the induced graph
		for (Edge edge : tag.getIncident()) {
			if (!edge.getRelType().equals(RelTypes.Interested)) continue;
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
					if (!edge.getRelType().equals(RelTypes.Knows)) continue;
					Long idAdjPerson = edge.getOtherNode(person).getId();
					if (!vertices.contains(idAdjPerson)) continue;
					stack.addFirst(idAdjPerson);
				}
			}
			if (sizeComp > score) score = sizeComp;
		}
		return score;
	}

	public HashMap<Long, Node> getPersons() {
		return persons;
	}

	public HashMap<Long, Node> getTags() {
		return tags;
	}

	public HashMap<Long, Node> getPlaces() {
		return places;
	}

	public HashMap<Long, Long> getCommentCreator() {
		return commentCreator;
	}

	public HashMap<Long, Long> getOrgPlace() {
		return orgPlace;
	}

	public HashMap<Long, Long> getPlacePlace() {
		return placePlace;
	}

	public HashMap<Edge, Edge> getEdges() {
		return edges;
	}

	public int query1(long p1, long p2, int x) {
		Node goal = persons.get(p2);
		if (goal == null) return -1;
		LinkedList<Node> queue = new LinkedList<Node> ();
		LinkedList<Integer> dist = new LinkedList<Integer> ();
		HashSet<Node> visited = new HashSet<Node> ();
		queue.addFirst(persons.get(p1));
		dist.addFirst(0);
		while (!queue.isEmpty()) {
			Node person = queue.removeFirst();			
			int d = dist.removeFirst();
			if (visited.contains(person)) continue;
			if (person.equals(goal)) return d;
			visited.add(person);
			for (Edge edge: person.getIncident()) {
				if (edge.getRelType() != RelTypes.Knows) continue;
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
		return -1;
	}
	
	public LinkedList<String> query2(int k, String d) throws ParseException {
		LinkedList<String> topTags = new LinkedList<String> ();
		Date date = DataLoader.sdf.parse(d + ":00:00:00");
		
		PriorityQueue<Long> sorted = 
			new PriorityQueue<Long> (k, new TagComparator(date));
		for (Long tagID : tags.keySet()) {
			sorted.add(tagID);
			if (sorted.size() > k) sorted.poll();
		} 
		
		while (!sorted.isEmpty()) {
			try {
				topTags.addFirst((String) tags.get(sorted.poll())
											 .getPropertyValue("name"));
			} catch (NotFoundException e) {
				System.err.println("ERROR: All tags should have a name");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		return topTags;
	}
}
