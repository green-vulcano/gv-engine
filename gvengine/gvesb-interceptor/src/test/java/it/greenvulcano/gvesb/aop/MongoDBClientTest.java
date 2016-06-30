package it.greenvulcano.gvesb.aop;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;

public class MongoDBClientTest {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("****************TEST START****************");
		final CountDownLatch latch = new CountDownLatch(2);

		//MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017,localhost:27018,localhost:27019");
		MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017"));

		MongoDatabase database = mongoClient.getDatabase("mydbtest_gv");
		MongoCollection<Document> collection = database.getCollection("mydbtest_gv");


		//INSERT_ONE
		Document doc = new Document("serviceInstanceId", "C0A8380156CE359F0001F113")
		.append("system", "GVESB")
		.append("count", 1)
		.append("startDate", new Date())
		.append("info", new Document("x", 9000).append("y", 9000));
		collection.insertOne(doc, new SingleResultCallback<Void>() {
			//@Override
			public void onResult(final Void result, final Throwable t) {
				System.out.println("Inserted One Finished! ");
				latch.countDown();
			}
		});

		//INSERT_MANY
		List<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < 100; i++) {
		    documents.add(new Document("i", i));
		}
		collection.insertMany(documents, new SingleResultCallback<Void>() {
		    //@Override
		    public void onResult(final Void result, final Throwable t) {
		        System.out.println("Documents inserted!");
		        latch.countDown();
		    }
		});
		
		Date t1 = new Date();
		System.out.println("****************TEST Waiting END****************");
		latch.await();
		Date t2 = new Date();
		
		System.out.println("****************TEST END - Tempo not waited -> in T(millis): "+ (t2.getTime()-t1.getTime()) +"****************");

	}

}
