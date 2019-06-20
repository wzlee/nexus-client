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
public class NexusReSTClient {

	private final static String TMPDIR = System.getProperty( "java.io.tmpdir" );

	private Client client;
	private NexusReSTClientProxy restClient;

	/**
	 * 
	 * @param baseURL
	 *            the url of the Nexus repository
	 */
	public NexusReSTClient( String baseURL ) {
		client = ClientBuilder.newClient();
		ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
		restClient = webTarget.proxy( NexusReSTClientProxy.class );
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
	public NexusReSTClient( String baseURL, String username, String password ) {
		client = ClientBuilder.newClient();
		ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
		if ( username != null && password != null )
			webTarget.register( new BasicAuthentication( username, password ) );
		restClient = webTarget.proxy( NexusReSTClientProxy.class );
	}

	protected void finalize() throws Throwable {
		client.close();
		super.finalize();
	}

	public List< Repository > listRepositories() throws NexusReSTClientException {
		try {
			String response = restClient.listRepositories();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure( DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true );
			return Arrays.asList( mapper.readValue( response, Repository[].class ) );
		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
	};

	public List< Asset > searchAssets( Query q ) throws NexusReSTClientException {
		return searcher( q, this::searchAssets, new TypeReference< List< Asset > >() {} );
	}

	public List< Asset > listAssets( String repository ) throws NexusReSTClientException {
		return ( List< Asset > ) lister( repository, restClient::listAssets, new TypeReference< List< Asset > >() {} );
	};

	public File getAsset(
			String assetId,
			String downloadDir ) throws NexusReSTClientException {
		try {
			String response = restClient.getAsset( assetId );
			ObjectMapper mapper = new ObjectMapper();
			Asset asset = mapper.readValue( response, Asset.class );
			return saveAsset( asset.getDownloadUrl(), downloadDir );
		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
	};

	public void deleteAsset( String assetId ) throws NexusReSTClientException {
		try {
			restClient.deleteAsset( assetId );
		} catch ( RuntimeException e ) {
			throw new NexusReSTClientException( e );
		}
	};
	
	public List< Component > searchComponents( Query q ) throws NexusReSTClientException {
		return searcher( q, this::searchComponents, new TypeReference< List< Component > >() {} );
	}

	public List< Component >
			listComponents( String repository ) throws NexusReSTClientException {
		return lister( repository, restClient::listComponents, new TypeReference< List< Component > >() {} );
	};

	public List< File > getComponent( String id, String downloadDir ) throws NexusReSTClientException {
		try {
			String response = restClient.getComponent( id );
			ObjectMapper mapper = new ObjectMapper();
			Component component = mapper.readValue( response, Component.class );
			return saveComponent( component, downloadDir );

		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
	};

	@SuppressWarnings( "unchecked" )
	private < T > List< T > lister( String repository, BiFunction< String, String, String > listFunction, TypeReference< List< T > > typeRef ) throws NexusReSTClientException {
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
			throw new NexusReSTClientException( e );
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
	private < T > List< T > searcher( Query q, BiFunction< Query, String, String > searchFunction, TypeReference< List< T > > typeRef ) throws NexusReSTClientException {
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
			throw new NexusReSTClientException( e );
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
		String filePath =
				downloadDir + File.pathSeparator + fileName;
		ReadableByteChannel readableByteChannel =
				Channels.newChannel( new URL( url ).openStream() );
		FileOutputStream fileOutputStream = new FileOutputStream( filePath );
		fileOutputStream.getChannel().transferFrom( readableByteChannel, 0, Long.MAX_VALUE );
		fileOutputStream.close();
		Files.copy(
				Paths.get( tmpPath ),
				Paths.get( filePath ),
				StandardCopyOption.REPLACE_EXISTING );
		return new File( filePath );

	}

	private List< File > saveComponent( Component component, String downloadDir ) throws IOException {

		List< Asset > assets = component.getAssets();
		List< File > files = new ArrayList< File >( assets.size() );
		for ( Asset asset : assets ) {
			String url = asset.getDownloadUrl();
			String fileName = url.substring( url.lastIndexOf( '/' ) + 2 );
			String tmpPath = TMPDIR + File.pathSeparator + fileName;
			String filePath =
					downloadDir + File.pathSeparator + fileName;
			ReadableByteChannel readableByteChannel =
					Channels.newChannel( new URL( url ).openStream() );
			FileOutputStream fileOutputStream = new FileOutputStream( filePath );
			fileOutputStream.getChannel().transferFrom( readableByteChannel, 0, Long.MAX_VALUE );
			fileOutputStream.close();
			Files.copy(
					Paths.get( tmpPath ),
					Paths.get( filePath ),
					StandardCopyOption.REPLACE_EXISTING );
			files.add( new File( filePath ) );
		}
		return files;

	}
}
