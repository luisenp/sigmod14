package sigmod14.mem;

public class Configuration {
	public static Configuration INSTANCE = new Configuration();
	
	int NThreadsMain;
	int NThreadsQ4;
	
	public int getNThreadsMain() {
		return NThreadsMain;
	}

	public void setNThreadsMain(int nThreadsMain) {
		NThreadsMain = nThreadsMain;
	}

	public int getNThreadsQ4() {
		return NThreadsQ4;
	}

	public void setNThreadsQ4(int nThreadsQ4) {
		NThreadsQ4 = nThreadsQ4;
	}

	private Configuration() {
		
	}
	
}
