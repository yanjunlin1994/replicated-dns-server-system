import java.io.Serializable;

public class Proposal implements Serializable {
    private int ID;
    private String value;
    public Proposal(int i, String c) {
        this.ID = i;
        this.value = c;
    }
    /**
     * Clone
     * @param p
     */
    public Proposal(Proposal p) {
        this.ID = p.getID();
        this.value = p.getValue();
    }
    
    public int getID() {
        return this.ID;
    }

    public void setID(int iD) {
        this.ID = iD;
    }

    public String getValue() {
        return this.value;
    }
    public void setValue(String v) {
        this.value = v;
    }
    @Override
    public String toString() { 
        return "[Proposal ID." + this.ID + " " + this.value + "]";
    }
}
