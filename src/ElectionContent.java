/**
 * Content about the election
 *
 */
public class ElectionContent {
    private int status;
    private int biggestCandidate;
    public ElectionContent() {
        this.status = -1;
        this.biggestCandidate = -1;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getBiggestCandidate() {
        return biggestCandidate;
    }
    public void setBiggestCandidate(int biggestCandidate) {
        this.biggestCandidate = biggestCandidate;
    }
    public void clear() {
//        System.out.println("[ElectionContent] [clear]"); 
        this.status = -1;
        this.biggestCandidate = -1;
    }

}
