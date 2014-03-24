package sigmod14.mem.graph;

import java.util.LinkedList;

public class Tag extends AbstractNode {
	private String name;
	LinkedList<AbstractEdge> interested;
	LinkedList<AbstractEdge> membersForums;
	
	public Tag(long id) {
		super(id);
		name = null;
		interested = new LinkedList<AbstractEdge>();
		membersForums = new LinkedList<AbstractEdge>();
	}
	
	public Tag(long id, String name) {
		this(id);
		this.name = name;
	}
	
	public void addInterested(AbstractEdge person) {
		interested.add(person);
	}

	public void addMemberForum(AbstractEdge person) {
		membersForums.add(person);
	}
	
	public String getName() throws NotFoundException {
		if (name == null) throw new NotFoundException();
		return name;
	}
	
	public LinkedList<AbstractEdge> getInterested() {
		return interested;
	}
	
	public LinkedList<AbstractEdge> getMembersForums() {
		return membersForums;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
