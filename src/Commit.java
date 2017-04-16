import java.io.Serializable;
/**
 * Simple commit from leader
 *
 */
public class Commit implements Serializable {
    private String value;
    public Commit (String c) {
        this.value = c;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String v) {
        this.value = v;
    }
    @Override
    public String toString() { 
        return "[Commit " + this.value + "]";
    }

}
