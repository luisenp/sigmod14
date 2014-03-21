package sigmod14.mem.graph;

import java.util.LinkedList;


public class Person extends AbstractNode {
	private long birthday;
	private LinkedList<Edge> interests;
	private LinkedList<Edge> locations;
	private LinkedList<Edge> forumsTags;
		
	public Person(long id, long birthday) {
		super(id);
		this.birthday = birthday;
		interests = new LinkedList<Edge>();
		locations = new LinkedList<Edge>();
		forumsTags = new LinkedList<Edge>();
	}
	
	public long getBirthday() {
		return birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}
	
	public LinkedList<Edge> getInterests() {
		return interests;
	}

	public LinkedList<Edge> getLocations() {
		return locations;
	}

	public LinkedList<Edge> getForumTags() {
		return forumsTags;
	}
}
