package sigmod14.mem.graph;

import java.util.HashMap;
import java.util.LinkedList;

import sigmod14.mem.Database.EdgeTypes;
import sigmod14.mem.Database.RelTypes;

public class Node extends AbstractNode {
	private HashMap<String,Object> properties;
	private LinkedList<Edge> incident;
	
	private LinkedList<Edge> incidentOther;
	
	public Node(long id) {
		super(id);
		incident = new LinkedList<Edge> ();
		incidentOther = new LinkedList<Edge> ();
		properties = new HashMap<String,Object> ();
	}
	
	// Creates an edge such that edge.out = this and edge.in = other
	public 
	Edge createEdge(Node other, EdgeTypes edgeType, RelTypes relType) {
		Edge e = new Edge(other, this, edgeType, relType);
		incident.add(e);
		return e;
	}

	// Creates an edge such that edge.out = this and edge.in = other
	public 
	Edge createEdgeOther(Node other, EdgeTypes edgeType, RelTypes relType) {
		Edge e = new Edge(other, this, edgeType, relType);
		incidentOther.add(e);
		return e;
	}
	
	public void addEdge(Edge edge) {
		incident.add(edge);
	}

	public void addEdgeOther(Edge edge) {
		incidentOther.add(edge);
	}
	
	public Object getPropertyValue(String property) throws NotFoundException {
		if (!properties.containsKey(property)) throw new NotFoundException(); 
		return properties.get(property);
	}

	public void setProperty(String property, Object value) {
		properties.put(property, value);
	}

	public LinkedList<Edge> getIncident() {
		return incident;
	}

	public LinkedList<Edge> getIncidentOther() {
		return incidentOther;
	}
	
	public 
	Edge findEdgeTo(Node other, RelTypes type) throws NotFoundException {
		for (Edge e : incident) {
			if (e.getOut().equals(other)) return e;
		}
		throw new NotFoundException();
	}
}
