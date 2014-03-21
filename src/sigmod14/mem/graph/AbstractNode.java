package sigmod14.mem.graph;


public abstract class AbstractNode {
	private long id;
	
	public AbstractNode(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public boolean equals(AbstractNode other) {
		return id == other.id;
	}
	
	public int hashCode() {
		return (int) id;
	}
	
	public String toString() {
		return String.valueOf(id);
	}
}
