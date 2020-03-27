package it.greenvulcano.gvesb.iam.repository.mongodb;

import java.util.Arrays;

import org.bson.Document;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import it.greenvulcano.gvesb.iam.domain.mongodb.CredentialsBson;
import it.greenvulcano.gvesb.iam.domain.mongodb.UserBson;

public class Repository {
    
    private String mongodbURI;
    private String databaseName;
    private MongoClient mongoClient;
    
    public void setMongodbURI(String mongodbURI) {
        this.mongodbURI = mongodbURI;
    }
        
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public MongoClient getMongoClient() {
        return mongoClient;
    }
    
    public MongoDatabase getDatabase() {
        return  mongoClient.getDatabase(databaseName);
    }
    
    public MongoCollection<Document> getUserCollection() {
      return  mongoClient.getDatabase(databaseName)
                         .getCollection(UserBson.COLLECTION_NAME);
    }
    
    public MongoCollection<Document> getCredentialsCollection() {
        return  mongoClient.getDatabase(databaseName)
                           .getCollection(CredentialsBson.COLLECTION_NAME);
      }
    
    public void init() {

        ConnectionString connectionString = new ConnectionString(mongodbURI);        
        mongoClient = MongoClients.create(connectionString);
        

        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBson.COLLECTION_NAME)
                   .createIndex(Indexes.ascending("username"),  new IndexOptions().unique(true));
        
        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBson.COLLECTION_NAME)
                   .createIndex(Indexes.compoundIndex(Arrays.asList(Indexes.ascending("username"), Indexes.ascending("version"))));
        
        
        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBson.COLLECTION_NAME)
                   .createIndex(Indexes.ascending("roles.name"));
        
    }

}
