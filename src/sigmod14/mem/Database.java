package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
	private String dataDirectory;
	
	// data storage
	private HashMap<Long,Node> persons;
	private HashMap<Long,Node> comments;
	private HashMap<Edge,Edge> edges;
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new HashMap<Long,Node> ();
		comments = new HashMap<Long,Node> ();
		edges = new HashMap<Edge,Edge> ();
	}
	
	public void setDataDirectory(String dir) {
		this.dataDirectory = dir + "/";
	}
	
	public void readData() throws FileNotFoundException {
		// reading persons
		Scanner scanner = new Scanner( new File(dataDirectory + personFName + ".csv") );
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long id = Long.parseLong(fields[0]);
			Node person = new Node(id, NodeTypes.Person);
			person.setProperty("birthday", fields[4]);
			persons.put(id, person);
		}
		scanner.close();

		// reading person_knows_person
		scanner = new Scanner( new File(dataDirectory + personKnows + ".csv") );
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
		
		// reading comments_has_creator
		scanner = 
			new Scanner(new File(dataDirectory + commentCreatorFName + ".csv"));
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
		
		// reading comment_replyOf_comment
		scanner = 
			new Scanner(new File(dataDirectory + commentReplyFName + ".csv"));
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
			
			Node creatorReply = reply.getIncident().getLast().getOut();
			Node creatorRepliedTo = repliedTo.getIncident().getLast().getOut();
			
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
			for (Edge e: person.getIncident()) {
				if (e.getRelType() != RelTypes.Knows) continue;
				Node adjPerson = e.getOut() == person ? e.getIn() : e.getOut();
				int replyOut = -1, replyIn = -1;
				try {
					replyOut = (Integer) e.getPropertyValue("repOut");
					replyIn = (Integer) e.getPropertyValue("repIn");
				} catch (NotFoundException e1) {
					System.err.println("ERROR: Property should had been defined");
					e1.printStackTrace();
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
	
	private 
	Edge findUndirectedEdge(Node n1, Node n2, RelTypes relType) 
			throws NotFoundException {
		Node out = n1.getId() < n2.getId() ? n1 : n2;
		Node in = n1.getId() < n2.getId() ? n2 : n1;
		Edge e = new Edge(out, in, EdgeTypes.Undirected, relType);
		if (edges.containsKey(e)) return edges.get(e);
		throw new NotFoundException();
	}
}
