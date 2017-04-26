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
    private ElectionContent electionContent;
    /**
     * Constructor
     * @param configuration_filename
     * @param ID
     * @throws IOException 
     */
    public MessagePasser(String configuration_filename, int ID) throws IOException {
        this.myConfig = new Configuration(configuration_filename);         
        this.myID = ID;
        this.me = this.myConfig.getNodeMap().get(ID);  
        /* set the dns file for current node */
        this.me.setDnsfile(new DNSFile(this.myID)); 
        this.currentLeader = new Leader();
        this.AcceptorListenerCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.LeaderListenerCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.AcceptorMpCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.LeaderMpCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.electionContent = new ElectionContent();
        /*
         * Start RPC listener
         */
        try {
            this.listener = new ListenerImpl(this.myConfig, this.me, this.currentLeader,
                            this.AcceptorListenerCommQueue, this.LeaderListenerCommQueue, this.electionContent);
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
        this.LeaderElectionSection();
        TimeUnit.SECONDS.sleep(2);
        while (true) {   
            result = this.LeaderAcceptorBranch();
            if (result == 2) {
<<<<<<< HEAD
                
                System.out.println("[mp][run now] welcome back! Leader fails, So New Leader Election!");
                TimeUnit.SECONDS.sleep(7);
                this.currentLeader.setStatus(-1);         
                this.electionContent.setStatus(0);
//                this.myConfig.removeNode(this.currentLeader.getID());
                this.currentLeader.setID(-1);  
                this.runForElectionEntrance();
                TimeUnit.SECONDS.sleep(5);
                if (this.electionContent.getBiggestCandidate() == this.myID) {
                    this.broadcastVictory();
                }   
                this.electionContent.clear();
=======
                System.out.println("[mp][run now] welcome back! Leader fails, So New Leader Election!");
                TimeUnit.SECONDS.sleep(10);
                this.currentLeader.setStatus(-1);
                this.currentLeader.setID(-1);          
                this.electionContent.setStatus(0);
                this.myConfig.removeNode(this.currentLeader.getID());
                this.runForElectionEntrance();
                TimeUnit.SECONDS.sleep(8);
                if (this.electionContent.getBiggestCandidate() == this.myID) {
                    this.broadcastVictory();
                }
>>>>>>> 17644eb8cf72c569d38615c73b8407cc8b06540c
            }
        }
    }
    /**
     * Decide which branch the processor is going to.
     * @return
     */
    public int LeaderAcceptorBranch() {
        if (this.myID == this.currentLeader.getID()) {
        	System.out.println("[LeaderAcceptorBranch] LeaderEntrance " + this.myID);
            return this.LeaderEntrance(); 
        }
        else {
        	System.out.println("[LeaderAcceptorBranch] AcceptorEntrance " + this.myID);
            return this.AcceptorEntrance();
        }
    }
    /**
     * Open a new leader routine thread.
     * @return
     */
    public int LeaderEntrance() {
        Thread myleaderRoutine = new Thread(new LeaderRoutine(this.myID, this.myConfig, this.currentLeader, this.LeaderListenerCommQueue, this.LeaderMpCommQueue));
        myleaderRoutine.start();
        return this.waitLeaderRoutine();
    }
    /**
     * Open an acceptor thread routine.
     * @return
     */
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
    //--------------------------------------Election------------------------------------------
    /**
     * Elect a new leader according to id
     * @param currentld
     */
    public synchronized int LeaderElectionSection() {
        int nextLeaderID = -1;
        nextLeaderID = this.myConfig.getNextLeader(this.currentLeader.getID());
        this.currentLeader.clean();
        this.currentLeader.setID(nextLeaderID);
        this.currentLeader.setStatus(1);
        if (this.myID == nextLeaderID) {
            System.out.println("[LeaderElectionSection] I am the leader!"); 
        }
        System.out.println("[LeaderElectionSection] leader is:" + this.currentLeader);  
        return nextLeaderID;
    }
    //------------------- entrance for running for leader
    public void runForElectionEntrance() {
        if (this.ProposeToBeLeader() != 1) {
<<<<<<< HEAD
            System.out.println("[runForElectionEntrance] first stage return"); 
            return;
        }
        if (this.ConfirmToBeLeader() != 1) {
            System.out.println("[runForElectionEntrance] second stage return"); 
=======
            return;
        }
        if (this.ConfirmToBeLeader() != 1) {
>>>>>>> 17644eb8cf72c569d38615c73b8407cc8b06540c
            return;
        }
        
    }
    //------------------- First stage of running for leader
    public synchronized int ProposeToBeLeader() {
        // set the biggest candidate id to be my ID if myID is larger that candidate id
        if (this.electionContent.getBiggestCandidate() > this.myID) {
            return 0;
        } else {
            this.electionContent.setBiggestCandidate(this.myID);
        }   
        //set my proposal to other nodes to verify that the old leader can no longer be reached by a majority
        int agreeCount = 0;
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            try {
                boolean agree = lisnode.ElectLeaderRequest(this.myID);
                System.out.println("[MP] [WantToBeLeader] first stage agree Recived from " + noid + ": " + agree);
                if (agree) {
                    agreeCount++;
                }
            } catch (Exception e) {
                System.err.println("[MP] [WantToBeLeader] Someone loses connection");
<<<<<<< HEAD
//                this.myConfig.removeNode(noid);
                continue;//continue to other listeners
            }
        }
        if (this.electionContent.getBiggestCandidate() <= this.myID) {
            agreeCount++;
        }
        if (agreeCount >= (this.getMajorityNumber())) {
            System.out.println("[MP] [WantToBeLeader] agreeCount: " + agreeCount);
=======
                this.myConfig.removeNode(noid);
                continue;//continue to other listeners
            }
        }   
        if (agreeCount >= this.getMajorityNumber()) {
>>>>>>> 17644eb8cf72c569d38615c73b8407cc8b06540c
           return 1;
        }
        return 0;
    }
    //------------------- Second stage of running for leader
<<<<<<< HEAD
    public synchronized int ConfirmToBeLeader() {
=======
    public int ConfirmToBeLeader() {
>>>>>>> 17644eb8cf72c569d38615c73b8407cc8b06540c
        int agreeCount = 0;
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            try {
                boolean agree = lisnode.ElectLeaderConfirm(this.myID);
                System.out.println("[MP] [ConfirmToBeLeader] second stage agree Recived from " + noid + ": " + agree);
                if (agree) {
                    agreeCount++;
                }
            } catch (Exception e) {
                System.err.println("[MP] [ConfirmToBeLeader] Someone loses connection");
<<<<<<< HEAD
//                this.myConfig.removeNode(noid);
                continue;//continue to other listeners
            }
        }
        if (this.electionContent.getBiggestCandidate() <= this.myID) {
            agreeCount++;
        }
=======
                this.myConfig.removeNode(noid);
                continue;//continue to other listeners
            }
        }
        
>>>>>>> 17644eb8cf72c569d38615c73b8407cc8b06540c
        if (agreeCount >= this.getMajorityNumber()) {
            this.electionContent.setBiggestCandidate(this.myID);
           return 1;
        }  
        return 0;
    }
<<<<<<< HEAD
    public synchronized void broadcastVictory() {
        System.out.println("[MP] [broadcastVictory]");
=======
    public void broadcastVictory() {
>>>>>>> 17644eb8cf72c569d38615c73b8407cc8b06540c
        this.currentLeader.setStatus(1);
        this.currentLeader.setID(this.myID);
        this.electionContent.setStatus(1);
        this.electionContent.setBiggestCandidate(-1); //clear the candidate
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            try {
                lisnode.ElectLeaderVictory(this.myID);
            } catch (Exception e) {
                System.err.println("[MP] [ConfirmToBeLeader] Someone loses connection");
<<<<<<< HEAD
//                this.myConfig.removeNode(noid);
=======
                this.myConfig.removeNode(noid);
>>>>>>> 17644eb8cf72c569d38615c73b8407cc8b06540c
                continue;//continue to other listeners
            }
        }
    }
    public int getMajorityNumber() {
        System.out.println("[MP majority] " + ((this.myConfig.getNodeMap().size() / 2) + 1));
        return ((this.myConfig.getNodeMap().size() / 2) + 1);
    }
}
