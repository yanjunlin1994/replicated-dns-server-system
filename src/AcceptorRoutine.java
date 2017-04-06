import java.util.concurrent.TimeUnit;

/**
 * Acceptor routine
 *
 */
public class AcceptorRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
    private MessagePasser mesgpasser;
    private int heartbeatCount;
    public AcceptorRoutine(int id, Configuration myConfig, MessagePasser mp) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.mesgpasser = mp;
    }
    public void addHeartbeat() {
        this.heartbeatCount = (++this.heartbeatCount) % 200_000_000;
    }
    public int getHeartbeatCount() {
    	return this.heartbeatCount;
    }
    /* Test if the heartbeat message keeps updating. If not, assumes the leader is out of reach, select a new leader */
    @SuppressWarnings("resource")
    @Override
    public void run(){
        System.out.println("[Acceptor Routine starts]");
        try {
	    int prevHeartbeat = this.heartbeatCount;
	    while(true) {
	    	System.out.println("[Acceptor Routine checks leader HeartBeat]");
			TimeUnit.SECONDS.sleep(7);
			if (this.heartbeatCount == prevHeartbeat) {
			    int leaderID = this.mesgpasser.LeaderElectionSection();
			    System.out.println("[Elect Leader] " + myID + " selects new leader: "+ leaderID);
			    prevHeartbeat = this.heartbeatCount;
			    if (leaderID == this.myID) {
	  		        Thread myleaderRoutine = new Thread(new LeaderRoutine(this.myID, this.myConfig, new Leader()));
	                myleaderRoutine.start();
				    break;
			    }
		    }
			prevHeartbeat = this.heartbeatCount;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
