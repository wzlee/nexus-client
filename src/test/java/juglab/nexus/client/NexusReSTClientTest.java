package juglab.nexus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import juglab.nexus.client.domain.Asset;
import juglab.nexus.client.domain.Component;
import juglab.nexus.client.domain.Query;
import juglab.nexus.client.domain.Query.Sort;
import juglab.nexus.client.domain.Repository;

/**
 */
public class NexusReSTClientTest {

	private final static String BASE_URL = "https://dais-maven.mpi-cbg.de";
	private final static String ASSET_REPO = "imagej-public";
	private final static String REPO = "maven-public";
	private final static String ASSET_ID =
			"aW1hZ2VqLXB1YmxpYzoxMzRmNmM1NjJmOTU4NDJmOTVjYmI1Y2IyZDc1MTEzZg";
	private final static String COMPONENT_ID = "bWF2ZW4tcHVibGljOjU4YWQyNTA2ZGE3Y2Y0YjU2NDk5OWE3NjQ0ZGM0ZGJj";
	private final static String TMPDIR = System.getProperty( "java.io.tmpdir" );
	private final static String CURDIR = System.getProperty( "user.dir" );

	private static NexusReSTClient client;
	private static NexusReSTClient authClient;

	@BeforeClass
	public static void init() {
		client = new NexusReSTClient( BASE_URL );
		authClient = new NexusReSTClient( BASE_URL, "dadada", "ddddd" );
	}

	@Test
	public void testSearchAssetsNonExistentName() throws Exception {
		Query q = new Query();
		q.setName( "xxxxx" );
		List< Asset > assets = client.searchAssets( q );
		assertTrue( assets.size() == 0 );
	}

	@Test
	public void testSearchAssetsByName() throws Exception {
		Query q = new Query();
		q.setName( "commons-io" );
		List< Asset > assets = client.searchAssets( q );
		System.out.println( "Found " + assets.size() + " \"asset-io\" assets" );
		assertTrue( assets.size() > 0 );
	}

	@Test
	public void testSearchAssetsAndDownloadByNameAndVersionReturnsMoreThanOneResult() {
		try {
			Query q = new Query();
			q.setName( "commons-io" );
			q.setVersion( "2.6" );
			client.searchAssetsAndDownload( q, "commons-io.jar", CURDIR );
		} catch ( NexusReSTClientException e ) {
			assertEquals( 400, e.getHttpErrorCode() );
		}
	}

	@Test
	public void testSearchAssetsAndDownload() throws Exception {
		Query q = new Query();
		q.setSortBy( Sort.NAME );
		q.setName( "commons-io" );
		q.setVersion( "2.6" );
		File downloaded = client.searchAssetsAndDownload( q, "commons-io.jar", CURDIR );
		assertTrue( downloaded.exists() );
		assertTrue( downloaded.length() > 0 );
	}

	@Test
	public void testListRepositories() throws Exception {
		List< Repository > repos = client.listRepositories();
		assertTrue( repos.size() > 0 );
		boolean testraw = false;
		boolean testmaven = false;
		for ( Repository repo : repos ) {
			if ( repo.getName().equals( "test-raw" ) ) testraw = true;
			if ( repo.getName().equals( "test-maven" ) ) testmaven = true;
		}
		assertTrue( testraw );
		assertTrue( testmaven );
	};

	@Test
	public void testListAssets() throws Exception {
		List< Asset > assets = client.listAssets( ASSET_REPO );
		assertTrue( assets.size() > 0 );
	};

	@Test
	public void testListAssetsForUnknownRepo() {
		try {
			client.listAssets( "xkxkxk" );
		} catch ( NexusReSTClientException e ) {
			assertEquals( 404, e.getHttpErrorCode() );
		}
	};

	@Test
	public void testGetAsset() throws Exception {
		File downloadedFile = client.getAsset( ASSET_ID, TMPDIR );
		assertTrue( downloadedFile.exists() );
		assertTrue( downloadedFile.length() > 0 );
	};

	@Test
	public void testListComponents() throws Exception {
		List< Component > components = client.listComponents( REPO );
		assertTrue( components.size() > 0 );
		Component testComp = components.get( 0 );
		assertTrue( testComp.getId().equals( COMPONENT_ID ) );
		assertTrue( testComp.getRepository().equals( "maven-public" ) );
		assertTrue( testComp.getAssets().size() > 0 );

	}

	@Test
	public void testAnonCannotDeleteAsset() {
		try {
			client.deleteAsset( ASSET_ID );
		} catch ( NexusReSTClientException e ) {
			assertEquals( 403, e.getHttpErrorCode() );
		}
	};

	@Test
	public void testDeleteAssetWrongAuth() {
		try {
			authClient.deleteAsset( ASSET_ID );
		} catch ( NexusReSTClientException e ) {
			assertEquals( 401, e.getHttpErrorCode() );
		}
	};

}
