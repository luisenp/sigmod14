package sigmod14.mem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
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

	private static class 
	PersonDegreeComparator implements Comparator<Person> {		
		public int compare(Person p1, Person p2) {
			return -1*Integer.compare(p1.getKnows().size(), p2.getKnows().size());
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

    class BFSOuter implements Callable<BFSOuterResult>  {
        Person p;
        HashSet<Person> vertices;
        PriorityQueue<PersonCentrality> pq;
        int k;
        int n1;
        long rp = -1, sp = 0;

        BFSOuter(Person p, HashSet<Person> vertices, int k, PriorityQueue<PersonCentrality> pq, int n1) {
            this.p = p;
            this.vertices = vertices;
            this.k = k;
            this.pq = pq;
            this.n1 = n1;
        }
        
        public BFSOuterResult call() {
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
//            pq.add(new PersonCentrality(p, new Centrality(rp*rp, n1*sp)));
//            if (pq.size() > k) pq.poll();
            return new BFSOuterResult(p, rp,sp);
        }
    }


    class BFSOuterResult {
        Person p;
        long rp, sp;
        BFSOuterResult(Person p, long rp, long sp) {
            this.p = p;
            this.rp = rp;
            this.sp = sp;
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

        // from each node p on the graph, do a BFS and compute centrality
        PriorityQueue<PersonCentrality> pq =
            new PriorityQueue<PersonCentrality>
                (k + 1, new PersonCentralityComparator());
        int cnt = 0;
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        List<BFSOuter> tasks = new LinkedList<BFSOuter>();
        for (Person p : sortedVertices) {
            if (4*cnt++ > vertices.size())
              break;
            tasks.add(new BFSOuter(p, vertices, k, pq, n1));
        }

        try {
	        List<Future<BFSOuterResult>> results = pool.invokeAll(tasks);
	        pool.shutdown();
	        for (Future<BFSOuterResult> r : results) {
	            BFSOuterResult res;
	            res = r.get();
	            pq.add(new PersonCentrality(res.p, new Centrality(res.rp*res.rp, n1*res.sp)));
	            if (pq.size() > k) pq.poll();
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        String queryAns = "";
        while (!pq.isEmpty()) {
            PersonCentrality pc = pq.poll();
            queryAns = String.valueOf(pc.person.getId()) + " " + queryAns;
        }
        return queryAns;
    }

	public HashMap<String,String> getAnswers() {
		return answers;
	}
	
	public void addQuery(String query) {
		queries.add(query);
	}
}
