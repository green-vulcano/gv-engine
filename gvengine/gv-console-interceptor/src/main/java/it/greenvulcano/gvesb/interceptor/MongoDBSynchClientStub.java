package it.greenvulcano.gvesb.interceptor;

import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_SERVICE_NAME;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class MongoDBSynchClientStub {
	private static final Logger logger = LoggerFactory.getLogger(MongoDBSynchClientStub.class);
	
	private static String connectionString;
	private static String database;
	private static String traceLevelCollection;

	private static MongoCollection<Document> mongoTraceLevelCollection;

	public static synchronized MongoCollection<Document> getMongoDBTraceLevelCollection() {

		if (mongoTraceLevelCollection == null) {
			MongoClientURI connectionURI = new MongoClientURI(connectionString);
			MongoClient mongoClient = new MongoClient(connectionURI);
			
			MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
			mongoTraceLevelCollection = mongoDatabase.getCollection(traceLevelCollection);
			
			logger.debug("connectionString : " + connectionString);
			logger.debug("database : " + database);
			logger.debug("traceLevelCollection : " + traceLevelCollection);
		}

		return mongoTraceLevelCollection;

	}
	
	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		MongoDBSynchClientStub.connectionString = connectionString;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		MongoDBSynchClientStub.database = database;
	}
	
	public String getTraceLevelCollection() {
		return traceLevelCollection;
	}

	public void setTraceLevelCollection(String traceLevelCollection) {
		MongoDBSynchClientStub.traceLevelCollection = traceLevelCollection;
	}
	


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MongoDBSynchClientStub [connectionString=");
		builder.append(connectionString);
		builder.append(", database=");
		builder.append(database);
		builder.append(", traceLevelCollection=");
		builder.append(traceLevelCollection);
		builder.append("]");
		return builder.toString();
	}
	
	public static void main(String[] args) {
		MongoDBSynchClientStub clientSynch = new MongoDBSynchClientStub();
		clientSynch.setConnectionString("mongodb://localhost:27017");
		clientSynch.setDatabase("console_db");
		clientSynch.setTraceLevelCollection("trace_level_service");
		
		Document docTraceLevel = new Document(GVC_SERVICE_NAME, "TOUPPER_CALL");
		com.mongodb.client.MongoCollection<Document> traceLevelCollection = MongoDBSynchClientStub.getMongoDBTraceLevelCollection();
		
		Document traceLevel = traceLevelCollection.find(docTraceLevel).first();
		
		System.out.println("traceLevel: " + traceLevel.toJson());
		int enabled = traceLevel.getInteger(GVTraceLevelServiceFields.GVC_TRACE_LEVEL_ENABLED, 0);
		System.out.println("enabled: " + enabled);
		String traceLevelUsed = traceLevel.getString(GVTraceLevelServiceFields.GVC_TRACE_LEVEL_TRACE_LEVEL);
		
		System.out.println("traceLevelUsed: " + traceLevelUsed);
	}
}
