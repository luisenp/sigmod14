package sigmod14;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Scanner;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Query1 implements Query {
	private static final String DB_PATH = "target/q1-db";
	GraphDatabaseService graphDb;
	
	private static enum RelTypes implements RelationshipType {
		//CREATED,
		//REPLIED,
		//KNOWS;
		CREATED,
		USER;
	}
	private static Label commentLabel = DynamicLabel.label( "Comment" );
	private static Label userLabel = DynamicLabel.label( "User" );
	
	public void setup(String data_path, String query_path) throws FileNotFoundException {
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		
		//read in comment creation
		Scanner scanner = new Scanner(new File("data/outputDir-1k/comment_hasCreator_person.csv"));
		scanner.nextLine();
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			Node commentNode = graphDb.createNode(commentLabel);
			commentNode.setProperty("id", fields[0]);
			Node userNode = graphDb.createNode(userLabel);
			userNode.setProperty("id", fields[1]);
			userNode.createRelationshipTo(commentNode, RelTypes.CREATED);
		}
		//read in person knows person
		scanner = new Scanner(new File("data/outputDir-1k/person_knows_person.csv"));
		scanner.nextLine();
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			Node person1 = graphDb.findNodesByLabelAndProperty(userLabel, "id", fields[0]).iterator().next();
			Node person2 = graphDb.findNodesByLabelAndProperty(userLabel, "id", fields[1]).iterator().next();
			Relationship r1 = person1.createRelationshipTo(person2, RelTypes.USER);
			Relationship r2 = person2.createRelationshipTo(person1, RelTypes.USER);
			r1.setProperty("replies", 0);
			r2.setProperty("replies", 0);
		}
		
		//read in comment replies
		scanner = new Scanner(new File("data/outputDir-1k/comment_replyOf_comment.csv"));
		scanner.nextLine();
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			// this is amazing
			Node replyPersonNode = graphDb
					.findNodesByLabelAndProperty(commentLabel, "id", fields[0])
					.iterator().next()
					.getSingleRelationship(RelTypes.CREATED, Direction.INCOMING)
					.getEndNode();
			Node basePersonNode = graphDb
					.findNodesByLabelAndProperty(commentLabel, "id", fields[1])
					.iterator().next()
					.getSingleRelationship(RelTypes.CREATED, Direction.INCOMING)
					.getEndNode();
			Relationship knowage = null;
			for (Relationship r : replyPersonNode.getRelationships(Direction.OUTGOING, RelTypes.USER)) {
				if(r.getEndNode().equals(basePersonNode)) {
					knowage = r;
				}
			}
			knowage.setProperty("replies",
					(Integer) knowage.getProperty("replies") + 1);
		 }	
	}
	public void run(String data_path, String query_path) {
		
	}
	
	public void teardown() {
		
	}
	
}
