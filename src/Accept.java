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
//    private int proposalId;
	private ProposalID proposalId;
    /* The DNS Entry in leader's accept proposal */
    private DNSEntry dnsentry;
    public Accept(int logId, ProposalID proposalId, DNSEntry dnsentry) {
    	this.logId = logId;
    	this.proposalId = proposalId;
    	this.dnsentry = dnsentry;
    }
    public int getLogId() {
    	return logId;
    }
	public ProposalID getProposalID() {
		return proposalId;
	}
	public void setProposalId(ProposalID proposalId) {
		this.proposalId = proposalId;
	}
	public void setLogId(int logId) {
        this.logId = logId;
    }
	public DNSEntry getValue() {
		return dnsentry;
	}
	public void setAcceptedValue(DNSEntry dnsentry) {
		this.dnsentry = dnsentry;
	}
    @Override
    public String toString() { 
        return "[Accept " + " ID." + this.proposalId + " " + this.dnsentry + "]";
    }
}
