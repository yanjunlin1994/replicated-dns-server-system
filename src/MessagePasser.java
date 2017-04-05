import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class MessagePasser {
    private Configuration myConfig;
    private Node me;
    private int myID;
    private ListenerImpl listener;
    private Leader currentLeader;
    private AcceptorContent myAcceptorContent;
    private AcceptorRoutine myAcceptorRoutine;
//    private DebugLog mylog;
    public MessagePasser(String configuration_filename, int ID) {
        this.myConfig = new Configuration(configuration_filename);
        this.myID = ID;
        this.me = this.myConfig.getNodeMap().get(ID);  
//        this.mylog = new DebugLog(me.getNodeID());
        this.currentLeader = new Leader();
        this.myAcceptorContent = new AcceptorContent();
	this.myAcceptorRoutine = new AcceptorRoutine(this.myID, this.myConfig, this);
        /*
         * Start RPC listener
         */
        try {
            this.listener = new ListenerImpl(this.myConfig, this.me, this.currentLeader, this.myAcceptorContent, this.myAcceptorRoutine);
            LocateRegistry.createRegistry(Integer.valueOf(this.me.getPort()));
            Naming.rebind("//localhost:" + this.me.getPort() + "/Listener" + me.getNodeID(), listener);
            System.out.println("Listener " + this.myID + " is listening on port:" + this.me.getPort());
        } catch(RemoteException e) {
            e.printStackTrace();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
    }
    /**
     * RUN entry
     * @throws InterruptedException
     */
    public void runNow() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10); //wait for other DNS replicas join in
        
        this.myConfig.updateListenerIntfMap(this.myID);
        
        TimeUnit.SECONDS.sleep(8);
        this.LeaderElectionSection();
        
        TimeUnit.SECONDS.sleep(3);
        /* open another thread to send heart beat message if I am leader */
        if (this.myID == this.currentLeader.getID()) {
            Thread myleaderRoutine = new Thread(new LeaderRoutine(this.myID, this.myConfig, this.currentLeader));
            myleaderRoutine.start();  
        } 
        /* open another thread to receive heart beat message from leader if I am acceptor */
        else {
            Thread myAcceptorRoutine = new Thread(this.myAcceptorRoutine);
            myAcceptorRoutine.start();
        }
    }
    /**
     * Elect a new leader according to id
     * @param currentld
     */
    public synchronized int LeaderElectionSection() {
        int nextLeaderID = -1;
        nextLeaderID = this.myConfig.getNextLeader(this.currentLeader.getID());
        this.currentLeader.clean();
        this.currentLeader.setID(nextLeaderID);
        if (this.myID == nextLeaderID) {
            System.out.println("[LeaderElectionSection] I am the leader!"); 
        }
        System.out.println("[LeaderElectionSection] leader is:" + this.currentLeader);  
        return nextLeaderID;
    }
}
