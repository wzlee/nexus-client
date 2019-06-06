package juglab.nexus.client;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Test;

/**
 */
public class NexusReSTClientTest {

	private final static String BASE_URL = "https://dais-maven.mpi-cbg.de";
	private final static String ASSET_REPO = "imagej-public";
	private final static String ID =
			"aW1hZ2VqLXB1YmxpYzoxMzRmNmM1NjJmOTU4NDJmOTVjYmI1Y2IyZDc1MTEzZg";
	private final static String TMPDIR = System.getProperty( "java.io.tmpdir" );

	@Test
	public void testAssetList() throws Exception {
		Map< String, Object > map = NexusReSTClientImpl.getAssetList( BASE_URL, ASSET_REPO );
		assertTrue( map.keySet().contains( "continuationToken" ) );
	};

	@Test
	public void testGetAsset() throws Exception {
		File downloadedFile = NexusReSTClientImpl.getAsset( BASE_URL, ID, TMPDIR );
		assertTrue( downloadedFile.exists() );
		assertTrue( downloadedFile.length() > 0 );
	};

	@Test( expected = NexusReSTClientException.class )
	public void testAnonCannotDeleteAsset() throws NexusReSTClientException {
		NexusReSTClientImpl.deleteAsset( BASE_URL, null, null, ID );
	};

}
