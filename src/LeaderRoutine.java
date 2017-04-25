import java.util.concurrent.BlockingQueue;
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
    private int logId;
    private Node me;
    public LeaderRoutine(int id, Configuration myConfig, Leader currentL, BlockingQueue<InterThreadMessage> i, BlockingQueue<InterThreadMessage> m) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.currentLeader = currentL;
        this.majority = (myConfig.getListenerIntfMap().size() / 2) + 1;
        this.RoundID = 0;
        this.interRoundProposal = null;
        this.LeaderListenerCommQueue = i;
        this.LeaderMpCommQueue = m;
        this.me = myConfig.getNodeMap().get(myID);
    }
    @SuppressWarnings("resource")
    @Override
    public void run(){
        System.out.println("[LeaderRoutine Routine starts]");
        /* start the heartbeat thread */
        Thread leaderHB = new Thread(new LeaderHeartBeat(this.myID, this.myConfig));
        leaderHB.start();
        /* start dealing with proposal */
        System.out.println("[LeaderRoutine start running]");
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
        System.out.println("[LeaderRoutine] new round");
        /* TODO why poll the proposal out? */
        Proposal np = this.currentLeader.pollProposal(); 
        int newProposalNum = this.myConfig.getNodeMap().get(this.myID).pollProposalNum();
        np.setProposalId(newProposalNum);  
        /* find an appropriate log id for the new proposal */
        this.logId = me.getDnsfile().getMinUnchosenLogId();
        np.setLogId(logId);
        
        int result = -1;
        /* Before sending the proposal, initialize the transaction information in it */
        while (result != 0) {
        	/* while having rejects.
        	 * If this proposal is from last round, it should update its proposal id and keep the same log id.
        	 * If it is a new proposal from client, start with smallest proposal id and choose the least unchosen id in the log.
        	 */
            if (this.interRoundProposal != null) {
            	System.out.println("Continue with last proposal");
                np = this.interRoundProposal;
            }
            DNSFile dnsfile = me.getDnsfile();
            /* leader write its proposal into the log */
            dnsfile.writeEntry(new Entry(np.getLogId(), np.getProposalId(), np.getProposalId(), np.getDnsentry()));
            System.out.println("[leader] write new proposal into log");
            result = this.NewRoundHandler(np);
        }
    }
    /**
     * Control of all paxos functions.
     * @param np
     * @throws Exception 
     */
    public int NewRoundHandler(Proposal p) throws Exception {
//        System.out.println("[LeaderRoutine] [NewRoundHandler]");
    	/* TODO the proposal id of the second proposal is not correct. It starts with 10. Actually it should start with 0.*/
        Proposal np = new Proposal(p);//copy the proposal argument
        int prepareSucceed = -1; //-1: fail; 0:succeed
        /* keep trying new proposal number until prepare succeed */
        while (this.myConfig.getNodeMap().get(this.myID).proposalNumSetSize() > 0) {     
            this.SetNewRoundParam(np);
            this.interRoundProposal = null; //clear the interRound proposal
            /* start prepare phase */
            if (this.prepare(np) != -1) {
                prepareSucceed = 0;
                break;
            }
        }
        if (prepareSucceed == -1) {
            throw new Exception("[LeaderRoutine] [NewRoundHandler] [prepare error] proposalNumSet exhausted");
        }
        /* continue with accept phase */
        if (this.accept() == -1) {  
            if (this.RejectHandler() == -1) {
                throw new Exception("[LeaderRoutine] [NewRoundHandler] [reject handle ERROR]");
            }
            return -1;
        } else {
        	/* successfully commit */
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
        this.currentRound = new Round(myID, (np.getProposalId() / 10), logId);
        this.currentRound.setCurrentProposal(np);    
        System.out.println("[LeaderRoutine] [SetNewRound] log: "+this.currentRound.getLogId() + " proposalid: " + np.getProposalId() + "-> " + np);
    }
    //------------------- prepare -----------------------
    /**
     * Leader sends prepare proposal to slaves.
     * @param p
     * @return
     */
    public int prepare(Proposal p) {
        System.out.println("[LeaderRoutine] prepare");
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            try {
            	/* Leader receive the promise from slaves */
                Promise aPromise = lisnode.LeaderPrepareProposal(p);
                this.currentRound.addPromiseMap(aPromise);// add to promise map
                System.out.println("[LeaderRoutine] [prepare] Recived: " + aPromise);
                if (aPromise.getIsIfrealPromise()) {
                    this.currentRound.increPromiseCount();
                }
            } catch (Exception e) {
                System.err.println("[Leader Routine] [Prepare] Someone loses connection");
                this.myConfig.removeNode(noid);
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
        DNSEntry modifiedValue = this.currentRound.findPromiseMaxIDValue();
        if (!modifiedValue.hasAccepted()) {
            //use current proposal value without modifying it
            System.out.println("[LeaderRoutine] [createNewAccept] no existing value before, use my own value");                
            modifiedValue = this.currentRound.getCurrentProposal().getDnsentry();
        }
        System.out.println("[LeaderRoutine] [createNewAccept] accept value is: " + modifiedValue);
        Accept accp = new Accept(this.currentRound.getLogId(), this.currentRound.getCurrentProposal().getProposalId(), modifiedValue);
        this.currentRound.setAcceptProposal(accp); //add to current round
        return accp;
    }
    
    /**
     * send Accept message to nodes after receiving majority promise
     */
    public void BroadCastAccept(Accept acp) {
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
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
                System.err.println("[Leader Routine] [BroadCastAccept] Someone loses connection");
                this.myConfig.removeNode(noid);
                continue;//continue to other listeners
            }
        }  
    }
    //------------------- Deal With Reject -------------------------
    /**
     * if accept fails, create a new round with 
     * proposal number larger than reject's minproposal
     * and origin accpet value.
     */
    public int RejectHandler() {
        int newProposalID;
        try {
            newProposalID = this.findNewProposalIDLargerThanRej();
            if (newProposalID == -1) {
                System.err.println("[LeaderRoutine] [RejectHandler] proposalNumSet exhausted");
                return -1;
            }
            /* TODO when is getAcceptProposal() update? */
            Proposal np = new Proposal(me.getDnsfile().getMinUnchosenLogId(), newProposalID, this.currentRound.getAcceptProposal().getValue());
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
        System.out.println("[LeaderRoutine] [commit]");
        /* the proposed value is chosen */
        //TODO: write value make it a function
    	DNSFile dnsfile = me.getDnsfile();
    	/* TODO write minUnchosenLogId into disk */
    	dnsfile.incrementMinUnchosenLogId(logId);
    	Entry entry = dnsfile.readEntry(logId);
    	entry.setChosen();
    	System.out.println("[commit an entry] " + entry);
    	dnsfile.writeEntry(entry);
    }
}
