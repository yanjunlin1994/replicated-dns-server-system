import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
/**
 * Leader sends heart beat messages to every acceptors
 */
public class LeaderHeartBeat implements Runnable {
    private int myID;
    private Configuration myConfig;
 
    public LeaderHeartBeat(int id, Configuration myConfig) {
        this.myID = id;
        this.myConfig = myConfig;   
    }
    @Override
    public void run(){
        System.out.println("[LeaderRoutine] [LeaderHeartBeat starts]");
        try {            
            HeartBeatMessage hbm = new HeartBeatMessage(this.myID, "leader heartbeat", "I am alive :)");
            while (true) {
                this.MulticastLeaderHeartBeat(hbm);
                hbm.increSeqNum();
                TimeUnit.SECONDS.sleep(5); //send heartbeat messsage every 5 seconds
            }
        } catch (InterruptedException e) {
            System.err.println("[LeaderRoutine] [LeaderHeartBeat] run fails ");
        } 
    }
    public void MulticastLeaderHeartBeat(HeartBeatMessage hbmessage) {
    	for (int noid : this.myConfig.getListenerIntfMap().keySet()) {
            ListenerIntf lisnode = this.myConfig.getListenerIntfMap().get(noid);
            try {
                lisnode.LeaderHeartBeat(hbmessage);  
            } catch (Exception e) {
            	try {
					this.myConfig.getListenerIntfMap().put(noid, (ListenerIntf) Naming.lookup("//localhost:" + this.myConfig.getNodeMap().get(noid).getPort() + "/Listener" + noid));
				} catch (MalformedURLException | RemoteException | NotBoundException e1) {
					System.err.println("[LH.MulticastLeaderHeartBeat] reconnect node " + noid + " fails");
				}
                continue;
            }
        }
    }

}
