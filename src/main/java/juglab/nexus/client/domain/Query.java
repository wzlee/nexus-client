package juglab.nexus.client.domain;

import javax.validation.constraints.Null;

/**
 * Object representation of a list of search parameters
 * 
 * @author turek
 *
 */

public class Query {

	public enum Sort {
		GROUP, NAME, VERSION, REPOSITORY
	};

	public enum Order {
		ASC, DESC
	};

	@Null
	private String keyword;
	@Null
	private String repository;
	@Null
	private String format;
	@Null
	private String group;
	@Null
	private String name;
	@Null
	private String version;
	@Null
	private String mavenGroupId;
	@Null
	private String mavenArtifactId;
	@Null
	private Sort sortBy;
	@Null
	private Order orderBy;

	/**
	 * "Empty" query object
	 */
	public Query() {}

	/**
	 * Query parameters for both asset and component searches
	 * 
	 * @param keyword
	 * @param repository
	 * @param format
	 * @param group
	 * @param name
	 * @param version
	 * @param mavenGroupId
	 * @param mavenArtifactId
	 * @param sortBy
	 * @param orderBy
	 */
	public Query( String keyword, String repository, String format, String group, String name, String version, String mavenGroupId, String mavenArtifactId, Sort sortBy, Order orderBy ) {
		this.keyword = keyword;
		this.repository = repository;
		this.format = format;
		this.group = group;
		this.name = name;
		this.version = version;
		this.mavenGroupId = mavenGroupId;
		this.mavenArtifactId = mavenArtifactId;
		this.sortBy = sortBy;
		this.orderBy = orderBy;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getRepository() {
		return repository;
	}

	public String getFormat() {
		return format;
	}

	public String getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getMavenGroupId() {
		return mavenGroupId;
	}

	public String getMavenArtifactId() {
		return mavenArtifactId;
	}

	public String getSortBy() {
		
		return (sortBy == null) ? null : sortBy.toString().toLowerCase();
	}

	public String getOrderBy() {
		return (orderBy == null) ? null : orderBy.toString().toLowerCase();
	}

	
	public void setKeyword( String keyword ) {
		this.keyword = keyword;
	}

	
	public void setRepository( String repository ) {
		this.repository = repository;
	}

	
	public void setFormat( String format ) {
		this.format = format;
	}

	
	public void setGroup( String group ) {
		this.group = group;
	}

	
	public void setName( String name ) {
		this.name = name;
	}

	
	public void setVersion( String version ) {
		this.version = version;
	}

	
	public void setMavenGroupId( String mavenGroupId ) {
		this.mavenGroupId = mavenGroupId;
	}

	
	public void setMavenArtifactId( String mavenArtifactId ) {
		this.mavenArtifactId = mavenArtifactId;
	}

	
	public void setSortBy( Sort sortBy ) {
		this.sortBy = sortBy;
	}

	
	public void setOrderBy( Order orderBy ) {
		this.orderBy = orderBy;
	}

}
