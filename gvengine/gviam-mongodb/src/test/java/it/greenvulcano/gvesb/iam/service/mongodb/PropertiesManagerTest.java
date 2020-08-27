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
import it.greenvulcano.gvesb.iam.domain.mongodb.UserBson;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.repository.mongodb.Repository;

import static org.junit.Assert.fail;

import org.bson.Document;
import org.junit.*;

public class PropertiesManagerTest {

    private static MongodExecutable mongodExecutable;
    private static MongodProcess mongod;

    private static MongoClient mongoClient;

    private static GVPropertiesManager propertiesManager = new GVPropertiesManager();
    private static GVUsersManager userManager = new GVUsersManager();

    @BeforeClass
    public static void init() throws Exception {

        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "localhost";
        int port = 27017;
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION).net(new Net(bindIp, port, Network.localhostIsIPv6())).build();

        mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();

        mongoClient = MongoClients.create();

        Repository mongodbRepository = new Repository();
        mongodbRepository.setGviamDatabaseName("gviam");
        mongodbRepository.setGvstoreDatabaseName("gviam");
        mongodbRepository.setMongodbURI("mongodb://localhost:27017");
        mongodbRepository.init();
        
        userManager.setMongodbRepository(mongodbRepository);
        propertiesManager.setMongodbRepository(mongodbRepository);
        propertiesManager.setUsersManager(userManager);

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
    public void testProperties() {

        Assert.assertTrue(propertiesManager.retrieveAll().isEmpty());

        try {
            propertiesManager.store("test1", "test1 value", "testuser", "192.168.1.1");
            fail("Expected UserNotFoundException");
        
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserNotFoundException);
        }

        try {
            userManager.createUser("testuser", "testuser");

            propertiesManager.store("test1", "test1 value", "testuser", "192.168.1.1");

            Assert.assertEquals(1, propertiesManager.retrieveAll().size());
            Assert.assertEquals("test1 value", propertiesManager.retrieve("test1").orElseThrow().getValue());

            propertiesManager.store("test1", "test2 value", "testuser", "192.168.1.1");

            Assert.assertEquals(1, propertiesManager.retrieveAll().size());
            Assert.assertEquals("test2 value", propertiesManager.retrieve("test1").orElseThrow().getValue());

            propertiesManager.delete("test1");

            Assert.assertTrue(propertiesManager.retrieveAll().isEmpty());
        } catch (Exception e) {

            e.printStackTrace();
            fail(e.getMessage());
        }

    }

}
