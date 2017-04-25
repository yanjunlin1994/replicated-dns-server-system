import java.io.Serializable;

public class Acknlg implements Serializable {
	private static final long serialVersionUID = -4725913617202844833L;
	private int src;
//    private int minProposal; //min proposal id
    private ProposalID minProposal;
    private boolean ifrealAcknlg; //could send the nack back as a type of promise
    public Acknlg(int s, ProposalID mp, boolean real) {
        this.src = s;
        this.minProposal = mp;
        this.ifrealAcknlg = real;
    }
    public Acknlg(Acknlg a) {
        this.src = a.getSrc();
        this.minProposal = a.getMinProposal();
        this.ifrealAcknlg = a.getisIfrealAcknlg();
    }
    public int getSrc() {
        return src;
    }
    public void setSrc(int src) {
        this.src = src;
    }
    public ProposalID getMinProposal() {
        return minProposal;
    }
    public void setMinProposal(ProposalID minProposal) {
        this.minProposal = minProposal;
    }
    public boolean getisIfrealAcknlg() {
        return ifrealAcknlg;
    }
    public void setIfrealAcknlg(boolean ifrealAcknlg) {
        this.ifrealAcknlg = ifrealAcknlg;
    }
    @Override
    public String toString() { 
        return "[ACK from " + this.src + "  " + this.ifrealAcknlg + " minProposal: " + this.minProposal + "]";
    }

}
