package sigmod14.mem.graph;

import java.util.HashSet;

public class Tag extends Node {
	private String name;
	HashSet<Person> interested;
	HashSet<Person> membersForums;
	
	public Tag(int id) {
		super(id);
		name = null;
		interested = new HashSet<Person>();
		membersForums = new HashSet<Person>();
	}
	
	public Tag(int id, String name) {
		this(id);
		this.name = name;
	}
	
	public void addInterestedPerson(Person person) {
		interested.add(person);
	}

	public void addMemberForumEdge(Person person) {
		membersForums.add(person);
	}
	
	public String getName() throws NotFoundException {
		if (name == null) throw new NotFoundException();
		return name;
	}
	
	public HashSet<Person> getInterested() {
		return interested;
	}
	
	public HashSet<Person> getMembersForums() {
		return membersForums;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
