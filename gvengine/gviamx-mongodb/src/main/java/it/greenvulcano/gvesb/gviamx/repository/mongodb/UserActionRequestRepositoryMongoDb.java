package it.greenvulcano.gvesb.gviamx.repository.mongodb;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest;
import it.greenvulcano.gvesb.gviamx.domain.mongodb.UserActionRequest.NotificationStatus;
import it.greenvulcano.gvesb.gviamx.repository.UserActionRepository;

public class UserActionRequestRepositoryMongoDb implements UserActionRepository {
    
    private String mongodbURI;
    private String databaseName;
    private MongoClient mongoClient;
    
    private final static String COLLECTION_NAME = "user_actions"; 
    
    public void setMongodbURI(String mongodbURI) {
        this.mongodbURI = mongodbURI;
    }
        
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public MongoClient getMongoClient() {
        return mongoClient;
    }
        
    private MongoCollection<Document> getCollection() {
        return  mongoClient.getDatabase(databaseName)
                           .getCollection(COLLECTION_NAME);
    }
    
    public void init() {

        ConnectionString connectionString = new ConnectionString(mongodbURI);        
        mongoClient = MongoClients.create(connectionString);
        
        mongoClient.getDatabase(databaseName)
                   .getCollection(COLLECTION_NAME)
                   .createIndex(Indexes.ascending("issue_time"));
        
        mongoClient.getDatabase(databaseName)
                   .getCollection(COLLECTION_NAME)
                   .createIndex(Indexes.compoundIndex(Arrays.asList(Indexes.ascending("email"), Indexes.ascending("action"))), new IndexOptions().unique(true));
        
    }
    
    @Override
    public Optional<UserActionRequest> get(String key) {

        Document request = getCollection().find(Filters.eq("_id", key)).first();        
        return Optional.ofNullable(request).map(UserActionRequest::fromDocument);
    }

    @Override
    public Optional<UserActionRequest> get(String email, UserActionRequest.Action type) {

        Document request = getCollection().find(Filters.and(Filters.eq("email", email), Filters.eq("action", type.toString()))).first();
        return Optional.ofNullable(request).map(UserActionRequest::fromDocument);
    }

    @Override
    public void add(UserActionRequest entity) {
        
        Instant i = Instant.now();
        entity.setUpdateTime(i);
        entity.setIssueTime(i);
        getCollection().updateOne(Filters.and(Filters.eq("email", entity.getEmail()), Filters.eq("action", entity.getAction())),  
                                  Updates.combine(Updates.set("_id", entity.getId()),
                                                  Updates.set("userid", entity.getUserId()),
                                                  Updates.set("data", entity.getRequest()),
                                                  Updates.set("issue_time", entity.getIssueTime()),
                                                  Updates.set("update_time", entity.getUpdateTime()),
                                                  Updates.set("expires_in", entity.getExpiresIn()),
                                                  Updates.set("token", entity.getToken()),
                                                  Updates.set("status", entity.getNotificationStatus().toString())), 
                                  new UpdateOptions().upsert(true));

    }
    
    @Override
    public void updateStatus(String key, NotificationStatus status) {
        
        getCollection().updateOne(Filters.eq("_id", key), Updates.set("status", status.toString()));
    }

    @Override
    public Set<UserActionRequest> getAll() {

        Set<UserActionRequest> result = new LinkedHashSet<>();        
        getCollection().find().sort(Sorts.ascending("issue_time")).iterator().forEachRemaining(d->result.add(UserActionRequest.fromDocument(d)));
        
        return result;
    }

    @Override
    public void remove(String key) {

        getCollection().deleteOne(Filters.eq("_id", key));

    }

}
