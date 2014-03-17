package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;

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
	
	// file names 
	public static final String personFName = "person";
	public static final String tagFName = "tag";
	public static final String commentCreatorFName = "comment_hasCreator_person";
	public static final String commentReplyFName = "comment_replyOf_comment";
	public static final String personKnows = "person_knows_person";
	public static final String personTagFName = "person_hasInterest_tag";
	public static final String personLocation = "person_isLocatedIn_place";
	public static final String placePlaceFName = "place_isPartOf_place";
	public static final String personStudyFName = "person_studyAt_organisation";
	public static final String personWorkFName = "person_workAt_organisation";
	public static final String orgLocFName = "organisation_isLocatedIn_place";
	public static final String forumTagFName = "forum_hasTag_tag";
	public static final String forumMemberFName = "forum_hasMember_person";
	
	public String dataDir;
	
	private static final String charset = "ISO-8859-1";
	private static final SimpleDateFormat sdf = 
			new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");
	
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
	

	private Edge findUndirectedEdge(Node n1, Node n2, RelTypes relType) 
			throws NotFoundException {
		Node out = n1.getId() < n2.getId() ? n1 : n2;
		Node in = n1.getId() < n2.getId() ? n2 : n1;
		Edge e = new Edge(out, in, EdgeTypes.Undirected, relType);
		if (edges.containsKey(e)) return edges.get(e);
		throw new NotFoundException();
	}
	
	public void setDataDirectory(String dir) {
		this.dataDir = dir + "/";
	}
	
	public void readData() throws FileNotFoundException, ParseException {
		// data used to create person graph 
		readPerson();
		readPersonKnowsPerson();
		
		// data used for query1
		readCommentHasCreator();
		readCommentReply();			
		commentCreator.clear();	// no need to store comments
		
		// data used for query2
		readTag();
		readPersonInterest();
		
		// data used for query3
		readOrganizationPlace();
		readPersonWorkStudy();
		readPlacePlace();
		orgPlace.clear();
	}

	private void readCommentReply() throws FileNotFoundException {
		Scanner scanner = 
			new Scanner(new File(dataDir + commentReplyFName + ".csv"),
					    charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");

			// (*) reply will already be on DB iff it has a creator who knows
			//     someone. Otherwise it is useless for query1
			Long replyID = Long.parseLong(fields[0]);
			if (!commentCreator.containsKey(replyID)) 
				continue;
			
			Long repliedToID = Long.parseLong(fields[1]);
			if (!commentCreator.containsKey(repliedToID)) 
				continue; // see (*) above
			
			Node creatorReply = 
				persons.get(commentCreator.get(replyID));
			Node creatorRepliedTo = 
				persons.get(commentCreator.get(repliedToID));
			
			Edge edge;
			try {
				edge = findUndirectedEdge(creatorReply, 
							 	  	      creatorRepliedTo, 
							 	  	      RelTypes.Knows);
			} catch (NotFoundException e) {
				// no point in keeping this reply. creators must know e/other
				continue;
			}
			String property = 
				edge.getOut().equals(creatorReply) ? "repOut" : "repIn";

			Integer replies = -1;
			try {
				replies = (Integer) edge.getPropertyValue(property) + 1;
			} catch (NotFoundException e) {
				System.err.println("ERROR: Reply property should exist.");
				e.printStackTrace();
				System.exit(-1);
			}
			edge.setProperty(property, replies);	
		}
		scanner.close();
	}


	// this method assumes that readPerson() and readPersonKnowsPerson
	// have already been called
	private void readCommentHasCreator() throws FileNotFoundException {
		String file = dataDir + commentCreatorFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");

			// if the creator is not already in DB then there is no point
			// in storing this comment because creator doesn't know anyone
			Long personID = Long.parseLong(fields[1]);
			if (!persons.containsKey(personID)) continue;
			
			Long commentID = Long.parseLong(fields[0]);
			commentCreator.put(commentID, personID);	
		}
		scanner.close();
	}


	private void readPersonKnowsPerson() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(dataDir + personKnows + ".csv"),
				                      charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			
			Long person1ID = Long.parseLong(fields[0]);
			if (!persons.containsKey(person1ID)) 
				persons.put(person1ID, new Node(person1ID, NodeTypes.Person));
			
			Long person2ID = Long.parseLong(fields[1]);
			if (!persons.containsKey(person2ID)) 
				persons.put(person2ID, new Node(person2ID, NodeTypes.Person));

			// convention is that the node with lowest ID will be "out" node
			Node person1 = persons.get(Math.min(person1ID, person2ID));
			Node person2 = persons.get(Math.max(person1ID, person2ID));
			Edge edge = new Edge(person1, 
							     person2, 
					             EdgeTypes.Undirected, 
					             RelTypes.Knows);			
			
			// person_knows_person has both directed edges, we only need one
			if (edges.containsKey(edge)) continue;
			
			person1.addEdge(edge);
			person2.addEdge(edge);
			edge.setProperty("repOut", 0);
			edge.setProperty("repIn", 0);			
			edges.put(edge, edge);
		}
		scanner.close();
	}


	private void readPerson() throws FileNotFoundException, ParseException {
		String file = dataDir + personFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long id = Long.parseLong(fields[0]);
			Node person = new Node(id, NodeTypes.Person);
			Date birthday = sdf.parse(fields[4] + ":00:00:00");
			person.setProperty("birthday", birthday);
			persons.put(id, person);
		}
		scanner.close();
	}
	
	private void readTag() throws FileNotFoundException {
		File file = new File(dataDir + tagFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long id = Long.parseLong(fields[0]);
			String name = fields[1];
			Node tag = new Node(id, NodeTypes.Tag);
			tag.setProperty("name", name);
			tags.put(id, tag);
			
		}
		scanner.close();
	}
	
	private void readPersonInterest() throws FileNotFoundException {
		String file = dataDir + personTagFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			
			Long personID = Long.parseLong(fields[0]);
			if (!persons.containsKey(personID)) 
				persons.put(personID, new Node(personID, NodeTypes.Person));
			Node person = persons.get(personID);
			
			Long tagID = Long.parseLong(fields[1]);
			if (!tags.containsKey(tagID))
				tags.put(tagID, new Node(tagID, NodeTypes.Tag));
			Node tag = tags.get(tagID);
			
			Edge edge = tag.createEdge(person, 
									   EdgeTypes.Directed, 
									   RelTypes.Interested);
			edges.put(edge, edge);
			
		}
		scanner.close();
	}

	private void readOrganizationPlace() throws FileNotFoundException {
		File file = new File(dataDir + orgLocFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long idOrg = Long.parseLong(fields[0]);
			Long idPlace = Long.parseLong(fields[1]);
			orgPlace.put(idOrg, idPlace);
		}
		scanner.close();		
	}
	
	private void readPersonWorkStudy() throws FileNotFoundException {
		readPersonOrg(personWorkFName);
		readPersonOrg(personStudyFName);
	}
	
	private void readPersonOrg(String fileName) throws FileNotFoundException {
		String file = dataDir + fileName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			
			Long personID = Long.parseLong(fields[0]);
			if (!persons.containsKey(personID)) 
				continue;	// person doesn't know other persons
			Node person = persons.get(personID);
			
			Long orgID = Long.parseLong(fields[1]);
			if (!orgPlace.containsKey(orgID))
				continue;	// no place for this organization
			Long placeID = orgPlace.get(orgID);
			if (!places.containsKey(placeID))
				places.put(placeID, new Node(placeID, NodeTypes.Place));
			Node place = places.get(placeID);
			
			Edge edge = place.createEdge(person, 
							     	     EdgeTypes.Directed, 
									     RelTypes.LocationOf);
			edges.put(edge, edge);			
		}
		scanner.close();
	}
	
	private void readPlacePlace() throws FileNotFoundException {
		File file = new File(dataDir + placePlaceFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long idPlace1 = Long.parseLong(fields[0]);
			Long idPlace2 = Long.parseLong(fields[1]);
			placePlace.put(idPlace1, idPlace2);
		}
		scanner.close();
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
		Date date = sdf.parse(d + ":00:00:00");
		
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
	
}
