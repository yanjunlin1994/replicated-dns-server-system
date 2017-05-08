import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
public class LeaderRoutine implements Runnable {
	
	private enum STATE {
		NEW_PROPOSAL, INCREMENT_PROPOSALID, SET_PREVIOUS_PROPOSALVALUE, SET_PROPOSALID, PREPARE_SUCCEED,
		PREPARE_FAIL, PREPARE_LEARN, ACCEPT_SUCCEED_NEWVALUE, ACCEPT_SUCCEED_PREVIOUS_VALUE, ACCEPT_FAIL, SUCCEED;
	};
	
	private int paxosPrepareRound;
	private int userRequest;
	private int validateRound;
	
    private int myID;
    private Configuration myConfig;
    private Leader currentLeader;
    private int majority;
    private Round currentRound;
    private Node me;
    private boolean skipPrepare;
    /* Include the node Id of the nodes which has no more accepted value beyond current log id */
    private HashSet<Integer> noMoreAcceptedValueSet;
//    private BlockingQueue<InterThreadMessage> LeaderListenerCommQueue;
//    private BlockingQueue<InterThreadMessage> LeaderMpCommQueue;
    private Stopwatch stopwatch;
    
    public LeaderRoutine(int id, Configuration myConfig, Leader currentL) {
        this.myID = id;
        this.myConfig = myConfig;   
        this.currentLeader = currentL;
        this.majority = ((myConfig.getListenerIntfMap().size() / 2) + 1);
        this.me = myConfig.getNodeMap().get(myID);
        /* Initially, skip is set to false and noMoreAcceptedSet is empty */
        this.skipPrepare = false;
        noMoreAcceptedValueSet = new HashSet<Integer>();
//        this.LeaderListenerCommQueue = LeaderListenerCommQueue;
        
    }
    @Override
    public void run(){
        /* start dealing with proposal */
//        System.out.println("[ProposerRoutine start running]");
    	boolean first = true;
        STATE state;
        Proposal np = null;
        DNSEntry dnsentry = null;
        Thread leaderHB = new Thread(new LeaderHeartBeat(this.myID, this.myConfig));
        leaderHB.start();
        while (true) {
            if (this.currentLeader.getProcessQueueSize() > 0) {
            	if (first) {
            		stopwatch = new Stopwatch();
            		first = false;
            	}
                try {
                	state = STATE.NEW_PROPOSAL;
                	while (state != STATE.SUCCEED) {
	                	/* No matter skip prepare or not, set prepare proposal and newRound param properly,
	                	 * because accept() wil use it*/
                		if (state == STATE.NEW_PROPOSAL) {
                			userRequest = userRequest + 1;
                			paxosPrepareRound = paxosPrepareRound + 1;
	                		np = ReceiveNewProposal();
	                		dnsentry = new DNSEntry(np.getDnsentry());
	                	} else if (state == STATE.INCREMENT_PROPOSALID) {
	                		paxosPrepareRound = paxosPrepareRound + 1;
	                		np.getProposalId().incrementProposalId();
	                		this.me.getDnsfile().setProposalId(this.currentRound.getPrepareProposalID());
	                	} else if (state == STATE.SET_PREVIOUS_PROPOSALVALUE) {
	                		paxosPrepareRound = paxosPrepareRound + 1;
	                        np = new Proposal(this.me.getDnsfile().getMinUnchosenLogId(), new ProposalID(myID), dnsentry);
	                	} else if (state == STATE.SET_PROPOSALID) {
	                		paxosPrepareRound = paxosPrepareRound + 1;
	                		validateRound = validateRound + 1;
	                		np = RejectHandler();
	                		/* proposalID has been reset */
	                	}
                		
                		/* set round param: prepareProposal */
                		SetNewRoundParam(np);
                		
                		/* prepare stage */
                		if (!skipPrepare) {
                			state = prepare(np);
                		}
                		
                		/* Result of prepare stage */
                		if (state == STATE.PREPARE_LEARN) {
                			/* LEARN: In this case, already set the round param in prepare() */
                			state = STATE.ACCEPT_SUCCEED_NEWVALUE;
                		} else if (state == STATE.PREPARE_FAIL) {
                			state = STATE.INCREMENT_PROPOSALID;
                			continue;
                		} else {
                			/* including set Round param: AcceptProposal */
                			state = accept();
                		}
                		
                		/* Result of accept stage */
                		if (state == STATE.ACCEPT_FAIL) {
                			state = STATE.SET_PROPOSALID;
                			continue;
                		} else if (state == STATE.ACCEPT_SUCCEED_PREVIOUS_VALUE) {
                			this.commit();
                			state = STATE.SUCCEED;
                		} else {
                			this.commit();
                    		state = STATE.SET_PREVIOUS_PROPOSALVALUE;
                		}
                		
                	}
                	System.out.println("Prepare:" + this.paxosPrepareRound + ", userRequest: " + this.userRequest + ", validate: " + this.validateRound + ", time: " + stopwatch.elapsedTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public Proposal ReceiveNewProposal() {
    	/* Create a new proposal */
    	Proposal p = this.currentLeader.pollProposal();
//    	System.out.println("[pR.pollProposal] " + p);
        Proposal np = new Proposal(p);
        /* The dnsfile.proposalId is set propoerly right now. */
        np.setDnsentry(p.getDnsentry());
        /* Start with a smallest ProposalID */
        np.setProposalId(me.getDnsfile().getProposalId());
        /* The dnsfile.minUnchosenLogId is set propoerly right now. Use it to set logId */
        np.setLogId(me.getDnsfile().getMinUnchosenLogId());
//        System.out.println("[LR.ReceiveNewProposal] new proposal " + np);
        return np;
    }
    //------------------- New Round Parameters -----------------------
    /**
     * Setting new round parameter
     * @param np
     */
    public void SetNewRoundParam(Proposal np) {
        this.currentRound = new Round(myID, np.getProposalId().getRoundId(), np.getLogId());
        this.currentRound.setPrepareProposal(np);    
//        System.out.println("[LR.setNewRound] proposal: " + np);
    }
    //------------------- prepare -----------------------
    /**
     * Leader sends prepare proposal to slaves.
     * @param p
     * @return
     */
    public STATE prepare(Proposal p) {
        System.out.println("[LR.prepare]");
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            try {
            	/* Leader receive the promise from slaves */
                Promise aPromise = lisnode.LeaderPrepareProposal(p);
                this.currentRound.addPromiseMap(aPromise);// add to promise map
//                System.out.println("[LR.prepare] Recived: " + aPromise);
                if (aPromise.getIsIfrealPromise()) {
                	/* If one node sends noMoreAccpetedValue, add the node into noMoreAcceptedValueSet */
                    if (aPromise.getNoMoreAcceptedValue()) {
                    	noMoreAcceptedValueSet.add(aPromise.getSrc());
                    }
                    this.currentRound.increPromiseCount();
                }
            } catch (Exception e) {
                System.err.println("[Leader Routine] [Prepare] Someone loses connection");
                try {
					this.myConfig.getListenerIntfMap().put(noid, (ListenerIntf) Naming.lookup("//localhost:" + this.myConfig.getNodeMap().get(noid).getPort() + "/Listener" + noid));
				} catch (MalformedURLException | RemoteException | NotBoundException e1) {
					System.err.println("[LR.broadcastAccept] reconnect node " + noid + " fails");
				}
                continue;//continue to other listeners
            }
        } //end receiving promises
        Entry entry = this.currentRound.getChosen();
    	if (entry != null) {
    		/* Set Accept Proposal, get unMinchosenLogId from dnsfile */
    		Accept accp = new Accept(this.currentRound.getLogId(), entry.getAcceptedProposalId(), entry.getdns(), this.me.getDnsfile().getMinUnchosenLogId());
    		this.SetRoundAcceptParam(accp);
    		this.currentRound.setPrepareProposalID(entry.getAcceptedProposalId());
//    		System.out.println("[LR.prepare] exists chosen value"); 
    		return STATE.PREPARE_LEARN;
    	} else if (this.currentRound.getPromiseCount() >= this.majority) {//including promise from leader itself
//            System.out.println("[LR.prepare] prepare Majority Achieved!");
    		if (noMoreAcceptedValueSet.size() >= this.majority) {
            	skipPrepare = true;
            }
            return STATE.PREPARE_SUCCEED;
        } else {
        	/* If there is a chosen value in the promise, directly set currentRound's value to the chosen value */
//            System.out.println("[LR.prepare] prepare not reach majority, try next proposal proposal ID");   
            return STATE.PREPARE_FAIL;
        }
    }
    public void SetRoundAcceptParam(Accept accp) {
    	this.currentRound.setAcceptProposal(accp);
    }
    
    //------------------- Accept -------------------------
    public STATE accept() {
    	/* If there is a prepare stage, check if acceptors return some accepted value */
    	boolean flag = false;
    	Accept accp;
    	if (!skipPrepare) {
    		/* Create an Accept proposal */
    		DNSEntry modifiedValue = this.currentRound.findPromiseMaxIDValue();
//        	System.out.println("[PR.accept] return from findPromiseMaxIDValue");
        	flag = modifiedValue.hasAccepted();
            if (!flag) {               
                modifiedValue = this.currentRound.getPrepareProposal().getDnsentry();
            }
            accp = new Accept(this.currentRound.getLogId(), this.currentRound.getPrepareProposalID(), modifiedValue, this.me.getDnsfile().getMinUnchosenLogId());
            this.SetRoundAcceptParam(accp);
    	} else {
    		/* If there is no prepare stage, in Accept proposal, just use its previous value */
    		accp = new Accept(this.currentRound.getLogId(), this.currentRound.getPrepareProposal().getProposalId(), this.currentRound.getPrepareProposal().getDnsentry(), this.me.getDnsfile().getMinUnchosenLogId());
    	}
//    	System.out.println("[LR.accept] Accept(): " + accp);
    	this.SetRoundAcceptParam(accp);
        this.BroadCastAccept(accp);
        /* Set Accept proposal in currentRound.acceptProposal */
        if (this.currentRound.getRejAck()) {
        	return STATE.ACCEPT_FAIL;
        } else if (!flag) {
        	return STATE.ACCEPT_SUCCEED_PREVIOUS_VALUE;
        } else {
            return STATE.ACCEPT_SUCCEED_NEWVALUE;      
        }   
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
//                System.out.println("[LR.broadcastAccept] received: " + ack);
                if (ack.getisIfrealAcknlg()) {
                    this.currentRound.increAcceptCount();
                } else {
                	/* If one node rejects the accept proposal, set skipPrepare to false and remove the node from noMoreAcceptedValueSet */
                	skipPrepare = false;
//                	noMoreAcceptedValueSet.remove(new Integer(ack.getSrc()));
                	noMoreAcceptedValueSet.clear();
                	this.currentRound.setRejAck();
                	this.currentRound.addRejAcknlgSet(ack.getMinProposal()); //add rej's minproposal to the RejAcknlgSet
                }
            } catch (RemoteException e) {
                System.err.println("[LR.broadcastAccept] node " + noid +" loses connection");
                // Try to connect to the unconnected server one more time
				try {
					this.myConfig.getListenerIntfMap().put(noid, (ListenerIntf) Naming.lookup("//localhost:" + this.myConfig.getNodeMap().get(noid).getPort() + "/Listener" + noid));
				} catch (MalformedURLException | RemoteException | NotBoundException e1) {
					System.err.println("[LR.broadcastAccept] reconnect node " + noid + " fails");
				}
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
    public Proposal RejectHandler() {
        ProposalID newProposalID;
        newProposalID = this.findNewProposalIDLargerThanRej();
        /* TODO when is getAcceptProposal() update? It returns NullPointerException here */
        Proposal np = new Proposal(me.getDnsfile().getMinUnchosenLogId(), newProposalID, this.currentRound.getAcceptProposal().getValue());
        return np;
    }
    /**
     * Find the New proposa ID that is larger than the rejects' max minproposal ID.
     * @throws Exception 
     * 
     */
    public ProposalID findNewProposalIDLargerThanRej() {
        ProposalID RejMaxMinProposalID = this.currentRound.findRejMaxMinproposalID();
        if (RejMaxMinProposalID.isNone()) {
            System.err.println("[LeaderRoutine] [findNewProposalIDLargerThanRej]ERROR: no existing rejects");
        }
        if (RejMaxMinProposalID.isChosen()) {
        	this.currentRound.setPrepareProposalID(new ProposalID(Integer.MAX_VALUE, myID));
        	return this.currentRound.getPrepareProposalID();
        }
        /* Get a proposalId that is larger than the max rejProposalID */
        while (this.me.getDnsfile().getProposalId().Compare(RejMaxMinProposalID) <= 0) {
        	/* Each time increment the proposalID, store it in the dnsfile object */
        	this.currentRound.getPrepareProposalID().incrementProposalId();
//        	System.out.println(this.currentRound.getPrepareProposalID() + ", " + RejMaxMinProposalID);
        }
        this.me.getDnsfile().setProposalId(this.currentRound.getPrepareProposalID());
        return this.me.getDnsfile().getProposalId();
    }
  //------------------- Commit -------------------------
    public void commit() {
        /* the proposed value is chosen */
    	DNSFile dnsfile = me.getDnsfile();
    	Entry entry = new Entry(this.currentRound.getLogId(), this.currentRound.getPrepareProposalID(), this.currentRound.getAcceptProposal().getProposalID(), this.currentRound.getAcceptProposal().getValue());
    	entry.setChosen();
    	System.out.println("[LR.commit]: " + entry);
    	/* leader commits, WRITE the chosen entry into log */
    	dnsfile.writeEntry(entry);
    	dnsfile.incrementMinUnchosenLogId(this.currentRound.getLogId());
    }
}
