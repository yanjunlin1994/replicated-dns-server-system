/**
 * Main test class.
 * initializes the MessagePasser and make it run.
 *
 */
public class Driver {
    public static void main(String args[]) throws InterruptedException{
        String configFileName = args[0];
        int myID = Integer.valueOf(args[1]);
        MessagePasser mp = new MessagePasser(configFileName, myID);
        mp.runNow();
    }   
}
