import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DNSFile {
	private static final String LOGFILE = ".dns";
	private String dnsfile;
	private EntryWriter er;
	private int minUnchosenLogId;
	private ProposalID proposalId;
	/* The node doesn't accept any value in the log entries beyong noMoreAcceptedLogId */
	private int noMoreAcceptedLogId;
	private HashMap<ProposalID, Set<Integer>> proposalIdMapToUnchosenLogId;
	public DNSFile(int node) {
		dnsfile = String.valueOf(node)+LOGFILE;
		try {
			er = new EntryWriter(dnsfile, node);
		} catch (IOException e) {
			e.printStackTrace();
		}
		minUnchosenLogId = er.readMinUnchosenLogId();
		proposalId = er.readProposalId();
		noMoreAcceptedLogId = er.readNoMoreAcceptedLogId();
		this.proposalIdMapToUnchosenLogId = er.getProposalIdMapToUnchosenLogId();
		proposalIdMapToUnchosenLogId.forEach((proposalId, set)-> {
			set.forEach(logId -> {
				System.out.print(proposalId+":"+logId+"\t");
			});
		}); 
	}
	public void addToMap(ProposalID proposalId, int logId) {
		if (!proposalIdMapToUnchosenLogId.containsKey(proposalId)) {
			proposalIdMapToUnchosenLogId.put(proposalId, new HashSet<Integer>());
		}
		proposalIdMapToUnchosenLogId.get(proposalId).add(logId);
	}
	/**
	 *  Since already set the log of logId to chosen, remove it form the proposalIdMapToUnchosenLogId. 
	 */
	public void removeFromMap(ProposalID proposalId, int logId) {
		if (!proposalIdMapToUnchosenLogId.containsKey(proposalId)) {
			System.err.println("[DNSFile removeFromMap] The proposalId is not in the map");
		}
		HashSet<Integer> set = (HashSet<Integer>) proposalIdMapToUnchosenLogId.get(proposalId);
		if (!set.contains(logId)) {
			System.err.println("[DNSFile removeFromMap] The logId is not in the map");
		}
		set.remove(logId);
	}
	public HashMap<ProposalID, Set<Integer>> getProposalIdMapToUnchosenLogId() {
		return proposalIdMapToUnchosenLogId;
	}
	/* -------------- operations about noMoreAcceptedLogId ---------*/
	public int getNoMoreAcceptedLogId() {
		return noMoreAcceptedLogId;
	}
	/** If the node accepts a value in a log entry that is beyond current noMoreAcceptedLogId,
	 * update the value of noMoreAcceptedLogId.
	 */
	public void updateNoMoreAcceptedLogId(int log) {
		if (log >= noMoreAcceptedLogId) {
			noMoreAcceptedLogId = log + 1;
		}
		er.writeNoMoreAcceptedLogId(noMoreAcceptedLogId);
	}
	/* -------------- operations about proposalId ------------- */
	public ProposalID getProposalId() {
		return proposalId;
	}
	public void incrementProposalId() {
		proposalId.incrementProposalId();
		er.writeProposalId(proposalId);
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
