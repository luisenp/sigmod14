package sigmod14;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

// going to put a bunch of common definitions/methods here for now.
public class Database {	
	public static enum RelTypes implements RelationshipType {
		PERSON_PERSON,
		TAG_PERSON;
	}
	
	public static Label personLabel = DynamicLabel.label( "Person" );
	public static Label tagLabel = DynamicLabel.label( "Tag" );
	
	public static Node 
	createPersonNode(GraphDatabaseService graphDb, String id) {
		Node person = graphDb.createNode(personLabel);
		person.setProperty("id", id);
		person.setProperty("birthday", "");
		return person;
	}

	public static Node
	createTagNode(GraphDatabaseService graphDb, String id) {
		Node tag = graphDb.createNode(tagLabel);
		tag.setProperty("id", id);
		return tag;
	}
}
