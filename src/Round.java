import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * ALL Information in a single Round
 *
 */
public class Round {
    private int nodeID;
    private int RoundID;
    
    private Proposal currentProposal;
    private int promiseCount;
    private HashMap<Integer, Promise> promiseMap;
    
    private Accept acceptProposal;
    private int acceptCount;
    
    private HashMap<Integer, Acknlg> AcknlgMap;
    private boolean rejAck;
    /** record the rej's minproposal ID */
    private HashSet<ProposalID> rejAcknlgSet;
    private int logId;
    public Round(int nid, int rid, int logId) {
        this.nodeID = nid;
        this.RoundID = rid;
        this.promiseCount = 0;
        this.promiseMap = new HashMap<Integer, Promise>();
        this.acceptCount = 0;
        this.AcknlgMap = new HashMap<Integer, Acknlg>();
        this.rejAck = false;
        this.rejAcknlgSet = new HashSet<ProposalID>();
        this.logId = logId;
    }
    public int getLogId() {
    	return logId;
    }
    public int getNodeID() {
        return nodeID;
    }
    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }
    public int getRoundID() {
        return RoundID;
    }
    public void setRoundID(int roundID) {
        RoundID = roundID;
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
    //--------------- Count
    public int getPromiseCount() {
        return promiseCount;
    }
    public void setPromiseCount(int promiseCount) {
        this.promiseCount = promiseCount;
    }
    public int getAcceptCount() {
        return acceptCount;
    }
    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }
    //-------------- Get Map/Set
    public HashMap<Integer, Promise> getPromiseMap() {
        return this.promiseMap;
    }
    public HashMap<Integer, Acknlg> getAcknlgMap() {
        return this.AcknlgMap;
    }  
    public HashSet<ProposalID> getRejAcknlgSet() {
        return this.rejAcknlgSet;
    }
    
    public void setRejAck() {
        this.rejAck = true;
    }
    public boolean getRejAck() {
        return this.rejAck;
    }
    //------------ Add to Map/Set
    /**
     * add the promise to the promise map.
     * @param p
     */
    public void addPromiseMap(Promise p) {
        Promise np = new Promise(p);
        this.promiseMap.put(p.getSrc(), np);
    }
    /**
     * add the Acknlg to the Acknlg map.
     * @param a
     */
    public void addAcknlgMap(Acknlg a) {
        Acknlg na = new Acknlg(a);
        this.AcknlgMap.put(a.getSrc(), na);
    }
    /**
     * Add reject's minproposal value to RejAcknlgSet.
     * @param proposalID
     */
    public void addRejAcknlgSet(ProposalID proposalID) {
        this.rejAcknlgSet.add(proposalID);
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
     * Find max value corresponded to the promise with largest ID.
     * @return max value
     */
    public DNSEntry findPromiseMaxIDValue() {
    	/* promiseMap: <Integer, Promise> 
    	 * In the accepted promise in the promiseMap, find the source node id with the maximum accpetedId.
    	 */
    	int maxsrc = -1;
    	ProposalID maxAcceptedId = new ProposalID();
    	for (Promise p: this.promiseMap.values()) {
    		if (p.getacceptedValue().hasAccepted() && p.getAcceptedId().Compare(maxAcceptedId) > 0) {
    			maxAcceptedId = p.getAcceptedId();
    			maxsrc = p.getSrc();
    		}
    	}
    	if (maxsrc == -1) {
    		System.out.println("[Round Class] [findPromiseMaxIDValue] no accept dnsEntry ");
    		return new DNSEntry();
    	} else {
    	    System.out.println("[Round Class] [findPromiseMaxIDValue] value is: " +
                    this.promiseMap.get(maxsrc).getacceptedValue());
	        return this.promiseMap.get(maxsrc).getacceptedValue();     
    	}
    }
    /**
     * If any rejects, find the largest minproposal ID in those rejects.
     * @return 
     */
    public ProposalID findRejMaxMinproposalID() {
        if (this.rejAcknlgSet.size() < 1) {
            return new ProposalID();
        }
        ProposalID maxMinproposal = Collections.max(this.rejAcknlgSet);
        System.out.println("[Round Class] [findRejMaxMinproposalID] minproposal ID is: " +
                maxMinproposal);
        return maxMinproposal;
    }
}
