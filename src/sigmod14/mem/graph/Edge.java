package sigmod14.mem.graph;


public class Edge {
	protected Node out;
	protected Node in;
	
	public Edge(Node in, Node out) {
		this.in = in;
		this.out = out;
	}
		
	public Node getOut() {
		return out;
	}

	public Node getIn() {
		return in;
	}
	
	public Node getOtherNode(Node node) {
		return this.getOut().equals(node) ? getIn() : getOut();
	}
	
	public boolean equals(Object o) {
		Edge other = (Edge) o;
		return in.equals(other.in) && out.equals(other.out);
	}
	
	public int hashCode() {
		return (int) (out.getId() + 37*in.getId());
	}
}
