import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class LeaderRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
    private Leader currentLeader;
    private int majority;
    private Round currentRound;
 
    public LeaderRoutine(int id, Configuration myConfig, Leader currentL) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.currentLeader = currentL;
        this.majority = (myConfig.getNodeMap().size() / 2) + 1;
        this.currentRound = new Round(myID);
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
    /**
     * Receive a new proposal.
     * add proposal to Round.
     * enter prepare stage.
     */
    public synchronized void ReceiveNewProposal() {
        System.out.println("[LeaderRoutine] have a new proposal!");
        Proposal np = this.currentLeader.pollProposal(); 
        this.currentRound.setCurrentProposal(np);
        System.out.println("[LeaderRoutine] Proposal is : " + np);
        this.prepare(np);
    }
    public void prepare(Proposal p) {
        System.out.println("[LeaderRoutine] prepare");
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                Promise aPromise = lisnode.LeaderPrepareProposal(p);
                this.currentRound.addPromiseMap(aPromise);// add to promise map
                System.out.println("[LeaderRoutine] prepare Recived: " + aPromise);
                if (aPromise.getIsIfrealPromise()) {
                    this.currentRound.increPromiseCount();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //end receiving promises
        if (this.currentRound.getPromiseCount() >= this.majority) {
            System.out.println("[LeaderRoutine] prepare Majority Achieved!");
            String modifiedValue = this.currentRound.findPromiseMaxIDValue();
            if (modifiedValue == null) {
                //use current proposal value without modifying it
                modifiedValue = this.currentRound.getCurrentProposal().getValue();
            }
            System.out.println("[LeaderRoutine] accept value is: " + modifiedValue);
            Accept accp = new Accept(this.currentRound.getCurrentProposal().getID(), 
                                     modifiedValue);
            this.currentRound.setAcceptProposal(accp);
            BroadCastAccept(accp);
        }
    }
    /**
     * send Accept message to nodes after receiving majority promise
     */
    public void BroadCastAccept(Accept acp) {
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                Acknlg ack = lisnode.LeaderAcceptProposal(acp);
                this.currentRound.addAcknlgMap(ack);// add to ack map
                System.out.println("[LeaderRoutine] ack Recived: " + ack);
                if (ack.getIsIfrealAcknlg()) {
                    this.currentRound.increAcceptCount();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.currentRound.getAcceptCount() >= this.majority) {
            System.out.println("[LeaderRoutine] Majority ACK!");
        }
    }

}
