import java.io.Serializable;

public class Acknlg implements Serializable {
    private int src;
    private int ID; //min proposal id
    private String value;
    private boolean ifrealAcknlg;//could send the nack back as a type of promise
    public Acknlg(int s, int i) {
        this.src = s;
        this.ID = i;
        this.ifrealAcknlg = false;
    }
    public Acknlg(Acknlg a) {
        this.src = a.getSrc();
        this.ID = a.getID();
        this.value = a.getValue();
        this.ifrealAcknlg = a.getIsIfrealAcknlg();
    }
    
    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
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
    
    public boolean getIsIfrealAcknlg() {
        return this.ifrealAcknlg;
    }

    public void setIfrealAcknlg(boolean i) {
        this.ifrealAcknlg = i;
    }

    @Override
    public String toString() { 
        return "[" + this.ifrealAcknlg + " Promise ID." + this.ID + " " + this.value + "]";
    }

}
