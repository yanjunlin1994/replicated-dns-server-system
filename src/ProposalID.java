import java.io.Serializable;

public class ProposalID implements Serializable {
	private static final long serialVersionUID = 1141384734335772126L;
	private int roundId;
	private int nodeId;
	public ProposalID() {
		this.roundId = -1;
		this.nodeId = -1;
	}
	public ProposalID(byte[] byteArray) {
		System.arraycopy(byteArray, 0, roundId, 0, 4);
		System.arraycopy(byteArray, 4, nodeId, 0, 4);
	}
	public ProposalID(int nodeId) {
		this.roundId = 0;
		this.nodeId = nodeId;
	}
	public boolean larger(ProposalID pi) {
		if (roundId > pi.roundId) {
			return true;
		} else if (roundId < pi.roundId) {
			return false;
		} else if (nodeId > pi.roundId) {
			return true;
		} else {
			return false;
		}
	}
	public void incrementProposalId() {
		this.roundId += 1;
	}
	public int getRoundId() {
		return roundId;
	}
	public void setRoundId(int i) {
		this.roundId = i;
	}
	public byte[] toByte() {
		byte[] re = new byte[8];
		System.arraycopy(roundId, 0, re, 0, 4);
		System.arraycopy(nodeId, 0, re, 4, 4);
		return re;
	}
}
