package juglab.nexus.client.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representation of a Nexus asset
 * 
 * @author turek
 *
 */

public class Asset {

	@JsonProperty("downloadUrl")
	private String downloadUrl;
	@JsonProperty("path")
	private String path;
	@JsonProperty("id")
	private String id;
	@JsonProperty("repository")
	private String repository;
	@JsonProperty("format")
	private String format;
	@JsonProperty("checksum")
	private Checksum checksum;
	
	
	public String getDownloadUrl() {
		return downloadUrl;
	}
	
	public void setDownloadUrl( String downloadUrl ) {
		this.downloadUrl = downloadUrl;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath( String path ) {
		this.path = path;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId( String id ) {
		this.id = id;
	}
	
	public String getRepository() {
		return repository;
	}
	
	public void setRepository( String repository ) {
		this.repository = repository;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat( String format ) {
		this.format = format;
	}
	
	public Checksum getChecksum() {
		return checksum;
	}
	
	public void setChecksum( Checksum checksum ) {
		this.checksum = checksum;
	}
}
