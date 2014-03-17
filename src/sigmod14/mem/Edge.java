package sigmod14.mem;

import java.util.HashMap;

import sigmod14.mem.Database.EdgeTypes;
import sigmod14.mem.Database.RelTypes;

public class Edge {
	private Node out;
	private Node in;
	private EdgeTypes edgeType;
	private RelTypes relType;
	private HashMap<String,Object> properties;
	
	public Edge(Node in, Node out, 
				EdgeTypes edgeType, RelTypes relType) {
		this.in = in;
		this.out = out;
		this.edgeType = edgeType;
		this.relType = relType;
		this.properties = new HashMap<String,Object>();
	}
	
	public EdgeTypes getEdgeType() {
		return edgeType;
	}

	public RelTypes getRelType() {
		return relType;
	}

	public Object getPropertyValue(String property) throws NotFoundException {
		if (!properties.containsKey(property)) throw new NotFoundException(); 
		return properties.get(property);
	}

	public void setProperty(String property, Object value) {
		properties.put(property, value);
	}

	public Database.NodeTypes getTypeOut() {
		return out.getType();
	}

	public Database.NodeTypes getTypeIn() {
		return in.getType();
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
		return in.equals(other.in) && out.equals(other.out) 
				&& edgeType.equals(other.getEdgeType())
				&& relType.equals(other.getRelType());
	}
	
	public int hashCode() {
		return relType.ordinal() + 37*(edgeType.ordinal() 
										+ 37*(in.hashCode() 
												+ 37*out.hashCode()));
	}
	
}
