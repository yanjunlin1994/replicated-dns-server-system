import java.io.Serializable;
import java.util.Arrays;

public class ProposalID implements Serializable, Comparable<ProposalID> {
	private static final long serialVersionUID = 1141384734335772126L;
	private int roundId;
	private int nodeId;
	public static final ProposalID CHOSEN_PROPOSALID= new ProposalID(Integer.MAX_VALUE, Integer.MAX_VALUE);
	/* TODO: when to use an empty proposal? */
	public ProposalID() {
		this.roundId = -1;
		this.nodeId = -1;
	}
	public ProposalID(int roundId, int nodeId) {
		this.roundId = roundId;
		this.nodeId = nodeId;
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
		if (roundId == Integer.MAX_VALUE && pi.getRoundId() == Integer.MAX_VALUE) {
			return 0;
		} else if (roundId > pi.roundId) {
			return 1;
		} else if (roundId < pi.roundId) {
			return -1;
		} else if (nodeId > pi.nodeId) {
			return 1;
		} else if (nodeId < pi.nodeId) {
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
	public int compareTo(ProposalID pi) {
		return this.Compare(pi);
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
		return roundId + "." + nodeId;
	}
	@Override
	public boolean equals(Object o) {
		return this.compareTo((ProposalID) o) == 0;
	}
	@Override
	public int hashCode() {
		if (roundId == Integer.MAX_VALUE) {
			return new Integer(Integer.MAX_VALUE).hashCode()/7;
		}
		return new Integer(roundId).hashCode() / 7 + new Integer(nodeId).hashCode() / 6 * 5;
	}
	public boolean isChosen() {
		return roundId == Integer.MAX_VALUE;
	}
}
