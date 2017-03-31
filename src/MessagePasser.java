import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class MessagePasser {
    private Configuration myConfig;
    private Node me;
    private ListenerImpl listener;
//    private LeaderElection leaderElect;
    private int currentLeader;
//    private DebugLog mylog;
    public MessagePasser(String configuration_filename, int ID) {
        this.myConfig = new Configuration(configuration_filename);
        this.me = this.myConfig.getNodeMap().get(ID);
//        this.mylog = new DebugLog(me.getNodeID());
        this.currentLeader = -1;
        /*
         * Start RPC listener
         */
        try {
            this.listener = new ListenerImpl(myConfig, me);
            LocateRegistry.createRegistry(Integer.valueOf(me.getPort()));
            Naming.rebind("//localhost:" + me.getPort() + "/Listener" + me.getNodeID(), listener);
            System.out.println("Listener " + me.getNodeID() + " is listening on port:" + me.getPort());
        } catch(RemoteException e) {
            e.printStackTrace();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
    }
    /**
     * RUN entry
     * @throws InterruptedException
     */
    public void runNow() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10); //wait for other DNS replicas join in
        this.myConfig.updateListenerIntfMap(this.me.getNodeID()); 
//        this.LeaderElectionSection(this.currentLeader);
        TimeUnit.SECONDS.sleep(8);
        this.MulticastToEveryNode();
    }
    public void LeaderElectionSection(int currentld) {
        this.currentLeader = this.myConfig.getNextLeader(currentld);
        System.out.println("[LeaderElectionSection] leader is:" + this.currentLeader);    
    }
    public void MulticastToEveryNode() {
        for (ListenerIntf lisnode : this.myConfig.getListenerIntfMap().values()) {
            try {
                HeartBeatMessage hbmessage = new HeartBeatMessage(this.me.getNodeID(), "testhb", "testhb :D");
                lisnode.NormalHeartBeat(hbmessage);  
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
 
}
