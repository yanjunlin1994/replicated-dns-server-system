import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.*;
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
     * @throws RemoteException
     */
    protected ListenerImpl(Configuration config, Node m, Leader cl, BlockingQueue<InterThreadMessage> ai,
                             BlockingQueue<InterThreadMessage> li) throws RemoteException {
        super(0);
        this.myConfig = config;
        this.me = m;
        this.AcceptorListenerCommQueue = ai;
        this.LeaderListenerCommQueue = li;
        this.currentLeader = cl;
        this.myAcceptorContent = new AcceptorContent(this.me.getNodeID());
    }
    /**
     * Receive Hello message
     */
    @Override
    public HeartBeatMessage HelloChat(HeartBeatMessage h) throws RemoteException {
        HeartBeatMessage mesg = h;
        System.out.println("[Recieve Hello] " + mesg);
        HeartBeatMessage helloBack = new HeartBeatMessage(this.me.getNodeID(), "hello", "helloback");
        return helloBack;
    }
    /**
     * Receive Leader HeartBeat
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
     * Receive client request
     *
     */
    @Override
    public synchronized String clientRequest(String st) throws RemoteException {
        String response;
        System.out.println("[Recieve clientRequest] " + st);
        System.out.println("[check current leader: " + this.currentLeader.getID() + "]");
        if (me.getNodeID() == this.currentLeader.getID()) {
        	/* Once receiving a new request, leader adds the request into the processQueue. */
            response = "[ I am leader, I can handle this request]"; 
            Proposal np = new Proposal(-1, st);
            this.currentLeader.addNewProposal(np);
        } else {
            if (this.currentLeader.getID() == -1) {
                response = "[ We haven't elect a leader yet ]";
            } else {
            	/* If client sends request to the acceptor, acceptor forward the request to leader. */
            	myConfig.getListenerIntfMap().get(currentLeader.getID()).clientRequest(st);
            	response = "[ I am not leader, I will forward your request to the leader " + this.currentLeader.getID() + "]";
            }
        }
        return response;
    }
    /**
     * Receive proposal from leader
     */
    @Override
    public Promise LeaderPrepareProposal(Proposal p) throws RemoteException {
        System.out.println("[Recieve LeaderPrepareProposal] " + p);
        Promise pro = new Promise(this.me.getNodeID(), this.myAcceptorContent.getAcceptedProposal(), this.myAcceptorContent.getAcceptedValue());
        if (p.getID() > this.myAcceptorContent.getMinProposal()) {
            //set my MinProposal number
            this.myAcceptorContent.setMinProposal(p.getID());
            System.out.println("[Recieve LeaderPrepareProposal Promise!]    ");
            System.out.println(this.myAcceptorContent);
            pro.setIfrealPromise(true);
            return pro;
        } else {
            System.out.println("[Recieve LeaderPrepareProposal No promise : (  ]     ");
            System.out.println(this.myAcceptorContent);
            return pro;
        }
        
    }
    /**
     * Receive a accept from leader
     */
    @Override
    public Acknlg LeaderAcceptProposal(Accept a) throws RemoteException {
        System.out.println("[Recieve LeaderAcceptProposal] " + a);
        Acknlg ack = new Acknlg(this.me.getNodeID(), this.myAcceptorContent.getMinProposal());
        if (a.getID() >= this.myAcceptorContent.getMinProposal()) {
            this.myAcceptorContent.setMinProposal(a.getID());
            this.myAcceptorContent.setAcceptedProposal(a.getID());
            this.myAcceptorContent.setAcceptedValue(a.getValue());
            System.out.println("[Recieve LeaderAcceptProposal ACK! ]");
            System.out.println(this.myAcceptorContent);
            ack.setIfrealAcknlg(true);
            return ack;
        } else {
            System.out.println("[Recieve LeaderAcceptProposal no ack ]");
            System.out.println(this.myAcceptorContent);
            return ack;
        }
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
