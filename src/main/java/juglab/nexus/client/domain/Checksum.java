package juglab.nexus.client.domain;

/**
 * 
 * @author turek
 *
 */

public class Checksum {
	private String sha1;
	private String md5;
	
	public String getSHA1() {
		return sha1;
	}
	
	public void setSHA1( String sha1 ) {
		this.sha1 = sha1;
	}
	
	public String getMD5() {
		return md5;
	}
	
	public void setMD5( String md5 ) {
		this.md5 = md5;
	}
}
