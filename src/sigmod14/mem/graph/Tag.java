package sigmod14.mem.graph;

import java.util.LinkedList;

public class Tag extends Node {
	private String name;
	LinkedList<Edge> interested;
	LinkedList<Edge> membersForums;
	
	public Tag(long id) {
		super(id);
		name = null;
		interested = new LinkedList<Edge>();
		membersForums = new LinkedList<Edge>();
	}
	
	public Tag(long id, String name) {
		this(id);
		this.name = name;
	}
	
	public void addInterestedEdge(Edge person) {
		interested.add(person);
	}

	public void addMemberForumEdge(Edge person) {
		membersForums.add(person);
	}
	
	public String getName() throws NotFoundException {
		if (name == null) throw new NotFoundException();
		return name;
	}
	
	public LinkedList<Edge> getInterested() {
		return interested;
	}
	
	public LinkedList<Edge> getMembersForums() {
		return membersForums;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
