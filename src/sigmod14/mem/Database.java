package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
		Comment,
		Forum,
		Place,
		Organization
	}
	
	public static enum EdgeTypes {
		Directed,
		Undirected,
	}

	public static enum RelTypes {
		Knows,
		Replied,
		Created,
		Interested,
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
	
	private static final String charset = "UTF-8";
	private static final SimpleDateFormat sdf = 
			new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");
	
	// data storage
	private HashMap<Long,Node> persons;
	private HashMap<Long,Node> comments;
	private HashMap<Long,Node> tags;
	private HashMap<Edge,Edge> edges;
	

	private class TagComparator implements Comparator<Long> {
		private Date date;	
		public TagComparator(Date date) {
			this.date = date;
		}		
		public int compare(Long tag1ID, Long tag2ID) {		
			Integer score1 = getScoreTag(tag1ID, date);
			Integer score2 = getScoreTag(tag2ID, date);
			return score1.compareTo(score2);
		}
	}
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new HashMap<Long,Node> ();
		comments = new HashMap<Long,Node> ();
		tags = new HashMap<Long,Node> ();
		edges = new HashMap<Edge,Edge> ();
	}
	

	private 
	Edge findUndirectedEdge(Node n1, Node n2, RelTypes relType) 
			throws NotFoundException {
		Node out = n1.getId() < n2.getId() ? n1 : n2;
		Node in = n1.getId() < n2.getId() ? n2 : n1;
		Edge e = new Edge(out, in, EdgeTypes.Undirected, relType);
		if (edges.containsKey(e)) return edges.get(e);
		throw new NotFoundException();
	}
	
	private Node getOtherNode(Edge edge, Node node) {
		return edge.getOut() == node ? edge.getIn() : edge.getOut();
	}
	
	public void setDataDirectory(String dir) {
		this.dataDir = dir + "/";
	}
	
	public void readData() throws FileNotFoundException, ParseException {
		readPerson();
		readPersonKnowsPerson();
		readCommentHasCreator();
		readCommentReply();		
		readPersonInterest();		
	}


	private void readPersonInterest() throws FileNotFoundException {
		Scanner scanner = 
			new Scanner(new File(dataDir + personTagFName + ".csv"),
						charset);
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
			if (!comments.containsKey(replyID)) continue;
			Node reply = comments.get(replyID);
			
			Long repliedToID = Long.parseLong(fields[1]);
			if (!comments.containsKey(repliedToID)) continue; // see (*) above
			Node repliedTo = comments.get(repliedToID);
			
			Node creatorReply = reply.getIncident().getLast().getIn();
			Node creatorRepliedTo = repliedTo.getIncident().getLast().getIn();
			
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
		Scanner scanner = 
			new Scanner(new File(dataDir + commentCreatorFName + ".csv"),
					    charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");

			// if the creator is not already in DB then there is no point
			// in storing this comment because creator doesn't know anyone
			Long personID = Long.parseLong(fields[1]);
			if (!persons.containsKey(personID)) continue;
			Node person = persons.get(personID);
			
			Long commentID = Long.parseLong(fields[0]);
			Node comment = new Node(commentID, NodeTypes.Comment);
			comments.put(commentID, comment);
			
			comment.createEdge(person, EdgeTypes.Directed, RelTypes.Created);
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
		Scanner scanner = 
			new Scanner(new File(dataDir + personFName + ".csv"), charset);
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
				Node adjPerson = getOtherNode(edge, person);
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
	
	public LinkedList<Long> query2(int k, String d) throws ParseException {
		LinkedList<Long> topTags = new LinkedList<Long> ();
		Date date = sdf.parse(d + ":00:00:00");
		
		PriorityQueue<Long> sorted = 
			new PriorityQueue<Long> (k, new TagComparator(date));
		for (Long tagID : tags.keySet()) {
			sorted.add(tagID);
			if (sorted.size() > k) sorted.poll();
		} 
		
		for (Long id : sorted) System.out.print(getScoreTag(id, date) + " ");
		System.out.println();
		
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
					Long idAdjPerson = getOtherNode(edge, person).getId();
					if (!vertices.contains(idAdjPerson)) continue;
					stack.addFirst(idAdjPerson);
				}
			}
			if (sizeComp > score) score = sizeComp;
		}
		return score;
	}
	
}
