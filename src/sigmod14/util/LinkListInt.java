package sigmod14.util;

public class LinkListInt {
	private int head;
	private int tail;
	private int array[];
	
	public LinkListInt(int size) {
		array = new int[size];
		head = tail = 0;
	}
	
	public void add(int x) {
		array[head++] = x;
	}
	
	public int removeFirst() {
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
