package sigmod14.mem.graph;

import java.util.HashMap;
import java.util.LinkedList;


public class Person extends Node {
	private long birthday;
	private HashMap<Integer,Integer> knows;
	private LinkedList<Tag> interests;
	private LinkedList<Node> locations;
	
	public Person(int id) {
		this(id, -123456789012345678L);
	}

	public Person(int id, long birthday) {
		super(id);
		this.birthday = birthday;		
		knows = new HashMap<Integer, Integer>();
		interests = new LinkedList<Tag>();
		locations = new LinkedList<Node>();
	}
	
	public long getBirthday() throws NotFoundException {
		if (birthday == -123456789012345678L) throw new NotFoundException();
		return birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}

	public HashMap<Integer,Integer> getKnows() {
		return knows;
	}
	
	public LinkedList<Tag> getInterests() {
		return interests;
	}

	public LinkedList<Node> getLocations() {
		return locations;
	}
	
	public void addKnows(int id) {
		knows.put(id, 0);
	}
	
	public void addReply(int id) {
		knows.put(id, knows.get(id) + 1);
	}
	
	public int getReplies(int id) {
		return knows.get(id);
	}
	
	public void addInterestEdge(Tag tag) {
		interests.add(tag);
	}
	
	public void addLocationEdge(Node location) {
		locations.add(location);
	}
	
	public boolean knows(int id) {
		return knows.containsKey(id);
	}
}
