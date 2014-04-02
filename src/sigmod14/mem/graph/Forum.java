package sigmod14.mem.graph;

import java.util.LinkedList;

public class Forum extends Node {
	LinkedList<Integer> tags;
	
	public Forum(int id) {
		super(id);
		tags = new LinkedList<Integer> ();
	}

	public void addTagEdge(int tagID) {
		tags.add(tagID);
	}
	
	public LinkedList<Integer> getTags() {
		return tags;
	}
}
