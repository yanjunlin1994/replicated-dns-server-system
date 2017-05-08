import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
public class MessagePasser {
    private Configuration myConfig;
    private Node me;
    private int myID;
    private ListenerImpl listener;
    private BlockingQueue<InterThreadMessage> AcceptorListenerCommQueue;
    private BlockingQueue<InterThreadMessage> LeaderListenerCommQueue;
    /* From MessagePasser to AcceptorRoutine, tell Acceptor who is leader now */
    /**
     * Constructor
     * @param configuration_filename
     * @param ID
     * @throws IOException 
     */
    public MessagePasser(String configuration_filename, int ID) throws IOException {
        this.myConfig = new Configuration(configuration_filename);         
        this.myID = ID;
        this.me = this.myConfig.getNodeMap().get(ID);  
        /* set the dns file for current node */
        this.me.setDnsfile(new DNSFile(this.myID));
        this.AcceptorListenerCommQueue = new LinkedBlockingQueue<InterThreadMessage>();

        try {
            this.listener = new ListenerImpl(this.myConfig, this.me, this.AcceptorListenerCommQueue);
            LocateRegistry.createRegistry(Integer.valueOf(this.me.getPort()));
            LocateRegistry.createRegistry(Integer.valueOf(this.me.getPort()) + 1);
            Naming.rebind("//localhost:" + this.me.getPort() + "/Listener" + me.getNodeID(), listener);
            System.out.println("Listener " + this.myID + " is listening on port:" + this.me.getPort());
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
    public synchronized void runNow() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10); //wait for other DNS replicas join in
        this.myConfig.updateListenerIntfMap(this.myID);
        /* open acceptor routine */
        new Thread(new AcceptorRoutine(this.myID, this.myConfig, this.AcceptorListenerCommQueue,this.LeaderListenerCommQueue, this.listener)).start();
    }
}
