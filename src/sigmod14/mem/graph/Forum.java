package sigmod14.mem.graph;

import java.util.LinkedList;

public class Forum extends AbstractNode {
	LinkedList<AbstractEdge> tags;
	
	public Forum(long id) {
		super(id);
		tags = new LinkedList<AbstractEdge> ();
	}

	public void addTagEdge(AbstractEdge edge) {
		tags.add(edge);
	}
	
	public LinkedList<AbstractEdge> getTags() {
		return tags;
	}
}
