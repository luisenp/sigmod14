package sigmod14.mem.graph;


public class AbstractEdge {
	protected AbstractNode out;
	protected AbstractNode in;
	
	public AbstractEdge(AbstractNode in, AbstractNode out) {
		this.in = in;
		this.out = out;
	}
		
	public AbstractNode getOut() {
		return out;
	}

	public AbstractNode getIn() {
		return in;
	}
	
	public AbstractNode getOtherNode(AbstractNode node) {
		return this.getOut().equals(node) ? getIn() : getOut();
	}
}
