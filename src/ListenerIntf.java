import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ListenerIntf extends Remote {
    public HeartBeatMessage HelloChat(HeartBeatMessage h) throws RemoteException;
    public void LeaderHeartBeat(HeartBeatMessage h) throws RemoteException;
    public Promise LeaderPrepareProposal(Proposal p) throws RemoteException;
    public Acknlg LeaderAcceptProposal(Accept a) throws RemoteException;
	public String clientRequest(DNSEntry dnsentry) throws RemoteException;  
	public boolean ElectLeaderRequest(int proid) throws RemoteException;
	public boolean ElectLeaderConfirm(int proid) throws RemoteException;
	public void ElectLeaderVictory(int proid) throws RemoteException;
}
