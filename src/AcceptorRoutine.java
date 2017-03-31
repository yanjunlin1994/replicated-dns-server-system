
public class AcceptorRoutine implements Runnable {
    private int myID;
    private Configuration myConfig;
 
    public AcceptorRoutine(int id, Configuration myConfig) {
        this.myID = id;
        this.myConfig = myConfig;   
    }
    @SuppressWarnings("resource")
    @Override
    public void run(){
        System.out.println("[Acceptor Routine starts]");
        while (true) {
            
        }        
    }
}
