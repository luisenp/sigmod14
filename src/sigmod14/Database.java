package sigmod14;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;

// going to put a bunch of common definitions/methods here for now.
public class Database {	
	public static enum RelTypes implements RelationshipType {
		PERSON;
	}
	
	public void createPersonNode(GraphDatabaseService graphDb) {
		
	}
}
