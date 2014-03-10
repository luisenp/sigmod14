package sigmod14;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

public class Query2 implements Query {
	private static final String DB_PATH = "target/q2-db";
	GraphDatabaseService graphDb;
		
	public void 
	setup(String data_path, String query_path) throws FileNotFoundException {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );

		IndexDefinition personIndex;
		IndexDefinition tagIndex;
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			personIndex = schema.indexFor(Database.personLabel)
											.on("id")
											.create();
			tagIndex = schema.indexFor(Database.tagLabel)
					.on("id")
					.create();
			tx.success();
		}

		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			schema.awaitIndexOnline(personIndex, 10, TimeUnit.SECONDS);
		}

		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			schema.awaitIndexOnline(tagIndex, 10, TimeUnit.SECONDS);
		}
		
		ReadIn(0); // read in person
		ReadIn(1); // read in tag interests
		ReadIn(2); // read in knows
	}
	
	public void run(String data_path, String query_path) {
		Date dummyDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		try {
			dummyDate = sdf.parse("1950-01-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		HashMap<String,Integer> personIdx = new HashMap<String,Integer>();
		Transaction tx = graphDb.beginTx();		
		try {
			ResourceIterator<Node> t = graphDb
				.findNodesByLabelAndProperty(Database.tagLabel, "id", "246")
				.iterator();
			Node tag = t.next();
			t.close();
			
			// Indexing relevant nodes
			int cnt = 0;
			for (Relationship r 
					: tag.getRelationships(Database.RelTypes.TAG_PERSON)) {
				Node person = r.getEndNode();

				System.out.println(person.getProperty("id"));
				System.out.println(person.getProperty("birthday"));
				
				Date birthday = null;
				String bdString = (String) person.getProperty("birthday");
				if (bdString.isEmpty()) continue;
				try {
					birthday = sdf.parse(bdString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (birthday.compareTo(dummyDate) < 0) continue;				
				personIdx.put((String) person.getProperty("id"), cnt);
				System.out.println(birthday);
				cnt++;
			}
			
			boolean graph[][] = new boolean[cnt][];
			for (int i = 0; i < cnt; i++) graph[i] = new boolean[cnt];
			
			
			tx.success();
		} finally {
			tx.close();
		}
	}
	
	public void teardown() {
		
	}
	
	public void fillPersonInterest(String line) {
		String[] fields = line.split("\\|");
		String personID = fields[0];
		String tagID = fields[1];
		ResourceIterator<Node> p = graphDb
			.findNodesByLabelAndProperty(Database.personLabel,"id", personID)
			.iterator();
		ResourceIterator<Node> t = graphDb
				.findNodesByLabelAndProperty(Database.tagLabel,"id", tagID)
				.iterator();
		Node person = p.hasNext() ? 
				p.next() : Database.createPersonNode(graphDb, personID);

		Node tag = t.hasNext() ? 
				t.next() : Database.createTagNode(graphDb, tagID);
		p.close();
		t.close();

		tag.createRelationshipTo(person, Database.RelTypes.TAG_PERSON);
	}

	
	public void createPersonNode(String line) {
		String[] fields = line.split("\\|");
		String ID = fields[0];
		Node person = Database.createPersonNode(graphDb, ID);
		person.setProperty("birthday", fields[4]);
	}
	
	public void fillKnows(String line) {
		String[] fields = line.split("\\|");
		ResourceIterator<Node> p1 = graphDb
			.findNodesByLabelAndProperty(Database.personLabel, "id", fields[0])
			.iterator();
		ResourceIterator<Node> p2 = graphDb
			.findNodesByLabelAndProperty(Database.personLabel, "id", fields[1])
			.iterator();
		Node person1 = p1.hasNext() ? 
				p1.next() : Database.createPersonNode(graphDb, fields[0]);
		Node person2 = p2.hasNext() ? 
				p2.next() : Database.createPersonNode(graphDb, fields[1]);;
		p1.close();
		p2.close();
		person1.createRelationshipTo(person2, Database.RelTypes.PERSON_PERSON);		
	}
	
	public void ReadIn(int type) throws FileNotFoundException {
		String personFile = "data/outputDir-1k/person.csv";
		String personTagFile = "data/outputDir-1k/person_hasInterest_tag.csv";
		String personKnowsFile = "data/outputDir-1k/person_knows_person.csv";

		// TODO is there a better way than using these ugly switch/case?
		
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

		//TODO I had some problems with the file encoding and scanner on Window.
		// we should check if the code works well on the contest machines.
		Scanner scanner = new Scanner(file, "UTF-8");
		scanner.nextLine();
		Transaction tx = graphDb.beginTx();
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				switch (type) {	
				case 0:
					createPersonNode(line);
					break;
				case 1:
					System.out.println(line);
					fillPersonInterest(line);
					break;
				case 2:
					fillKnows(line);
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
