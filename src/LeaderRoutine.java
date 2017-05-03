import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
public class LeaderRoutine implements Runnable {
	
	private static final int SUCCEED = 0;
	private static final int FAIL = -1;
	private static final int NEW_ROUND = -1;
	private static final int FINISH_ROUND = 0;

    private int myID;
    private Configuration myConfig;
    private Leader currentLeader;
    private int majority;
    private Round currentRound;
    private Proposal interRoundProposal; //temp proposal between reject and start a new round.
    private Node me;
    private boolean skipPrepare;
    /* Include the node Id of the nodes which has no more accepted value beyond current log id */
    private HashSet<Integer> noMoreAcceptedValueSet;
    public LeaderRoutine(int id, Configuration myConfig, Leader currentL, BlockingQueue<InterThreadMessage> i, BlockingQueue<InterThreadMessage> m) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.currentLeader = currentL;
        this.majority = ((myConfig.getListenerIntfMap().size() / 2) + 1);
        this.interRoundProposal = null;
        this.me = myConfig.getNodeMap().get(myID);
        /* Initially, skip is set to false and noMoreAcceptedSet is empty */
        this.skipPrepare = false;
        noMoreAcceptedValueSet = new HashSet<Integer>();
    }
    @Override
    public void run(){
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
    	/* Create a new proposal */
    	Proposal p = this.currentLeader.pollProposal();
    	System.out.println("[LR.pollProposal] " + p);
        Proposal np = new Proposal(p);
        /* The dnsfile.proposalId is set propoerly right now. Use it to set proposal id */
        np.setDnsentry(p.getDnsentry());
        np.setProposalId(me.getDnsfile().getProposalId());
        /* The dnsfile.minUnchosenLogId is set propoerly right now. Use it to set logId */
        np.setLogId(me.getDnsfile().getMinUnchosenLogId());
        
        System.out.println("[LR.ReceiveNewProposal] new proposal " + np);
        
        int result = NEW_ROUND;
        /* Before sending the proposal, initialize the transaction information in it */
        while (result == NEW_ROUND) {
          	/* If this proposal is from last round, it should update its proposal id and keep the same log id.
          	 * If it is a new proposal from client, start with smallest proposal id and choose the least unchosen id in the log.
          	 */
        	if (this.interRoundProposal != null) {
                np = this.interRoundProposal;
                System.out.println("[LR.ReceiveNewProposal] previous proposal " + np);
            
        	}
            /* leader WRITE its proposal into the log, acceptedProposalId need to create a new ProposalID object!! */
        	Entry tmp = new Entry(np.getLogId(), np.getProposalId(), new ProposalID(np.getProposalId()), np.getDnsentry());
            me.getDnsfile().writeEntry(tmp);
//            System.out.println("[LR. write] " + tmp + ", " + tmp.toByte().length+", "+ tmp.getAcceptedProposalId().toByte().length +" "+ tmp.getMinProposalId().toByte().length +" "+ tmp.getdns().getDns().length +" "+ tmp.getdns().getIp().length+". ");
            result = this.NewRoundHandler(np);
        }
    }
    /**
     * Control of all paxos functions.
     * @param np
     * @throws Exception 
     */
    public int NewRoundHandler(Proposal p) throws Exception {
        Proposal np = new Proposal(p);//copy the proposal argument
        this.interRoundProposal = null; //clear the interRoundProposal  
        /* If the leader receives noMoreAccepts from majority, it can skip prepare stage */
        if (!skipPrepare) {
        	System.out.print("[LR.NewRound] didn't skip");
        	/* If the proposer test fail many times, shouldn't it abort this transaction? */
        	while (true) {
        		/* start a new round */
        		this.SetNewRoundParam(np);
        		if (this.prepare(np) == SUCCEED) {
        			break;
        		}
        		/* If the prepare phase doesn't succeed, restart a new prepare phase,
        		 * keep trying new proposalId until succeed */
        		this.me.getDnsfile().incrementProposalId();
        		np.setProposalId(this.me.getDnsfile().getProposalId());
        	}
        } else {
        	System.out.println("[LR.NewRound] skip Prepare");
        	this.SetNewRoundParam(np);
        }
        /* continue with accept phase */
        if (this.accept() == FAIL) {
            if (this.RejectHandler() == FAIL) {
                throw new Exception("[LeaderRoutine] [NewRoundHandler] [reject handle ERROR]");
            }
            return NEW_ROUND;
        } else {
        	/* successfully commit */
            this.commit();
        	return FINISH_ROUND;
        }
    }
    //------------------- New Round Parameters -----------------------
    /**
     * Setting new round parameter
     * @param np
     */
    public void SetNewRoundParam(Proposal np) {
        this.currentRound = new Round(myID, np.getProposalId().getRoundId(), np.getLogId());
        this.currentRound.setCurrentProposal(np);    
        System.out.println("[LR.setNewRound] proposal: " + np);
    }
    //------------------- prepare -----------------------
    /**
     * Leader sends prepare proposal to slaves.
     * @param p
     * @return
     */
    public int prepare(Proposal p) {
        System.out.println("[LR.prepare]");
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            try {
            	/* Leader receive the promise from slaves */
                Promise aPromise = lisnode.LeaderPrepareProposal(p);
                this.currentRound.addPromiseMap(aPromise);// add to promise map
                System.out.println("[LR.prepare] Recived: " + aPromise);
                if (aPromise.getIsIfrealPromise()) {
                	/* If one node sends noMoreAccpetedValue, add the node into noMoreAcceptedValueSet */
                    if (aPromise.getNoMoreAcceptedValue()) {
                    	noMoreAcceptedValueSet.add(aPromise.getSrc());
                    }
                    this.currentRound.increPromiseCount();
                }
            } catch (Exception e) {
                System.err.println("[Leader Routine] [Prepare] Someone loses connection");
                continue;//continue to other listeners
            }
        } //end receiving promises
        /* TODO: directly add 1 to promiseCount. At this time the leader cannot be an acceptor? */
        if (this.currentRound.getPromiseCount() + 1 >= this.majority) {//including promise from leader itself
            System.out.println("[LR.prepare] prepare Majority Achieved!");
            /* If majority sends noMoreAcceptedValue, the proposer can skip prepare in next round */
            if (noMoreAcceptedValueSet.size() >= this.majority) {
            	skipPrepare = true;
            }
            return SUCCEED;
        } else {
            System.out.println("[LR.prepare] prepare not reach majority, try next proposal proposal ID");   
            return FAIL;
        }
    }
    //------------------- Accept -------------------------
    public int accept() {
    	Accept acpt = null;
    	this.currentRound.setLogId(this.me.getDnsfile().getMinUnchosenLogId());
    	/* If there is a prepare stage, check if acceptors return some accepted value */
    	if (!skipPrepare) {
    		acpt = this.createNewAccept();
    	} else {
    		acpt = new Accept(this.currentRound.getLogId(), this.currentRound.getCurrentProposal().getProposalId(), this.currentRound.getCurrentProposal().getDnsentry(), this.me.getDnsfile().getMinUnchosenLogId());
    	}
    	System.out.println("[LR.accept] Accept(): " + acpt);
        this.BroadCastAccept(acpt);
        if (this.currentRound.getRejAck()) {
            return FAIL;
        } else {
        	//If no reject, return success
            return SUCCEED;      
        }   
    }
    /**
     * create a new Accept object.
     * @return new Accept object.
     */
    public Accept createNewAccept() {
        DNSEntry modifiedValue = this.currentRound.findPromiseMaxIDValue();
        if (!modifiedValue.hasAccepted()) {               
            modifiedValue = this.currentRound.getCurrentProposal().getDnsentry();
        }
        Accept accp = new Accept(this.currentRound.getLogId(), this.currentRound.getCurrentProposal().getProposalId(), modifiedValue, this.me.getDnsfile().getMinUnchosenLogId());
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
                System.out.println("[LR.broadcastAccept] received: " + ack);
                if (ack.getisIfrealAcknlg()) {
                    this.currentRound.increAcceptCount();
                } else {
                	/* If one node rejects the accept proposal, set skipPrepare to false and remove the node from noMoreAcceptedValueSet */
                	skipPrepare = false;
                	noMoreAcceptedValueSet.remove(new Integer(ack.getSrc()));
                	this.currentRound.setRejAck();
                	this.currentRound.addRejAcknlgSet(ack.getMinProposal()); //add rej's minproposal to the RejAcknlgSet
                }
            } catch (RemoteException e) {
                System.err.println("[LR.broadcastAccept] node " + noid +" loses connection");
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
        ProposalID newProposalID;
        try {
            newProposalID = this.findNewProposalIDLargerThanRej();
            /* TODO when is getAcceptProposal() update? */
            Proposal np = new Proposal(me.getDnsfile().getMinUnchosenLogId(), newProposalID, this.currentRound.getAcceptProposal().getValue());
            this.interRoundProposal = np;
            return SUCCEED;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FAIL;              
    }
    /**
     * Find the New proposa ID that is larger than the rejects' max minproposal ID.
     * @throws Exception 
     * 
     */
    public ProposalID findNewProposalIDLargerThanRej() throws Exception {
        ProposalID RejMaxMinProposalID = this.currentRound.findRejMaxMinproposalID();
        if (RejMaxMinProposalID.isNone()) {
            throw new Exception("[LeaderRoutine] [findNewProposalIDLargerThanRej]ERROR: no existing rejects");
        }
        /* Get a proposalId that is larger than the max rejProposalID */
        while (this.me.getDnsfile().getProposalId().Compare(RejMaxMinProposalID) <= 0) {
        	this.me.getDnsfile().getProposalId().incrementProposalId();
        }
        return this.me.getDnsfile().getProposalId();
    }
  //------------------- Commit -------------------------
    public void commit() {
        /* the proposed value is chosen */
    	DNSFile dnsfile = me.getDnsfile();
    	Entry entry = dnsfile.readEntry(this.currentRound.getLogId());
    	entry.setChosen();
    	System.out.println("[LR.commit]: " + entry);
    	/* leader commits, WRITE the chosen entry into log */
    	dnsfile.writeEntry(entry);
    	dnsfile.incrementMinUnchosenLogId(this.currentRound.getLogId());
    }
}
