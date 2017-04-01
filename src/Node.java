
/**
 * Node class.
 */
public class Node {
    private int nodeID;
    private String ip;
    private int port;
    private boolean ifLeader;
   
    public Node(int id, String i, int prt) {
        this.nodeID = id;
        this.ip = i;
        this.port = prt;
        this.ifLeader = false;
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

    public boolean isIfLeader() {
        return ifLeader;
    }

    public void setIfLeader(boolean ifLeader) {
        this.ifLeader = ifLeader;
    }
    @Override
    public String toString() { 
        return "I am Node " + this.nodeID;
    }
}