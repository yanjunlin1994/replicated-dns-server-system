import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
public class RequestLeader implements Runnable {
	private enum STATE {
		NEW_PROPOSAL, INCREMENT_PROPOSALID, SET_PROPOSALID, PREPARE_SUCCEED,
		PREPARE_FAIL, ACCEPT_SUCCEED, ACCEPT_FAIL, SUCCEED, END;
	};
	
    private int myID;
    private Configuration myConfig;
    private int majority;
    private Round currentRound;
    private Node me;
    private Stopwatch stopwatch;
    private BlockingQueue<InterThreadMessage> ElectionAcceptorCommQueue;
    private Random random;
    public RequestLeader(int id, Configuration myConfig, BlockingQueue<InterThreadMessage> ElectionAcceptorCommQueue) {
    	this.myID = id;
        this.myConfig = myConfig;
        this.majority = ((myConfig.getListenerIntfMap().size() / 2) + 1);
        this.me = myConfig.getNodeMap().get(myID);
        this.ElectionAcceptorCommQueue = ElectionAcceptorCommQueue;
        this.random = new Random();
    }
    public void waitForRandomTime(int nodeId) {
    	long time = (long)random.nextFloat() * 1000 + (myConfig.getNodeMap().size() - 1 - nodeId) * 2000;
    	try {
			Thread.sleep(time);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    @Override
    public void run(){
        /* start dealing with proposal */
        System.out.println("[RequestLeaderRun]");
        STATE state;
        Proposal np = null;
        state = STATE.NEW_PROPOSAL;
        while (state != STATE.SUCCEED) {
        	/* set proposal */
            if (state == STATE.NEW_PROPOSAL) {
            	waitForRandomTime(myID);
	            np = NewProposal();
	        } else if (state == STATE.INCREMENT_PROPOSALID) {
//	        	waitForRandomTime(myID);
//	        	for (int i = 0; i < myID + 1; i++) {
	        		np.getProposalId().incrementProposalId();
//	        	}
	        } else if (state == STATE.SET_PROPOSALID) {
//	        	waitForRandomTime(myID);
//	        	for (int i = 0; i < myID + 1 ; i++) {
//	        		np.getProposalId().incrementProposalId();
//	        	}
	            np = RejectHandler();
	        /* proposalID has been reset */
	        }
            /* set round param: prepareProposal */
            SetNewRoundParam(np);
            /* prepare stage */
            state = prepare(np);
            /* Result of prepare stage */
            if (state == STATE.PREPARE_FAIL) {
            	state = STATE.INCREMENT_PROPOSALID;
                continue;
            } else if (state == STATE.END) {
            	return;
            } else {
            	state = accept();
            }
            /* Result of accept stage */
            if (state == STATE.ACCEPT_FAIL) {
            	state = STATE.SET_PROPOSALID;
                continue;
            } else if (state == STATE.ACCEPT_SUCCEED) {
            	this.commit();
            	state = STATE.SUCCEED;
            } else if (state == STATE.END){
            	return;
            }
        }
    }
    
    public Proposal NewProposal() {
    	/* Create a new proposal */
        Proposal np = new Proposal(-1, new ProposalID(myID), new DNSEntry("leader", String.valueOf(myID), "null"));
        System.out.println("[LR.NewProposal] new proposal " + np);
        return np;
    }
    //------------------- New Round Parameters -----------------------
    /**
     * Setting new round parameter
     * @param np
     */
    public void SetNewRoundParam(Proposal np) {
    	/* For a new round, set ProposalID, set Proposal */
        this.currentRound = new Round(myID, np.getProposalId().getRoundId(), np.getLogId());
        this.currentRound.setPrepareProposal(np);    
//        System.out.println("[LR.setNewRound] proposal: " + np);
    }
    public void SetRoundAcceptParam(Accept accp) {
    	this.currentRound.setAcceptProposal(accp);
    }
    //------------------- prepare -----------------------
    /**
     * Leader sends prepare proposal to slaves.
     * @param p
     * @return
     */
    public STATE prepare(Proposal p) {
//        System.out.println("[LR.prepare]");
    	int connection = 0;
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            if (lisnode == null) {
            	try {
					this.myConfig.getListenerIntfMap().put(noid, (ListenerIntf) Naming.lookup("//localhost:" + this.myConfig.getNodeMap().get(noid).getPort() + "/Listener" + noid));
					connection = connection + 1;
            	} catch (MalformedURLException | RemoteException | NotBoundException e1) {
					System.err.println("[LR.broadcastAccept] reconnect node " + noid + " fails");
				}
                continue;//continue to other listeners
            }
            try {
                Promise aPromise = lisnode.LeaderPrepareProposal(p);
                this.currentRound.addPromiseMap(aPromise);// add to promise map
                System.out.println("[LR.prepare] Recived: " + aPromise + ", myPrepare: " + p.getProposalId());
                if (aPromise.getIsIfrealPromise()) {
                    this.currentRound.increPromiseCount();
                }
                connection = connection + 1;
            } catch (Exception e) {
                System.err.println("[Leader Routine] [Prepare] Someone loses connection");
                try {
					this.myConfig.getListenerIntfMap().put(noid, (ListenerIntf) Naming.lookup("//localhost:" + this.myConfig.getNodeMap().get(noid).getPort() + "/Listener" + noid));
					connection = connection + 1;
                } catch (MalformedURLException | RemoteException | NotBoundException e1) {
					System.err.println("[LR.broadcastAccept] reconnect node " + noid + " fails");
				}
                continue;//continue to other listeners
            }
        } //end receiving promises
    	if (this.currentRound.getPromiseCount() >= this.majority) {//including promise from leader itself
            System.out.println("[LR.prepare] prepare Majority Achieved!");
            return STATE.PREPARE_SUCCEED;
        } else if (connection < this.majority) {
        	return STATE.END;
        } else {
        	/* If there is a chosen value in the promise, directly set currentRound's value to the chosen value */
            System.out.println("[LR.prepare] prepare not reach majority, try next proposal proposal ID");   
            return STATE.PREPARE_FAIL;
        }
    }
    //------------------- Accept -------------------------
    public STATE accept() {
    	/* If there is a prepare stage, check if acceptors return some accepted value */
    	
    	DNSEntry modifiedValue = this.currentRound.findPromiseMaxIDValue();
    	System.out.println("[RL.accept] after prepare, update AcceptedValue: " + modifiedValue);
//    	System.out.println("[PR.accept] return from findPromiseMaxIDValue");
    	boolean flag = modifiedValue.hasAccepted();
        if (!flag) {         
        	System.out.println("[RL.accept] keep previous value");
            modifiedValue = this.currentRound.getPrepareProposal().getDnsentry();
        }
        Accept accp = new Accept(this.currentRound.getLogId(), this.currentRound.getPrepareProposalID(), modifiedValue, -1);
        this.SetRoundAcceptParam(accp);       
    	System.out.println("[LR.accept] Accept(): " + accp);
        STATE result = this.BroadCastAccept(accp);
        return result;
    }    
    /**
     * send Accept message to nodes after receiving majority promise
     */
    public STATE BroadCastAccept(Accept acp) {
    	int connection = 0;
        for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            if (lisnode == null) {
            	try {
					this.myConfig.getListenerIntfMap().put(noid, (ListenerIntf) Naming.lookup("//localhost:" + this.myConfig.getNodeMap().get(noid).getPort() + "/Listener" + noid));
					connection = connection + 1;
            	} catch (MalformedURLException | RemoteException | NotBoundException e1) {
					System.err.println("[LR.broadcastAccept] reconnect node " + noid + " fails");
				}
            	continue;
            }
            try {
                Acknlg ack = lisnode.LeaderAcceptProposal(acp);
                this.currentRound.addAcknlgMap(ack);// add to ack map
                System.out.println("[LR.broadcastAccept] received: " + ack);
                if (ack.getisIfrealAcknlg()) {
                    this.currentRound.increAcceptCount();
                } else {
                	this.currentRound.setRejAck();
                	this.currentRound.addRejAcknlgSet(ack.getMinProposal()); //add rej's minproposal to the RejAcknlgSet
                }
                connection = connection + 1;
            } catch (RemoteException e) {
                System.err.println("[LR.broadcastAccept] node " + noid +" loses connection");
                // Try to connect to the unconnected server one more time
				try {
					this.myConfig.getListenerIntfMap().put(noid, (ListenerIntf) Naming.lookup("//localhost:" + this.myConfig.getNodeMap().get(noid).getPort() + "/Listener" + noid));
					connection = connection + 1;
				} catch (MalformedURLException | RemoteException | NotBoundException e1) {
					System.err.println("[LR.broadcastAccept] reconnect node " + noid + " fails");
				}
                continue;//continue to other listeners
            }
        }
        if (connection < this.majority) {
        	return STATE.END;
        } else if (this.currentRound.getRejAck()) {
            return STATE.ACCEPT_FAIL;
        } else {
        	return STATE.ACCEPT_SUCCEED;     
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
        /* TODO when is getAcceptProposal() update? */
//        Proposal np = new Proposal(-1, newProposalID, this.currentRound.getAcceptProposal().getValue());
//        newProposalID = new ProposalID(this.currentRound.getPrepareProposalID());
//        newProposalID.incrementProposalId();
        Proposal np = new Proposal(-1, newProposalID, this.currentRound.getAcceptProposal().getValue());
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
        /* TODO: GET a large enough proposalID */
        while (this.currentRound.getPrepareProposalID().Compare(RejMaxMinProposalID) <= 0) {
        	this.currentRound.getPrepareProposalID().incrementProposalId();
        	System.out.println(this.currentRound.getPrepareProposalID() + ", " + RejMaxMinProposalID);
        }
        return this.currentRound.getPrepareProposalID();
    }
  //------------------- Commit -------------------------
    public void commit() {
        /* the proposed value is chosen */
    	Entry entry = new Entry(this.currentRound.getLogId(), this.currentRound.getPrepareProposalID(), this.currentRound.getAcceptProposal().getProposalID(), this.currentRound.getAcceptProposal().getValue());
    	System.out.println("[LR.commit]: " + entry);
    	/* leader commits, WRITE the chosen entry into log */
    	/* TODO Send a message to Acceptor, notify about the decision of leader */
    	try {
    		int leaderID = Integer.valueOf(new String(this.currentRound.getAcceptProposal().getValue().getIp()).trim());
    		int port = this.myConfig.getNodeMap().get(leaderID).getPort() + 1;
    		if (leaderID == myID) {
    			try {
    	    		Leader leaderImpl = new Leader(myID);
    	    		/* register leaderImpl on a port */
    	            Naming.rebind("//localhost:" + port + "/Leader" + leaderID, leaderImpl);
    	    		new Thread(new LeaderRoutine(myID, myConfig, leaderImpl)).start();
    	    	} catch (RemoteException e) {
    	    		e.printStackTrace();
    	    	} catch (MalformedURLException e) {
    				e.printStackTrace();
    			}
			}
    		ElectionAcceptorCommQueue.put(new InterThreadMessage(myID, myID, "newLeader", String.valueOf(leaderID), -1));
    		this.myConfig.getListenerIntfMap().get(myID).chosenLeader(entry, port, leaderID);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
}
