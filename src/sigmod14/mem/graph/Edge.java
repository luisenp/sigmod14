package sigmod14.mem.graph;

import java.util.HashMap;

import sigmod14.mem.Database.EdgeTypes;
import sigmod14.mem.Database.RelTypes;

public class Edge extends AbstractEdge {
	private EdgeTypes edgeType;
	private RelTypes relType;
	private HashMap<String,Object> properties;
	
	public Edge(AbstractNode in, AbstractNode out,
				EdgeTypes edgeType, RelTypes relType) {
		super(in, out);
		this.edgeType = edgeType;
		this.relType = relType;
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
		if (properties == null) properties = new HashMap<String, Object>();
		properties.put(property, value);
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
