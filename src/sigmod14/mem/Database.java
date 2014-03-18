package sigmod14.mem;

import java.util.HashMap;

public class Database {
	public static final Database INSTANCE = new Database();
	
	public static enum NodeTypes {
		PERSON,
		TAG,
		FORUM,
		PLACE,
	}
	
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
	private HashMap<Long,Long> commentCreator;
	private HashMap<Long,Long> orgPlace;
	private HashMap<Long,Long> placeLocatedAtPlace;
	private HashMap<Edge,Edge> edges;
	private HashMap<String,Long> namePlaces;
	private HashMap<Long,Node> forums;
	
	// private constructor to instantiate public INSTANCE
	private Database() {
		persons = new HashMap<Long,Node> (100000);
		tags = new HashMap<Long,Node> (100000);
		places = new HashMap<Long,Node> (10000);
		forums = new HashMap<Long,Node> (10000);

		commentCreator = new HashMap<Long,Long> (10000000);
		orgPlace = new HashMap<Long,Long> (10000);
		placeLocatedAtPlace = new HashMap<Long,Long> (10000);
		namePlaces = new HashMap<String,Long> (10000);
		
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
	

	public HashMap<Long, Node> getPersons() {
		return persons;
	}

	public HashMap<Long, Node> getTags() {
		return tags;
	}

	public HashMap<Long, Node> getPlaces() {
		return places;
	}

	public HashMap<Long, Long> getCommentCreator() {
		return commentCreator;
	}

	public HashMap<Long, Long> getOrgPlace() {
		return orgPlace;
	}

	public HashMap<Long, Long> getPlaceLocatedAtPlace() {
		return placeLocatedAtPlace;
	}

	public HashMap<Edge, Edge> getEdges() {
		return edges;
	}
	
	public HashMap<String,Long> getNamePlaces() {
		return namePlaces;
	}
	
	public HashMap<Long,Node> getForums() {
		return forums;
	}
	
	public void clearCommentCreator() {
		commentCreator = null;
	}
}
