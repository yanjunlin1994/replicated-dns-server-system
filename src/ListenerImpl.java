import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.io.IOException;
import java.rmi.Naming;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
public class ListenerImpl extends UnicastRemoteObject implements ListenerIntf{
    private Configuration myConfig;
    private Node me;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
    private BlockingQueue<InterThreadMessage> LeaderListenerCommQueue;
    
    private Leader currentLeader;
    private AcceptorContent myAcceptorContent;//TODO: should be stored in disk for crash recovery
                                               //together with round number
    
    /**
     * Constructor
     * @param config
     * @param m
     * @throws IOException 
     */
    protected ListenerImpl(Configuration config, Node m, Leader cl, BlockingQueue<InterThreadMessage> ai,
                             BlockingQueue<InterThreadMessage> li) throws IOException {
        super(0);
        this.myConfig = config;
        this.me = m;
        this.AcceptorListenerCommQueue = ai;
        this.LeaderListenerCommQueue = li;
        this.currentLeader = cl;
        this.myAcceptorContent = new AcceptorContent(this.me.getNodeID());
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
        System.out.println("[ListenerImpl][LeaderHeartBeat] [Recieve LeaderHeartBeat] " + mesg);
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
        System.out.println("[check current leader: " + this.currentLeader.getID() + "]");
        if (me.getNodeID() == this.currentLeader.getID()) {
        	/* Once receiving a new request, leader adds the request into the processQueue. */
            response = "[ I am leader, I can handle this request]"; 
            /* generate a proposal and add it into leader's request queue */
            Proposal np = new Proposal(-1, -1, dnsentry);
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
        if (p.getProposalId() > entry.getMinProposalId()) {
        	/* update the minProposalId in log file */
        	entry.setMinProposalId(p.getProposalId());
        	dnsfile.writeEntry(entry);
        	System.out.println("[Recieve LeaderPrepareProposal Promise!]    ");
        	System.out.println(this.myAcceptorContent);
        }
        Promise pro = null;
        if (!entry.getdns().hasAccepted()) {
        	pro = new Promise(this.me.getNodeID(), entry.getAcceptedProposalId(), entry.getdns(), false);
        } else { 
        	/* set accptedProposal as 1 and acceptedValue as null if the node hasn't accepted any proposal */
        	pro = new Promise(this.me.getNodeID(), -1, null, true);
        }
        return pro;
//        Promise pro = new Promise(this.me.getNodeID(), this.myAcceptorContent.getAcceptedProposal(), this.myAcceptorContent.getAcceptedValue());
//        if (p.getLogId() > this.myAcceptorContent.getMinProposal()) {
//            //set my MinProposal number
//            this.myAcceptorContent.setMinProposal(p.getID());
//            System.out.println("[Recieve LeaderPrepareProposal Promise!]    ");
//            System.out.println(this.myAcceptorContent);
//            pro.setIfrealPromise(true);
//            return pro;
//        } else {
//            System.out.println("[Recieve LeaderPrepareProposal No promise : (  ]     ");
//            System.out.println(this.myAcceptorContent);
//            return pro;
//        }
    }
    /**
     * Acceptor receive an accept request from leader.
     * If the acceptor's minproposal is smaller than the leader's proposal id, update its minProposalId.
     */
    @Override
    public Acknlg LeaderAcceptProposal(Accept a) throws RemoteException {
        System.out.println("[Recieve LeaderAcceptProposal] " + a);
        DNSFile dnsfile = me.getDnsfile();
        Entry entry = dnsfile.readEntry(a.getID());
        if (a.getID() > entry.getMinProposalId()) {
        	entry.setAcceptedProposalId(a.getID());
        	entry.setMinProposalId(a.getID());
        	entry.setdnsEntry(a.getValue());
        	dnsfile.writeEntry(entry);
        }
        Acknlg ack = new Acknlg(this.me.getNodeID(), entry.getMinProposalId());
        return ack;
//        Acknlg ack = new Acknlg(this.me.getNodeID(), this.myAcceptorContent.getMinProposal());
//        if (a.getID() >= this.myAcceptorContent.getMinProposal()) {
//            this.myAcceptorContent.setMinProposal(a.getID());
//            this.myAcceptorContent.setAcceptedProposal(a.getID());
//            this.myAcceptorContent.setAcceptedValue(a.getValue());
//            System.out.println("[Recieve LeaderAcceptProposal ACK! ]");
//            System.out.println(this.myAcceptorContent);
//            ack.setIfrealAcknlg(true);
//            return ack;
//        } else {
//            System.out.println("[Recieve LeaderAcceptProposal no ack ]");
//            System.out.println(this.myAcceptorContent);
//            return ack;
//        }
    }
    /**
     * Receive a commit from leader
     */
    @Override
    public int LeaderCommitProposal(Commit c) throws RemoteException {
        System.out.println("[Recieve LeaderCommitProposal] " + c);
        Commit nc = new Commit(c);
        this.myAcceptorContent.cleanContent();//clear to content in my current acceptor
        return writeToCommitLog(nc);
        
    }
    public int writeToCommitLog(Commit c) {
        //TODO: write to commitLog file
        return 0;
    }
}
