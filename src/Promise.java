import java.io.Serializable;
/**
 * Promise includes:
 * <accepted id, accepted value>
 */
public class Promise implements Serializable {
	private int acceptedId;
	private DNSEntry accptedValue;
    private int src;
//    private int ID;
//    private String value;
    private boolean ifrealPromise;//could send the nack back as a type of promise
    public Promise(int src, int acceptedId, DNSEntry acceptedValue, boolean realPromise) {
    	this.src = src;
    	this.acceptedId = acceptedId;
    	this.accptedValue = accptedValue;
    	this.ifrealPromise = realPromise;
    }
    public int getAcceptedId() {
		return acceptedId;
	}
	public void setAcceptedId(int acceptedId) {
		this.acceptedId = acceptedId;
	}
	public DNSEntry getAccptedValue() {
		return accptedValue;
	}
	public void setAccptedValue(DNSEntry accptedValue) {
		this.accptedValue = accptedValue;
	}
	public boolean isIfrealPromise() {
		return ifrealPromise;
	}
	@Override
	public String toString() { 
	    return "[Promise from " + this.src + "  " + this.ifrealPromise + " Promise ID." + this.acceptedId + " " + this.accptedValue + "]";
	}
//	public Promise(int s, int i, String c) {
//        this.src = s;
//        this.ID = i;
//        this.value = c;
//        this.ifrealPromise = false;
//    }
    public Promise(Promise p) {
        this.src = p.getSrc();
        this.acceptedId = p.getAcceptedId();
        this.accptedValue = new DNSEntry();
        this.accptedValue.setIp(p.getAccptedValue().getIp());
        this.accptedValue.setDns(p.getAccptedValue().getDns());
        this.ifrealPromise = p.getIsIfrealPromise();
    }
	  public int getSrc() {
	      return src;
	  }
	  public void setSrc(int src) {
	      this.src = src;
	  }
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
    public boolean getIsIfrealPromise() {
        return this.ifrealPromise;
    }
    public void setIfrealPromise(boolean i) {
        this.ifrealPromise = i;
    }
//    @Override
//    public String toString() { 
//        return "[Promise from " + this.src + "  " + this.ifrealPromise + " Promise ID." + this.ID + " " + this.value + "]";
//    }
    

}
