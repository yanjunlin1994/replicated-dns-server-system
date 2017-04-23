import java.io.IOException;

public class DNSFile {
	private static final String LOGFILE = ".dns";
	private String dnsfile;
	private EntryWriter er;
	private int minUnchosenLogId;
	
	public DNSFile(String node) throws IOException {
		dnsfile = node+LOGFILE;
		er = new EntryWriter(dnsfile);
		minUnchosenLogId = 0;
	}
	/**
	 * Write/Update Entry at the 'logId' line
	 */
	public void writeEntry(int logId) {
		Entry entry = new Entry(logId);
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
		return minUnchosenLogId;
	}
}
