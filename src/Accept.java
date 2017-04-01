import java.io.Serializable;
/**
 * Simple accept from leader
 *
 */
public class Accept implements Serializable {
    private int ID;
    private String value;
    public Accept(int i, String c) {
        this.ID = i;
        this.value = c;
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
        return "[Accept " + " ID." + this.ID + " " + this.value + "]";
    }

}
