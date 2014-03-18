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
	
	private class TagComparator implements Comparator<Long> {
		private Date date;
		private HashMap<Long, Integer> scores;
		
		public TagComparator(Date date) {
			this.date = date;
			scores = new HashMap<Long, Integer>();
		}
		

		private int getScoreTag(long tagID) {
			if (scores.containsKey(tagID)) return scores.get(tagID);
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
			scores.put(tagID, score);
			return score;
		}
		
		public int compare(Long tag1ID, Long tag2ID) {		
			Integer score1 = getScoreTag(tag1ID);
			Integer score2 = getScoreTag(tag2ID);
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

	private class PersonPairComparator implements Comparator<PersonPair> {
		private int getScorePair(PersonPair pp) {
			int score = 0;
			Node person1 = pp.person1;
			Node person2 = pp.person2;
			for (Edge e1 : person1.getIncident()) {
				if (e1.getRelType() != RelTypes.INTERESTED) continue;
				Node tag = e1.getOut();
				for (Edge e2 : tag.getIncident()) {
					if (e2.getIn().equals(person2)) {
						score++;
						break;
					}
				}
			}
			return score;
		}
		public int compare(PersonPair pp1, PersonPair pp2) {
			int score1 = getScorePair(pp1);
			int score2 = getScorePair(pp2);
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
	
	private QueryHandler() {
		persons = Database.INSTANCE.getPersons();
		tags = Database.INSTANCE.getTags();
		placeLocatedAtPlace = Database.INSTANCE.getPlaceLocatedAtPlace();
		namePlaces = Database.INSTANCE.getNamePlaces();
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
		return -1;
	}
	
	public LinkedList<String> query2(int k, String d) throws ParseException {
		LinkedList<String> topTags = new LinkedList<String> ();
		Date date = DataLoader.sdf.parse(d + ":00:00:00");
		
		PriorityQueue<Long> sorted = 
			new PriorityQueue<Long> (k + 1, new TagComparator(date));
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
	
	boolean personIsLocatedAt(long personID, long placeID) {
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
	
	public LinkedList<String> query3(int k, int hops, String placeName) {
		Long placeID = namePlaces.get(placeName);
		HashSet<Long> personsAtPlace = new HashSet<Long> ();
		for (Long personID : persons.keySet()) {
			if (personIsLocatedAt(personID, placeID)) 
				personsAtPlace.add(personID);
		}
		
		PriorityQueue<PersonPair> pq = 
			new PriorityQueue<PersonPair>(k + 1, new PersonPairComparator()); 
		for (Long idP1 : personsAtPlace) {
			Node p1 = persons.get(idP1);
			LinkedList<Node> queue = new LinkedList<Node>();
			LinkedList<Integer> dist = new LinkedList<Integer>();
			HashSet<Long> visited = new HashSet<Long>();
			queue.addFirst(p1);
			dist.addFirst(0);
			while (!queue.isEmpty()) {
				Node p2 = queue.removeFirst();
				int d = dist.removeFirst();
				if (visited.contains(p2.getId())) continue;
				visited.add(p2.getId());
				if (p1.getId() != p2.getId() && p1.getId() < p2.getId()
					&& personsAtPlace.contains(p2.getId())) {
					// get the score and add to the queue
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

		LinkedList<String> topPairs = new LinkedList<String> ();
		while (!pq.isEmpty()) {
			topPairs.addFirst(pq.poll().toString());
		}
		System.out.println(topPairs);
		return topPairs;
	}

	public void query4(int k, String tagName) {
		Node tag = null;
		for (Node node : tags.values()) {
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
		
		HashSet<Node> vertices = new HashSet<Node>();
		for (Edge edge : tag.getIncident()) {
			if (edge.getRelType() != RelTypes.MEMBERFORUMTAG) continue;
			vertices.add(edge.getIn());
		}
		System.out.println(vertices.size());
	}
	
}
