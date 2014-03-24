package sigmod14.mem.graph;

public class KnowsEdge extends Edge {
	short repIn;
	short repOut;
	
	public KnowsEdge(Node in, Node out) {
		super(in, out);
		repIn = 0;
		repOut = 0;
	}
	
	public void incRepOut() {
		repOut++;
	}

	public void incRepIn() {
		repIn++;
	}
	
	public short getRepOut() {
		return repOut;
	}
	
	public short getRepIn() {
		return repIn;
	}
}
