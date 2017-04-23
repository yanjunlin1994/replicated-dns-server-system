import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Node class.
 */
public class Node {
    private int nodeID;
    private String ip;
    private int port;
    private boolean isActive;
    /* proposal number set (like 1, 11, 21, ...) */
    private Queue<Integer> proposalNumSet;
    /* Each node has its dns file, which stores the dns content and transaction information */
    private DNSFile dnsfile;
    public Node(int id, String i, int prt) {
        this.nodeID = id;
        this.ip = i;
        this.port = prt;
        this.isActive = true;
        this.proposalNumSet = new LinkedBlockingQueue<Integer>();
        this.dnsfile = new DNSFile(String.valueOf(nodeID));
    }
    public DNSFile getDnsfile() {
		return dnsfile;
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

    public void addProposalNum(int k) {
        this.proposalNumSet.offer(k);  
    }
    public int pollProposalNum() {
        return this.proposalNumSet.poll();
    }
    public int proposalNumSetSize() {
        return this.proposalNumSet.size();
    }

    
    @Override
    public String toString() { 
        return "I am Node " + this.nodeID;
    }
}