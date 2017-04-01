import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * ALL Information in a single Round
 *
 */
public class Round {
    private int nodeID;
    private Proposal currentProposal;
    private int promiseCount;
    private HashMap<Integer, Promise> promiseMap;
    private Accept acceptProposal;
    private int acceptCount;
    private HashMap<Integer, Acknlg> AcknlgMap;
    
    
   
    public Round(int id) {
        this.nodeID = id;
        this.promiseCount = 0;
        this.promiseMap = new HashMap<Integer, Promise>();
        this.AcknlgMap = new HashMap<Integer, Acknlg>();
    }


    public int getNodeID() {
        return nodeID;
    }


    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }


    public Proposal getCurrentProposal() {
        return currentProposal;
    }


    public void setCurrentProposal(Proposal currentProposal) {
        this.currentProposal = currentProposal;
    }


    

    public Accept getAcceptProposal() {
        return acceptProposal;
    }


    public void setAcceptProposal(Accept acceptProposal) {
        this.acceptProposal = acceptProposal;
    }


    public int getAcceptCount() {
        return acceptCount;
    }
    public int getPromiseCount() {
        return promiseCount;
    }
    public void setPromiseCount(int promiseCount) {
        this.promiseCount = promiseCount;
    }


    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }
    public HashMap<Integer, Promise> getPromiseMap() {
        return this.promiseMap;
    }
    
    public HashMap<Integer, Acknlg> getAcknlgMap() {
        return this.AcknlgMap;
    }
    


    
    /**
     * add the promise to the promise map
     * @param p
     */
    public void addPromiseMap(Promise p) {
        Promise np = new Promise(p);
        this.promiseMap.put(p.getSrc(), np);
    }
    /**
     * add the Acknlg to the Acknlg map
     * @param a
     */
    public void addAcknlgMap(Acknlg a) {
        Acknlg na = new Acknlg(a);
        this.AcknlgMap.put(a.getSrc(), na);
    }
    /**
     * increment the promise count
     */
    public void increPromiseCount() {
        this.promiseCount = this.promiseCount + 1;
    }
    /**
     * increment the accept count
     */
    public void increAcceptCount() {
        this.acceptCount = this.acceptCount + 1;
    }
    /**
     * 
     * @return
     */
    public String findPromiseMaxIDValue() {
        int maxsrc = 0;
        for (Promise p : this.promiseMap.values()) {
            if ((this.promiseMap.get(maxsrc) == null) || 
                    (p.getID() > (this.promiseMap.get(maxsrc)).getID())) {
                maxsrc = p.getSrc();
            }
        }
        return this.promiseMap.get(maxsrc).getValue();
        
    }
    

}
