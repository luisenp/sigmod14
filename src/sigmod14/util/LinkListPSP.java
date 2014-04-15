package sigmod14.util;

import sigmod14.mem.graph.Person;


public class LinkListPSP {
	private int head;
	private int tail;
	private PersonShortPair array[];
	
	public LinkListPSP(int size) {
		array = new PersonShortPair[size];
		for (int i = 0; i < size; i++) {
			array[i] = new PersonShortPair(null, (short) 0);
		}
		head = tail = 0;
	}
	
	public void add(Person person, short distance) {
		array[head].setPerson(person);
		array[head++].setDistance(distance);
	}
	
	public PersonShortPair removeFirst() {
		return array[tail++];
	}
	
	public void reset() {
		head = tail = 0;
	}
	
	public boolean isEmpty() {
		return head == tail;
	}
	
	public int size() {
		return head - tail;
	}
}
