import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ListenerIntf extends Remote {
    public HeartBeatMessage HelloChat(HeartBeatMessage h) throws RemoteException;
    public void NormalHeartBeat(HeartBeatMessage h) throws RemoteException;
}
