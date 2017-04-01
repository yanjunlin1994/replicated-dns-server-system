
public class Proposal {
    private String content;
    public Proposal(String c) {
        this.content = c;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    @Override
    public String toString() { 
        return "This is a proposal " + this.content;
    }
    

}
