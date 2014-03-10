package sigmod14;

import java.text.SimpleDateFormat;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

// going to put a bunch of common definitions/methods here for now.
public class Database {	
	public static final String DB_PATH = "target/q2-db";
	
	public static enum RelTypes implements RelationshipType {
		PERSON_PERSON,
		TAG_PERSON;
	}
	
	public static Label personLabel = DynamicLabel.label( "Person" );
	public static Label tagLabel = DynamicLabel.label( "Tag" );
	
	public static SimpleDateFormat sdf = 
			new SimpleDateFormat("yyyy-mm-dd:HH:mm:SS");


	public static final GraphDatabaseService graphDb = 
			new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
	
	public 
	static Node createPersonNode(GraphDatabaseService graphDb, String id) {
		Node person = graphDb.createNode(personLabel);
		person.setProperty("id", id);
		return person;
	}

	public
	static Node	createTagNode(GraphDatabaseService graphDb, String id) {
		Node tag = graphDb.createNode(tagLabel);
		tag.setProperty("id", id);
		return tag;
	}
}
