package sigmod14.mem;

import java.util.HashMap;
import java.util.LinkedList;

import sigmod14.mem.Database.EdgeTypes;
import sigmod14.mem.Database.NodeTypes;
import sigmod14.mem.Database.RelTypes;

public class Node {
	private long id;
	private NodeTypes type;
	private HashMap<String,Object> properties;
	private LinkedList<Edge> incident;
	
	public Node(long id, NodeTypes type) {
		this.id = id;
		this.type = type;
		incident = new LinkedList<Edge> ();
		properties = new HashMap<String,Object> ();
	}
	
	// Creates an edge such that edge.out = this and edge.in = other
	public 
	Edge createEdge(Node other, EdgeTypes edgeType, RelTypes relType) {
		Edge e = new Edge(other, this, edgeType, relType);
		incident.add(e);
		return e;
	}
	
	public void addEdge(Edge edge) {
		incident.add(edge);
	}
	
	public NodeTypes getType() {
		return type;
	}
	
	public Object getPropertyValue(String property) throws NotFoundException {
		if (!properties.containsKey(property)) throw new NotFoundException(); 
		return properties.get(property);
	}

	public void setProperty(String property, Object value) {
		properties.put(property, value);
	}
	
	public long getId() {
		return id;
	}

	public LinkedList<Edge> getIncident() {
		return incident;
	}
	
	public boolean equals(Object o) {
		Node other = (Node) o;
		return type == other.type && id == other.id;
	}
	
	public int hashCode() {
		return type.ordinal() + 37*(int)id;
	}
	
	public 
	Edge findEdgeTo(Node other, RelTypes type) throws NotFoundException {
		for (Edge e : incident) {
			if (e.getOut().equals(other)) return e;
		}
		throw new NotFoundException();
	}
	
	public String toString() {
		return "" + type + ": " + id;
	}
	
}
