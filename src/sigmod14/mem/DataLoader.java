package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import sigmod14.mem.Database.EdgeTypes;
import sigmod14.mem.Database.NodeTypes;
import sigmod14.mem.Database.RelTypes;

public class DataLoader {
	public static final DataLoader INSTANCE = new DataLoader();
	
	private static final String charset = "ISO-8859-1";
	public static final SimpleDateFormat sdf =
			new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");
	
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

	private String dataDir;

	// pointers to Database storage
	private HashMap<Long,Node> persons;
	private HashMap<Long,Node> tags;
	private HashMap<Long,Node> places;
	private HashMap<Long,Long> commentCreator;
	private HashMap<Long,Long> orgPlace;
	private HashMap<Long,Long> placePlace;
	private HashMap<Edge,Edge> edges;
	
	private DataLoader() {
		persons = Database.INSTANCE.getPersons();
		tags = Database.INSTANCE.getTags();
		places = Database.INSTANCE.getPlaces();

		commentCreator = Database.INSTANCE.getCommentCreator();
		orgPlace = Database.INSTANCE.getOrgPlace();
		placePlace = Database.INSTANCE.getPlacePlace();
		
		edges = Database.INSTANCE.getEdges();
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
			
			Node creatorReply = persons.get(commentCreator.get(replyID));
			Node creatorRepliedTo = 
					persons.get(commentCreator.get(repliedToID));
			
			Edge edge;
			try {
				edge = Database.INSTANCE.findUndirectedEdge(creatorReply, 
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

}
