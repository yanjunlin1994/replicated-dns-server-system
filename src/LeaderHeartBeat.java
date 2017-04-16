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
    @SuppressWarnings("resource")
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
//            e.printStackTrace();
        }        
    }
    public void MulticastLeaderHeartBeat(HeartBeatMessage hbmessage) {
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                lisnode.LeaderHeartBeat(hbmessage);  
            } catch (Exception e) {
                System.err.println("[LeaderRoutine] [LeaderHeartBeat] Someone close the connection");
            }
        }
    }

}
