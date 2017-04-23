import java.io.IOException;
import java.io.Serializable;

public class DNSEntry implements Serializable {
	private static final long serialVersionUID = -2456546379723602208L;
	private static final int DNS_MAXLENGTH = 32;
	private static final int IP_MAXLENGTH = 16;
	private byte[] dns;
	private byte[] ip;
	public DNSEntry() {
		byte[] nullarray = new String("null").getBytes();
		dns = new byte[DNS_MAXLENGTH];
		System.arraycopy(nullarray, 0, dns, 0, nullarray.length);
		ip = new byte[IP_MAXLENGTH];
		System.arraycopy(nullarray, 0, ip, 0, nullarray.length);
	}
	public byte[] getDns() {
		return dns;
	}
	public void setDns(byte[] ds) {
		System.arraycopy(ds, 0, this.dns, 0, DNS_MAXLENGTH);
	}
	public byte[] getIp() {
		return ip;
	}
	public void setIp(byte[] i) {
		System.arraycopy(i, 0, this.ip, 0, IP_MAXLENGTH);
	}
	/**
	 * Set dns field.
	 */
	public void setdns(String dns) {
		if (dns.length() > DNS_MAXLENGTH) {
			System.err.println("[DNSEntry setdns] string length exceeds limit.");
		}
		//TODO:length
		System.arraycopy(dns.getBytes(), 0, this.dns, 0, dns.length());
	}
	/**
	 * Set ip field.
	 */
	public void setip(String ip) {
		if (ip.length() > IP_MAXLENGTH) {
			System.err.println("[DNSEntry setip] string length exceeds limit.");
		}
		//TODO:length
		System.arraycopy(ip.getBytes(), 0, this.ip, 0, ip.length());
	}
	/**
	 * Return DNSEntry object's byte representation.
	 */
	public byte[] toByte() {
		byte[] array = new byte[DNS_MAXLENGTH + IP_MAXLENGTH];
		System.arraycopy(dns, 0, array, 0, DNS_MAXLENGTH);
		System.arraycopy(ip, 0, array, DNS_MAXLENGTH, IP_MAXLENGTH);
		return array;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(new String(dns)).append(":").append(new String(ip));
		return sb.toString();
	}
	/**
	 * Create a new DNSEntry by copy the given char array.
	 */
	public DNSEntry(String dns, String ip) {
		this.dns = new byte[DNS_MAXLENGTH];
		this.ip = new byte[IP_MAXLENGTH];
		if (dns.length() > DNS_MAXLENGTH || ip.length() > IP_MAXLENGTH) {
			System.err.println("[DNSEntry(dns, ip)] string length exceeds limit.");
		}
		//TODO: length
		System.arraycopy(dns.getBytes(), 0, this.dns, 0, dns.length());
		System.arraycopy(ip.getBytes(), 0, this.ip, 0, ip.length());
	}
	/**
	 * Create DNSEntry from byte array read from a line.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public DNSEntry(byte[] byteArray) throws IOException, ClassNotFoundException {
		dns = new byte[DNS_MAXLENGTH];
		ip = new byte[IP_MAXLENGTH];
		System.arraycopy(byteArray, 0, this.dns, 0, DNS_MAXLENGTH);
		System.arraycopy(byteArray, DNS_MAXLENGTH, this.ip, 0, IP_MAXLENGTH);
	}
	public boolean hasAccepted() {
		if (new String(dns).equals("null") || new String(ip).equals("null")) {
			return false;
		} else {
			return true;
		}
	}
}
