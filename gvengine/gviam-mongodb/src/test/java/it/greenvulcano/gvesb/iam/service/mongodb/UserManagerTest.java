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
import it.greenvulcano.gvesb.iam.domain.mongodb.UserBSON;

import static org.junit.Assert.fail;

import java.util.Set;

import org.bson.Document;
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
			
		mongoClient = MongoClients.create();
		
		userManager.setMongoClient(mongoClient);
		userManager.setDatabaseName("gviam");
				
	}
	
	@AfterClass
	public static void destroy() {
	    mongod.stop();
	    mongodExecutable.stop();
	}
	
	@Before
	public void cleanup() {
	    mongoClient.getDatabase("gviam").getCollection(UserBSON.COLLECTION_NAME).deleteMany(new Document());
	}
		
	@Test
	public void testCreateUser()  { 
	    
	    try {
	    
	        Assert.assertTrue(userManager.getUsers().isEmpty());   
	        User testuser = userManager.createUser("testuser", "testuser");
	    
	        Assert.assertNotNull(testuser.getId());
	        Assert.assertEquals("testuser", testuser.getUsername() );
	        
	        System.out.println("******** CREATED User: "+ testuser.toString());
	        	        
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
                
                User testuser = (UserBSON) userManager.getUser("testuser");
                
                System.out.println("******** EDITED User: "+ testuser.toString());
                Assert.assertEquals(1, ((UserBSON)testuser).getVersion());
                Assert.assertEquals(1, testuser.getRoles().size());                
                Assert.assertEquals("TESTER_A", testuser.getRoles().iterator().next().getName());
                
                Role testerA = userManager.getRole("TESTER_A");
                System.out.println("******** ROLE: "+ testerA.toString());
                Assert.assertTrue(testuser.getRoles().contains(testerA));
                
                userManager.addRole("testuser", "TESTER_B");
                testuser = (UserBSON) userManager.getUser("testuser");
                
                System.out.println("******** EDITED User: "+ testuser.toString());
                Assert.assertEquals(2, ((UserBSON)testuser).getVersion());
                Assert.assertEquals(2, testuser.getRoles().size());
                Assert.assertEquals(2, userManager.getRoles().size());
                
                userManager.addRole("testuser", "TESTER_A");
                testuser = (UserBSON) userManager.getUser("testuser");
                
                System.out.println("******** EDITED User: "+ testuser.toString());
                Assert.assertEquals(3, ((UserBSON)testuser).getVersion());
                Assert.assertEquals(2, testuser.getRoles().size());                
                Assert.assertEquals(2, userManager.getRoles().size());
                
                userManager.deleteRole("TESTER_A");
                testuser = (UserBSON) userManager.getUser("testuser");
                
                System.out.println("******** EDITED User: "+ testuser.toString());
                Assert.assertEquals(3, ((UserBSON)testuser).getVersion());
                Assert.assertEquals(1, testuser.getRoles().size());                
                Assert.assertEquals(1, userManager.getRoles().size());
                
                testerA = userManager.getRole("TESTER_A");
                Assert.assertNull(testerA);
                
                userManager.revokeRole("testuser", "TESTER_B");
                testuser = (UserBSON) userManager.getUser("testuser");
                
                System.out.println("******** EDITED User: "+ testuser.toString());
                Assert.assertEquals(4, ((UserBSON)testuser).getVersion());
                Assert.assertEquals(0, testuser.getRoles().size());
                Assert.assertEquals(0, userManager.getRoles().size());
                
            } catch (Exception e) {
                
                e.printStackTrace();            
                fail(e.getMessage());
            }
            
        }


}
