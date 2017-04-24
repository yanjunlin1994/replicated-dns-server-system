import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Entry implements Serializable {
	private static final long serialVersionUID = 3433680946993754863L;
	private int logId;
	private int minProposalId;
	private int acceptedProposalId;
	private DNSEntry dnsEntry;
	private static final int DNSENTRY_SIZE = new DNSEntry().toByte().length;
	/**
	 * Create a default entry for logId in dnsFile 
	 */
	protected Entry(int logId) {
		this.logId = logId;
		minProposalId = -1;
		acceptedProposalId = -1;
		dnsEntry = new DNSEntry();
	}
	public Entry(int logId, int minProposalId, int acceptedId, DNSEntry dnsentry) {
		this.logId = logId;
		this.minProposalId = minProposalId;
		this.acceptedProposalId = acceptedId;
		this.dnsEntry = dnsentry;
	}
	/**
	 * Return the Entry object's byte representation.
	 */
	public byte[] toByte() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		byte[] re = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeInt(logId);
			oos.writeInt(minProposalId);
			oos.writeInt(acceptedProposalId);
			oos.write(dnsEntry.toByte());
			oos.flush();
			baos.flush();
			re = baos.toByteArray();
			oos.close();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return re;
	}
	/**
	 * Create an Entry using String read from one line in dnsFile.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected Entry(byte[] byteArray) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		ObjectInputStream ois = new ObjectInputStream(bais);
		logId = ois.readInt();
		minProposalId = ois.readInt();
		acceptedProposalId = ois.readInt();
		byte[] array = new byte[DNSENTRY_SIZE];
		int entrylen = ois.read(array);
		if (entrylen < DNSENTRY_SIZE) {
			throw new RuntimeException("[Entry byteArray] Didn't read enough bytes");
		} else {
			dnsEntry = new DNSEntry(array);
		}
	}
	/* If acceptedProposalId equals to MAX_VALUE, the corresponding value is chosen */
	protected boolean isChosen() {
		if (acceptedProposalId == Integer.MAX_VALUE) {
			return true;
		} else {
			return false;
		}
	}
	/* Set acceptedProposalId to MAX_VALUE */
	protected void setChosen() {
		acceptedProposalId = Integer.MAX_VALUE;
	}
	public int getLogId() {
		return logId;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}
	public int getMinProposalId() {
		return minProposalId;
	}
	public void setMinProposalId(int minProposalId) {
		this.minProposalId = minProposalId;
	}
	public int getAcceptedProposalId() {
		return acceptedProposalId;
	}
	public void setAcceptedProposalId(int acceptedProposalId) {
		this.acceptedProposalId = acceptedProposalId;
	}
	public DNSEntry getdns() {
		return dnsEntry;
	}
	public void setdnsEntry(DNSEntry dnsEntry) {
		this.dnsEntry = dnsEntry;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(logId).append(",").append(minProposalId).append(",").append(acceptedProposalId).append(",").append(dnsEntry);
		return sb.toString();
	}
}
