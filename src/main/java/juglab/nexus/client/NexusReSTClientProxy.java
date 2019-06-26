package juglab.nexus.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;


/**
 * <p>
 * Client for the Nexus Sonatype ReST service. 
 * Uses the <a href="https://docs.jboss.org/resteasy/docs/4.0.0.Final/userguide/html/RESTEasy_Client_Framework.html">RESTEasy Proxy Framework</a>
 * </p>
 */
public interface NexusReSTClientProxy {

	/**
	 * List all repositories
	 * 
	 * @param repository name
	 * @return a (JSON) list of assets for given repository
	 */
	@GET
	@Path("/service/rest/v1/repositories")
	@Produces(MediaType.APPLICATION_JSON)
	public String listRepositories();
	
	/**
	 * Search for assets
	 */
	@GET
	@Path("/service/rest/v1/search/assets")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchAssets(@QueryParam("sort") String sort, @QueryParam("direction") String order, @QueryParam("q") String keyword, @QueryParam("repository") String repository, @QueryParam("format") String format, @QueryParam("group") String group, @QueryParam("name") String name, @QueryParam("version") String version, @QueryParam("maven.groupId") String mavenGroupId, @QueryParam("maven.artifactId") String mavenArtifactId, @QueryParam("continuationToken") String continuationToken);
	
	/**
	 * Search for assets and download
	 */
	@GET
	@Path("/service/rest/v1/search/assets/download")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchAssetsAndDownload(@QueryParam("sort") String sort, @QueryParam("direction") String order, @QueryParam("q") String keyword, @QueryParam("repository") String repository, @QueryParam("format") String format, @QueryParam("group") String group, @QueryParam("name") String name, @QueryParam("version") String version, @QueryParam("maven.groupId") String mavenGroupId, @QueryParam("maven.artifactId") String mavenArtifactId);
	
	/**
	 * List assets - assets are individual files
	 * 
	 * @param repository name
	 * @return a (JSON) list of assets for given repository
	 */
	@GET
	@Path("/service/rest/v1/assets")
	@Produces(MediaType.APPLICATION_JSON)
	public String listAssets(@QueryParam("repository") String repository, @QueryParam("continuationToken") String continuationToken);

	
	/**
	 * Get an asset
	 * 
	 * @param id of asset to be retrieved
	 * @return a JSON list of the asset information, including the direct download URL
	 */
	@GET
	@Path("/service/rest/v1/assets/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAsset(@PathParam("id") String id);

	/**
	 * Delete an asset
	 * 
	 * @param id of asset to be deleted
	 * 
	 */
	@DELETE
	@Path("/service/rest/v1/assets/{id}")
	@Consumes(MediaType.TEXT_PLAIN)
	public void deleteAsset(@PathParam("id") String id);
	
	/**
	 * Search for components
	 */
	@GET
	@Path("/service/rest/v1/search")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchComponents(@QueryParam("sort") String sort, @QueryParam("direction") String order, @QueryParam("q") String keyword, @QueryParam("repository") String repository, @QueryParam("format") String format, @QueryParam("group") String group, @QueryParam("name") String name, @QueryParam("version") String version, @QueryParam("maven.groupId") String mavenGroupId, @QueryParam("maven.artifactId") String mavenArtifactId, @QueryParam("continuationToken") String continuationToken);
	
	
	/**
	 * List components - components are collections of assets 
	 * see https://help.sonatype.com/repomanager3/repository-manager-concepts/components%2C-repositories%2C-and-repository-formats
	 * 
	 * @param repository name
	 * @return a list of components for given repository
	 */
	@GET
	@Path("/service/rest/v1/components")
	@Produces(MediaType.APPLICATION_JSON)
	public String listComponents(@QueryParam("repository") String repository, @QueryParam("continuationToken") String continuationToken);

	/**
	 * Get a component
	 * 
	 * @param id of component to be retrieved
	 * @return a JSON list of the asset information, including the direct download URL
	 */
	@GET
	@Path("/service/rest/v1/components/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getComponent(@PathParam("id") String id);
	
	/**
	 * Delete a component
	 * 
	 * @param id of component to be deleted
	 */
	@DELETE
	@Path("/service/rest/v1/components/{id}")
	@Consumes(MediaType.TEXT_PLAIN)
	public void deleteComponent(@PathParam("id") String id);
	
	/**
	 * Upload a component
	 * 
	 * @param id of component to be deleted
	 */
	@POST
	@Path("/service/rest/v1/components")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void uploadComponent();

	
}