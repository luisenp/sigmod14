package sigmod14.util;

public class HashMapLongInt {
	private static final int INITIAL_BUCKET_SIZE = 4;
	private int storage[][];
	
	public HashMapLongInt(int cap) {
		storage = new int[cap][];
	}

	private int hash(long key) {
		return (int) (key % storage.length);
	}
	
	private void put(int array[][], long key, int val) {
		int bucket = hash(key);
		int keyhi = (int) key >> 32;
		int keylo = (int) (key << 32) >> 32;
		if (array[bucket] == null) {
			array[bucket] = new int[3*INITIAL_BUCKET_SIZE];
			array[bucket][0] = -1;			
		}
		int L = array[bucket].length;
		int i = 0;
		while (i < L) {
			if (array[bucket][i] == -1) {
				array[bucket][i] = keyhi;
				array[bucket][i + 1] = keylo;
				array[bucket][i + 2] = val;
				if (i + 3 < L)
					array[bucket][i + 3] = -1;
				break;
			}
			i++;
		}
		if (i == L) {
			int newarray[] = new int[2*L];
			for (int j = 0; j < L; j++) {
				newarray[j] = array[bucket][j];
			}
			newarray[L] = keyhi;
			newarray[L + 1] = keylo; 
			newarray[L + 2] = val; 
			newarray[L + 3] = -1;
			array[bucket] = newarray;
		}
	}	
	
	void put(long key, int val) {
		put(storage, key, val);
	}
	
	public long get(long key) {
		int bucket = hash(key);
		int keyhi = (int) key >> 32;
		int keylo = (int) (key << 32) >> 32;
		if (storage[bucket] == null) 
			return -1;
		int L = storage[bucket].length;
		for (int i = 0; i < L; i += 3) {
			if (storage[bucket][i] == keyhi && storage[bucket][i + 1] == keylo) 
				return storage[bucket][i + 2]; 
		}
		return -1;
	}
	
	public boolean containsKey(long key) {
		return get(key) != -1;
	}
}
