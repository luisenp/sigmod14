package sigmod14;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

public class Q1Cost implements CostEvaluator<Double> {
	private int x;
	Q1Cost(int x) {
		this.x = x;
	}
	public Double getCost(Relationship relationship, Direction direction) {
		Relationship otherRelationship = null;
		Iterable<Relationship> candidates;
		if(direction == Direction.OUTGOING) {
			candidates = relationship.getEndNode().getRelationships(Direction.OUTGOING);
			for (Relationship r: candidates) {
				if(r.getEndNode() == relationship.getStartNode()) {
					otherRelationship = r;
					break;
				}
			}
		}
		else {
			candidates = relationship.getStartNode().getRelationships(Direction.INCOMING);
			for (Relationship r: candidates) {
				if(r.getStartNode() == relationship.getEndNode()) {
					otherRelationship = r;
					break;
				}
			}
		}
		if((Integer) relationship.getProperty("replies") >= this.x && 
				(Integer) otherRelationship.getProperty("replies") >= this.x) {
			return 1.0;
		}
		return Double.MAX_VALUE;
	}
}
