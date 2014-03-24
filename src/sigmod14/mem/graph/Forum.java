package sigmod14.mem.graph;

import java.util.LinkedList;

public class Forum extends Node {
	LinkedList<Long> tags;
	
	public Forum(long id) {
		super(id);
		tags = new LinkedList<Long> ();
	}

	public void addTagEdge(long tagID) {
		tags.add(tagID);
	}
	
	public LinkedList<Long> getTags() {
		return tags;
	}
}
