import java.io.Serializable;

/**
 * A single heartbeat message
 *
 */
public class HeartBeatMessage extends Message implements Serializable {
    private int myID;
    private String kind;  
    private String content;
    private int seqNum;
    
    public HeartBeatMessage(int id, String k, String c) {
        this.myID = id;
        this.kind = k;
        this.content = c; 
        this.seqNum = 0;
    }
    public int getMyID() {
        return myID;
    }
    public void setMyID(int myID) {
        this.myID = myID;
    }
    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getSeqNum() {
        return seqNum;
    }
    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }
    public void increSeqNum() {
        this.seqNum = this.seqNum + 1;
    }
    @Override
    public String toString() { 
        return "[HeartBeatMessage from " + this.myID + 
               " seq: " + this.seqNum + " kind: " +
                this.kind + " content: " + this.content + " ]";
    }
    
}
