import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
public class ListenerImpl extends UnicastRemoteObject implements ListenerIntf{
	private static final long serialVersionUID = -1227249378955929227L;
	private Configuration myConfig;
    private Node me;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
    private BlockingQueue<InterThreadMessage> LeaderListenerCommQueue;
    private Leader currentLeader;
    private ElectionContent electionContent;
    /**
     * Constructor
     * @param config
     * @param m
     * @throws IOException 
     */
    protected ListenerImpl(Configuration config, Node m, Leader cl, BlockingQueue<InterThreadMessage> ai,
                             BlockingQueue<InterThreadMessage> li, ElectionContent ec) throws IOException {
        super(0);
        this.myConfig = config;
        this.me = m;
        this.AcceptorListenerCommQueue = ai;
        this.LeaderListenerCommQueue = li;
        this.currentLeader = cl;
        this.electionContent = ec;
    }
    /**
     * When slave receives a hello message from master, it will print out the hello message, as well
     * as sends back a acknowledgment.
     */
    @Override
    public HeartBeatMessage HelloChat(HeartBeatMessage h) throws RemoteException {
        HeartBeatMessage mesg = h;
        System.out.println("[Recieve Hello] " + mesg);
        HeartBeatMessage helloBack = new HeartBeatMessage(this.me.getNodeID(), "hello", "helloback");
        return helloBack;
    }
    /**
     * When slaves receive a heartbeat message from master, it will add the message into the AcceptorLisenerCommQueue.
     */
    @Override
    public synchronized void LeaderHeartBeat(HeartBeatMessage h) throws RemoteException {
        HeartBeatMessage mesg = h;
//        System.out.println("[ListenerImpl][LeaderHeartBeat] [Recieve LeaderHeartBeat] " + mesg);
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
        System.out.println("[Recieve clientRequest] " + dnsentry);
//        System.out.println("[check current leader: " + this.currentLeader.getID() + "]");
        if (me.getNodeID() == this.currentLeader.getID()) {
        	/* Once receiving a new request, leader adds the request into the processQueue. */
            response = "[ I am leader, I can handle this request]"; 
            /* generate a proposal and add it into leader's request queue */
            Proposal np = new Proposal(-1, new ProposalID(), dnsentry);
            this.currentLeader.addNewProposal(np);
        } else {
            if (this.currentLeader.getID() == -1) {
                response = "[ We haven't elect a leader yet ]";
            } else {
            	/* If client sends request to the acceptor, acceptor forward the request to leader. */
            	myConfig.getListenerIntfMap().get(currentLeader.getID()).clientRequest(dnsentry);
            	response = "[ I am not leader, I will forward your request to the leader " + this.currentLeader.getID() + "]";
            }
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
    public Promise LeaderPrepareProposal(Proposal p) throws RemoteException {
        System.out.println("[Recieve LeaderPrepareProposal] " + p);
        /* Slave look into its log file to check the minProposalId, acceptedId, and acceptedValue */
        DNSFile dnsfile = me.getDnsfile();
        /* get the entry for current log id. If the slave does not have an entry, return an empty entry */
        Entry entry = dnsfile.readEntry(p.getLogId());
        boolean realPromise = false;
        if (p.getProposalId().Compare(entry.getMinProposalId()) > 0) {
        	/* update the minProposalId in log file */
        	entry.setMinProposalId(p.getProposalId());
        	realPromise = true;
        	/* slave updates the minProposalId in the entry */
        	dnsfile.writeEntry(entry);
        	System.out.println("[Recieve LeaderPrepareProposal] promise, update proposalId: " + p.getProposalId() + " entry:" + entry);
        } else {
        	System.out.println("[Recieve LeaderPrepareProposal no promise, proposalId " + p.getProposalId() +" smaller than original: " + entry.getMinProposalId() + " entry:" + entry);
        }
        Promise pro = null;
        /* If the acceptor has accepted the value, return it to the proposer */
        if (entry.getdns().hasAccepted()) {
        	System.out.println("[acceptor received prepare, already accepted] ");
        } else { 
        	/* set accptedProposal as -1 and acceptedValue as null if the node hasn't accepted any proposal */
        	System.out.println("[acceptor received prepare, haven't accepted] ");
        }
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
    public Acknlg LeaderAcceptProposal(Accept a) throws RemoteException {
        System.out.println("[Recieve LeaderAcceptProposal] " + a);
        DNSFile dnsfile = me.getDnsfile();
        Entry entry = dnsfile.readEntry(a.getLogId());
        Acknlg ack = null;
        /* Use proposer's proposerId, minUnchosedLogId to validate acceptor's log entry. 
         * To validate an log entry, set the acceptedId to be INTEGER.MAX_VALUE and remove it from proposalIdToUnchosenLogId map.
         */
        HashMap<ProposalID, Set<Integer>> map = this.me.getDnsfile().getProposalIdMapToUnchosenLogId();
        map.forEach((proposalId, set)-> {
			set.forEach(logId -> {
				System.out.print(proposalId+":"+logId+"\t");
			});
		});
        if (map.containsKey(a.getProposalID())) {
        	System.out.println("[contains key(a.getProposalID())]");
	        for (Integer unchosenLog : map.get(a.getProposalID())) {
	        	System.out.println("can " + a.getProposalID() + " validate " + unchosenLog +" ?");
	        	if (unchosenLog < a.getFirstUnchosenLogId()) {
	        		System.out.println("[LeaderAcceptProposal] validate log " + unchosenLog + " by proposer's proposal.");
	        		/* set the entry to be chosen */
	        		entry = this.me.getDnsfile().readEntry(unchosenLog);
	        		entry.setChosen();
	        		this.me.getDnsfile().writeEntry(entry);
	        		/* remove the logId from proposalIdToUnchosenLogId map. */
	        		this.me.getDnsfile().removeFromMap(a.getProposalID(), unchosenLog);
	        	}
	        }
        }
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
            System.out.println("[Recieve LeaderAcceptProposal ACK! ] " + ack + ", "+entry);
        } else {
            ack = new Acknlg(this.me.getNodeID(), entry.getMinProposalId(), false);
            System.out.println("[Recieve LeaderAcceptProposal no ack ]" + ack);
        }
        return ack;
    }
    //------------------------------Leader election----------------------
    @Override
    public boolean ElectLeaderRequest(int proid) throws RemoteException {
        System.out.println("[Recieve ElectLeaderRequest] " + proid);
        if ((this.currentLeader.getStatus() == -1) && (this.electionContent.getStatus() == 0) &&
                (this.electionContent.getBiggestCandidate() <= proid)) {
            return true;
        }
        return false;
    }
    @Override
    public synchronized boolean ElectLeaderConfirm(int proid) throws RemoteException {
        System.out.println("[Recieve ElectLeaderConfirm] " + proid);
        if ((this.currentLeader.getStatus() == -1) && (this.electionContent.getStatus() == 0) &&
                (this.electionContent.getBiggestCandidate() <= proid)) {
            this.electionContent.setBiggestCandidate(proid);
            return true;
        }
        return false;
    }
    @Override
    public synchronized void ElectLeaderVictory(int proid) throws RemoteException {
        System.out.println("[Recieve ElectLeaderVictory] " + proid);
        this.currentLeader.setStatus(1);
        this.currentLeader.setID(proid);
        this.electionContent.setStatus(1);
        this.electionContent.setBiggestCandidate(-1); //clear the candidate
    }
    
}
