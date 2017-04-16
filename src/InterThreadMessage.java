/**
 * The message between threads.
 *
 */
public class InterThreadMessage {
    private int src;
    private int dest;
    private String kind; 
    /** Anytype content */
    private String content;
    private int seqNum;
    
    public InterThreadMessage(int s, int d, String k, String c, int sq) {
        this.src = s;
        this.dest = d;
        this.kind = k;
        this.content = c; 
        this.seqNum = sq;
    }

    

    public int getSrc() {
        return src;
    }



    public void setSrc(int src) {
        this.src = src;
    }



    public int getDest() {
        return dest;
    }



    public void setDest(int dest) {
        this.dest = dest;
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
        StringBuilder sb = new StringBuilder();
        sb.append("{InterThreadMessage No.");
        sb.append(this.seqNum);
        sb.append(" from ");
        sb.append(this.src);
        sb.append(" to ");
        sb.append(this.dest);
        sb.append(" kind: ");
        sb.append(this.kind);
        sb.append(" content: ");
        sb.append(this.content);
        sb.append(" }");
        return sb.toString();
    }
    
}
