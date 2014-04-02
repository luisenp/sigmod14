package sigmod14.mem.graph;


public class Node {
	protected int id;
	
	public Node(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean equals(Object o) {
		return id == ((Node) o).id;
	}
	
	public int hashCode() {
		return id;
	}
	
	public String toString() {
		return String.valueOf(id);
	}
}
