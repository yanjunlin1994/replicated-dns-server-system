import java.io.IOException;

public class DNSFile {
	private static final String LOGFILE = ".dns";
	private String dnsfile;
	private EntryWriter er;
	private int minUnchosenLogId;
	private ProposalID proposalId;
	public DNSFile(String node) {
		dnsfile = node+LOGFILE;
		try {
			er = new EntryWriter(dnsfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		minUnchosenLogId = er.readMinUnchosenLogId();
		proposalId = er.readProposalId();
	}
	/* -------------- operations about proposalId ------------- */
	public ProposalID getProposalId() {
		return proposalId;
	}
	public void setProposalId(ProposalID id) {
		this.proposalId = id;
		er.writeProposalId(proposalId);
	}
	/* -------------- operations about minUnchosenLogId ------------ */
	public void setMinUnchosenLogId(int id) {
		this.minUnchosenLogId = id;
		er.writeMinUnchosenLogId(id);
	}
	public int getMinUnchosenLogId() {
		System.out.println("[get MinUnchosenLogId] " + minUnchosenLogId);
		return minUnchosenLogId;
	}
	/**
	 * update minUnchosenLogId.
	 */
	public void incrementMinUnchosenLogId(int logId) {
		if (minUnchosenLogId == logId) {
			minUnchosenLogId += 1;
			while(readEntry(minUnchosenLogId).isChosen()) {
				minUnchosenLogId += 1;
			}
		}
		er.writeMinUnchosenLogId(minUnchosenLogId);
		System.out.println("[update MinUnchosenLogId] " + minUnchosenLogId);
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
}
