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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
public class MessagePasser {
    private Configuration myConfig;
    private Node me;
    private int myID;
    private ListenerImpl listener;
    private Leader currentLeader;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
    private BlockingQueue<InterThreadMessage> LeaderListenerCommQueue;
    private BlockingQueue<InterThreadMessage> AcceptorMpCommQueue;
    private BlockingQueue<InterThreadMessage> LeaderMpCommQueue;
    /**
     * Constructor
     * @param configuration_filename
     * @param ID
     */
    public MessagePasser(String configuration_filename, int ID) {
        this.myConfig = new Configuration(configuration_filename);
        this.myID = ID;
        this.me = this.myConfig.getNodeMap().get(ID);  
        this.currentLeader = new Leader();
        this.AcceptorListenerCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.LeaderListenerCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.AcceptorMpCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.LeaderMpCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        /*
         * Start RPC listener
         */
        try {
            this.listener = new ListenerImpl(this.myConfig, this.me, this.currentLeader,
                            this.AcceptorListenerCommQueue, this.LeaderListenerCommQueue);
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
    public synchronized void runNow() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10); //wait for other DNS replicas join in
        
        this.myConfig.updateListenerIntfMap(this.myID);
        
        TimeUnit.SECONDS.sleep(8);

        int result = -1;
        while (true) {
            this.LeaderElectionSection();
            TimeUnit.SECONDS.sleep(2);
            result = this.LeaderAcceptorBranch();
            if (result == 2) {
                System.out.println("[mp][run now] welcome back! Leader fails, So New Leader Election!");
                this.myConfig.removeNode(this.currentLeader.getID());
                TimeUnit.SECONDS.sleep(8);
            }
        }
    }
    public int LeaderAcceptorBranch() {
        if (this.myID == this.currentLeader.getID()) {
            return this.LeaderEntrance(); 
        }
        else {
            return this.AcceptorEntrance();
        }
    }
    public int LeaderEntrance() {
        Thread myleaderRoutine = new Thread(new LeaderRoutine(this.myID, this.myConfig, this.currentLeader, this.LeaderListenerCommQueue, this.LeaderMpCommQueue));
        myleaderRoutine.start();
        return this.waitLeaderRoutine();
    }
    public int AcceptorEntrance() {
        Thread myAcceptorRoutine = new Thread(new AcceptorRoutine(this.myID, this.myConfig, this.AcceptorListenerCommQueue, this.AcceptorMpCommQueue));
        myAcceptorRoutine.start();
        return this.waitAcceptorRoutine();      
    } 
    public synchronized int waitLeaderRoutine() {
        while (true) {
            if (this.LeaderMpCommQueue.size() > 0) {
                InterThreadMessage newMessage = this.LeaderMpCommQueue.poll();
                System.out.println("[mp][waitLeaderRoutine] receive message from leader routine " + newMessage); 
                return 1;
            }
            /** sleep for efficiency */
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public synchronized int waitAcceptorRoutine() {
        while (true) {
            if (this.AcceptorMpCommQueue.size() > 0) {
                InterThreadMessage newMessage = this.AcceptorMpCommQueue.poll();
                System.out.println("[mp][waitAcceptorRoutine] receive message from acceptor routine " + newMessage); 
                return 2;
            }
            /** sleep for efficiency */
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
