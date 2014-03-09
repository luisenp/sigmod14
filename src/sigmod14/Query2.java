package sigmod14;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Query2 implements Query {
	private static final String DB_PATH = "target/q2-db";
	GraphDatabaseService graphDb;
	
	private static enum RelTypes implements RelationshipType {
		PERSON;
	}
	
	private static Label personLabel = DynamicLabel.label( "Person" );
	
	@SuppressWarnings("unchecked")
	public void 
	setup(String data_path, String query_path) throws FileNotFoundException {
		GraphDatabaseService graphDb = 
				new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );

		String personFile = "data/outputDir-1k/person.csv";
		String personTagFile = "data/outputDir-1k/person_hasInterest_tag.csv";

		//read in person
		Scanner scanner = new Scanner(new File(personFile));
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			String ID = fields[0];
			Node personNode = graphDb.createNode(personLabel);
			personNode.setProperty("id", ID);

			Date birthday = null;
			try {
				birthday = new SimpleDateFormat("yyyy-mm-dd").parse(fields[4]);
			} catch (ParseException e) {
				System.err.println("Error reading birthday.");
			}
			personNode.setProperty("birthday", birthday);
			
		}
		scanner.close();
		
		//read in tag interests
		scanner = new Scanner(new File(personTagFile));
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			String personID = fields[0];
			String tagID = fields[1];
			Node person = null;
			for (Node n: graphDb.findNodesByLabelAndProperty(personLabel, 
															 "id", personID)) {
				person = n;
				break;
			}
			if (person == null) {
				person = graphDb.createNode(personLabel);
				person.setProperty("id", personID);
				person.setProperty("tags", new HashSet<String> ());
			}
			HashSet<String> tags = (HashSet<String>) person.getProperty("tags");
			tags.add(tagID);
		}
		scanner.close();
		
		//read in person knows person
		scanner = new Scanner(new File("data/outputDir-1k/person_knows_person.csv"));
		scanner.nextLine();
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			Node person1 = graphDb.findNodesByLabelAndProperty(personLabel, "id", fields[0]).iterator().next();
			Node person2 = graphDb.findNodesByLabelAndProperty(personLabel, "id", fields[1]).iterator().next();
			Relationship r1 = person1.createRelationshipTo(person2, RelTypes.PERSON);
			Relationship r2 = person2.createRelationshipTo(person1, RelTypes.PERSON);
		}
		
		scanner.close();
	}
	public void run(String data_path, String query_path) {
		
	}
	
	public void teardown() {
		
	}
	
}
