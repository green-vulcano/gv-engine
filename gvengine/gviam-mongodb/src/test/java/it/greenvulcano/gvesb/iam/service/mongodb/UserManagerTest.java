package it.greenvulcano.gvesb.iam.service.mongodb;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import it.greenvulcano.gvesb.iam.domain.User;

import static org.junit.Assert.fail;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManagerTest {
	
	private final static Logger LOG = LoggerFactory.getLogger(UserManagerTest.class);
	
	private static MongodExecutable mongodExecutable;
	private static MongodProcess mongod;
	
	private static MongoClient mongoClient;
			
	private static GVUsersManager userManager = new GVUsersManager();
	
	@BeforeClass
	public static void init() throws Exception {
		MongodStarter starter = MongodStarter.getDefaultInstance();

		String bindIp = "localhost";
		int port = 27017;
		IMongodConfig mongodConfig = new MongodConfigBuilder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(bindIp, port, Network.localhostIsIPv6()))
			.build();

		mongodExecutable = starter.prepare(mongodConfig);		
		mongod = mongodExecutable.start();
			
		mongoClient = new MongoClient(bindIp, port);		
		
		userManager.setMongoClient(mongoClient);
		userManager.setDatabaseName("gviam");
				
	}
		
	@Test
	public void testCreateUser()  { 
	    
	    try {
	    
	        Assert.assertTrue(userManager.getUsers().isEmpty());   
	        User testuser = userManager.createUser("testuser", "testuser");
	    
	        Assert.assertNotNull(testuser.getId());
	        Assert.assertEquals("testuser", testuser.getUsername() );
	        
	        System.out.println("******** CREATED User: "+ testuser.toString());
	        
	        Assert.assertEquals(1, userManager.getUsers().size());
	        
	    } catch (Exception e) {
	        
	        e.printStackTrace();	        
	        fail(e.getMessage());
	    }
	    
	}

}
