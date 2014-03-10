package sigmod14;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

public class Query2 implements Query {
	private static final String DB_PATH = "target/q2-db";
	GraphDatabaseService graphDb;
	
	private static Label personLabel = DynamicLabel.label( "Person" );
	
	public void 
	setup(String data_path, String query_path) throws FileNotFoundException {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );

		IndexDefinition personIndex;
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			personIndex = schema.indexFor(personLabel)
											.on("id")
											.create();
			tx.success();
		}

		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			schema.awaitIndexOnline(personIndex, 10, TimeUnit.SECONDS);
		}
		
		ReadIn(0); // read in person
		ReadIn(1); // read in tag interests
		ReadIn(2); // read in knows
	}
	
	public void run(String data_path, String query_path) {
		
	}
	
	public void teardown() {
		
	}
	
	public void fillPersonInterest(String line) {
		String[] fields = line.split("\\|");
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
			person.setProperty("birthday", "");
			person.setProperty("tags", "");
		}
		String tags = (String) person.getProperty("tags");
		tags += "+" + tagID;
		person.setProperty("tags", tags);
	}

	
	public void createPersonNode(String line) {
		String[] fields = line.split("\\|");
		String ID = fields[0];
		Node person = graphDb.createNode(personLabel);
		person.setProperty("id", ID);
		person.setProperty("birthday", fields[4]);
		person.setProperty("tags", "");
	}
	
	public void fillKnows(String line) {
		String[] fields = line.split("\\|");
		Node person1 = graphDb
				.findNodesByLabelAndProperty(personLabel, "id", fields[0])
				.iterator().next();
		Node person2 = graphDb
				.findNodesByLabelAndProperty(personLabel, "id", fields[1])
				.iterator().next();
		person1.createRelationshipTo(person2, Database.RelTypes.PERSON);		
	}
	
	public void ReadIn(int type) throws FileNotFoundException {
		String personFile = "data/outputDir-1k/person.csv";
		String personTagFile = "data/outputDir-1k/person_hasInterest_tag.csv";
		String personKnowsFile = "data/outputDir-1k/person_knows_person.csv";

		File file = null;
		switch (type) {
		case 0:
			file = new File(personFile);
			break;
		case 1:
			file = new File(personTagFile);
			break;
		case 2:
			file = new File(personKnowsFile);
			break;
		default:
			break;
		}
		
		Scanner scanner = new Scanner(file);
		scanner.nextLine();
		Transaction tx = graphDb.beginTx();
		try {
			while (scanner.hasNextLine()) {
				switch (type) {
				case 0:
					createPersonNode(scanner.nextLine());
					break;
				case 1:
					fillPersonInterest(scanner.nextLine());
					break;
				case 2:
					fillKnows(scanner.nextLine());
					break;
				default:
					break;
				}
			}
			tx.success();
		} finally {
			tx.close();
		}
		scanner.close();
		
	}
}
