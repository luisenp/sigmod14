package sigmod14.mem;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FastFileIterator {
	private int pagesize = 1024 * 8;
	private RandomAccessFile file;
	private byte[][] page = new byte[2][pagesize];
	private int currentPage = 0;
	private boolean morefile = true;
	private int loc = 0;
	public FastFileIterator(String filename) throws IOException {
		this.file = new RandomAccessFile(filename,"r");
		this.morefile = (-1 != file.read(page[0]));
		//burn the first line
		while(page[currentPage][loc++] != '\n') {}
	}
	
	private void readPage() throws IOException {
		 morefile = (-1 != file.read(page[1-currentPage]));
		 currentPage = 1 - currentPage;
		 loc = 0;
	}
	
	public long next() throws IOException {
		long result = 0;
		while (hasNext()) {
			if(loc == pagesize) {
				readPage();
			}
			if(page[currentPage][loc] == '\n' || page[currentPage][loc]  == '|' ) {
				loc++;
				break;
			}
			result = result * 10 + page[currentPage][loc] - '0';
			loc++;
		}
		return result;
	}
	
	public boolean hasNext() {
		return !(morefile && loc == pagesize); 
	}
	
	public void close() throws IOException {
		file.close();
	}
}