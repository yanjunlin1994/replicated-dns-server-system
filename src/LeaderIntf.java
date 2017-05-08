import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LeaderIntf extends Remote {
	public void addNewProposal(Proposal p) throws RemoteException;
}
