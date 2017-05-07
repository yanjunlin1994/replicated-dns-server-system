import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
/**
 * EntryWriter class is used to read/write dnsFile entry by entry.
 */
public class EntryWriter {
	private File file;
	/* Number of bytes when Entry object is serialized */
	private static final int ENTRY_SIZE = new Entry(0).toByte().length;
	private static final int PROPOSALID_SIZE = new ProposalID().toByte().length;
	private static final int INT_BYTE_SIZE = 4;
//	private static final String FIRSTLINE = "minUnchosenLogId/proposalId/unAcceptedLogId  log id/minProposalId/acceptedProposalId/dns:ip" + System.getProperty("line.separator");
	private static final String FIRSTLINE = "proposalId  log id/minProposalId/acceptedProposalId/dns:ip" + System.getProperty("line.separator");
	private RandomAccessFile raf;
	/* headcount is the length of meta data in file's head */
//	private int headcount = FIRSTLINE.length() + System.getProperty("line.separator").length() + INT_BYTE_SIZE*2 + PROPOSALID_SIZE;
//	private int headcount = FIRSTLINE.length() + INT_BYTE_SIZE*2 + PROPOSALID_SIZE;
	private int headcount = FIRSTLINE.length() + PROPOSALID_SIZE;
	private int minUnchosenLogId;
	private int noMoreAcceptedLogId;
	private HashMap<ProposalID, Set<Integer>> proposalIdMapToUnchosenLogId;
	public EntryWriter(String filename, int node) throws IOException {
		file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
			raf = new RandomAccessFile(file, "rw");
			/* the first line */
			raf.writeBytes(FIRSTLINE);
			/* the unchosenLogId, default value: 0 */
//			raf.writeInt(0);
			/* the proposalId, default value: 0.nodeId */
            raf.write(new ProposalID(node).toByte());
            /* the unAcceptedLogId. Initially the log is empty, and it doesn't accept anything */
//            raf.writeInt(0);
//			raf.writeBytes(System.getProperty("line.separator"));
			raf.close();
			minUnchosenLogId = 0;
			noMoreAcceptedLogId = 0;
			proposalIdMapToUnchosenLogId = new HashMap<ProposalID, Set<Integer>>();
		} else {
			proposalIdMapToUnchosenLogId = new HashMap<ProposalID, Set<Integer>>();
			initialSetParam();
		}
	}
	public int getMinUnchosenLogId() {
		return minUnchosenLogId;
	}
	public int getNoMoreAcceptedLogId() {
		return noMoreAcceptedLogId;
	}
	public HashMap<ProposalID, Set<Integer>> getProposalIdMapToUnchosenLogId() {
		return proposalIdMapToUnchosenLogId;
	}
	public synchronized void initialSetParam() {
		boolean first = true;
		Entry entry = null;
		int logNum = logNum();
		for (int i = 0; i < logNum; i++) {
			entry = read(i);
			/* Set MinUnchosenLogId */
			if (!entry.isChosen()) {
				minUnchosenLogId = entry.getLogId();
				first = false;
			}
			/* If the log is not chosen but has been accepted, add its id in the hashmap */
			if (!entry.isChosen() && entry.getdns().hasAccepted()) {
				if (!proposalIdMapToUnchosenLogId.containsKey(entry.getAcceptedProposalId())) {
					proposalIdMapToUnchosenLogId.put(entry.getAcceptedProposalId(), new HashSet<Integer>());
				}
				proposalIdMapToUnchosenLogId.get(entry.getAcceptedProposalId()).add(entry.getLogId());
			}
			/* Set noMoreAcceptedLogId */
			if (!entry.getdns().hasAccepted()) {
				noMoreAcceptedLogId = entry.getLogId();
			}
		}
	}
 	/**
	 * Read the value of noMoreAcceptedLogId from the log.
	 */
//	public synchronized int readNoMoreAcceptedLogId() {
//		try {
//			raf = new RandomAccessFile(file, "rw");
//			raf.seek(FIRSTLINE.length() + INT_BYTE_SIZE + PROPOSALID_SIZE);
//			int noMoreAcceptedLogId = raf.readInt();
//			return noMoreAcceptedLogId;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return Integer.MAX_VALUE;
//	}
	/**
	 * Write the noMoreAcceptedLogId to log.
	 */
//	public synchronized void writeNoMoreAcceptedLogId(int id) {
//		try {
//			raf = new RandomAccessFile(file, "rw");
//			raf.seek(FIRSTLINE.length() + INT_BYTE_SIZE + PROPOSALID_SIZE);
//			raf.writeInt(id);
//			raf.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	public synchronized ProposalID readProposalId() {
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(FIRSTLINE.length() + INT_BYTE_SIZE);
			byte[] byteArray = new byte[PROPOSALID_SIZE];
			raf.read(byteArray);
			raf.close();
			return new ProposalID(byteArray);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ProposalID();
	}
	public synchronized void writeProposalId(ProposalID proposalId) {
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(FIRSTLINE.length() + INT_BYTE_SIZE);
			raf.write(proposalId.toByte());
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//	public synchronized void writeMinUnchosenLogId(int id) {
//		try {
//			raf = new RandomAccessFile(file, "rw");
//			raf.seek(FIRSTLINE.length());
//			raf.writeInt(id);;
//			raf.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	public synchronized int readMinUnchosenLogId() {
//		try {
//			raf = new RandomAccessFile(file, "rw");
//			raf.seek(FIRSTLINE.length());
//			int minUnchosenLogId = raf.readInt();
//			raf.close();
//			return minUnchosenLogId;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return -1;
//	}
	/**
	 * read Entry object from dnsFile.
	 */
	public synchronized Entry read(int logId) {
		try {
			raf = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Entry entry = null;
		try {
			byte[] byteArray = new byte[ENTRY_SIZE];
			/* If I set target as headcount+logId*ENTRY_SIZE-1, the acceptor will always read 1 more byte */
			int target = headcount + logId * ENTRY_SIZE;
			raf.seek(target);
			int filelength = (int) file.length();
			if (target >= filelength) {
//				System.out.println("[EntryWriter read] Reach the end of the file, this is log: " + logId);
				entry = new Entry(logId);
				write(entry);
			} else {
				int len = raf.read(byteArray);
				if (len < ENTRY_SIZE) {
					throw new RuntimeException("[EntryWriter read] Didn't read enough bytes");
				} else {
					/* If it is an totally empty entry: all 0s, return empty dns Entry */
					if (Arrays.equals(byteArray, new byte[ENTRY_SIZE])) {
//						System.out.println("\t\t2");
						return new Entry(0);
					}
//					System.out.println("logId:" + logId + ", byte size: " + byteArray.length + ", ENTRY_SIZE: " + ENTRY_SIZE);
					entry = new Entry(byteArray);
				}
			}
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return entry;
	}
	/**
	 * Write entry content to its log slot in dnsFile.
	 */ 
	public synchronized void write(Entry entry) {
		try {
			raf = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if (entry == null) {
			return;
		}
		int logId = entry.getLogId();
		try {
			int target = headcount + logId * ENTRY_SIZE;
			raf.seek(target);
			raf.write(entry.toByte());
//			System.out.println("\twrite log at: " + target + ", " + entry+". Size: " + entry.toByte().length);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public synchronized int logNum() {
		return (int) ((file.length() - headcount) /  ENTRY_SIZE);
	}
}
