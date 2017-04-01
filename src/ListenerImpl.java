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
    /**
     * Constructor
     * @param config
     * @param m
     * @throws RemoteException
     */
    protected ListenerImpl(Configuration config, Node m, Leader l, AcceptorContent acp) throws RemoteException {
        super(0);
        this.myConfig = config;
        this.me = m;
        this.currentLeader = l;
        this.myAcceptorContent = acp;
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
        return;
    }
    /**
     * Receive client request
     */
    @Override
    public synchronized String clientRequest(String st) throws RemoteException {
        String response;
        System.out.println("[Recieve clientRequest] " + st);
        System.out.println("[check current leader: " + this.currentLeader.getID() + "]");
        if (me.getNodeID() == this.currentLeader.getID()) {
            response = "[ I am leader, I can handle this request]";
            int newProposalNum = this.me.pollProposalNum();
            System.out.println("[Recieve clientRequest] handle this request newProposalNum " + newProposalNum);
            Proposal np = new Proposal(newProposalNum, st);
            this.currentLeader.addNewProposal(np);
            
            
        } else {
            if (this.currentLeader.getID() == -1) {
                response = "[ We haven't elect a leader yet ]";
                return response;
            }
            response = "[ I am not leader, I can't handle this request, forward it to leader " + this.currentLeader.getID() +"]";
        }
        return response;
    }
    /**
     * Receive proposal from leader
     */
    @Override
    public Promise LeaderPrepareProposal(Proposal p) throws RemoteException {
        System.out.println("[Recieve LeaderPrepareProposal] " + p);
        Promise pro = new Promise(this.myAcceptorContent.getAcceptedProposal(), this.myAcceptorContent.getAcceptedValue());
        if (p.getID() > this.myAcceptorContent.getMinProposal()) {
            //set my id
            this.myAcceptorContent.setMinProposal(p.getID());
            System.out.println("[Promise!]");
            pro.setIfrealPromise(true);
            return pro;
        } else {
            return pro;
        }
        
    }
}