package sigmod14.mem;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import sigmod14.mem.graph.AbstractEdge;
import sigmod14.mem.graph.AbstractNode;
import sigmod14.mem.graph.Edge;
import sigmod14.mem.graph.Node;
import sigmod14.mem.graph.NotFoundException;
import sigmod14.mem.graph.Person;

public class Database {
	public static final Database INSTANCE = new Database();
	
	public static enum EdgeTypes {
		DIRECTED,
		UNDIRECTED,
	}

	public static enum RelTypes {
		KNOWS,
		INTERESTED,
		LOCATEDAT,
		FORUMTAG,
		MEMBERFORUMTAG
	}	
		
	// data storage
	private HashMap<Long,Person> persons;
	private HashMap<Long,AbstractNode> tags;
	private HashMap<Long,AbstractNode> places;
	private HashMapLong commentCreator;
	private HashMapLong placeOrg;
	private HashMapLong placeLocatedAtPlace;
	private HashMap<String,String> namePlaces;
	private HashMap<Long,AbstractNode> forums;

	private HashMap<Edge,Edge> edges;
	private HashMap<Edge,Edge> edgesTagsForums;
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new HashMap<Long,Person> (100000);
		tags = new HashMap<Long,AbstractNode> (100000);
		places = new HashMap<Long,AbstractNode> (10000);
		forums = new HashMap<Long,AbstractNode> (10000);

		commentCreator = new HashMapLong(4999999);
		placeOrg = new HashMapLong(10007);
		placeLocatedAtPlace = new HashMapLong(10007);
		namePlaces = new HashMap<String,String> (10000);
		
		edges = new HashMap<Edge,Edge> (500000);
		edgesTagsForums = new HashMap<Edge,Edge> (1000000);
	}
	
	// this method is used by DataLoader.loadCommentReplyTo() 
	// to quickly find whether two persons know e/o
	public 
	Edge findUndirectedEdge(AbstractNode n1, AbstractNode n2, RelTypes relType) 
			throws NotFoundException {
		AbstractNode out = n1.getId() < n2.getId() ? n1 : n2;
		AbstractNode in = n1.getId() < n2.getId() ? n2 : n1;
		Edge e = new Edge(out, in, EdgeTypes.UNDIRECTED, relType);
		if (edges.containsKey(e)) return edges.get(e);
		throw new NotFoundException();
	}
	
	public void clearCommentCreator() {
		commentCreator = null;
	}
	 
	public void addCommentCreator(long commentID, long personID) {
		commentCreator.put(commentID, personID);		
	}
	public boolean commentHasCreator(long id) {
		return commentCreator.containsKey(id);
	}
	
	public Person getCommentCreator(long id) {
		return persons.get(commentCreator.get(id));
	}
	
	public boolean containsPerson(long id) {
		return persons.containsKey(id);
	}
	
	public Person addPerson(long id) {
		if (!containsPerson(id)) {
			Person person = new Person(id);
			persons.put(id, person);
			return person;
		} else {
			return persons.get(id);
		}
	}
	
	public Person addPerson(long id, Date birthday) {
		if (!containsPerson(id)) {
			Person person = new Person(id, birthday.getTime());
			persons.put(id, person);
			return person;
		} else {
			Person person = persons.get(id);
			person.setBirthday(birthday.getTime());
			return person;
		}
	}
	
	public Person getPerson(long id) {
		return persons.get(id);
	}
	
	public Collection<Long> getAllPersons() {
		return persons.keySet();
	}
	
	public void addReply(long replyID, long repliedToID) {
		Person creatorReply = getCommentCreator(replyID);
		Person creatorRepliedTo = getCommentCreator(repliedToID);
		
		Edge edge;
		try {
			edge = Database.INSTANCE.findUndirectedEdge(creatorReply, 
								                        creatorRepliedTo, 
											            RelTypes.KNOWS);
		} catch (NotFoundException e) {
			// no point in keeping this reply, creators must know e/other
			return;
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
	
	public void addKnowsRelationship(long person1ID, long person2ID) {
		Person person1 = addPerson(Math.min(person1ID, person2ID));
		Person person2 = addPerson(Math.max(person1ID, person2ID));
		Edge edge = new Edge(person1, 
						     person2, 
				             EdgeTypes.UNDIRECTED, 
				             RelTypes.KNOWS);			
		
		// person_knows_person has both directed edges, we only need one
		if (edges.containsKey(edge)) return;
		
		person1.addKnowsEdge(edge);
		person2.addKnowsEdge(edge);
		edge.setProperty("repOut", 0);
		edge.setProperty("repIn", 0);
		edges.put(edge, edge);
	}

	public AbstractNode addTag(long id) {
		if (!tags.containsKey(id)) {
			Node tag = new Node(id);
			tags.put(id, tag);
			return tag;
		} else {
			Node tag = (Node) tags.get(id);
			return tag;			
		}
	}
	
	public AbstractNode addTag(long id, String name) {
		if (!tags.containsKey(id)) {
			Node tag = new Node(id);
			tag.setProperty("name", name);
			tags.put(id, tag);
			return tag;
		} else {
			Node tag = (Node) tags.get(id);
			tag.setProperty("name", name);
			return tag;			
		}
	}
	
	public AbstractNode getTag(long id) {
		return tags.get(id);
	}
	
	public String getTagName(long id) throws NotFoundException {
		return (String) ((Node) tags.get(id)).getPropertyValue("name"); 
	}
	
	public Collection<Long> getAllTags() {
		return tags.keySet();
	}
	
	public void addInterestRelationship(long personID, long tagID) {
		Person person = addPerson(personID);
		Node tag = (Node) addTag(tagID);
		Edge edge = 
			tag.createEdge(person, EdgeTypes.DIRECTED, RelTypes.INTERESTED);
		person.addInterestEdge(edge);
		edges.put(edge, edge);
	}
	
	public AbstractNode addPlace(long id) {
		if (!places.containsKey(id)) {
			Node place = new Node(id);
			places.put(id, place);
			return place;
		} else {
			Node place = (Node) places.get(id);
			return place;	
		}
	}
	
	public AbstractNode addPlaceNamed(String name, long id) {
		if (!places.containsKey(id)) {
			Node place = new Node(id);
			places.put(id, place);
			if (!namePlaces.containsKey(name))
				namePlaces.put(name, String.valueOf(id));
			else
				namePlaces.put(name, namePlaces.get(name) + " " + id);
			return place;
		} else {
			Node place = (Node) places.get(id);
			if (!namePlaces.containsKey(name)) 
				namePlaces.put(name, String.valueOf(id));
			else
				namePlaces.put(name, namePlaces.get(name) + " " + id);
			return place;	
		}
	}
	
	public String getPlacesNamed(String name) {
		return namePlaces.get(name);
	}
	
	public void addPlaceOrg(long orgID, long placeID) {
		placeOrg.put(orgID, placeID);
	}

	public long getPlaceOrg(long orgID) {
		return placeOrg.get(orgID);
	}
	
	public boolean containsPlaceOrg(long id) {
		return placeOrg.containsKey(id);
	}
	
	public void addPersonLocatedRelationship(long personID, long placeID) {
		Node place = (Node) addPlace(placeID);
		Person person = getPerson(personID);
		Edge edge = 
			new Edge(place, person, EdgeTypes.DIRECTED, RelTypes.LOCATEDAT);
		person.addLocationEdge(edge);
		edges.put(edge, edge);
	}
	
	public void addPlaceLocatedRelationship(long place1ID, long place2ID) {
		placeLocatedAtPlace.put(place1ID, place2ID);		
	}
	
	public Long getPlaceLocation(long id) {
		long place = placeLocatedAtPlace.get(id);
		return place == -1? null : place;
	}
	
	public void addForumTagRelationship(long forumID, long tagID) {
		if (!forums.containsKey(forumID))
			forums.put(forumID, new Node(forumID));
		Node forum = (Node) forums.get(forumID);
		Node tag = (Node) tags.get(tagID);
		forum.createEdge(tag, EdgeTypes.DIRECTED, RelTypes.FORUMTAG);		
	}
	
	public boolean containsForum(long forumID) {
		return forums.containsKey(forumID);
	}
	
	public void addInterestAllForumTags(long personID, long forumID) {
		Node forum = (Node) forums.get(forumID);
		if (forum == null) 
			return;
		AbstractNode person = persons.get(personID);
		for (AbstractEdge edge : forum.getIncident()) {
			AbstractNode tag = edge.getIn();
			Edge e = new Edge(person, 
							  tag, 
							  EdgeTypes.DIRECTED, 
							  RelTypes.MEMBERFORUMTAG);
			if (edgesTagsForums.containsKey(e)) 
				continue;
			((Node) tag).addEdgeOther(e);
			edgesTagsForums.put(e, e);				
		}
	}
}
