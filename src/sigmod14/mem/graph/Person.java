package sigmod14.mem.graph;

import java.util.LinkedList;


public class Person extends Node {
	private long birthday;
	private LinkedList<KnowsEdge> knows;
	private LinkedList<Tag> interests;
	private LinkedList<Node> locations;
	
	public Person(long id) {
		this(id, -123456789012345678L);
	}

	public Person(long id, long birthday) {
		super(id);
		this.birthday = birthday;
		knows = new LinkedList<KnowsEdge>();
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

	public LinkedList<KnowsEdge> getKnows() {
		return knows;
	}
	
	public LinkedList<Tag> getInterests() {
		return interests;
	}

	public LinkedList<Node> getLocations() {
		return locations;
	}
	
	public void addKnowsEdge(KnowsEdge edge) {
		knows.add(edge);
	}
	
	public void addInterestEdge(Tag tag) {
		interests.add(tag);
	}
	
	public void addLocationEdge(Node location) {
		locations.add(location);
	}
}
