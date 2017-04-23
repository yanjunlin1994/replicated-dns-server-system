import java.io.Serializable;
/**
 * Accept proposal from leader.
 * Include: log id, accept id, accept value(dnsEntry)
 *
 */
public class Accept implements Serializable {
	/* Which log slot should the <dns entry> written to */
	private int logId;
	/* The proposal Id in leader's accept proposal */
    private int proposalId;
    /* The DNS Entry in leader's accept proposal */
    private DNSEntry dnsentry;
    public int getLogId() {
    	return logId;
    }
	public int getID() {
		return proposalId;
	}
	public void setProposalId(int proposalId) {
		this.proposalId = proposalId;
	}
	public DNSEntry getValue() {
		return dnsentry;
	}
	public void setAcceptedValue(DNSEntry dnsentry) {
		this.dnsentry = dnsentry;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}
//    private String value;
//    public Accept(int i, String c) {
//        this.ID = i;
//        this.value = c;
//    }
//    public int getID() {
//        return this.ID;
//    }
//    public void setID(int iD) {
//        this.ID = iD;
//    }
//    public String getValue() {
//        return this.value;
//    }
//    public void setValue(String v) {
//        this.value = v;
//    }
//    @Override
//    public String toString() { 
//        return "[Accept " + " ID." + this.ID + " " + this.value + "]";
//    }

}
