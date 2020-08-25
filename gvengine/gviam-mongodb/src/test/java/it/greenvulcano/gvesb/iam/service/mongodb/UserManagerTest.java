package it.greenvulcano.gvesb.iam.service.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.mongodb.UserBson;
import it.greenvulcano.gvesb.iam.repository.mongodb.Repository;

import static org.junit.Assert.fail;

import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;
import org.junit.*;

public class UserManagerTest {
	
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
			
		mongoClient = MongoClients.create();
		
		Repository mongodbRepository = new Repository();
		mongodbRepository.setGviamDatabaseName("gviam");
		mongodbRepository.setGvstoreDatabaseName("gviam");
		mongodbRepository.setMongodbURI("mongodb://localhost:27017");
		mongodbRepository.init();
		
		userManager.setMongodbRepository(mongodbRepository);
				
	}
	
	@AfterClass
	public static void destroy() {
	    mongod.stop();
	    mongodExecutable.stop();
	}
	
	@Before
	public void cleanup() {
	    mongoClient.getDatabase("gviam").getCollection(UserBson.COLLECTION_NAME).deleteMany(new Document());
	}
		
	@Test
	public void testCreateUser()  { 
	    
	    try {
	    
	        Assert.assertTrue(userManager.getUsers().isEmpty());   
	        User testuser = userManager.createUser("testuser", "testuser");
	    
	        Assert.assertNotNull(testuser.getId());
	        Assert.assertEquals("testuser", testuser.getUsername() );
	        	        
	        Set<User> users = userManager.getUsers();	        
	        Assert.assertEquals(1, users.size());	        
	        Assert.assertTrue(testuser.equals(users.iterator().next()));
	 
	        userManager.deleteUser("testuser");
	        Assert.assertTrue(userManager.getUsers().isEmpty());
	        
	    } catch (Exception e) {
	        
	        e.printStackTrace();	        
	        fail(e.getMessage());
	    }
	    
	}
	
	@Test
        public void testRoles()  { 
            
            try {
            
                Assert.assertTrue(userManager.getUsers().isEmpty());   
                
                userManager.createUser("testuser", "testuser");            
                userManager.addRole("testuser", "TESTER_A");
                
                User testuser = (UserBson) userManager.getUser("testuser");
                
                Assert.assertEquals(1, ((UserBson)testuser).getVersion());
                Assert.assertEquals(1, testuser.getRoles().size());                
                Assert.assertEquals("TESTER_A", testuser.getRoles().iterator().next().getName());
                
                Role testerA = userManager.getRole("TESTER_A");
                Assert.assertTrue(testuser.getRoles().contains(testerA));
                
                userManager.createUser("testuser2", "testuser2");            
                userManager.addRole("testuser2", "TESTER_A");
                
                testuser = (UserBson) userManager.getUser("testuser2");

                Assert.assertEquals(1, ((UserBson)testuser).getVersion());
                Assert.assertEquals(1, testuser.getRoles().size());                
                Assert.assertEquals("TESTER_A", testuser.getRoles().iterator().next().getName());
                
                testerA = userManager.getRole("TESTER_A");
                Assert.assertTrue(testuser.getRoles().contains(testerA));
                
                userManager.addRole("testuser", "TESTER_B");
                testuser = (UserBson) userManager.getUser("testuser");
                
                Assert.assertEquals(2, ((UserBson)testuser).getVersion());
                Assert.assertEquals(2, testuser.getRoles().size());
                Assert.assertEquals(2, userManager.getRoles().size());
                
                userManager.addRole("testuser", "TESTER_A");
                testuser = (UserBson) userManager.getUser("testuser");
                
                Assert.assertEquals(3, ((UserBson)testuser).getVersion());
                Assert.assertEquals(2, testuser.getRoles().size());                
                Assert.assertEquals(2, userManager.getRoles().size());
                
                userManager.deleteRole("TESTER_A");
                testuser = (UserBson) userManager.getUser("testuser");
                
                Assert.assertEquals(3, ((UserBson)testuser).getVersion());
                Assert.assertEquals(1, testuser.getRoles().size());                
                Assert.assertEquals(1, userManager.getRoles().size());
                
                testerA = userManager.getRole("TESTER_A");
                Assert.assertNull(testerA);
                
                userManager.revokeRole("testuser", "TESTER_B");
                testuser = (UserBson) userManager.getUser("testuser");
                
                Assert.assertEquals(4, ((UserBson)testuser).getVersion());
                Assert.assertEquals(0, testuser.getRoles().size());
                Assert.assertEquals(0, userManager.getRoles().size());
                
            } catch (Exception e) {
                
                e.printStackTrace();            
                fail(e.getMessage());
            }
            
        }
	
	@Test
        public void testPassword()  {
	    Assert.assertTrue(userManager.getUsers().isEmpty());   
            
	    try {    
                userManager.createUser("testuser", "testuser");            
                
                User testuser = userManager.getUser("testuser");
                Assert.assertEquals(DigestUtils.sha256Hex("testuser"), testuser.getPassword().get());
                
                userManager.resetUserPassword("testuser", "defaultPassword");
                
                testuser = userManager.getUser("testuser");
                Assert.assertEquals(DigestUtils.sha256Hex("defaultPassword"), testuser.getPassword().get());
                Assert.assertTrue(testuser.isExpired());
                
                userManager.setUserExpiration("testuser", false);
                
                userManager.changeUserPassword("testuser", "defaultPassword", "password1");
                testuser = userManager.getUser("testuser");
                Assert.assertEquals(DigestUtils.sha256Hex("password1"), testuser.getPassword().get());
                Assert.assertFalse(testuser.isExpired());
                
            } catch (Exception e) {
                
                e.printStackTrace();            
                fail(e.getMessage());
            }
            
            
	}


}
