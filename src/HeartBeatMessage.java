import java.io.Serializable;

/**
 * A single heartbeat message
 *
 */
public class HeartBeatMessage implements Serializable {
    private int myID;
    private String kind;  
    private String content;
    
    public HeartBeatMessage(int id, String k, String c) {
        this.myID = id;
        this.kind = k;
        this.content = c; 
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
    @Override
    public String toString() { 
        return "HeartBeatMessage: " + this.myID + " " + this.kind + " " + this.content;
    }
    
}
