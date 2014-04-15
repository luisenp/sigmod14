package sigmod14.util;

public class HashMapLong {
	private static final int INITIAL_BUCKET_SIZE = 8;
	private long storage[][];
	
	public HashMapLong(int cap) {
		storage = new long[cap][];
	}

	public int hash(long key) {
		return (int) (key % storage.length);
	}
	
	public void put(long array[][], long key, long val) {
		int bucket = hash(key);
		if (array[bucket] == null) {
			array[bucket] = new long[INITIAL_BUCKET_SIZE];
			array[bucket][0] = -1;			
		}
		int L = array[bucket].length;
		int i = 0;
		while (i < L) {
			if (array[bucket][i] == -1) {
				array[bucket][i] = key;
				array[bucket][i + 1] = val;
				if (i + 2 < L)
					array[bucket][i + 2] = -1;
				break;
			}
			i++;
		}
		if (i == L) {
			long newarray[] = new long[2*L];
			for (int j = 0; j < L; j++) {
				newarray[j] = array[bucket][j];
			}
			newarray[L] = key;
			newarray[L + 1] = val; 
			newarray[L + 2] = -1;
			array[bucket] = newarray;
		}
	}	
	
	public void put(long key, long val) {
		put(storage, key, val);
	}
	
	public long get(long key) {
		int bucket = hash(key);
		if (storage[bucket] == null) 
			return -1;
		int L = storage[bucket].length;
		for (int i = 0; i < L; i += 2) {
			if (storage[bucket][i] == key) 
				return storage[bucket][i + 1]; 
		}
		return -1;
	}
	
	public boolean containsKey(long key) {
		return get(key) != -1;
	}
}
