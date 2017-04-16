/**
 * Acceptor content.
 *
 */
public class AcceptorContent {
    private int myID;
    private int roundNum;
    private int minProposal;
    private int AcceptedProposal;
    private String AcceptedValue;
    private String lastestAction;
    public AcceptorContent(int myID) {
        this.myID = myID;
        this.roundNum = -1;
        this.minProposal = -1;
        this.AcceptedProposal = -1;
        this.AcceptedValue = null;
        this.lastestAction = null;
    }  
    public int getMyID() {
        return this.myID;
    }
    public void setMyID(int myID) {
        this.myID = myID;
    }
    public int getRoundNum() {
        return roundNum;
    }
    public void setRoundNum(int roundNum) {
        this.roundNum = roundNum;
    }
    public int getMinProposal() {
        return this.minProposal;
    }
    public void setMinProposal(int minProposal) {
        this.minProposal = minProposal;
    }
    public int getAcceptedProposal() {
        return this.AcceptedProposal;
    }
    public void setAcceptedProposal(int acceptedProposal) {
        this.AcceptedProposal = acceptedProposal;
    }
    public String getAcceptedValue() {
        return this.AcceptedValue;
    }
    public void setAcceptedValue(String acceptedValue) {
        this.AcceptedValue = acceptedValue;
    }
    public String getLastestAction() {
        return this.lastestAction;
    }
    public void setLastestAction(String lastestAction) {
        this.lastestAction = lastestAction;
    }
    //TODO: export acceptor content to log file
    public void export() {
        
    }
    @Override
    public String toString() { 
        StringBuilder sb = new StringBuilder();
        sb.append("[AcceptedContent Round: ");
        sb.append(this.roundNum);
        sb.append(" minProposal ");
        sb.append(this.minProposal);
        sb.append(" AcceptedProposal: ");
        sb.append(this.AcceptedProposal);
        sb.append(" AcceptedValue: ");
        sb.append(this.AcceptedValue);
        sb.append(" lastestAction: ");
        sb.append(this.lastestAction);
        sb.append(" ]");
        return sb.toString();
    }
}

