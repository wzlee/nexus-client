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
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.HttpResponseCodes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 */
public class NexusReSTClientImpl {

	private final static String TMPDIR = System.getProperty( "java.io.tmpdir" );

	public static Map< String, Object >
			getAssetList( String baseURL, String repositoryName ) throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			ResteasyWebTarget webTarget = ( ResteasyWebTarget ) client.target( baseURL );
			NexusReSTClient restClient = webTarget.proxy( NexusReSTClient.class );
			String response = restClient.listAssets( repositoryName );
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(
					response,
					new TypeReference< Map< String, Object > >() {} );
		} catch ( IOException e ) {
			throw new NexusReSTClientException( e );
		}
		finally {
			client.close();
		}
	};

	public static File getAsset(
			String baseURL,
			String assetId,
			String downloadDir ) throws NexusReSTClientException {
		return getAsset( baseURL, null, null, assetId, downloadDir );
	}

	public static File getAsset(
			String baseURL,
			String usrName,
			String pwd,
			String assetId,
			String downloadDir ) throws NexusReSTClientException {
		Client client = null;
		try {
			client = ClientBuilder.newClient();
			WebTarget target = client.target( baseURL );
			ResteasyWebTarget webTarget = ( ResteasyWebTarget ) target;
			NexusReSTClient restClient = webTarget.proxy( NexusReSTClient.class );

			String response = restClient.getAsset( assetId );
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree( response );
			String url = jsonNode.get( "downloadUrl" ).asText();
			return saveAsset( url, downloadDir );

		} catch ( IOException e ) {
			throw new NexusReSTClientException( e );
		}
		finally {
			client.close();
		}
	};

	public static void deleteAsset(
			String baseURL,
			String usrName,
			String pwd,
			String assetId ) throws NexusReSTClientException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target( baseURL );
		ResteasyWebTarget rtarget = ( ResteasyWebTarget ) target;
		NexusReSTClient restClient = rtarget.proxy( NexusReSTClient.class );

		Response response = restClient.deleteAsset( assetId );
		if ( response.getStatus() != HttpResponseCodes.SC_OK )
			throw new NexusReSTClientException( response.getStatus() );
	};

	private static File saveAsset( String url, String downloadDir ) throws IOException {

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

}
