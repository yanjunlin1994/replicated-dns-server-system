import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Property of a leader
 * 
 *
 */
public class Leader {
    private int status;
    private int ID;
    private Queue<Proposal> processQueue;
    public Leader () {
        this.status = -1;
        this.ID = -1;
        this.processQueue = new LinkedBlockingQueue<Proposal>();
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
    public int getProcessQueueSize() {
        if (this.processQueue == null) {
            return 0;
        }
        return this.processQueue.size();
    }
    public void addNewProposal(Proposal p) {
        this.processQueue.offer(p);  
    }
    public Proposal pollProposal() {
        return this.processQueue.poll();
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
