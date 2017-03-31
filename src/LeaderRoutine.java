import java.util.concurrent.TimeUnit;

public class LeaderRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
 
    public LeaderRoutine(int id, Configuration myConfig) {
        this.myID = id;
        this.myConfig = myConfig;   
    }
    @SuppressWarnings("resource")
    @Override
    public void run(){
        System.out.println("[LeaderRoutine Routine starts]");
        try {
            HeartBeatMessage hbm = new HeartBeatMessage(this.myID, "leader heartbeat", "I am alive :)");
            while (true) {
                TimeUnit.SECONDS.sleep(10); //send heartbeat messsage every 10 seconds
                this.MulticastToEveryNode(hbm);
                hbm.increSeqNum();
            }         
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }
    public void MulticastToEveryNode(HeartBeatMessage hbmessage) {
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                lisnode.LeaderHeartBeat(hbmessage);  
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
