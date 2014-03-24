package sigmod14.mem.graph;

import java.util.HashMap;

import sigmod14.mem.Database.EdgeTypes;

public class Edge extends AbstractEdge {
	private EdgeTypes edgeType;
	private HashMap<String,Object> properties;
	
	public Edge(AbstractNode in, AbstractNode out,
				EdgeTypes edgeType) {
		super(in, out);
		this.edgeType = edgeType;
	}
	
	public EdgeTypes getEdgeType() {
		return edgeType;
	}

	public Object getPropertyValue(String property) throws NotFoundException {
		if (!properties.containsKey(property)) throw new NotFoundException(); 
		return properties.get(property);
	}

	public void setProperty(String property, Object value) {
		if (properties == null) properties = new HashMap<String, Object>();
		properties.put(property, value);
	}
	
	public boolean equals(Object o) {
		Edge other = (Edge) o;
		return in.equals(other.in) && out.equals(other.out) 
				&& edgeType.equals(other.getEdgeType());
	}
	
	public int hashCode() {
		return edgeType.ordinal() + 37*(in.hashCode() + 37*out.hashCode());
	}
}
