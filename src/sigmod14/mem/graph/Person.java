package sigmod14.mem.graph;

import java.util.LinkedList;


public class Person extends AbstractNode {
	private long birthday;
	private LinkedList<AbstractEdge> knows;
	private LinkedList<AbstractEdge> interests;
	private LinkedList<AbstractEdge> locations;
	
	public Person(long id) {
		this(id, -1L);
	}

	public Person(long id, long birthday) {
		super(id);
		this.birthday = birthday;
		knows = new LinkedList<AbstractEdge>();
		interests = new LinkedList<AbstractEdge>();
		locations = new LinkedList<AbstractEdge>();
	}
	
	public long getBirthday() {
		return birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}

	public LinkedList<AbstractEdge> getKnows() {
		return knows;
	}
	
	public LinkedList<AbstractEdge> getInterests() {
		return interests;
	}

	public LinkedList<AbstractEdge> getLocations() {
		return locations;
	}
	
	public void addKnowsEdge(AbstractEdge edge) {
		knows.add(edge);
	}
	
	public void addInterestEdge(AbstractEdge edge) {
		interests.add(edge);
	}
	
	public void addLocationEdge(AbstractEdge edge) {
		locations.add(edge);
	}
}
