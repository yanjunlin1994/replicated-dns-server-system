import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
public class MessagePasser {
    private Configuration myConfig;
    private Node me;
    private int myID;
    private ListenerImpl listener;
    private Leader currentLeader;
    private BlockingQueue<InterThreadMessage> LeaderListenerCommQueue;
    private BlockingQueue<InterThreadMessage> AcceptorMpCommQueue;
    private BlockingQueue<InterThreadMessage> LeaderMpCommQueue;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
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
        this.LeaderListenerCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.AcceptorMpCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.LeaderMpCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.AcceptorListenerCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        this.electionContent = new ElectionContent();

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
        /* Initial leader election */
        this.currentLeader.setID(1);
        /* What does setStatus mean */
        this.currentLeader.setStatus(1);
//        this.LeaderElectionSection();
        TimeUnit.SECONDS.sleep(2);
        InterThreadMessage msg = null;
        /* open acceptor routine */
        AcceptorRoutine acceptRoutine = new AcceptorRoutine(this.myID, this.myConfig, this.AcceptorMpCommQueue, this.AcceptorListenerCommQueue, this.currentLeader.getID());
        new Thread(acceptRoutine).start();
        /* TODO: for leader, open leader routine. how can I combine this leader election with the latter one? */
        if (currentLeader.getID() == myID) {
        	new Thread(new LeaderRoutine(this.myID, this.myConfig, this.currentLeader, this.LeaderMpCommQueue)).start();
        }
        /* TODO: In the first experiment, leader will not change
        /* In the second experiment, change leader 
         */
//        while (true) {
//        	/* AcceptorRoutine will notify MessagePasser to elect new leader */
//        	
//        	if (!AcceptorMpCommQueue.isEmpty()) {
//        		msg = AcceptorMpCommQueue.poll();
//        		/* TODO: How to detect duplicate, by adding a unique number in each message. Reset the unique number each time acceptor changes a leader */
//        		if (msg.getKind().equals("leaderElection")) {
//        			System.out.println("[MP.run] Elect a new leader.");
//                    TimeUnit.SECONDS.sleep(7);
//                    /* start to elect a new leader */
//                    this.currentLeader.setStatus(-1);        
//                    this.electionContent.setStatus(0);
//                    this.currentLeader.setID(-1);
//                    /* TODO: If the majority does not vote yes, the node should not be the leader */
//                    this.runForElectionEntrance();
//                    TimeUnit.SECONDS.sleep(5);
//                    if (this.electionContent.getBiggestCandidate() == this.myID) {
//                        this.broadcastVictory();
//                        new Thread(new LeaderRoutine(this.myID, this.myConfig, this.currentLeader, this.LeaderListenerCommQueue, this.LeaderMpCommQueue)).start();
//                    }
//                    acceptRoutine.setLeaderID(this.currentLeader.getID());
//                    this.electionContent.clear();
//        		}
//        	}
//            Thread.sleep(5000);
//        }
    }
    //--------------------------------------Election------------------------------------------
//    /**
//     * Elect a new leader according to id
//     * @param currentld
//     */
//    public synchronized int LeaderElectionSection() {
//        int nextLeaderID = -1;
//        nextLeaderID = this.myConfig.getNextLeader(this.currentLeader.getID());
//        this.currentLeader.clean();
//        this.currentLeader.setID(nextLeaderID);
//        this.currentLeader.setStatus(1);
//        if (this.myID == nextLeaderID) {
//            System.out.println("[LeaderElectionSection] I am the leader!"); 
//        }
//        System.out.println("[LeaderElectionSection] leader is:" + this.currentLeader);  
//        return nextLeaderID;
//    }
//    //------------------- entrance for running for leader
//    public void runForElectionEntrance() {
//        if (this.ProposeToBeLeader() != 1) {
////            System.out.println("[runForElectionEntrance] first stage return"); 
//            return;
//        }
//        if (this.ConfirmToBeLeader() != 1) {
////            System.out.println("[runForElectionEntrance] second stage return");
//            return;
//        }
//        
//    }
//    //------------------- First stage of running for leader
//    public synchronized int ProposeToBeLeader() {
//        // set the biggest candidate id to be my ID if myID is larger that candidate id
//        if (this.electionContent.getBiggestCandidate() > this.myID) {
//            return 0;
//        } 
//        //set my proposal to other nodes to verify that the old leader can no longer be reached by a majority
//        int agreeCount = 0;
//        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
//            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
//            try {
//                boolean agree = lisnode.ElectLeaderRequest(this.myID);
//                System.out.println("[MP] [WantToBeLeader] first stage agree Recived from " + noid + ": " + agree);
//                if (agree) {
//                    agreeCount++;
//                }
//            } catch (Exception e) {
//                System.err.println("[MP] [WantToBeLeader] Someone loses connection");
////                this.myConfig.removeNode(noid);
//                continue;//continue to other listeners
//            }
//        }
//        if (this.electionContent.getBiggestCandidate() <= this.myID) {
//            agreeCount++;
//        }
//        if (agreeCount >= (this.getMajorityNumber())) {
//            System.out.println("[MP] [WantToBeLeader] agreeCount: " + agreeCount);
//           return 1;
//        }
//        return 0;
//    }
//    //------------------- Second stage of running for leader
//    public synchronized int ConfirmToBeLeader() {
//        int agreeCount = 0;
//        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
//            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
//            try {
//                boolean agree = lisnode.ElectLeaderConfirm(this.myID);
//                System.out.println("[MP] [ConfirmToBeLeader] second stage agree Recived from " + noid + ": " + agree);
//                if (agree) {
//                    agreeCount++;
//                }
//            } catch (Exception e) {
//                System.err.println("[MP] [ConfirmToBeLeader] Someone loses connection");
////                this.myConfig.removeNode(noid);
//                continue;//continue to other listeners
//            }
//        }
//        if (this.electionContent.getBiggestCandidate() <= this.myID) {
//            agreeCount++;
//        }
//        if (agreeCount >= this.getMajorityNumber()) {
//            this.electionContent.setBiggestCandidate(this.myID);
//           return 1;
//        }  
//        return 0;
//    }
//    public synchronized void broadcastVictory() {
//        System.out.println("[MP] [broadcastVictory]");
//        this.currentLeader.setStatus(1);
//        this.currentLeader.setID(this.myID);
//        this.electionContent.setStatus(1);
//        this.electionContent.setBiggestCandidate(-1); //clear the candidate
//        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
//            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
//            try {
//                lisnode.ElectLeaderVictory(this.myID);
//            } catch (Exception e) {
//                System.err.println("[MP] [ConfirmToBeLeader] Someone loses connection");
////                this.myConfig.removeNode(noid);
//                continue;//continue to other listeners
//            }
//        }
//    }
//    public int getMajorityNumber() {
////        System.out.println("[MP majority] " + ((this.myConfig.getNodeMap().size() / 2) + 1));
//        return ((this.myConfig.getNodeMap().size() / 2) + 1);
//    }
}
