package juglab.nexus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import juglab.nexus.client.domain.Repository;

/**
 */
public class NexusReSTClientTest {

	private final static String BASE_URL = "https://dais-maven.mpi-cbg.de";
	private final static String ASSET_REPO = "imagej-public";
	private final static String ID =
			"aW1hZ2VqLXB1YmxpYzoxMzRmNmM1NjJmOTU4NDJmOTVjYmI1Y2IyZDc1MTEzZg";
	private final static String TMPDIR = System.getProperty( "java.io.tmpdir" );

	@Test
	public void testListRepositories() throws Exception {
		List<Repository> repos = NexusReSTClient.listRepositories( BASE_URL );
		assertTrue( repos.size() > 0 );
		boolean testraw = false;
		boolean testmaven = false;
		for (Repository repo: repos)
		{
			if (repo.getName().equals( "test-raw" )) testraw = true;
			if (repo.getName().equals( "test-maven" )) testmaven = true;
		}
		assertTrue(testraw);
		assertTrue(testmaven);
	};

	@Test
	public void testListAssets() throws Exception {
		Map< String, Object > map = NexusReSTClient.listAssets( BASE_URL, ASSET_REPO );
		assertTrue( map.keySet().contains( "continuationToken" ) );
	};

	@Test
	public void testListAssetsForUnknownRepo() {
		try {
			NexusReSTClient.listAssets( BASE_URL, "xkxkxk" );
		} catch ( NexusReSTClientException e ) {
			assertEquals( 404, e.getHttpErrorCode() );
		}
	};

	@Test
	public void testGetAsset() throws Exception {
		File downloadedFile = NexusReSTClient.getAsset( BASE_URL, ID, TMPDIR );
		assertTrue( downloadedFile.exists() );
		assertTrue( downloadedFile.length() > 0 );
	};

	@Test
	public void testAnonCannotDeleteAsset() {
		try {
			NexusReSTClient.deleteAsset( BASE_URL, null, null, ID );
		} catch ( NexusReSTClientException e ) {
			assertEquals( 403, e.getHttpErrorCode() );
		}
	};

}
