package it.greenvulcano.gvesb.iam.repository.mongodb;

import java.util.Arrays;

import org.bson.Document;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import it.greenvulcano.gvesb.iam.domain.mongodb.CredentialsBson;
import it.greenvulcano.gvesb.iam.domain.mongodb.PropertyBson;
import it.greenvulcano.gvesb.iam.domain.mongodb.UserBson;

public class Repository {
    
    private String mongodbURI;
    private String gviamDatabaseName, gvstoreDatabaseName;
    private MongoClient mongoClient;
    
    public void setMongodbURI(String mongodbURI) {
        this.mongodbURI = mongodbURI;
    }
        
    public void setGviamDatabaseName(String gviamDatabaseName) {
        this.gviamDatabaseName = gviamDatabaseName;
    }
    
    
    public void setGvstoreDatabaseName(String gvstoreDatabaseName) {
        this.gvstoreDatabaseName = gvstoreDatabaseName;
    }
    
    public MongoClient getMongoClient() {
        return mongoClient;
    }
    
    public MongoCollection<Document> getUserCollection() {
      return  mongoClient.getDatabase(gviamDatabaseName)
                         .getCollection(UserBson.COLLECTION_NAME);
    }
    
    public MongoCollection<Document> getCredentialsCollection() {
        return  mongoClient.getDatabase(gviamDatabaseName)
                           .getCollection(CredentialsBson.COLLECTION_NAME);
    }
    
    public MongoCollection<Document> getPropertiesCollection() {
        return  mongoClient.getDatabase(gvstoreDatabaseName)
                           .getCollection(PropertyBson.COLLECTION_NAME);
    }
    
    public void init() {

        ConnectionString connectionString = new ConnectionString(mongodbURI);        
        mongoClient = MongoClients.create(connectionString);
        

        mongoClient.getDatabase(gviamDatabaseName)
                   .getCollection(UserBson.COLLECTION_NAME)
                   .createIndex(Indexes.ascending("username"),  new IndexOptions().unique(true));
        
        mongoClient.getDatabase(gviamDatabaseName)
                   .getCollection(UserBson.COLLECTION_NAME)
                   .createIndex(Indexes.compoundIndex(Arrays.asList(Indexes.ascending("username"), Indexes.ascending("version"))));
        
        
        mongoClient.getDatabase(gviamDatabaseName)
                   .getCollection(UserBson.COLLECTION_NAME)
                   .createIndex(Indexes.ascending("roles.name"));
        
        mongoClient.getDatabase(gviamDatabaseName)
                   .getCollection(CredentialsBson.COLLECTION_NAME)
                   .createIndex(Indexes.ascending("access_token"),  new IndexOptions().unique(true));
        
        mongoClient.getDatabase(gviamDatabaseName)
                   .getCollection(CredentialsBson.COLLECTION_NAME)
                   .createIndex(Indexes.ascending("resource_owner"));
        
        mongoClient.getDatabase(gvstoreDatabaseName)
                   .getCollection(PropertyBson.COLLECTION_NAME)
                   .createIndex(Indexes.ascending("key"),  new IndexOptions().unique(true));
        
    }

}
