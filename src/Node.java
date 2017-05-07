/**
 * Node class.
 */
public class Node {
    private int nodeID;
    private String ip;
    private int port;
    private boolean isActive;
    /* Each node has its dns file, which stores the dns content and transaction information */
    private DNSFile dnsfile;
    public Node(int id, String i, int prt) {
        this.nodeID = id;
        this.ip = i;
        this.port = prt;
        this.isActive = true;
        this.dnsfile = null;
    }
    
    public DNSFile getDnsfile() {
		return dnsfile;
	}
    /* It will only be used once when the nodes starts running */
	public void setDnsfile(DNSFile dnsfile) {
        this.dnsfile = dnsfile;
    }
    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    @Override
    public String toString() { 
        return "I am Node " + this.nodeID;
    }
}