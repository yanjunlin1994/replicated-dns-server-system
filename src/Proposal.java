import java.io.Serializable;

public class Proposal implements Serializable {
	private int logId;
    private int proposalId;
    private DNSEntry dnsentry;
//    private String value;
//    public Proposal(int i, String c) {
//        this.ID = i;
//        this.value = c;
//    }
    public Proposal(int logId, int proposalId, DNSEntry dnsentry) {
    	this.logId = logId;
    	this.proposalId = proposalId;
    	this.dnsentry = dnsentry;
    }
    /**
     * Clone
     * @param p
     */
    public Proposal(Proposal p) {
        this.proposalId = p.getProposalId();
        this.dnsentry = p.getDnsentry();
    }
    public int getLogId() {
		return logId;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}
	public int getProposalId() {
		return proposalId;
	}
	public void setProposalId(int proposalId) {
		this.proposalId = proposalId;
	}
	public DNSEntry getDnsentry() {
		return dnsentry;
	}
	public void setDnsentry(DNSEntry dnsentry) {
		this.dnsentry = dnsentry;
	}
	//    
//    public int getID() {
//        return this.ID;
//    }
//
//    public void setID(int iD) {
//        this.ID = iD;
//    }
//
//    public String getValue() {
//        return this.value;
//    }
//    public void setValue(String v) {
//        this.value = v;
//    }
    @Override
    public String toString() { 
        return "[Log ID. " + this.logId + " Proposal ID." + this.proposalId + " " + this.dnsentry + "]";
    }
}
