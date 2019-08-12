package juglab.nexus.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import juglab.nexus.client.domain.Asset;
import juglab.nexus.client.domain.Component;
import juglab.nexus.client.domain.Query;
import juglab.nexus.client.domain.Repository;

/**
 */
public class NexusRestClient {

	private final static String TMPDIR = System.getProperty( "java.io.tmpdir" );

	private Client client;
	private NexusRestClientProxy restClient;

	/**
	 * 
	 * @param baseURL
	 *            the url of the Nexus repository
	 */
	public NexusRestClient( String baseURL ) {
		client = ClientBuilder.newClient();
		ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
		restClient = webTarget.proxy( NexusRestClientProxy.class );
	}

	/**
	 * Use this constructor for actions that require authentication (deleting
	 * and accessing private repos):
	 * 
	 * @param baseURL
	 *            the url of the Nexus repository
	 * @param username
	 * @param password
	 */
	public NexusRestClient( String baseURL, String username, String password ) {
		client = ClientBuilder.newClient();
		ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
		if ( username != null && password != null )
			webTarget.register( new BasicAuthentication( username, password ) );
		restClient = webTarget.proxy( NexusRestClientProxy.class );
	}

	protected void finalize() throws Throwable {
		client.close();
		super.finalize();
	}

	/**
	 * List all the repositories (local and mirrored) hosted on server
	 */
	public List< Repository > listRepositories() throws NexusRestClientException {
		try {
			String response = restClient.listRepositories();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure( DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true );
			return Arrays.asList( mapper.readValue( response, Repository[].class ) );
		} catch ( RuntimeException | IOException e ) {
			throw new NexusRestClientException( e );
		}
	};

	/**
	 * Search for one or more assets using one or more search parameters
	 * 
	 * @param q
	 *            - Query object in which a number of supported search
	 *            parameters can be set
	 * @see juglab.nexus.client.domain.Query
	 */
	public List< Asset > searchAssets( Query q ) throws NexusRestClientException {
		return searcher( q, this::searchAssets, new TypeReference< List< Asset > >() {} );
	}

	/**
	 * Search for an assets and download it using one or more search parameters.
	 * 
	 * Check for the following error code returns:
	 * 400 : Search returned multiple assets. Refine search criteria to
	 * find a single asset or use the sort query parameter to retrieve the first
	 * result.
	 * 404 : Asset search returned no results
	 * 
	 * @param q
	 *            - Query object in which a number of supported search
	 *            parameters can be set
	 * @param fileName
	 *            - what to name the download
	 * @param downloadDir
	 *            - where to download the asset to
	 * @return File handle to down loaded asset
	 * 
	 * @throws NexusRestClientException
	 * 
	 *             Check for the following error code returns:
	 *             400 : Search returned multiple assets. Refine search criteria
	 *             to
	 *             find a single asset or use the sort query parameter to
	 *             retrieve the first
	 *             result.
	 *             404 : Asset search returned no results
	 * 
	 * @see juglab.nexus.client.domain.Query
	 */
	public File searchAssetsAndDownload( Query q, String fileName, String downloadDir ) throws NexusRestClientException {
		try {
			restClient.searchAssetsAndDownload(
					q.getSortBy(),
					q.getOrderBy(),
					q.getKeyword(),
					q.getRepository(),
					q.getFormat(),
					q.getGroup(),
					q.getName(),
					q.getVersion(),
					q.getMavenGroupId(),
					q.getMavenArtifactId() );

		} catch ( RuntimeException e ) {
			if ( e instanceof RedirectionException ) {
				URL url;
				try {
					url = ( ( RedirectionException ) e ).getLocation().toURL();
					String tmpPath = TMPDIR + File.pathSeparator + fileName;
					String finalPath =
							downloadDir + File.pathSeparator + fileName;
					return saveToFile(url, tmpPath, finalPath);
				} catch ( IOException e1 ) {
					throw new NexusRestClientException( e1 );
				}
			}
			throw new NexusRestClientException( e );
		}
		return null;

	}

	public List< Asset > listAssets( String repository ) throws NexusRestClientException {
		return ( List< Asset > ) lister( repository, restClient::listAssets, new TypeReference< List< Asset > >() {} );
	};

	public File getAsset(
			String assetId,
			String downloadDir ) throws NexusRestClientException {
		try {
			String response = restClient.getAsset( assetId );
			ObjectMapper mapper = new ObjectMapper();
			Asset asset = mapper.readValue( response, Asset.class );
			return saveAsset( asset.getDownloadUrl(), downloadDir );
		} catch ( RuntimeException | IOException e ) {
			throw new NexusRestClientException( e );
		}
	};

	public void deleteAsset( String assetId ) throws NexusRestClientException {
		try {
			restClient.deleteAsset( assetId );
		} catch ( RuntimeException e ) {
			throw new NexusRestClientException( e );
		}
	};

	public List< Component > searchComponents( Query q ) throws NexusRestClientException {
		return searcher( q, this::searchComponents, new TypeReference< List< Component > >() {} );
	}

	public List< Component >
			listComponents( String repository ) throws NexusRestClientException {
		return lister( repository, restClient::listComponents, new TypeReference< List< Component > >() {} );
	};

	public List< File > getComponent( String id, String downloadDir ) throws NexusRestClientException {
		try {
			String response = restClient.getComponent( id );
			ObjectMapper mapper = new ObjectMapper();
			Component component = mapper.readValue( response, Component.class );
			return saveComponent( component, downloadDir );

		} catch ( RuntimeException | IOException e ) {
			throw new NexusRestClientException( e );
		}
	};

	@SuppressWarnings( "unchecked" )
	private < T > List< T > lister( String repository, BiFunction< String, String, String > listFunction, TypeReference< List< T > > typeRef ) throws NexusRestClientException {
		try {
			boolean begin = true;
			String continuationToken = "";
			List< T > result = new ArrayList< T >();
			String response;
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure( DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true );
			while ( continuationToken != null ) {
				if ( begin ) {
					response = ( String ) listFunction.apply( repository, null );
					begin = false;
				} else
					response = ( String ) listFunction.apply( repository, continuationToken );
				Map< String, Object > map = ( ( ObjectMapper ) mapper ).readValue( response, Map.class );
				result.addAll( ( ( ObjectMapper ) mapper ).convertValue( map.get( "items" ), typeRef ) );
				continuationToken = ( String ) map.get( "continuationToken" );
			}
			return result;
		} catch ( RuntimeException | IOException e ) {
			throw new NexusRestClientException( e );
		}
	}

	private String searchAssets( Query q, String continuationToken ) {
		return restClient.searchAssets(
				q.getSortBy(),
				q.getOrderBy(),
				q.getKeyword(),
				q.getRepository(),
				q.getFormat(),
				q.getGroup(),
				q.getName(),
				q.getVersion(),
				q.getMavenGroupId(),
				q.getMavenArtifactId(),
				continuationToken );
	}

	@SuppressWarnings( "unchecked" )
	private < T > List< T > searcher( Query q, BiFunction< Query, String, String > searchFunction, TypeReference< List< T > > typeRef ) throws NexusRestClientException {
		try {
			boolean begin = true;
			String continuationToken = "";
			List< T > result = new ArrayList< T >();
			String response;
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure( DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true );
			while ( continuationToken != null ) {
				if ( begin ) {
					response = ( String ) searchFunction.apply( q, null );
					begin = false;
				} else
					response = ( String ) searchFunction.apply( q, continuationToken );
				Map< String, Object > map = ( ( ObjectMapper ) mapper ).readValue( response, Map.class );
				result.addAll( ( ( ObjectMapper ) mapper ).convertValue( map.get( "items" ), typeRef ) );
				continuationToken = ( String ) map.get( "continuationToken" );
			}
			return result;
		} catch ( RuntimeException | IOException e ) {
			throw new NexusRestClientException( e );
		}
	}

	private String searchComponents( Query q, String continuationToken ) {
		return restClient.searchComponents(
				q.getSortBy(),
				q.getOrderBy(),
				q.getKeyword(),
				q.getRepository(),
				q.getFormat(),
				q.getGroup(),
				q.getName(),
				q.getVersion(),
				q.getMavenGroupId(),
				q.getMavenArtifactId(),
				continuationToken );
	}

	private File saveAsset( String url, String downloadDir ) throws IOException {

		String fileName = url.substring( url.lastIndexOf( '/' ) + 2 );
		String tmpPath = TMPDIR + File.pathSeparator + fileName;
		String finalPath =
				downloadDir + File.pathSeparator + fileName;
		return saveToFile(new URL( url ), tmpPath, finalPath);
	}

	private List< File > saveComponent( Component component, String downloadDir ) throws IOException {

		List< Asset > assets = component.getAssets();
		List< File > files = new ArrayList< File >( assets.size() );
		for ( Asset asset : assets ) {
			String url = asset.getDownloadUrl();
			String fileName = url.substring( url.lastIndexOf( '/' ) + 2 );
			String tmpPath = TMPDIR + File.pathSeparator + fileName;
			String finalPath =
					downloadDir + File.pathSeparator + fileName;
			files.add( saveToFile(new URL( url ), tmpPath, finalPath) );
		}
		return files;

	}
	
	private File saveToFile(URL url, String tmpPath, String finalPath) throws IOException {

		ReadableByteChannel readableByteChannel =
				Channels.newChannel( url.openStream() );
		FileOutputStream fileOutputStream = new FileOutputStream( tmpPath );
		fileOutputStream.getChannel().transferFrom( readableByteChannel, 0, Long.MAX_VALUE );
		fileOutputStream.close();
		Files.copy(
				Paths.get( tmpPath ),
				Paths.get( finalPath ),
				StandardCopyOption.REPLACE_EXISTING );
		return new File(finalPath);
	}
}
