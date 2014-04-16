package sigmod14.mem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sigmod14.mem.graph.NotFoundException;
import sigmod14.mem.graph.Person;
import sigmod14.mem.graph.Tag;
import sigmod14.util.LinkListInt;

public class Query4Solver {
	private Database db;
	private LinkedList<String> queries;
	private HashMap<String,String> answers;
	private int numThreads;
	
	public Query4Solver(Database db, int numThreads) {
		this.db = db;
		this.numThreads = numThreads;
	}	
	
	private class Centrality {
		long num;
		long den;
		
		Centrality(long num, long den) {
			if (den == 0) {	// centrality = 0 if den = 0
				num = 0;
				den = 1;
			}
			this.num = num;
			this.den = den;
		}
		
		int compare(Centrality r) {
			long a = num*r.den;
			long b = r.num*den;
			if (a < b) return -1;
			if (a > b) return 1;
			return 0;
		}
		
		public String toString() {
			double d = (double) num/den;
			return String.valueOf(d);
		}
	}
	
	private class PersonCentrality {
		private Person person;
		private Centrality centrality;
		
		PersonCentrality(Person person, Centrality centrality) {
			this.person = person;
			this.centrality = centrality;
		}
		
		public String toString() {
			return person.toString() + " " + centrality.toString();
		}
	}
	
	private class 
	PersonCentralityComparator implements Comparator<PersonCentrality> {		
		public int compare(PersonCentrality pc1, PersonCentrality pc2) {
			int cmp = pc1.centrality.compare(pc2.centrality); 
			if (cmp == 0) {
				if (pc1.person.getId() > pc2.person.getId()) return -1;
				if (pc1.person.getId() < pc2.person.getId()) return 1;
				return 0;
			}
			return cmp;
		}
		
	}

	private class 
	PersonDegreeComparator implements Comparator<Person> {
		HashMap<Person,Integer> degrees = new HashMap<Person,Integer> ();
		
		private int getDegree(Person p) {
			if (degrees.containsKey(p)) return degrees.get(p);
			HashSet<Integer> connect2 = new HashSet<Integer>();
			for (Integer p2id : p.getKnows().keySet()) {
				Person p2 = db.getPerson(p2id);
				connect2.add(p2id);
				for (Integer p3id : p2.getKnows().keySet())
					connect2.add(p3id);
			}
			degrees.put(p, connect2.size());
			return connect2.size();
		}
		
		public int compare(Person p1, Person p2) {
			return -1*Integer.compare(getDegree(p1), getDegree(p2));
		}		
	}	
	

	// returns false if the maximum possible centrality with the 
	// given parameters is guaranteed to be lower or equal than c. 
	// returns true otherwise
	//
	// rp - current rp (reachable nodes from p)
	// sp - current sp (sum geod. distances of reachable nodes)
	// n - number of vertices on the graph
	// d - minimum geod. distances of reamining nodes
	// c - centrality to compare to
	private 
	boolean checkCentrality(long rp, long sp, long n, long d, Centrality c) {
		long x = n - rp;
		if (2*x*d <= d - 2*sp) return true;
		Centrality maxC = new Centrality(n, sp + x*d);
		if (maxC.compare(c) < 0) return false;
		return true;
	}

    class BFSOuter implements Callable<PriorityQueue<PersonCentrality>>  {
    	LinkedList<Person> persons;
        HashSet<Person> vertices;
        PriorityQueue<PersonCentrality> pq;
        int k;
        int n1;
        long rp = -1, sp = 0;

        BFSOuter(HashSet<Person> vertices, int k, int n1) {
        	this.persons = new LinkedList<Person>();
        	this.pq = 
        		new PriorityQueue<PersonCentrality> 
        							(k + 1, new PersonCentralityComparator());
            this.vertices = vertices;
            this.k = k;
            this.n1 = n1;
        }

        public void addPerson(Person p) {
        	persons.add(p);
        }
        
        public PriorityQueue<PersonCentrality> call() {
        	for (Person p : persons) {            	
            	HashSet<Person> visited = new HashSet<Person>();
    			LinkListInt queue = new LinkListInt(db.getNumPersons());
    			LinkListInt dist = new LinkListInt(db.getNumPersons());

    			queue.add(p.getId());
    			dist.add(0);
    			visited.add(p);
    			long rp = -1, sp = 0;
    			// do a BFS to compute relevant quantities rp, sp				
    			while (!queue.isEmpty()) {
    				Person p2 = db.getPerson(queue.removeFirst());
    				int d = dist.removeFirst();
    				// visit only vertices with the given forum tag
    				rp++;
    				sp += d;
    				if (pq.size() >= k
    					&& !checkCentrality(rp, sp, n1, d, pq.peek().centrality)) {
    					// stops if max. possible centrality is lower than the
    					// worst one in the priority queue
    					break;
    				}
    				for (Integer adjPersonID : p2.getKnows().keySet()) {
    					Person adjPerson = db.getPerson(adjPersonID);
    					if (!vertices.contains(adjPerson) 
    							|| visited.contains(adjPerson)) 
    						continue;
    					visited.add(adjPerson);
    					queue.add(adjPerson.getId());
    					dist.add(d + 1);
    				}
    			}	
	            pq.add(new PersonCentrality(p, new Centrality(rp*rp, n1*sp)));
	            if (pq.size() > k) pq.poll();
        	}
        	return pq;
        }
    }
    
    public String query4(int k, String tagName) {
        // finding the tag with the given name
        Tag tag = new Tag(0);
        for (int idTag : db.getAllTags()) {
            Tag node = db.getTag(idTag);
            try {
                if (db.getTagName(idTag).equals(tagName)) {
                    tag = node;
                }
            } catch (NotFoundException e) {
                System.err.println("ERROR: Tag should have a name");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        // finding all vertices on the induced graph
        HashSet<Person> vertices = new HashSet<Person>();
        for (Person person : tag.getMembersForums()) {
            vertices.add(person);
        }
        Person sortedVertices[] = vertices.toArray(new Person[vertices.size()]);        
        Arrays.sort(sortedVertices, new PersonDegreeComparator());

        int n1 = vertices.size() - 1;

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        ArrayList<BFSOuter> tasks = new ArrayList<BFSOuter>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            tasks.add(i, new BFSOuter(vertices, k, n1));
        }
        int cnt = 0;
        int factor = db.getNumPersons() < 10000 ? 1 : 25; 
        for (Person p : sortedVertices) {
            if (factor*cnt > vertices.size()) 
              break;
            tasks.get(cnt % numThreads).addPerson(p);
            cnt++;
        }
        PriorityQueue<PersonCentrality> pq =
                new PriorityQueue<PersonCentrality> 
                    (k + 1, new PersonCentralityComparator());
 
        try {
        	List<Future<PriorityQueue<PersonCentrality>>> results = 
        			pool.invokeAll(tasks);
	        for (Future<PriorityQueue<PersonCentrality>> r : results) {        	
				
					PriorityQueue<PersonCentrality> pqThread = r.get();
		            pq.addAll(pqThread);
		            while (pq.size() > k) 
		            	pq.poll();
	        }
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
        pool.shutdown();
        
        cnt = 0;
        String queryAns = "";
        while (cnt < k) {
            PersonCentrality pc = pq.poll();
            queryAns = String.valueOf(pc.person.getId()) + " " + queryAns;
            cnt++;
        }
        return queryAns.substring(0, queryAns.length() - 1);
    }


	public HashMap<String,String> getAnswers() {
		return answers;
	}
	
	public void addQuery(String query) {
		queries.add(query);
	}
}
