package sigmod14;

import java.io.FileNotFoundException;

public interface Query {
	public void setup(String data_path, String query_path) throws FileNotFoundException;
	public void run(String data_path, String query_path) throws FileNotFoundException;
	public void teardown();
}
