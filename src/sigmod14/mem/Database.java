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
		
	// data storage
	Person persons[];
	private HashMap<Integer,Tag> tags;
	private HashMap<Integer,Node> places;
	private HashMap<Integer,Forum> forums;
	
	private HashMapLong commentCreator;
	private HashMapLong placeOrg;
	private HashMapLong placeLocatedAtPlace;
	private HashMap<String,String> namePlaces;

	private HashMap<Edge,Edge> knowsEdges;
	
	private int numPersons;
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new Person[100001];
		tags = new HashMap<Integer,Tag> (10007);
		places = new HashMap<Integer,Node> (10007);
		forums = new HashMap<Integer,Forum> (1000000);

		commentCreator = new HashMapLong(50000017);
		placeOrg = new HashMapLong(10007);
		placeLocatedAtPlace = new HashMapLong(10007);
		namePlaces = new HashMap<String,String> (10007);
		
		knowsEdges = new HashMap<Edge,Edge> (1000003);
	}
	
	// this method is used by DataLoader.loadCommentReplyTo() 
	// to quickly find whether two persons know e/o
	public 
	KnowsEdge findUndirectedEdge(Node n1, Node n2) 
			throws NotFoundException {
		Node out = n1.getId() < n2.getId() ? n1 : n2;
		Node in = n1.getId() < n2.getId() ? n2 : n1;
		KnowsEdge e = new KnowsEdge(out, in);
		if (knowsEdges.containsKey(e)) return (KnowsEdge) knowsEdges.get(e);
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
		return persons[(int) commentCreator.get(id)];
	}
	
	public boolean containsPerson(int id) {
		return persons[id] != null;
	}
	
	public Person addPerson(int id) {
		if (!containsPerson(id)) {
			Person person = new Person(id);
			persons[id] = person;
			numPersons++;
			return person;
		} else {
			return persons[id];
		}
	}
	
	public Person addPerson(int id, Date birthday) {
		Person person = addPerson(id);
		person.setBirthday(birthday.getTime());
		return person;
	}
	
	public Person getPerson(int id) {
		return persons[id];
	}
	
	public Person[] getAllPersons() {
		return persons;
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
	
	public void addKnowsRelationship(int person1ID, int person2ID) {
		Person person1 = addPerson(Math.min(person1ID, person2ID));
		Person person2 = addPerson(Math.max(person1ID, person2ID));
		KnowsEdge edge = new KnowsEdge(person1, person2);			
		
		// person_knows_person has both directed edges, we only need one
		if (knowsEdges.containsKey(edge)) return;
		
		person1.addKnowsEdge(edge);
		person2.addKnowsEdge(edge);
		knowsEdges.put(edge, edge);
	}

	public Tag addTag(int id) {
		if (!tags.containsKey(id)) {
			Tag tag = new Tag(id);
			tags.put(id, tag);
			return tag;
		} else {
			Tag tag = (Tag) tags.get(id);
			return tag;			
		}
	}
	
	public Tag addTag(int id, String name) {
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
	
	public Tag getTag(int id) {
		return tags.get(id);
	}
	
	public String getTagName(int id) throws NotFoundException {		
		return tags.get(id).getName(); 
	}
	
	public Collection<Integer> getAllTags() {
		return tags.keySet();
	}
	
	public void addInterestRelationship(int personID, int tagID) {
		Person person = addPerson(personID);
		Tag tag = addTag(tagID);
		tag.addInterestedPerson(person);
		person.addInterestEdge(tag);
	}
	
	public Node addPlace(int id) {
		if (!places.containsKey(id)) {
			Node place = new Node(id);
			places.put(id, place);
			return place;
		} else {
			Node place = places.get(id);
			return place;	
		}
	}
	
	public Node addPlaceNamed(String name, int id) {
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
	
	public void addPersonLocatedRelationship(int personID, int placeID) {
		Node place = addPlace(placeID);
		Person person = getPerson(personID);
		person.addLocationEdge(place);
	}
	
	public void addPlaceLocatedRelationship(long place1ID, long place2ID) {
		placeLocatedAtPlace.put(place1ID, place2ID);		
	}
	
	public Long getPlaceLocation(long id) {
		long place = placeLocatedAtPlace.get(id);
		return place == -1? null : place;
	}
	
	public void addForumTagRelationship(int forumID, int tagID) {
		if (!forums.containsKey(forumID))
			forums.put(forumID, new Forum(forumID));
		Forum forum = forums.get(forumID);
		forum.addTagEdge(tagID);		
	}
	
	public boolean containsForum(int forumID) {
		return forums.containsKey(forumID);
	}
	
	public void addInterestAllForumTags(int personID, int forumID) {
		Forum forum = forums.get(forumID);
		if (forum == null) 
			return;
		Person person = persons[personID];
		for (Integer tagID : forum.getTags()) {
			Tag tag = tags.get(tagID);
			tag.addMemberForumEdge(person);
		}
	}
	
	public int getNumPersons() {
		return numPersons;
	}
	
	public void printDatabaseInfo() {
		System.err.println("Persons : " + numPersons);
		System.err.println("Tags : " + tags.size());
		System.err.println("Places : " + places.size());
		System.err.println("Forums : " + forums.size());
		System.err.println("Knows : " + knowsEdges.size());
	}
}
