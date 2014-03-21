package sigmod14.mem;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

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
	private HashMap<Long,Node> persons;
	private HashMap<Long,Node> tags;
	private HashMap<Long,Node> places;
	private HashMapLong commentCreator;
	private HashMapLong placeOrg;
	private HashMapLong placeLocatedAtPlace;
	private HashMap<Edge,Edge> edges;
	private HashMap<String,String> namePlaces;
	private HashMap<Long,Node> forums;
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new HashMap<Long,Node> (100000);
		tags = new HashMap<Long,Node> (100000);
		places = new HashMap<Long,Node> (10000);
		forums = new HashMap<Long,Node> (10000);

		commentCreator = new HashMapLong(4999999);
		placeOrg = new HashMapLong(10007);
		placeLocatedAtPlace = new HashMapLong(10007);
		namePlaces = new HashMap<String,String> (10000);
		
		edges = new HashMap<Edge,Edge> (500000);
	}
	
	// this method is used by DataLoader.loadCommentReplyTo() 
	// to quickly find whether two persons know e/o
	public Edge findUndirectedEdge(Node n1, Node n2, RelTypes relType) 
			throws NotFoundException {
		Node out = n1.getId() < n2.getId() ? n1 : n2;
		Node in = n1.getId() < n2.getId() ? n2 : n1;
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
	
	public Node getCommentCreator(long id) {
		return persons.get(commentCreator.get(id));
	}
	
	public boolean containsPerson(long id) {
		return persons.containsKey(id);
	}
	
	public Node addPerson(long id) {
		if (!containsPerson(id)) {
			Node person = new Node(id);
			persons.put(id, person);
			return person;
		} else {
			return persons.get(id);
		}
	}
	
	public Node addPerson(long id, Date birthday) {
		if (!containsPerson(id)) {
			Node person = new Node(id);
			person.setProperty("birthday", birthday);
			persons.put(id, person);
			return person;
		} else {
			Node person = persons.get(id);
			person.setProperty("birthday", birthday);
			return person;
		}
	}
	
	public Node getPerson(long id) {
		return persons.get(id);
	}
	
	public Collection<Long> getAllPersons() {
		return persons.keySet();
	}
	
	public void addKnowsRelationship(long person1ID, long person2ID) {
		Node person1 = addPerson(Math.min(person1ID, person2ID));
		Node person2 = addPerson(Math.max(person1ID, person2ID));
		Edge edge = new Edge(person1, 
						     person2, 
				             EdgeTypes.UNDIRECTED, 
				             RelTypes.KNOWS);			
		
		// person_knows_person has both directed edges, we only need one
		if (edges.containsKey(edge)) return;
		
		person1.addEdge(edge);
		person2.addEdge(edge);
		edge.setProperty("repOut", 0);
		edge.setProperty("repIn", 0);
		edges.put(edge, edge);
	}

	public Node addTag(long id) {
		if (!tags.containsKey(id)) {
			Node tag = new Node(id);
			tags.put(id, tag);
			return tag;
		} else {
			Node tag = tags.get(id);
			return tag;			
		}
	}
	
	public Node addTag(long id, String name) {
		if (!tags.containsKey(id)) {
			Node tag = new Node(id);
			tag.setProperty("name", name);
			tags.put(id, tag);
			return tag;
		} else {
			Node tag = tags.get(id);
			tag.setProperty("name", name);
			return tag;			
		}
	}
	
	public Node getTag(long id) {
		return tags.get(id);
	}
	
	public String getTagName(long id) throws NotFoundException {
		return (String) tags.get(id).getPropertyValue("name"); 
	}
	
	public Collection<Long> getAllTags() {
		return tags.keySet();
	}
	
	public void addInterestRelationship(long personID, long tagID) {
		Node person = addPerson(personID);
		Node tag = addTag(tagID);
		Edge edge = 
			tag.createEdge(person, EdgeTypes.DIRECTED, RelTypes.INTERESTED);
		person.addEdge(edge);
		edges.put(edge, edge);
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
		Node person = getPerson(personID);
		Edge edge = 
			person.createEdge(place, EdgeTypes.DIRECTED, RelTypes.LOCATEDAT);
		edges.put(edge, edge);
	}
	
	public void addPlaceLocatedRelationship(long place1ID, long place2ID) {
		placeLocatedAtPlace.put(place1ID, place2ID);		
	}
	
	public Long getPlaceLocation(long id) {
		return placeLocatedAtPlace.get(id);
	}
	
	public void addForumTagRelationship(long forumID, long tagID) {
		if (!forums.containsKey(forumID))
			forums.put(forumID, new Node(forumID));
		Node forum = forums.get(forumID);
		Node tag = tags.get(tagID);
		forum.createEdge(tag, EdgeTypes.DIRECTED, RelTypes.FORUMTAG);		
	}
	
	public boolean containsForum(long forumID) {
		return forums.containsKey(forumID);
	}
	
	public void addInterestAllForumTags(long personID, long forumID) {
		Node forum = forums.get(forumID);
		if (forum == null) 
			return;
		Node person = persons.get(personID);
		for (Edge edge : forum.getIncident()) {
			Node tag = edge.getIn();
			Edge e = new Edge(person, 
							  tag, 
							  EdgeTypes.DIRECTED, 
							  RelTypes.MEMBERFORUMTAG);
			if (edges.containsKey(e)) 
				continue;
			tag.addEdgeOther(e);
			edges.put(e, e);				
		}
	}
}
