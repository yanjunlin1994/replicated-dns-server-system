import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
public class ListenerImpl extends UnicastRemoteObject implements ListenerIntf{
	private static final long serialVersionUID = -1227249378955929227L;
	private static final int LEADER_LOGID = -10;
	private Configuration myConfig;
    private Node me;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
    private Entry leaderEntry;
    private AtomicInteger leader;
    private LeaderIntf leaderListener;
    /**
     * Constructor
     * @param config
     * @param m
     * @throws IOException 
     */
    protected ListenerImpl(Configuration config, Node m, BlockingQueue<InterThreadMessage> ai) throws IOException {
        super(0);
        this.myConfig = config;
        this.me = m;
        this.AcceptorListenerCommQueue = ai;
        leaderEntry = new Entry(LEADER_LOGID, new ProposalID(), new ProposalID(), new DNSEntry("leader", "null", "null"));
        leader = new AtomicInteger(-1);
    }
    /**
     * When slaves receive a heartbeat message from master, it will add the message into the AcceptorLisenerCommQueue.
     */
    @Override
    public synchronized void LeaderHeartBeat(HeartBeatMessage h) throws RemoteException {
        HeartBeatMessage mesg = h;
        InterThreadMessage lhb= new InterThreadMessage(mesg.getMyID(), this.me.getNodeID(), 
                                                "HeartBeatMessage", mesg.toString(), mesg.getSeqNum());
        this.AcceptorListenerCommQueue.add(lhb);
        return;
    }
    /**
     * When machine receives a user request:
     * 1. If it is the master
     * 2. If it is not the master
     * 3. If the cluster does not have a master yet
     */
    @Override
    public synchronized String clientRequest(DNSEntry dnsentry) throws RemoteException {
        String response;
//      System.out.println("[Recieve clientRequest] " + dnsentry);
        int leaderId = leader.get();
        if (leaderId == -1) {
        	response = "[leader not elected]";
        } else {
        	leaderListener.addNewProposal(new Proposal(-1, new ProposalID(), dnsentry));
        	response = "[forward to node " + leaderId +"]";
        }
        return response;
    }
    /**
     * Receive a prepare proposal from leader.
     * First, slave will look into its log file to see check its minProposalId, accepted Id and acceptedValue.
     * If the proposer id is larger than the acceptor's minProposal, set acceptor's minProposal to this proposal.
     * Return the acceptor's <acceptedProposal, acceptedValue>, return <null, null> if it hasn't accepted any.
     */
    @Override
    public synchronized Promise LeaderPrepareProposal(Proposal p) throws RemoteException {
    	/* If the proposal is for leader election */
    	boolean realPromise = false;
    	if (isLeaderPrepareProposal(p)) {
    		if (p.getProposalId().Compare(leaderEntry.getMinProposalId()) > 0) {
    			leaderEntry.setMinProposalId(p.getProposalId());
    			realPromise = true;
    			System.out.println("[Acpt receive Prepare] PROMISE. proposal: " + p + ", entry:" + leaderEntry);
    		} else {
    			/* no promise */
    			System.out.println("[Acpt receive Preparel NO promise. proposalId " + p.getProposalId() +" smaller than original: " + leaderEntry.getMinProposalId() + ", entry:" + leaderEntry);
    		}
    		Promise pro = new Promise(this.me.getNodeID(), leaderEntry.getAcceptedProposalId(), leaderEntry.getdns(), realPromise, false);
    		return pro;
    	}
        /* Slave look into its log file to check the minProposalId, acceptedId, and acceptedValue */
        DNSFile dnsfile = me.getDnsfile();
        /* get the entry for current log id. If the slave does not have an entry, return an empty entry */
        Entry entry = dnsfile.readEntry(p.getLogId());
        
        if (p.getProposalId().Compare(entry.getMinProposalId()) > 0) {
        	/* update the minProposalId in log file */
        	entry.setMinProposalId(p.getProposalId());
        	realPromise = true;
        	/* slave updates the minProposalId in the entry */
        	dnsfile.writeEntry(entry);
        	System.out.println("[Acpt receive Prepare] PROMISE. proposal: " + p + ", entry:" + entry);
        } else {
        	System.out.println("[Acpt receive Preparel NO promise. proposalId " + p.getProposalId() +" smaller than original: " + entry.getMinProposalId() + ", entry:" + entry);
        }
        Promise pro = null;
        /* If the proposal's log entry is larger than node's noMoreAcceptedLogId, then noMoreAcceptedValue is set to true */
        boolean noMoreAcceptedValue = (me.getDnsfile().getNoMoreAcceptedLogId() <= p.getLogId());
        pro = new Promise(this.me.getNodeID(), entry.getAcceptedProposalId(), entry.getdns(), realPromise, noMoreAcceptedValue);
        return pro;
    }
    /**
     * Acceptor receive an accept request from leader.
     * If the acceptor's minproposal is smaller than the leader's proposal id, update its minProposalId.
     */
    @Override
    public synchronized Acknlg LeaderAcceptProposal(Accept a) throws RemoteException {
    	Acknlg ack = null;
    	/* If the proposal is for leader election */
    	if (isLeaderAcceptProposal(a)) {
    		if (a.getProposalID().Compare(leaderEntry.getMinProposalId()) >= 0) {
    			leaderEntry.setAcceptedProposalId(a.getProposalID());
    			leaderEntry.setMinProposalId(a.getProposalID());
    			leaderEntry.setdnsEntry(a.getValue());
    			ack = new Acknlg(this.me.getNodeID(), leaderEntry.getMinProposalId(), true);
    			System.out.println("[Recieve LeaderAcceptProposal ACK! ] "+ ack + ", accept:" + a + ", entry: " + leaderEntry);
    		} else {
    			ack = new Acknlg(this.me.getNodeID(), leaderEntry.getMinProposalId(), false);
    			System.out.println("[Recieve LeaderAcceptProposal no ack ]" + ack + ", accept:" + a + ", entry: " + leaderEntry);
    		}
    		return ack;
    	}
        System.out.println("[Recieve LeaderAcceptProposal] " + a);
        DNSFile dnsfile = me.getDnsfile();
        /* Entry object entry is at logId */
        Entry entry = dnsfile.readEntry(a.getLogId()), unchosenEntry = null;
        /* Use proposer's proposerId, minUnchosedLogId to validate acceptor's log entry. 
         * To validate an log entry, set the acceptedId to be INTEGER.MAX_VALUE and remove it from proposalIdToUnchosenLogId map.
         */
        HashMap<ProposalID, Set<Integer>> map = this.me.getDnsfile().getProposalIdMapToUnchosenLogId();
        int maxValidateLog = -1;
        if (map.containsKey(a.getProposalID())) {
	        for (Integer unchosenLog : map.get(a.getProposalID())) {
	        	if (unchosenLog < a.getFirstUnchosenLogId()) {
//	        		System.out.println("[LeaderAcceptProposal] validate log " + unchosenLog + " by accept: " + a);
	        		/* set the entry to be chosen */
	        		unchosenEntry = this.me.getDnsfile().readEntry(unchosenLog);
	        		unchosenEntry.setChosen();
	        		
	        		if (unchosenEntry.getLogId() > maxValidateLog) {
	        			maxValidateLog = unchosenEntry.getLogId();
	        		}
	        		this.me.getDnsfile().writeEntry(unchosenEntry);
	        		/* remove the logId from proposalIdToUnchosenLogId map. */
	        		this.me.getDnsfile().removeFromMap(a.getProposalID(), unchosenLog);
	        	}
	        }
        }
        this.me.getDnsfile().incrementMinUnchosenLogId(maxValidateLog);
        
        /* If the proposer's proposerId is larger than acceptor's minProposal */
        if (a.getProposalID().Compare(entry.getMinProposalId()) >= 0) {
        	/* If an acceptor accepts a value, check if its noMoreAcceptedLogId needs to be updated */
        	dnsfile.updateNoMoreAcceptedLogId(a.getLogId());
        	/* set acceptor's acceptedId, acceptedValue, and minProposalId */
            entry.setAcceptedProposalId(a.getProposalID());
            entry.setMinProposalId(a.getProposalID());
            entry.setdnsEntry(a.getValue());
            dnsfile.writeEntry(entry);
            ack = new Acknlg(this.me.getNodeID(), entry.getMinProposalId(), true);
            /* add the unchosen log into proposalIdToUnchosenLogId map */
            this.me.getDnsfile().addToMap(a.getProposalID(), a.getLogId());
            System.out.println("[Recieve LeaderAcceptProposal ACK! ] "+ ack + ", accept:" + a + ", entry: " + entry);
        } else {
            ack = new Acknlg(this.me.getNodeID(), entry.getMinProposalId(), false);
            System.out.println("[Recieve LeaderAcceptProposal no ack ]" + ack + ", accept:" + a + ", entry: " + entry);
        }
//        System.out.println("[return leaderAcceptProposal]");
        return ack;
    }
    public boolean isLeaderPrepareProposal(Proposal p) {
    	if (new String(p.getDnsentry().getDns()).trim().equals("leader")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    public boolean isLeaderAcceptProposal(Accept a) {
    	if (new String(a.getValue().getDns()).trim().equals("leader")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    /* If the leader election completes, the RequestLeader thread will notify ListenImpl */
    @Override
    public synchronized void chosenLeader(Entry leaderEntry, int port, int leaderid) {
    	this.leaderEntry = new Entry(leaderEntry.getLogId(), leaderEntry.getMinProposalId(), leaderEntry.getAcceptedProposalId(), leaderEntry.getdns());
//    	this.leaderEntry.setChosen();
    	System.out.println("[ListenImpl.set leader] " + leaderEntry);
    	int id = Integer.valueOf(new String(leaderEntry.getdns().getIp()).trim());
    	leader.set(id);
//    	String lookupName = "//localhost:" + (this.myConfig.getNodeMap().get(id).getPort() + 1) + "/Leader" + id;
    	String lookupName = "//localhost:" + port + "/Leader" + leaderid;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        try {
			leaderListener = (LeaderIntf) Naming.lookup(lookupName);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.err.println("Unable to connect to leader");
		}
    }
    /* If the leader is out of connection, the AcceptorRoutine thread will notify ListenImpl */
    @Override
    public synchronized void unchosenLeader() {
    	leaderEntry = new Entry(LEADER_LOGID, new ProposalID(), new ProposalID(), new DNSEntry("leader", "null", "null"));
    	System.out.println("[LI.unchosen]" + leaderEntry);
    	leader.set(-1);
    }
}
