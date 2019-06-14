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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import juglab.nexus.client.domain.Asset;
import juglab.nexus.client.domain.Component;
import juglab.nexus.client.domain.Repository;

/**
 */
public class NexusReSTClient {

	private final static String TMPDIR = System.getProperty( "java.io.tmpdir" );

	private String baseURL;
	private String continuationToken = null;

	public NexusReSTClient( String baseURL ) {

		this.baseURL = baseURL;
	}

	public List< Repository > listRepositories() throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
			NexusReSTClientProxy restClient = webTarget.proxy( NexusReSTClientProxy.class );
			String response = restClient.listRepositories();
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure( DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true );
			return Arrays.asList( objectMapper.readValue( response, Repository[].class ) );
		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
		finally {
			client.close();
		}
	};

	@SuppressWarnings( "unchecked" )
	public List< Asset >
			listAssets( String repository) throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
			NexusReSTClientProxy restClient = webTarget.proxy( NexusReSTClientProxy.class );
			String response = restClient.listAssets( repository);
			ObjectMapper objectMapper = new ObjectMapper();
			Map< String, Object > map = objectMapper.readValue(
					response,
					Map.class );
			continuationToken = ( String ) map.get( "continuationToken" );
			return objectMapper.convertValue( map.get( "items" ), new TypeReference< List< Asset > >() {} );
		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
		finally {
			client.close();
		}
	};

	public File getAsset(
			String assetId,
			String downloadDir ) throws NexusReSTClientException {
		return getAsset( null, null, assetId, downloadDir );
	}

	public File getAsset(
			String usrName,
			String pwd,
			String assetId,
			String downloadDir ) throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			WebTarget target = client.target( baseURL );
			ResteasyWebTarget webTarget = ( ResteasyWebTarget ) target;
			NexusReSTClientProxy restClient = webTarget.proxy( NexusReSTClientProxy.class );

			String response = restClient.getAsset( assetId );
			ObjectMapper mapper = new ObjectMapper();
			Asset asset =  mapper.readValue(response, Asset.class);
			return saveAsset( asset.getDownloadUrl(), downloadDir );

		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
		finally {
			client.close();
		}
	};

	public void deleteAsset(
			String usrName,
			String pwd,
			String assetId ) throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			WebTarget target = client.target( baseURL );
			ResteasyWebTarget rtarget = ( ResteasyWebTarget ) target;
			NexusReSTClientProxy restClient = rtarget.proxy( NexusReSTClientProxy.class );
			restClient.deleteAsset( assetId );
		} catch ( RuntimeException e ) {
			throw new NexusReSTClientException( e );
		}
	};
	
	@SuppressWarnings( "unchecked" )
	public List< Component >
			listComponents( String repository) throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
			NexusReSTClientProxy restClient = webTarget.proxy( NexusReSTClientProxy.class );
			String response = restClient.listComponents( repository);
			ObjectMapper objectMapper = new ObjectMapper();
			Map< String, Object > map = objectMapper.readValue( response, Map.class );
			continuationToken = ( String ) map.get( "continuationToken" );
			return objectMapper.convertValue( map.get( "items" ), new TypeReference< List< Component > >() {} );
		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
		finally {
			client.close();
		}
	};
	
	public File getComponent(
			String id,
			String downloadDir ) throws NexusReSTClientException {
		return getAsset( null, null, id, downloadDir );
	}

	public File getComponent(
			String usrName,
			String pwd,
			String id,
			String downloadDir ) throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			WebTarget target = client.target( baseURL );
			ResteasyWebTarget webTarget = ( ResteasyWebTarget ) target;
			NexusReSTClientProxy restClient = webTarget.proxy( NexusReSTClientProxy.class );

			String response = restClient.getComponent( id );
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree( response );
			String url = jsonNode.get( "downloadUrl" ).asText();
			return saveComponent( url, downloadDir );

		} catch ( RuntimeException | IOException e ) {
			throw new NexusReSTClientException( e );
		}
		finally {
			client.close();
		}
	};



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
	
	private File saveComponent( String url, String downloadDir ) throws IOException {

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


	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL( String baseURL ) {
		this.baseURL = baseURL;
	}

	public String getContinuationToken() {
		return continuationToken;
	}

	public boolean hasContinuationToken() {

		return ( continuationToken != null );

	}
}
