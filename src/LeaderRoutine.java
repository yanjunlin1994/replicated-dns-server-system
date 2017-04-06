import java.io.FileWriter;
import java.io.IOException;
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
        //TODO:should update the accept content in leader itself
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                Promise aPromise = lisnode.LeaderPrepareProposal(p);
                this.currentRound.addPromiseMap(aPromise);// add to promise map
                System.out.println("[LeaderRoutine] prepare Recived: " + aPromise);
                if (aPromise.getIsIfrealPromise()) {
                    this.currentRound.increPromiseCount();
                }
            } catch (Exception e) {
                System.err.println("[Leader Routine] [Prepare Stage] Someone loses connection");
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
                } else {
                	this.currentRound.setRejAck();
                	this.currentRound.addRejAcknlg(acp.getID(), acp.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /* check if any reject, if yes, update the accept value and grab a new proposal number */
        if (this.currentRound.getRejAck()) {
        	int newProposalNum = this.myConfig.getNodeMap().get(myID).pollProposalNum();
            System.out.println("[Recieve clientRequest] handle this request newProposalNum " + newProposalNum);
            int maxId = -1;
            for (int id : this.currentRound.getRejAcknlgMap().keySet()) {
            	if (id > maxId) {
            		maxId = id;
            	}
            }
            Proposal np = new Proposal(newProposalNum, this.currentRound.getRejAcknlgMap().get(maxId));
            this.prepare(np);
        }
        /* check if reach a consensus, if yes, export accept to a file, and create a new round instance. */
        if (!this.currentRound.getRejAck() && this.currentRound.getAcceptCount() >= this.majority) {
            System.out.println("[LeaderRoutine] Majority ACK!");
<<<<<<< HEAD
            //TODO:check if any reject, if yes, go back and grab a new proposal number
                   //randomize the delay before starting to avoid livelock
            //TODO: 新建一个round instance 修改指针
            //TODO: export accept to a file (txt)
=======
            FileWriter fw;
            try {
                fw = new FileWriter(myConfig.getDNSFile(),true); //the true will append the new data
                fw.write(acp.getValue() + System.getProperty( "line.separator" ));//appends the string to the file
                fw.close();
            } catch(IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
            this.currentRound = new Round(this.myID);
>>>>>>> 61458281d74989fe99de6b018f0f1adff10e42b2
        }
    }

}
