import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.Naming;
import java.util.Map;
import java.util.HashMap;
public class ListenerImpl extends UnicastRemoteObject implements ListenerIntf{
    private Configuration myConfig;
    private Node me;
    private Leader currentLeader;
    private AcceptorContent myAcceptorContent;
    private CheckLeader checkLeader;
    /**
     * Constructor
     * @param config
     * @param m
     * @throws RemoteException
     */
    protected ListenerImpl(Configuration config, Node m, Leader l, AcceptorContent acp, CheckLeader chkleader) throws RemoteException {
        super(0);
        this.myConfig = config;
        this.me = m;
        this.currentLeader = l;
        this.myAcceptorContent = acp;
        this.checkLeader = chkleader;
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
    public void LeaderHeartBeat(HeartBeatMessage h) throws RemoteException {
        HeartBeatMessage mesg = h;
        System.out.println("[Recieve LeaderHeartBeat] " + mesg);
        checkLeader.addHeartbeat();
        //TODO:
        //if an acceptor doesn't receive heartbeat message from leader for xx seconds. Elect a new leader
        return;
    }
    /**
     * Receive client request
     *
     */
     //TODO:accpetor should forward request to leader
     //RPC leader add to queue
    @Override
    public synchronized String clientRequest(String st) throws RemoteException {
        String response;
        System.out.println("[Recieve clientRequest] " + st);
        System.out.println("[check current leader: " + this.currentLeader.getID() + "]");
        if (me.getNodeID() == this.currentLeader.getID()) {
        	/* Once receiving a new request, leader adds the request into the processQueue. */
            response = "[ I am leader, I can handle this request]";
            int newProposalNum = this.myConfig.getNodeMap().get(me.getNodeID()).pollProposalNum();
            System.out.println("[Recieve clientRequest] handle this request newProposalNum " + newProposalNum);
            Proposal np = new Proposal(newProposalNum, st);
            this.currentLeader.addNewProposal(np);
        } else {
            if (this.currentLeader.getID() == -1) {
                response = "[ We haven't elect a leader yet ]";
                return response;
            } else {
            	/* If client sends request to the acceptor, acceptor forward the request to leader. */
            	myConfig.getListenerIntfMap().get(currentLeader).clientRequest(st);
//              response = "[ I am not leader, I can't handle this request, forward it to leader " + this.currentLeader.getID() +"]";
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
}