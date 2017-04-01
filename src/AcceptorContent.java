
public class AcceptorContent {
    private int minProposal;
    private int AcceptedProposal;
    private String AcceptedValue;
    public AcceptorContent() {
        this.minProposal = -1;
        this.AcceptedProposal = -1;
        this.AcceptedValue = null;
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


    @Override
    public String toString() { 
        return "[AcceptedContent minProposal:" + this.minProposal + " AcceptedProposal:" +
                this.AcceptedProposal + " AcceptedValue:" + this.AcceptedValue;
    }

}

