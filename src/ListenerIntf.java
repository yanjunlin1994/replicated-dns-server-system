import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ListenerIntf extends Remote {
    public HeartBeatMessage HelloChat(HeartBeatMessage h) throws RemoteException;
    public void LeaderHeartBeat(HeartBeatMessage h) throws RemoteException;
    public String clientRequest(String st) throws RemoteException;
    public Promise LeaderPrepareProposal(Proposal p) throws RemoteException;
    public Acknlg LeaderAcceptProposal(Accept a) throws RemoteException;
    
    
    
    
}
