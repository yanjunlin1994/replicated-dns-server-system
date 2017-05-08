import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Acceptor routine
 */
public class AcceptorRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
    private long latestHeartbeat;
    private int leaderFailCount;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
    private BlockingQueue<InterThreadMessage> ElectionAcceptorCommQueue;
    private final int longestHeartBeatInterval = 10_000;
    private final int MaxLeaderFailureCount = 6;
    private int leaderID;
    private ListenerImpl listener;
    private boolean inElection;
    public AcceptorRoutine(int id, Configuration myConfig, BlockingQueue<InterThreadMessage> al, BlockingQueue<InterThreadMessage> LeaderListenerCommQueue, ListenerImpl listener) {
        this.myID = id;
        this.myConfig = myConfig;
        this.latestHeartbeat = 0;
        this.leaderFailCount = 0;
        this.AcceptorListenerCommQueue = al;
        this.ElectionAcceptorCommQueue = new LinkedBlockingQueue<InterThreadMessage>();
        leaderID = -1;
        this.listener = listener;
        inElection = false;
        leaderElection();
    }
    @Override
    public synchronized void run() {
        System.out.println("[AR.run]");
        InterThreadMessage msg; 
        while (true) {
        	/* One leader elecition runs at one time */
        	if (!inElection) {
	            if (this.checkHeartBeatExpire() != 0) {
	                this.increLeaderFailCount();
	                /* If there are multiple failure
	                 */
	                System.out.println("[AR] Leader fails for " + this.leaderFailCount + " times");
	                if (this.leaderFailCount > this.MaxLeaderFailureCount) {
	                    System.out.println("[AR] Leader failure maximum achieved");
	                    /* If there are too many timeouts between communication between leader and acceptor,
	                     *  the acceptor will try to elect a new leader */
	                    this.handleLeaderFailure();
	                }  
	            }
        	}
            /* Get new heartbeat message from queue and update the latest heartbeat time */
            if (this.AcceptorListenerCommQueue.size() > 0) {
                msg = this.AcceptorListenerCommQueue.poll();
                if (msg.getKind().equals("HeartBeatMessage") && msg.getSrc() == leaderID) {
                    this.setLatestHeartbeat(System.currentTimeMillis()); 
                }
            }
            if (this.ElectionAcceptorCommQueue.size() > 0) {
            	msg = this.ElectionAcceptorCommQueue.poll();
            	if (msg.getKind().equals("newLeader")) {
	            	leaderID = Integer.valueOf(msg.getContent());
	            	System.out.println("[AR.new leader] " + leaderID);
            	}
            	inElection = false;
            	this.leaderFailCount = 0;
            }
            /* sleep for 2 seconds for efficiency */
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }
    }
    public void setLeaderID(int leaderID) {
    	this.leaderID = leaderID;
    }
    /**
     * Check if there is a timeout since last heartbeat.
     * @return
     */
    public int checkHeartBeatExpire() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.latestHeartbeat > this.longestHeartBeatInterval) {
            return -1;
        }
        return 0;
    }
    /**
     * handle leader failure situation
     * @return
     */
    public int handleLeaderFailure() {
        System.out.println("[AR.handle failure]");
        this.listener.unchosenLeader();
        /* start to elect leader */
        leaderElection();
        return 0;
    }
    public long getLatestHeartbeat() {
        return latestHeartbeat;
    }
    public void setLatestHeartbeat(long latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }
    public int getLeaderFailCount() {
        return leaderFailCount;
    }
    public void setLeaderFailCount(int leaderFailCount) {
        this.leaderFailCount = leaderFailCount;
    }
    public void increLeaderFailCount() {
        this.leaderFailCount = this.leaderFailCount + 1;
    }
    public void leaderElection() {
    	System.out.println("[AR.leaderElection]");
    	inElection = true;
    	new Thread(new RequestLeader(myID, myConfig, this.ElectionAcceptorCommQueue)).start();
    }
}
