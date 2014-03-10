package sigmod14;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

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
		
		IndexDefinition indexDefinition;
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Schema schema = graphDb.schema();
		    indexDefinition = schema.indexFor(userLabel )
		            .on( "id" )
		            .create();
		    tx.success();
		}
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Schema schema = graphDb.schema();
		    schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
		}
		
		//read in comment creation
		Scanner scanner = new Scanner(new File(data_path + "/comment_hasCreator_person.csv"));
		scanner.nextLine();
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			try ( Transaction tx = graphDb.beginTx() ) {
				Node commentNode = graphDb.createNode(commentLabel);
				commentNode.setProperty("id", fields[0]);
				Node userNode = graphDb.createNode(userLabel);
				userNode.setProperty("id", fields[1]);
				userNode.createRelationshipTo(commentNode, RelTypes.CREATED);
				tx.success();
			}
		}
		scanner.close();
		//read in person knows person
		scanner = new Scanner(new File(data_path + "/person_knows_person.csv"));
		scanner.nextLine();
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			try ( Transaction tx = graphDb.beginTx() )
			{
				Node person1 = graphDb.findNodesByLabelAndProperty(userLabel, "id", fields[0]).iterator().next();
				Node person2 = graphDb.findNodesByLabelAndProperty(userLabel, "id", fields[1]).iterator().next();
				Relationship r1 = person1.createRelationshipTo(person2, RelTypes.USER);
				Relationship r2 = person2.createRelationshipTo(person1, RelTypes.USER);
				r1.setProperty("replies", 0);
				r2.setProperty("replies", 0);
			    tx.success();
			}
			
		}
		scanner.close();
		
		//read in comment replies
		scanner = new Scanner(new File(data_path + "/comment_replyOf_comment.csv"));
		scanner.nextLine();
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] fields = line.split("|");
			// this is amazing
			try ( Transaction tx = graphDb.beginTx() ) {
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
						break;
					}
				}
				knowage.setProperty("replies",
						(Integer) knowage.getProperty("replies") + 1);
				tx.success();	
			}
		 }
		scanner.close();
	}
	public void run(String data_path, String query_path) throws FileNotFoundException {
		Scanner queryScanner = new Scanner(new File(query_path + "/1k-sample-queries.txt"));
		Scanner answerScanner = new Scanner(new File(query_path + "/1k-sample-answers.txt"));
		
		while(queryScanner.hasNextLine() && answerScanner.hasNextLine()) {
			String query = queryScanner.nextLine();
			query = query.split("(")[1];
			query = query.split(")")[0];
			String[] items = query.split(",");
			int actualAnswer = Integer.parseInt(answerScanner.nextLine());
			Node p1 = null;
			Node p2 = null;
			try ( Transaction tx = graphDb.beginTx() ) {
				p1 = graphDb.findNodesByLabelAndProperty(userLabel, "id", Integer.parseInt(items[0])).iterator().next();
				p2 = graphDb.findNodesByLabelAndProperty(userLabel, "id", Integer.parseInt(items[1])).iterator().next();
				tx.success();
			}
			int x = Integer.parseInt(items[2]);
			int computedAnswer = execute(p1,p2,x);
			if(actualAnswer == computedAnswer) {
				System.out.println("Passed");
			}
			else {
				System.out.println("Failed");
			}
		}
		queryScanner.close();
		answerScanner.close();
	}
	
	public void teardown() {
		
	}
	
	public Integer execute(Node p1, Node p2, int x) {
		CostEvaluator<Double> coster = new Q1Cost(x);
		PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.allTypesAndDirections(), coster);
		WeightedPath path = finder.findSinglePath(p1, p2);
		return path.length();
	}
	
}
