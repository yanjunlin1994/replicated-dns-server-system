import java.io.Serializable;

public class Proposal implements Serializable {
	private int logId;
    private int proposalId;
    private DNSEntry dnsentry;
    public Proposal(int logId, int proposalId, DNSEntry dnsentry) {
    	this.logId = logId;
    	this.proposalId = proposalId;
    	this.dnsentry = new DNSEntry();
    	this.dnsentry.setDns(dnsentry.getDns());
    	this.dnsentry.setIp(dnsentry.getIp());
    }
    /**
     * Clone
     * @param p
     */
    public Proposal(Proposal p) {
        this.logId = p.getLogId();
        this.proposalId = p.getProposalId();
        this.dnsentry = p.getDnsentry();//TODO:
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
    @Override
    public String toString() { 
        return "[Log ID. " + this.logId + " Proposal ID." + this.proposalId + " " + this.dnsentry + "]";
    }
}
