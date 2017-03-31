import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.Naming;
import java.util.Map;
import java.util.HashMap;
public class ListenerImpl extends UnicastRemoteObject implements ListenerIntf{
    private Configuration myConfig;
    private Node me;
    /**
     * Constructor
     * @param config
     * @param m
     * @throws RemoteException
     */
    protected ListenerImpl(Configuration config, Node m) throws RemoteException {
        super(0);
        this.myConfig = config;
        this.me = m;
    }
    @Override
    public HeartBeatMessage HelloChat(HeartBeatMessage h) throws RemoteException {
        HeartBeatMessage mesg = h;
        System.out.println("[Recieve Hello] " + mesg);
        HeartBeatMessage helloBack = new HeartBeatMessage(this.me.getNodeID(), "hello", "helloback");
        return helloBack;
    }
    @Override
    public void LeaderHeartBeat(HeartBeatMessage h) throws RemoteException {
        HeartBeatMessage mesg = h;
        System.out.println("[Recieve LeaderHeartBeat] " + mesg);
        return;
    }

}