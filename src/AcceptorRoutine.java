import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Acceptor routine
 */
public class AcceptorRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
    private long latestHeartbeat;
    private int leaderFailCount;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
    private BlockingQueue<InterThreadMessage> AcceptorMpCommQueue;
    private final int longestHeartBeatInterval = 10_000;
    private final int MaxLeaderFailureCount = 8;
    
    public AcceptorRoutine(int id, Configuration myConfig, BlockingQueue<InterThreadMessage> al, BlockingQueue<InterThreadMessage> am) {
        this.myID = id;
        this.myConfig = myConfig;
        this.latestHeartbeat = 0;
        this.leaderFailCount = 0;
        this.AcceptorListenerCommQueue = al;
        this.AcceptorMpCommQueue = am;
    }
    @SuppressWarnings("resource")
    @Override
    public synchronized void run() {
        System.out.println("[Acceptor Routine starts]");
        while (true) {
            if (this.checkHeartBeatExpire() != 0) {
                this.increLeaderFailCount();
                /* If there are multiple failures */
                System.out.println("[Acceptor Routine] Leader fails for " + this.leaderFailCount + " times"); 
                if (this.leaderFailCount > this.MaxLeaderFailureCount) {
                    System.out.println("[Acceptor Routine] Leader failure maximum achieved, go to LeaderFailure handler"); 
                    /* If there are too many timeouts between communication between leader and acceptor,
                     *  the acceptor will try to elect a new leader */
                    this.handleLeaderFailure();
                    return;//kill thread
                }  
            }
            /* Get new heartbeat message from queue and update the latest heartbeat time */
            if (this.AcceptorListenerCommQueue.size() > 0) {
                InterThreadMessage newMessage = this.AcceptorListenerCommQueue.poll();
                this.processInterThreadMessage(newMessage);
            }
            /* sleep for 2 seconds for efficiency */
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Process the newly coming message in message queue.
     * @param newM If it is a heartbeat message, update the latest hearbeat time.
     */
    public void processInterThreadMessage(InterThreadMessage newM) {
        if (newM.getKind().equals("HeartBeatMessage")) {
//            System.out.println("[Acceptor Routine] [processInterThreadMessage] new Leader HeartBeat message");
            this.setLatestHeartbeat(System.currentTimeMillis()); 
        }
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
        System.out.println("[Acceptor Routine] [handleLeaderFailure]"); 
        InterThreadMessage lf= new InterThreadMessage(this.myID, this.myID, 
                                   "LeaderFailure", "LeaderFailure", -1);
        /* Add a 'leader fail' message into the AcceptorMpCommQueue, to notify the messagePasser process */
        this.AcceptorMpCommQueue.add(lf);
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
}
