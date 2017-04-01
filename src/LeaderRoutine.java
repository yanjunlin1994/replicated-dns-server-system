import java.util.concurrent.TimeUnit;

public class LeaderRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
    private Leader currentLeader;
 
    public LeaderRoutine(int id, Configuration myConfig, Leader currentL) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.currentLeader = currentL;
    }
    @SuppressWarnings("resource")
    @Override
    public synchronized void run(){
        System.out.println("[LeaderRoutine Routine starts]");
        /* start the heartbeat thread */
        Thread leaderHB = new Thread(new LeaderHeartBeat(this.myID, this.myConfig));
        leaderHB.start();
        /* start dealing with proposal */
        while (true) {
            if ((this.currentLeader.getProcessQueue() != null) && 
                    (this.currentLeader.getProcessQueue().size() > 0)) {
                System.out.println("[LeaderRoutine] have a new proposal!");
                Proposal np = this.currentLeader.getProcessQueue().poll(); 
                System.out.println("[LeaderRoutine] Proposal is : " + np);
            }
            
        }
    }
    
    public void MulticastToEveryNode(HeartBeatMessage hbmessage) {
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                lisnode.LeaderHeartBeat(hbmessage);  
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
