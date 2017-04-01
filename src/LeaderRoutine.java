import java.util.concurrent.TimeUnit;

public class LeaderRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
    private Leader currentLeader;
    private int majority;
 
    public LeaderRoutine(int id, Configuration myConfig, Leader currentL) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.currentLeader = currentL;
        this.majority = (myConfig.getNodeMap().size() / 2) + 1;
    }
    @SuppressWarnings("resource")
    @Override
    public void run(){
        System.out.println("[LeaderRoutine Routine starts]");
        /* start the heartbeat thread */
        Thread leaderHB = new Thread(new LeaderHeartBeat(this.myID, this.myConfig));
        leaderHB.start();
        /* start dealing with proposal */
        System.out.println("[LeaderRoutine Routine wating for proposal]");
        while (true) {
            if (this.currentLeader.getProcessQueueSize() > 0) {
                this.ReceiveNewProposal();
            }
        }
    }
    public synchronized void ReceiveNewProposal() {
        System.out.println("[LeaderRoutine] have a new proposal!");
        Proposal np = this.currentLeader.pollProposal(); 
        System.out.println("[LeaderRoutine] Proposal is : " + np);
        this.prepare(np);
    }
    public void prepare(Proposal p) {
        System.out.println("[LeaderRoutine] prepare");
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                Promise aPromise = lisnode.LeaderPrepareProposal(p); 
                System.out.println("[LeaderRoutine] prepare Recived: " + aPromise);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        
    }

}
