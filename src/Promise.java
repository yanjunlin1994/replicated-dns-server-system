import java.io.Serializable;
/**
 * Promise includes:
 * <accepted id, accepted value>
 */
public class Promise implements Serializable {
    private static final long serialVersionUID = -2120051092929991471L;
    private ProposalID acceptedId;
	private DNSEntry acceptedValue;
    private int src;
    private boolean ifrealPromise;//could send the nack back as a type of promise
    private boolean noMoreAcceptedValue;
    public Promise(int src, ProposalID acceptedId, DNSEntry acceptedValue, boolean realPromise, boolean noMoreAcceptedValue) {
    	this.src = src;
    	this.acceptedId = acceptedId;
    	this.acceptedValue = acceptedValue;
    	this.ifrealPromise = realPromise;
    	this.noMoreAcceptedValue = noMoreAcceptedValue;
    }
    public boolean getNoMoreAcceptedValue() {
    	return noMoreAcceptedValue;
    }
    public ProposalID getAcceptedId() {
		return acceptedId;
	}
	public void setAcceptedId(ProposalID acceptedId) {
		this.acceptedId = acceptedId;
	}
	public DNSEntry getacceptedValue() {
		return acceptedValue;
	}
	public void setacceptedValue(DNSEntry acceptedValue) {
		this.acceptedValue = acceptedValue;
	}
	public boolean isIfrealPromise() {
		return ifrealPromise;
	}
	@Override
	public String toString() { 
	    return "[Promise from node " + this.src + "," + this.ifrealPromise + ", id:" + this.acceptedId + "," + this.acceptedValue + "]";
	}

    public Promise(Promise p) {
        this.src = p.getSrc();
        this.acceptedId = p.getAcceptedId();
        this.acceptedValue = new DNSEntry();
        this.acceptedValue.setIp(p.getacceptedValue().getIp());
        this.acceptedValue.setDns(p.getacceptedValue().getDns());
        this.ifrealPromise = p.getIsIfrealPromise();
    }
	  public int getSrc() {
	      return src;
	  }
	  public void setSrc(int src) {
	      this.src = src;
	  }

    public boolean getIsIfrealPromise() {
        return this.ifrealPromise;
    }
    public void setIfrealPromise(boolean i) {
        this.ifrealPromise = i;
    }

}
