import java.io.Serializable;
/**
 * Accept proposal from leader.
 * Include: log id, accept id, accept value(dnsEntry)
 *
 */
public class Accept implements Serializable {
	private static final long serialVersionUID = 781432240375344999L;
	/* Which log slot should the <dns entry> written to */
	private int logId;
	/* The proposal Id in leader's accept proposal */
	private ProposalID proposalId;
    /* The DNS Entry in leader's accept proposal */
    private DNSEntry dnsentry;
    private int firstUnchosenLogId;
    public Accept(int logId, ProposalID proposalId, DNSEntry dnsentry, int firstUnchosenLogId) {
    	this.logId = logId;
    	this.proposalId = new ProposalID(proposalId);
    	this.dnsentry = dnsentry;
    	this.firstUnchosenLogId = firstUnchosenLogId;
    }
    
    public int getLogId() {
    	return logId;
    }
    public void setLogId(int logId) {
        this.logId = logId;
    }
	public ProposalID getProposalID() {
		return proposalId;
	}
	public void setProposalId(ProposalID proposalId) {
		this.proposalId = new ProposalID(proposalId);
	}
	
	public DNSEntry getValue() {
		return dnsentry;
	}
	public void setAcceptedValue(DNSEntry dnsentry) {
		this.dnsentry = dnsentry;
	}

    public void setFirstUnchosenLogId(int firstUnchosenLogId) {
        this.firstUnchosenLogId = firstUnchosenLogId;
    }

    public int getFirstUnchosenLogId() {
        return firstUnchosenLogId;
    }
    @Override
    public String toString() { 
        return "proposalId: " + this.proposalId + ", entry: " + this.dnsentry;
    }
}
