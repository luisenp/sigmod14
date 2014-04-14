package sigmod14.mem.graph;

import gnu.trove.list.linked.TIntLinkedList;

import java.util.LinkedList;

public class Forum extends Node {
//	LinkedList<Integer> tags;
	TIntLinkedList tags;
	
	public Forum(int id) {
		super(id);
//		tags = new LinkedList<Integer> ();
		tags = new TIntLinkedList();
	}

	public void addTagEdge(int tagID) {
		tags.add(tagID);
	}
	
//	public LinkedList<Integer> getTags() {
//		return tags;
//	}

	public TIntLinkedList getTags() {
		return tags;
	}
}
