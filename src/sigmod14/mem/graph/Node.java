package sigmod14.mem.graph;


public class Node {
	protected long id;
	
	public Node(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public boolean equals(Object o) {
		return id == ((Node) o).id;
	}
	
	public int hashCode() {
		return (int) id;
	}
	
	public String toString() {
		return String.valueOf(id);
	}
}
