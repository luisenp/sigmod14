package sigmod14.mem.graph;

import java.util.HashMap;
import java.util.LinkedList;

import sigmod14.mem.Database.EdgeTypes;

public class Node extends AbstractNode {
	private HashMap<String,Object> properties;
	private LinkedList<AbstractEdge> incident;
	
	private LinkedList<AbstractEdge> incidentOther;
	
	public Node(long id) {
		super(id);
		incident = new LinkedList<AbstractEdge> ();
		incidentOther = new LinkedList<AbstractEdge> ();
		properties = new HashMap<String,Object> ();
	}
	
	// Creates an edge such that edge.out = this and edge.in = other
	public 
	Edge createEdge(AbstractNode other, EdgeTypes edgeType) {
		Edge e = new Edge(other, this, edgeType);
		incident.add(e);
		return e;
	}

	// Creates an edge such that edge.out = this and edge.in = other
	public 
	Edge createEdgeOther(Node other, EdgeTypes edgeType) {
		Edge e = new Edge(other, this, edgeType);
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

	public LinkedList<AbstractEdge> getIncident() {
		return incident;
	}

	public LinkedList<AbstractEdge> getIncidentOther() {
		return incidentOther;
	}
	
	public 
	AbstractEdge findEdgeTo(AbstractNode other) 
			throws NotFoundException {
		for (AbstractEdge e : incident) {
			if (e.getOut().equals(other)) return e;
		}
		throw new NotFoundException();
	}
}
