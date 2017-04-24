import java.io.IOException;

public class DNSFile {
	private static final String LOGFILE = ".dns";
	private String dnsfile;
	private EntryWriter er;
	private int minUnchosenLogId;
	
	public DNSFile(String node) {
		dnsfile = node+LOGFILE;
		try {
			er = new EntryWriter(dnsfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		minUnchosenLogId = 0;
	}
	/**
	 * write an empty Entry of id 'logId'
	 */
	public void writeEntry(int logId) {
		Entry entry = new Entry(logId);
		er.write(entry);
	}
	/**
	 * Write the given entry of id 'logId'
	 * @param entry
	 */
	public void writeEntry(Entry entry) {
		er.write(entry);
	}
	public Entry readEntry(int logId) {
		Entry entry = er.read(logId);
		return entry;
	}
	public void setMinUnchosenLogId(int id) {
		this.minUnchosenLogId = id;
	}
	public int getMinUnchosenLogId() {
		System.out.println("[get MinUnchosenLogId] " + minUnchosenLogId);
		return minUnchosenLogId;
	}
	public void incrementMinUnchosenLogId(int logId) {
		if (minUnchosenLogId == logId) {
			minUnchosenLogId += 1;
			while(readEntry(minUnchosenLogId).isChosen()) {
				minUnchosenLogId += 1;
			}
		}
		System.out.println("[update MinUnchosenLogId] " + minUnchosenLogId);
	}
}
