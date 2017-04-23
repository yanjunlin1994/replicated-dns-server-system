import java.io.InputStream;
import java.rmi.Naming;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import java.util.List;

/**
 * Configuration object.
 */
public class Configuration {
    private HashMap<Integer,Node> nodeMap;
    private HashMap<Integer,ListenerIntf> ListenerIntfMap;
    private String dnsFile;
    /**
     * Configuration constructor.
     * Construct nodeMap based on configuration file.
     * @param config_fileName configuration file
     */
    public Configuration(String config_fileName){
        this.nodeMap = new HashMap<Integer,Node>();
        this.ListenerIntfMap = new HashMap<Integer, ListenerIntf>();
        InputStream IS = null;
        try {
            IS = new FileInputStream(new File(config_fileName));
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map<String, Object>) yaml.load(IS); 
        //-------------nodes-----------
        List<HashMap<Integer, Object>> nodes = (List<HashMap<Integer, Object>> )data.get("configuration");
        for (HashMap<Integer, Object> node : nodes) {
            Node newNode = new Node((int)node.get("id"), (String)node.get("ip"),
                                    (int)node.get("port"));
            this.nodeMap.put((int)node.get("id"),newNode); //put node in nodemap   
        }
        this.updateProposalNumSet();
        //------------- log file -----------
        dnsFile = (String) data.get("dnsFile");
//        System.out.println("[Configuration] dns target path: " + dnsFile);
       
    } 
    /**
     * Update the proposal number set of each node
     */
    public void updateProposalNumSet() {
        int base  = 0;
        int proposalNum = 20;
        for (Node nd : this.nodeMap.values()) {
           for (int i = 0; i < proposalNum; i++) {
               nd.addProposalNum(base + nd.getNodeID());
               base += 10;
           }
           base = 0;
        }
    }
    /**
     * Update the configuration file to include the listener for all other nodes. 
     * @param myID
     */
    public void updateListenerIntfMap(int myID) {
        for (Node nd : this.nodeMap.values()) {
            if (nd.getNodeID() == myID) {
                continue;
            }
            String lookupName = "//localhost:" + nd.getPort() + "/Listener" + nd.getNodeID();
            try {
                ListenerIntf NodeListener = (ListenerIntf) Naming.lookup(lookupName);
                HeartBeatMessage hello = new HeartBeatMessage(myID, "hello", "hello :D");
                HeartBeatMessage helloBack = NodeListener.HelloChat(hello);
                this.ListenerIntfMap.put(nd.getNodeID(),NodeListener);//put in ListenerInterface map
                System.out.println("[updateListenerIntfMap] [Recieve HelloBACK] "+ helloBack);
            } catch (Exception e) {
                System.out.println("[updateListenerIntfMap] connection fails with Node." + nd.getNodeID());
                continue;
            }
        }
    }
    /**
     * Get next leader ID
     * @param currentLeader's ID
     * @return next leader's ID
     */
    public int getNextLeader(int currentLeader) {
        int nextID = (currentLeader + 1) % nodeMap.size();
        return this.nodeMap.get(nextID).getNodeID();   
    }
    public void removeNode(int nodeID) {
        Node rmN = this.nodeMap.get(nodeID);
        rmN.setActive(false);
        this.ListenerIntfMap.remove(nodeID);   
    }
    public HashMap<Integer, ListenerIntf> getListenerIntfMap() {
        return this.ListenerIntfMap;
    }
    public HashMap<Integer, Node> getNodeMap() {
        return this.nodeMap;
    }
    public String getDNSFile() {
    	return this.dnsFile;
    }
}
