import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class LeaderRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
    private Leader currentLeader;
    private int majority;
    private Round currentRound;
    private int RoundID;
    private Proposal interRoundProposal; //temp proposal between reject and start a new round.
    private BlockingQueue<InterThreadMessage> LeaderListenerCommQueue;
    private BlockingQueue<InterThreadMessage> LeaderMpCommQueue;
 
    public LeaderRoutine(int id, Configuration myConfig, Leader currentL, BlockingQueue<InterThreadMessage> i, BlockingQueue<InterThreadMessage> m) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.currentLeader = currentL;
        this.majority = (myConfig.getNodeMap().size() / 2) + 1;
        this.RoundID = 0;
        this.interRoundProposal = null;
        this.LeaderListenerCommQueue = i;
        this.LeaderMpCommQueue = m;
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
                try {
                    this.ReceiveNewProposal();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * Receive a new proposal.
     * hand it to newRoundHandler.
     * @throws Exception 
     */
    public synchronized void ReceiveNewProposal() throws Exception {
        System.out.println("[LeaderRoutine] have a new proposal!");
        Proposal np = this.currentLeader.pollProposal(); 
        int result = -1;
        while (result != 0) {//while having rejects.
            if (this.interRoundProposal != null) {
                np = this.interRoundProposal;
            }
            result = this.NewRoundHandler(np);
        }
    }
    /**
     * Control of all paxos functions.
     * @param np
     * @throws Exception 
     */
    public int NewRoundHandler(Proposal p) throws Exception {
        System.out.println("[LeaderRoutine] [NewRoundHandler]");
        Proposal np = new Proposal(p);//copy the proposal argument
        this.interRoundProposal = null; //clear the interRound proposal
        int prepareSucceed = -1; //-1: fail; 0:succeed
        
        /** keep trying new proposal number until prepare succeed */
        while (this.myConfig.getNodeMap().get(this.myID).proposalNumSetSize() > 0) {
            int newProposalNum = this.myConfig.getNodeMap().get(this.myID).pollProposalNum();
            np.setID(newProposalNum);
            this.SetNewRoundParam(np);
            if (this.prepare(np) != -1) {
                prepareSucceed = 0;
                break;
            }
        }
        if (prepareSucceed == -1) {
            throw new Exception("[LeaderRoutine] [NewRoundHandler] [prepare error] proposalNumSet exhausted");
        }
        if (this.accept() == -1) {  
            if (this.RejectHandler() == -1) {
                throw new Exception("[LeaderRoutine] [NewRoundHandler] [reject handle ERROR]");
            }
            return -1;
        } else {
            this.commit();
            return 0;
        }
    }
    //------------------- New Round Parameters -----------------------
    /**
     * Setting new round parameter
     * @param np
     */
    public void SetNewRoundParam(Proposal np) {
        this.currentRound = new Round(myID, (np.getID() / 10));
        this.currentRound.setCurrentProposal(np);    
        System.out.println("[LeaderRoutine] [SetNewRound] Proposal is : " + np);
    }
    //------------------- prepare -----------------------
    public int prepare(Proposal p) {
        System.out.println("[LeaderRoutine] prepare");
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                Promise aPromise = lisnode.LeaderPrepareProposal(p);
                this.currentRound.addPromiseMap(aPromise);// add to promise map
                System.out.println("[LeaderRoutine] [prepare] prepare Recived: " + aPromise);
                if (aPromise.getIsIfrealPromise()) {
                    this.currentRound.increPromiseCount();
                }
            } catch (Exception e) {
                System.err.println("[Leader Routine] [Prepare] Someone loses connection");
                continue;//continue to other listeners
            }
        } //end receiving promises
        
        if (this.currentRound.getPromiseCount() + 1 >= this.majority) {//including promise from leader itself
            System.out.println("[LeaderRoutine] [prepare] prepare Majority Achieved!");
            return 0;
        } else {
            System.out.println("[LeaderRoutine] [prepare] prepare not reach majority, try next proposal proposal ID");   
            return -1;
        }
    }
    //------------------- Accept -------------------------
    public int accept() {
        Accept acpt = this.createNewAccept();
        this.BroadCastAccept(acpt);
        if (!this.currentRound.getRejAck()) {
            //If no reject, return success
            return 0;
        } else {
            return -1;      
        }   
    }
    /**
     * create a new Accept object.
     * @return new Accept object.
     */
    public Accept createNewAccept(){   
        String modifiedValue = this.currentRound.findPromiseMaxIDValue();
        if (modifiedValue == null) {
            //use current proposal value without modifying it
            System.out.println("[LeaderRoutine] [createNewAccept] no existing value before, use my own value");                
            modifiedValue = this.currentRound.getCurrentProposal().getValue();
        }
        System.out.println("[LeaderRoutine] [createNewAccept] accept value is: " + modifiedValue);
        Accept accp = new Accept(this.currentRound.getCurrentProposal().getID(), modifiedValue);
        this.currentRound.setAcceptProposal(accp); //add to current round
        return accp;
    }
    
    /**
     * send Accept message to nodes after receiving majority promise
     */
    public void BroadCastAccept(Accept acp) {
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                Acknlg ack = lisnode.LeaderAcceptProposal(acp);
                this.currentRound.addAcknlgMap(ack);// add to ack map
                System.out.println("[LeaderRoutine] [BroadCastAccept] ack Recived: " + ack);
                if (ack.getisIfrealAcknlg()) {
                    this.currentRound.increAcceptCount();
                } else {
                	this.currentRound.setRejAck();
                	this.currentRound.addRejAcknlgSet(ack.getMinProposal()); //add rej's minproposal to the RejAcknlgSet
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  
    }
    //------------------- Deal With Reject -------------------------
    /**
     * if accept fails, create a new round with 
     * proposal number larger than reject's minproposal
     * and origin accpet value
     */
    /**
     * Create a new proposal Number
     * @return
     */
    public int RejectHandler() {
        int newProposalID;
        try {
            newProposalID = this.findNewProposalIDLargerThanRej();
            if (newProposalID == -1) {
                System.err.println("[LeaderRoutine] [RejectHandler] proposalNumSet exhausted");
                return -1;
            }
            Proposal np = new Proposal(newProposalID, this.currentRound.getAcceptProposal().getValue());
            this.interRoundProposal = np;
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;              
    }
    /**
     * Find the New proposa ID that is larger than the rejects' max minproposal ID.
     * @throws Exception 
     */
    public int findNewProposalIDLargerThanRej() throws Exception {
        int RejMaxMinProposalID = this.currentRound.findRejMaxMinproposalID();
        if (RejMaxMinProposalID == -1) {
            throw new Exception("[LeaderRoutine] [findNewProposalIDLargerThanRej]ERROR: no existing rejects");
        }
        int newProposalID = -1;
        while (this.myConfig.getNodeMap().get(this.myID).proposalNumSetSize() > 0) {
            int newProposalNumTemp = this.myConfig.getNodeMap().get(this.myID).pollProposalNum();
            if (newProposalNumTemp > RejMaxMinProposalID) {
                newProposalID = newProposalNumTemp;
                return newProposalID;
            }
        }
        return newProposalID;
    }
    //------------------- Commit -------------------------
    public void commit() {
        Commit cm = new Commit(this.currentRound.getAcceptProposal().getValue());
        this.currentRound.setCommit(cm);
        this.CommitWriteToLog(cm);
    }
    /**
     * Write to disk
     * @param cm
     */
    public void CommitWriteToLog(Commit cm) {
        FileWriter fw;
        try {
            fw = new FileWriter(myConfig.getDNSFile(),true); //the true will append the new data
            fw.write(cm.getValue() + System.getProperty( "line.separator" ));//appends the string to the file
            fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

}
