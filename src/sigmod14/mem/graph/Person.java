package sigmod14.mem.graph;

import java.util.LinkedList;


public class Person extends Node {
	private long birthday;
	private LinkedList<Edge> knows;
	private LinkedList<Edge> interests;
	private LinkedList<Edge> locations;
	
	public Person(long id) {
		this(id, -123456789012345678L);
	}

	public Person(long id, long birthday) {
		super(id);
		this.birthday = birthday;
		knows = new LinkedList<Edge>();
		interests = new LinkedList<Edge>();
		locations = new LinkedList<Edge>();
	}
	
	public long getBirthday() throws NotFoundException {
		if (birthday == -123456789012345678L) throw new NotFoundException();
		return birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}

	public LinkedList<Edge> getKnows() {
		return knows;
	}
	
	public LinkedList<Edge> getInterests() {
		return interests;
	}

	public LinkedList<Edge> getLocations() {
		return locations;
	}
	
	public void addKnowsEdge(Edge edge) {
		knows.add(edge);
	}
	
	public void addInterestEdge(Edge edge) {
		interests.add(edge);
	}
	
	public void addLocationEdge(Edge edge) {
		locations.add(edge);
	}
}
