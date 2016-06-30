package it.greenvulcano.gvesb.aop;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;

public class MongoDBClientStub {

	private static String connectionString;
	private static String database;
	private static String collection;
//	private static String traceLevelCollection;

	private static MongoCollection<Document> mongoCollection;
//	private static MongoCollection<Document> mongoTraceLevelCollection;

	public static synchronized MongoCollection<Document> getMongoDBCollection() {

		if (mongoCollection == null) {
			MongoClient mongoClient = MongoClients.create(new ConnectionString(connectionString));

			MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
			mongoCollection = mongoDatabase.getCollection(collection);
		}

		return mongoCollection;

	}
	
//	public static synchronized MongoCollection<Document> getMongoTraceLevelCollection() {
//
//		if (mongoTraceLevelCollection == null) {
//			MongoClient mongoClient = MongoClients.create(new ConnectionString(connectionString));
//
//			MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
//			mongoTraceLevelCollection = mongoDatabase.getCollection(traceLevelCollection);
//		}
//
//		return mongoTraceLevelCollection;
//	}	
	
	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		MongoDBClientStub.connectionString = connectionString;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		MongoDBClientStub.database = database;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		MongoDBClientStub.collection = collection;
	}
	
//	public static String getTraceLevelCollection() {
//		return traceLevelCollection;
//	}
//
//	public static void setTraceLevelCollection(String traceLevelCollection) {
//		MongoDBClientStub.traceLevelCollection = traceLevelCollection;
//	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MongoDBClientStub [connectionString=");
		builder.append(connectionString);
		builder.append(", database=");
		builder.append(database);
		builder.append(", collection=");
		builder.append(collection);
//		builder.append(", traceLevelCollection=");
//		builder.append(traceLevelCollection);
		builder.append("]");
		return builder.toString();
	}
	
}
