package sigmod14.util;

import sigmod14.mem.graph.Person;

public class PersonShortPair {
	private Person person;
	private short distance;
	
	public PersonShortPair(Person person, short distance) {
		this.setPerson(person);
		this.setDistance(distance);
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public short getDistance() {
		return distance;
	}

	public void setDistance(short distance) {
		this.distance = distance;
	}
}

