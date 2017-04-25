import java.io.Serializable;
import java.util.Arrays;

public class ProposalID implements Serializable, Comparable {
	private static final long serialVersionUID = 1141384734335772126L;
	private int roundId;
	private int nodeId;
	/* TODO: when to use an empty proposal? */
	public ProposalID() {
		this.roundId = -1;
		this.nodeId = -1;
	}
	public ProposalID(byte[] byteArray) {
		roundId = byteToInt(Arrays.copyOfRange(byteArray, 0, 4));
		nodeId = byteToInt(Arrays.copyOfRange(byteArray, 4, 8));
	}
	public ProposalID(int nodeId) {
		this.roundId = 0;
		this.nodeId = nodeId;
	}
	public ProposalID(ProposalID pi) {
		this.roundId = pi.roundId;
		this.nodeId = pi.nodeId;
	}
	public int Compare(ProposalID pi) {
		if (roundId > pi.roundId) {
			return 1;
		} else if (roundId < pi.roundId) {
			return -1;
		} else if (nodeId > pi.roundId) {
			return 1;
		} else if (nodeId < pi.roundId) {
			return -1;
		} else {
			return 0;
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
	private byte[] intToByte(int value) {
		return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	private int byteToInt(byte[] bytes) {
		int value = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
		        | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
		return value;
	}
	public byte[] toByte() {
		byte[] re = new byte[8];
		System.arraycopy(intToByte(roundId), 0, re, 0, 4);
		System.arraycopy(intToByte(nodeId), 0, re, 4, 4);
		return re;
	}
	@Override
	public int compareTo(Object o) {
		ProposalID pi = (ProposalID) o;
		return this.compareTo(pi);
	}
	public boolean isNone() {
		if (roundId == -1 && nodeId == -1) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return roundId + ":" + nodeId;
	}
}
