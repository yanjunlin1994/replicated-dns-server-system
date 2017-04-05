import java.util.concurrent.TimeUnit;

public class CheckLeader implements Runnable {
	private int myID;
	private int heartbeatCount;
	private MessagePasser mesgpasser;
	public CheckLeader(int id, MessagePasser mp) {
		this.heartbeatCount = 0;
		this.mesgpasser = mp;
		this.myID = id;
	}
	public void addHeartbeat() {
		this.heartbeatCount = (++this.heartbeatCount) % 200_000_000;
	}
	/* Test if the heartbeat message keeps updating. If not, assumes the leader is out of reach, select a new leader */
	@Override
	public void run() {
		int prevHeartbeat = this.heartbeatCount;
		try {
			while(true) {
				TimeUnit.SECONDS.sleep(15);
				if (this.heartbeatCount == prevHeartbeat) {
					int leaderID = this.mesgpasser.LeaderElectionSection();
					System.out.println("[Elect Leader] " + myID + " selects new leader: "+ leaderID);
					prevHeartbeat = this.heartbeatCount;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
