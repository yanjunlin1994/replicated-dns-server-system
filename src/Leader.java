import java.util.ArrayDeque;
/**
 * Property of a leader
 * 
 *
 */
public class Leader {
    private int status;
    private int ID;
    private ArrayDeque<Proposal> processQueue;
    public Leader () {
        this.status = -1;
        this.ID = -1;
        this.processQueue = new ArrayDeque<Proposal>();
    }
    public Leader (int lid) {
        this.status = -1;
        this.ID = lid;
    }
    public int getStatus() {
        return this.status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getID() {
        return this.ID;
    }
    public void setID(int iD) {
        this.ID = iD;
    }
    public ArrayDeque<Proposal> getProcessQueue() {
        return this.processQueue;
    }
    /**
     * clean the informaton in leader
     */
    public void clean() {
        this.status = -1;
        this.ID = -1;
        this.processQueue.clear();
    }
    @Override
    public String toString() { 
        return "Leader~" + this.ID;
    }
}
