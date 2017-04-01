import java.util.ArrayDeque;
/**
 * Node class.
 */
public class Node {
    private int nodeID;
    private String ip;
    private int port;
    /* proposal number set (like 1, 11, 21, ...) */
    private ArrayDeque<Integer> proposalNumSet;
   
    public Node(int id, String i, int prt) {
        this.nodeID = id;
        this.ip = i;
        this.port = prt;
        this.proposalNumSet = new ArrayDeque<Integer>();
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
    public void addProposalNum(int k) {
        this.proposalNumSet.offer(k);  
    }
    public int pollProposalNum() {
        return this.proposalNumSet.poll();
    }

    
    @Override
    public String toString() { 
        return "I am Node " + this.nodeID;
    }
}