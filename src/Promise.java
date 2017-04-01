import java.io.Serializable;

public class Promise implements Serializable {
    private int ID;
    private String value;
    private boolean ifrealPromise;//could send the nack back as a type of promise
    public Promise(int i, String c) {
        this.ID = i;
        this.value = c;
        this.ifrealPromise = false;
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
    
    public boolean isIfrealPromise() {
        return this.ifrealPromise;
    }

    public void setIfrealPromise(boolean ifrealPromise) {
        this.ifrealPromise = ifrealPromise;
    }

    @Override
    public String toString() { 
        return "[" + this.ifrealPromise + " Promise ID." + this.ID + " " + this.value + "]";
    }

}
