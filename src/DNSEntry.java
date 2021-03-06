import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class DNSEntry implements Serializable {
	private static final long serialVersionUID = -2456546379723602208L;
	public static final int DNS_MAXLENGTH = 60;
	public static final int IP_MAXLENGTH = 32;
	private static final int OPERATION_LENGTH = 8;
	private byte[] dns;
	private byte[] ip;
	private byte[] operation;
	public DNSEntry(DNSEntry entry) {
		dns = new byte[DNS_MAXLENGTH];
		System.arraycopy(entry.dns, 0, dns, 0, DNS_MAXLENGTH);
		ip = new byte[IP_MAXLENGTH];
		System.arraycopy(entry.ip, 0, ip, 0, IP_MAXLENGTH); 
		operation = new byte[OPERATION_LENGTH];
		System.arraycopy(entry.operation, 0, operation, 0, OPERATION_LENGTH);
	}
	public DNSEntry() {
		byte[] nullarray = new String("null").getBytes();
		dns = new byte[DNS_MAXLENGTH];
		System.arraycopy(nullarray, 0, dns, 0, nullarray.length);
		ip = new byte[IP_MAXLENGTH];
		System.arraycopy(nullarray, 0, ip, 0, nullarray.length);
		operation = new byte[OPERATION_LENGTH];
		System.arraycopy(nullarray, 0, operation, 0, nullarray.length);
	}
	public byte[] getDns() {
		return dns;
	}
	public void setDns(byte[] ds) {
		if (ds.length > DNS_MAXLENGTH) {
			System.err.println("[DNSEntry setdns] byte array length exceeds limit.");
		}
		System.arraycopy(ds, 0, this.dns, 0, ds.length);
		Arrays.fill(this.dns, ds.length, DNS_MAXLENGTH, (byte)0);
	}
	public byte[] getIp() {
		return ip;
	}
	public void setIp(byte[] i) {
		if (i.length > IP_MAXLENGTH) {
			System.err.println("[DNSEntry setip] byte array length exceeds limit.");
		}
		System.arraycopy(i, 0, this.ip, 0, ip.length);
		Arrays.fill(this.ip, i.length, IP_MAXLENGTH, (byte)0);
	}
	/**
	 * Set dns field.
	 */
	public void setdns(String dns) {
		if (dns.length() > DNS_MAXLENGTH) {
			System.err.println("[DNSEntry setdns] string length exceeds limit.");
		}
		System.arraycopy(dns.getBytes(), 0, this.dns, 0, dns.length());
		Arrays.fill(this.dns, dns.length(), DNS_MAXLENGTH, (byte)0);
	}
	/**
	 * Set ip field.
	 */
	public void setip(String ip) {
		if (ip.length() > IP_MAXLENGTH) {
			System.err.println("[DNSEntry setip] string length exceeds limit.");
		}
		System.arraycopy(ip.getBytes(), 0, this.ip, 0, ip.length());
		Arrays.fill(this.ip, ip.length(), IP_MAXLENGTH, (byte)0);
	}
	/**
	 * Return DNSEntry object's byte representation.
	 */
	public byte[] toByte() {
		byte[] array = new byte[DNS_MAXLENGTH + IP_MAXLENGTH + OPERATION_LENGTH];
		System.arraycopy(dns, 0, array, 0, DNS_MAXLENGTH);
		System.arraycopy(ip, 0, array, DNS_MAXLENGTH, IP_MAXLENGTH);
		System.arraycopy(operation, 0, array, DNS_MAXLENGTH + IP_MAXLENGTH, OPERATION_LENGTH);
		return array;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(new String(dns).trim()).append(":").append(new String(ip).trim()).append(new String(operation).trim());
		return sb.toString();
	}
	/**
	 * Create a new DNSEntry by copy the given char array.
	 */
	public DNSEntry(String dns, String ip, String operation) {
		this.dns = new byte[DNS_MAXLENGTH];
		this.ip = new byte[IP_MAXLENGTH];
		this.operation = new byte[IP_MAXLENGTH];
		if (dns.length() > DNS_MAXLENGTH || ip.length() > IP_MAXLENGTH || operation.length() > OPERATION_LENGTH) {
			System.err.println("[DNSEntry(dns, ip)] string length exceeds limit.");
		}
		System.arraycopy(dns.getBytes(), 0, this.dns, 0, dns.length());
		Arrays.fill(this.dns, dns.length(), DNS_MAXLENGTH, (byte)0);
		System.arraycopy(ip.getBytes(), 0, this.ip, 0, ip.length());
		Arrays.fill(this.ip, ip.length(), IP_MAXLENGTH, (byte)0);
		System.arraycopy(operation.getBytes(), 0, this.operation, 0, operation.length());
		Arrays.fill(this.operation, operation.length(), OPERATION_LENGTH, (byte)0);
	}
	/**
	 * Create DNSEntry from byte array read from a line.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public DNSEntry(byte[] byteArray) throws IOException, ClassNotFoundException {
		dns = new byte[DNS_MAXLENGTH];
		ip = new byte[IP_MAXLENGTH];
		operation = new byte[OPERATION_LENGTH];
		System.arraycopy(byteArray, 0, this.dns, 0, DNS_MAXLENGTH);
		System.arraycopy(byteArray, DNS_MAXLENGTH, this.ip, 0, IP_MAXLENGTH);
		System.arraycopy(byteArray, DNS_MAXLENGTH + IP_MAXLENGTH, this.operation, 0, OPERATION_LENGTH);
	}
	public boolean hasAccepted() {
		if (new String(dns).trim().equals("null") || new String(ip).trim().equals("null")) {
			return false;
		} else {
			return true;
		}
	}
	public void setOperation(byte[] operation) {
		System.arraycopy(operation, 0, this.operation, 0, OPERATION_LENGTH);
	}
	public byte[] getOperation() {
		return this.operation;
	}
}
