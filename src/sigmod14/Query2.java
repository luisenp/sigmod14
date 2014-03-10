package sigmod14;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

public class Query2 implements Query {
	private static GraphDatabaseService graphDb = Database.graphDb;
		
	public void 
	setup(String data_path, String query_path) throws FileNotFoundException {
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
		
		long time = System.currentTimeMillis();
		ReadIn(0); // read in person
		System.out.println(System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		ReadIn(1); // read in tag interests
		System.out.println(System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		ReadIn(2); // read in knows
		System.out.println(System.currentTimeMillis() - time);
	}
	
	public void run(String data_path, String query_path) {		
		String[] fields = parse(query_path);
		int k = Integer.parseInt(fields[0]);
		Date d = null;		
		try {
			d = Database.sdf.parse(fields[1] + ":00:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		System.out.println("Score " + getScoreTag("1021", d));
	}
	
	public void teardown() {
		
	}
	
	public void populatePersonInterest(String line) {
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
	
	public void populateKnows(String line) {
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
	
	public void batchInsertUsers() throws FileNotFoundException {
		String personFile = "data/outputDir-1k/person.csv";
		BatchInserter inserter = BatchInserters.inserter(Database.DB_PATH);
		inserter.createDeferredSchemaIndex(Database.personLabel)
			.on("id").create();

		Scanner scanner = new Scanner(new File(personFile), "UTF-8");
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			
		}
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

		//TODO I had some problems with the file encoding and scanner on Windows.
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
					populatePersonInterest(line);
					break;
				case 2:
					populateKnows(line);
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
	
	private int getSizeLargestCC(ArrayList<LinkedList<Integer>> graph) {
		int score = 0;
		int n = graph.size();
		HashSet<Integer> visited = new HashSet<Integer>();
		for (int i = 0; i < n; i++) {
			if (visited.contains(i)) continue;
			LinkedList<Integer> stack = new LinkedList<Integer>();
			stack.addFirst(i);
			int cnt = 0;
			while (!stack.isEmpty()) {
				Integer node = stack.removeFirst();
				if (visited.contains(node)) continue;
				visited.add(node);
				cnt++;
				LinkedList<Integer> neighbors = graph.get(node);
				for (Integer neigh : neighbors) {
					stack.addFirst(neigh);
				}
			}
			if (cnt > score) score = cnt;
		}
		return score;
	}
	
	private String[] parse(String query) {
		String[] fields = query.split(",");
		fields[0] = fields[0].substring(7);
		fields[1] = fields[1].substring(1, fields[1].length() - 1);
		return fields;
	}
	
	
	private int getScoreTag(String tagID, Date d) {
		ArrayList< LinkedList<Integer> > graph;
		HashMap<String,Integer> personCache = new HashMap<String,Integer>();
		Transaction tx = graphDb.beginTx();		
		try {
			ResourceIterator<Node> t = graphDb
				.findNodesByLabelAndProperty(Database.tagLabel, "id", tagID)
				.iterator();
			Node tag = t.next();
			t.close();
			
			// Indexing relevant nodes
			int cnt = 0;
			for (Relationship r 
					: tag.getRelationships(Database.RelTypes.TAG_PERSON)) {
				Node person = r.getEndNode();

				if (!person.hasProperty("birthday")) continue;
				Date birthday = null;
				try {
					String bdString = (String) person.getProperty("birthday");
					birthday = Database.sdf.parse(bdString + ":00:00:00");
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (birthday.before(d)) continue;
				personCache.put((String) person.getProperty("id"), cnt);
				cnt++;
			}
			
			graph = new ArrayList< LinkedList<Integer> >(cnt);
			for (int i = 0; i < cnt; i++) 
				graph.add(i, new LinkedList<Integer> ());
			
			// Filling up graph
			for (Relationship r 
					: tag.getRelationships(Database.RelTypes.TAG_PERSON)) {
				Node person = r.getEndNode();
				String id = (String) person.getProperty("id");
				if (!personCache.containsKey(id)) continue;
				int index = personCache.get(id);

				for (Relationship edge 
						: person
							.getRelationships(Database.RelTypes.PERSON_PERSON, 
											  Direction.OUTGOING)) {
					Node knows = edge.getEndNode();
					String idKnows = (String) knows.getProperty("id");
					if (!personCache.containsKey(idKnows)) continue;
					int indexKnows = personCache.get(idKnows);
					graph.get(index).add(indexKnows);
					graph.get(indexKnows).add(index);
				}
			}
			tx.success();
		} finally {
			tx.close();
		}
		
		return getSizeLargestCC(graph);		
	}
}
