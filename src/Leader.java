import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * Property of a leader
 * 
 *
 */
public class Leader extends UnicastRemoteObject implements LeaderIntf {
	private static final long serialVersionUID = -8997057463677705808L;
	private int ID;
    private Queue<Proposal> processQueue;
    public Leader (int lid) throws RemoteException {
        this.ID = lid;
        processQueue = new ConcurrentLinkedQueue<Proposal>();
    }
    public int getProcessQueueSize() {
        if (this.processQueue == null) {
            return 0;
        }
        return this.processQueue.size();
    }
    @Override
    public void addNewProposal(Proposal p) {
        this.processQueue.offer(p);  
    }
    public Proposal pollProposal() {
        return this.processQueue.poll();
    }
    @Override
    public String toString() { 
        return "Leader~" + this.ID;
    }
}
