package sigmod14.mem;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import sigmod14.mem.graph.Edge;
import sigmod14.mem.graph.Node;
import sigmod14.mem.graph.KnowsEdge;
import sigmod14.mem.graph.Forum;
import sigmod14.mem.graph.NotFoundException;
import sigmod14.mem.graph.Person;
import sigmod14.mem.graph.Tag;

public class Database implements DB {
	public static final Database INSTANCE = new Database();
	
	public DB instance() {
		return INSTANCE;
	}
	
	public static enum EdgeTypes {
		DIRECTED,
		UNDIRECTED,
	}
		
	// data storage
	private HashMap<Long,Person> persons;
	private HashMap<Long,Tag> tags;
	private HashMap<Long,Node> places;
	private HashMapLong commentCreator;
	private HashMapLong placeOrg;
	private HashMapLong placeLocatedAtPlace;
	private HashMap<String,String> namePlaces;
	private HashMap<Long,Forum> forums;

	private HashMap<Edge,Edge> edges;
	private HashMap<Edge,Edge> edgesTagsForums;
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new HashMap<Long,Person> (100000);
		tags = new HashMap<Long,Tag> (100000);
		places = new HashMap<Long,Node> (10000);
		forums = new HashMap<Long,Forum> (10000);

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
	KnowsEdge findUndirectedEdge(Node n1, Node n2) 
			throws NotFoundException {
		Node out = n1.getId() < n2.getId() ? n1 : n2;
		Node in = n1.getId() < n2.getId() ? n2 : n1;
		KnowsEdge e = new KnowsEdge(out, in);
		if (edges.containsKey(e)) return (KnowsEdge) edges.get(e);
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
		
		KnowsEdge edge;
		try {
			edge = Database.INSTANCE.findUndirectedEdge(creatorReply, 
								                        creatorRepliedTo);
		} catch (NotFoundException e) {
			// no point in keeping this reply, creators must know e/other
			return;
		}
		if (edge.getOut().equals(creatorReply))
			edge.incRepOut();
		else
			edge.incRepIn();
	}
	
	public void addKnowsRelationship(long person1ID, long person2ID) {
		Person person1 = addPerson(Math.min(person1ID, person2ID));
		Person person2 = addPerson(Math.max(person1ID, person2ID));
		KnowsEdge edge = new KnowsEdge(person1, person2);			
		
		// person_knows_person has both directed edges, we only need one
		if (edges.containsKey(edge)) return;
		
		person1.addKnowsEdge(edge);
		person2.addKnowsEdge(edge);
		edges.put(edge, edge);
	}

	public Tag addTag(long id) {
		if (!tags.containsKey(id)) {
			Tag tag = new Tag(id);
			tags.put(id, tag);
			return tag;
		} else {
			Tag tag = (Tag) tags.get(id);
			return tag;			
		}
	}
	
	public Tag addTag(long id, String name) {
		if (!tags.containsKey(id)) {
			Tag tag = new Tag(id, name);
			tags.put(id, tag);
			return tag;
		} else {
			Tag tag = (Tag) tags.get(id);
			tag.setName(name);
			return tag;			
		}
	}
	
	public Tag getTag(long id) {
		return tags.get(id);
	}
	
	public String getTagName(long id) throws NotFoundException {		
		return tags.get(id).getName(); 
	}
	
	public Collection<Long> getAllTags() {
		return tags.keySet();
	}
	
	public void addInterestRelationship(long personID, long tagID) {
		Person person = addPerson(personID);
		Tag tag = addTag(tagID);
		Edge edge = new Edge(person, tag);
		tag.addInterestedEdge(edge);
		person.addInterestEdge(edge);
//		edgesInterests.put(edge, edge);
	}
	
	public Node addPlace(long id) {
		if (!places.containsKey(id)) {
			Node place = new Node(id);
			places.put(id, place);
			return place;
		} else {
			Node place = places.get(id);
			return place;	
		}
	}
	
	public Node addPlaceNamed(String name, long id) {
		if (!places.containsKey(id)) {
			Node place = new Node(id);
			places.put(id, place);
			if (!namePlaces.containsKey(name))
				namePlaces.put(name, String.valueOf(id));
			else
				namePlaces.put(name, namePlaces.get(name) + " " + id);
			return place;
		} else {
			Node place = places.get(id);
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
		Node place = addPlace(placeID);
		Person person = getPerson(personID);
		Edge edge = new Edge(place, person);
		person.addLocationEdge(edge);
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
			forums.put(forumID, new Forum(forumID));
		Forum forum = forums.get(forumID);
		forum.addTagEdge(tagID);		
	}
	
	public boolean containsForum(long forumID) {
		return forums.containsKey(forumID);
	}
	
	public void addInterestAllForumTags(long personID, long forumID) {
		Forum forum = forums.get(forumID);
		if (forum == null) 
			return;
		Node person = persons.get(personID);
		for (Long tagID : forum.getTags()) {
			Tag tag = tags.get(tagID);
			Edge e = new Edge(person, tag);
			if (edgesTagsForums.containsKey(e)) 
				continue;
			tag.addMemberForumEdge(e);
			edgesTagsForums.put(e, e);				
		}
	}
}
