package sigmod14.mem.graph;


public class AbstractNode {
	protected long id;
	
	public AbstractNode(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public boolean equals(Object o) {
		return id == ((AbstractNode) o).id;
	}
	
	public int hashCode() {
		return (int) id;
	}
	
	public String toString() {
		return String.valueOf(id);
	}
}
