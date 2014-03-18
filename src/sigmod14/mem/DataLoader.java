package sigmod14.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import sigmod14.mem.Database.EdgeTypes;
import sigmod14.mem.Database.RelTypes;

public class DataLoader {
	public static final DataLoader INSTANCE = new DataLoader();
	
	private String charset = "UTF-8";
	public final SimpleDateFormat sdf =
			new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");
	
	// file names 
	public static final String personFName = "person";
	public static final String tagFName = "tag";
	public static final String commentCreatorFName = "comment_hasCreator_person";
	public static final String commentReplyFName = "comment_replyOf_comment";
	public static final String personKnows = "person_knows_person";
	public static final String personTagFName = "person_hasInterest_tag";
	public static final String personLocation = "person_isLocatedIn_place";
	public static final String placeFName = "place";
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
	private HashMap<Long,Node> forums;
	private HashMap<Long,Long> commentCreator;
	private HashMap<Long,Long> orgPlace;
	private HashMap<Long,Long> placeLocatedAtPlace;
	private HashMap<String,String> namePlaces;
	private HashMap<Edge,Edge> edges;
	
	private DataLoader() {
		persons = Database.INSTANCE.getPersons();
		tags = Database.INSTANCE.getTags();
		places = Database.INSTANCE.getPlaces();
		forums = Database.INSTANCE.getForums();

		commentCreator = Database.INSTANCE.getCommentCreator();
		orgPlace = Database.INSTANCE.getOrgPlace();
		placeLocatedAtPlace = Database.INSTANCE.getPlaceLocatedAtPlace();
		namePlaces = Database.INSTANCE.getNamePlaces();
		
		edges = Database.INSTANCE.getEdges();
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public void setDataDirectory(String dir) {
		this.dataDir = dir + "/";
	}

	public void loadData() throws FileNotFoundException, ParseException {
		// data used to create person graph 
		loadPersons();
		readPersonKnowsPerson();
		
		// data used for query1
		loadCommentsCreator();
		loadCommentReplyTo();			
		// no need to store comments anymore
		commentCreator = null;
		Database.INSTANCE.clearCommentCreator(); 
		
		// data used for query2
		loadTags();
		loadPersonsInterest();
		
		// data used for query3
		loadPlaces();
		loadPersonsPlace();
		loadOrganizationsPlace();
		loadPersonWorkStudy();
		loadPlaceAtPlace();
//		orgPlace.clear();	// TODO fix this
		
		// data used for query4
		loadForumTag();
		loadForumMember();
	}
	
	private void loadCommentReplyTo() throws FileNotFoundException {
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
												            RelTypes.KNOWS);
			} catch (NotFoundException e) {
				// no point in keeping this reply, creators must know e/other
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
	private void loadCommentsCreator() throws FileNotFoundException {
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
				persons.put(person1ID, new Node(person1ID));
			
			Long person2ID = Long.parseLong(fields[1]);
			if (!persons.containsKey(person2ID)) 
				persons.put(person2ID, new Node(person2ID));

			// convention is that the node with lowest ID will be "out" node
			Node person1 = persons.get(Math.min(person1ID, person2ID));
			Node person2 = persons.get(Math.max(person1ID, person2ID));
			Edge edge = new Edge(person1, 
							     person2, 
					             EdgeTypes.UNDIRECTED, 
					             RelTypes.KNOWS);			
			
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


	private void loadPersons() throws FileNotFoundException, ParseException {
		String file = dataDir + personFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long id = Long.parseLong(fields[0]);
			Node person = new Node(id);
			Date birthday = sdf.parse(fields[4] + ":00:00:00");
			person.setProperty("birthday", birthday);
			persons.put(id, person);
		}
		scanner.close();
	}
	
	private void loadTags() throws FileNotFoundException {
		File file = new File(dataDir + tagFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long id = Long.parseLong(fields[0]);
			String name = fields[1];
			Node tag = new Node(id);
			tag.setProperty("name", name);
			tags.put(id, tag);
			
		}
		scanner.close();
	}
		
	private void loadPersonsInterest() throws FileNotFoundException {
		String file = dataDir + personTagFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			
			Long personID = Long.parseLong(fields[0]);
			if (!persons.containsKey(personID)) 
				persons.put(personID, new Node(personID));
			Node person = persons.get(personID);
			
			Long tagID = Long.parseLong(fields[1]);
			if (!tags.containsKey(tagID))
				tags.put(tagID, new Node(tagID));
			Node tag = tags.get(tagID);
			
			Edge edge = tag.createEdge(person, 
									   EdgeTypes.DIRECTED, 
									   RelTypes.INTERESTED);
			person.addEdge(edge);
			edges.put(edge, edge);
			
		}
		scanner.close();
	}

	private void loadPlaces() throws FileNotFoundException {
		String file = dataDir + placeFName + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();			
			String[] fields = line.split("\\|");
			Long idPlace = Long.parseLong(fields[0]);
			Node place = new Node(idPlace);
			places.put(idPlace, place);
			if (!namePlaces.containsKey(fields[1])) {
				namePlaces.put(fields[1], String.valueOf(idPlace));
			} else { 
				namePlaces.put(fields[1], 
							   namePlaces.get(fields[1]) + " " + idPlace);
			}	
		}
		scanner.close();
	}
	
	private void loadOrganizationsPlace() throws FileNotFoundException {
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
	
	private void loadPersonsPlace() throws FileNotFoundException {
		String file = dataDir + personLocation + ".csv";
		Scanner scanner = new Scanner(new File(file), charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			
			Long personID = Long.parseLong(fields[0]);
			if (!persons.containsKey(personID)) 
				continue;	// person doesn't know other persons
			Node person = persons.get(personID);
			
			Long placeID = Long.parseLong(fields[1]);
			if (!places.containsKey(placeID))
				places.put(placeID, new Node(placeID));
			Node place = places.get(placeID);
			
			Edge edge = person.createEdge(place,
							     	     EdgeTypes.DIRECTED, 
									     RelTypes.LOCATEDAT);
			edges.put(edge, edge);
		}
		scanner.close();
	}
	
	private void loadPersonWorkStudy() throws FileNotFoundException {
		loadPersonsOrg(personWorkFName);
		loadPersonsOrg(personStudyFName);
	}
	
	private void loadPersonsOrg(String fileName) throws FileNotFoundException {
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
				places.put(placeID, new Node(placeID));
			Node place = places.get(placeID);
			
			Edge edge = person.createEdge(place,
							     	     EdgeTypes.DIRECTED, 
									     RelTypes.LOCATEDAT);
			edges.put(edge, edge);
		}
		scanner.close();
	}
	
	private void loadPlaceAtPlace() throws FileNotFoundException {
		File file = new File(dataDir + placePlaceFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long idPlace1 = Long.parseLong(fields[0]);
			Long idPlace2 = Long.parseLong(fields[1]);
			placeLocatedAtPlace.put(idPlace1, idPlace2);
		}
		scanner.close();
	}
	
	private void loadForumTag() throws FileNotFoundException {
		File file = new File(dataDir + forumTagFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long idForum = Long.parseLong(fields[0]);
			Long idTag = Long.parseLong(fields[1]);
			if (!forums.containsKey(idForum))
				forums.put(idForum, new Node(idForum));
			Node forum = forums.get(idForum);
			Node tag = tags.get(idTag);
			forum.createEdge(tag, EdgeTypes.DIRECTED, RelTypes.FORUMTAG);
		}
		scanner.close();
	}
	
	private void loadForumMember() throws FileNotFoundException {
		File file = new File(dataDir + forumMemberFName + ".csv");
		Scanner scanner = new Scanner(file, charset);
		scanner.nextLine();
		while (scanner.hasNextLine()) {			
			String line = scanner.nextLine();
			String[] fields = line.split("\\|");
			Long idForum = Long.parseLong(fields[0]);
			Long idMember = Long.parseLong(fields[1]);			
			if (!forums.containsKey(idForum)) 
				continue; // no tags for this forum	
			Node forum = forums.get(idForum);
			Node person = persons.get(idMember);
			for (Edge edge : forum.getIncident()) {
				Node tag = edge.getIn();
				Edge e = new Edge(person, 
								  tag, 
								  EdgeTypes.DIRECTED, 
								  RelTypes.MEMBERFORUMTAG);
				if (edges.containsKey(e)) continue;
				tag.addEdge(e);
				edges.put(e, e);				
			}			
		}		
		scanner.close();		
	}
}
