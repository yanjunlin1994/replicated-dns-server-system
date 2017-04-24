import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/**
 * EntryWriter class is used to read/write dnsFile entry by entry.
 */
public class EntryWriter {
	private File file;
	/* Number of bytes when Entry object is serialized */
	private static final int ENTRY_SIZE = new Entry(0).toByte().length;
	private static final int INT_BYTE_SIZE = 4;
	private static final String FIRSTLINE = "log id, minProposalId, acceptedProposalId, dns:ip" + System.getProperty("line.separator");
	private RandomAccessFile raf;
	/* headcount is the length of the first line */
	private int headcount = FIRSTLINE.length() + System.getProperty("line.separator").length() + INT_BYTE_SIZE;
	public EntryWriter(String filename) throws IOException {
		file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
			raf = new RandomAccessFile(file, "rw");
			raf.writeBytes(FIRSTLINE);
			raf.writeInt(-1);
			raf.writeBytes(System.getProperty("line.seperator"));
			raf.close();
		}
	}
	public void writeMinUnchosenLogId(int id) {
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(FIRSTLINE.length());
			int minUnchosenLogId = raf.readInt();
			raf.close();
			return minUnchosenLogId;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	public int readMinUnchosenLogId() {
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(FIRSTLINE.length());
			int minUnchosenLogId = raf.readInt();
			raf.close();
			return minUnchosenLogId;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * read Entry object from dnsFile.
	 */
	public Entry read(int logId) {
		try {
			raf = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Entry entry = null;
		try {
			byte[] byteArray = new byte[ENTRY_SIZE];
			int target = headcount + logId * ENTRY_SIZE;
//			System.out.println("2: target "+target+" filelength:"+file.length());
			raf.seek(target);
			int filelength = (int) file.length();
			if (target >= filelength) {
				System.out.println("[EntryWriter read] Reach the end of the file");
				entry = new Entry(logId);
				write(entry);
			} else {
				int len = raf.read(byteArray);
				if (len < ENTRY_SIZE) {
					throw new RuntimeException("[EntryWriter read] Didn't read enough bytes");
				} else {
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
	public void write(Entry entry) {
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
			System.out.println("\twrite log at: " + target + ", " + entry);
			raf.write(entry.toByte());
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
