package it.greenvulcano.gvesb.interceptor;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_END_DATE;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_GV_BUFFER_OBJECT;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_GV_BUFFER_OBJECT_OUT;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_GV_BUFFER_PROPS;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_GV_BUFFER_PROPS_OUT;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_INPUT_OBJECT_TYPE;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_INPUT_OBJECT_TYPE_OUT;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_OPERATION_NAME;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_SERVICE_INSTANCE_ID;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_SERVICE_NAME;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_START_DATE;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_SUBOPERATION_NAME;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_SUBSYSTEM;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_SYSTEM;
import static it.greenvulcano.gvesb.interceptor.GVServiceInstanceFields.GVC_THREAD_NAME;
import static it.greenvulcano.gvesb.interceptor.GVTraceLevelServiceFields.GVC_TRACE_LEVEL_SERVICE_NAME;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.flow.hub.Event;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;


public class GVConsoleAspect {

	private static final Logger logger = LoggerFactory.getLogger(GVConsoleAspect.class);


	public static void logGVFlowPerformBefore(Event event)
	{
		logger.debug("************************logGVFlowPerformBefore - START************************");

		try {
			InvocationContext context = event.getInvocationContext();
			String serviceInstanceId = context.getId().toString();
			logger.debug("logGVFlowPerformBefore -> serviceInstanceId: " + serviceInstanceId + "-System: " + context.getSystem() + "-SubSystem: "+ context.getSubSystem() + "-Service: " + context.getService() + "-Operation: " +context.getOperation() + "-SubOperation: " + context.getSubOperation());
			
			//MongoCollection<org.bson.Document> traceLevelCollection = MongoDBSynchClientStub.getMongoDBCollection();
			Document docTraceLevel = new Document(GVC_TRACE_LEVEL_SERVICE_NAME, context.getService());
			com.mongodb.client.MongoCollection<Document> traceLevelCollection = MongoDBSynchClientStub.getMongoDBTraceLevelCollection();

			logger.debug("traceLevelCollection: " + traceLevelCollection);
			Document traceLevelFound = traceLevelCollection.find(docTraceLevel).first();

			logger.debug("before traceLevelFound: " + traceLevelFound);

			if(traceLevelFound != null) {

				int enabled = traceLevelFound.getInteger(GVTraceLevelServiceFields.GVC_TRACE_LEVEL_ENABLED, 0);
				String traceLevelUsed = traceLevelFound.getString(GVTraceLevelServiceFields.GVC_TRACE_LEVEL_TRACE_LEVEL);

				List<Document> propertiesDoc = null;
				String inputType 	= null;
				String inputObject 	= null;

				if(enabled > 0) {
					if(GVConsoleConstants.GVC_TRACE_LEVEL_DEBUG.equals(traceLevelUsed)) {

						GVBuffer buffer = event.getGvBuffer();
						if(buffer != null) {
							logger.debug("BUFFER: " + buffer.toString());
							logger.debug("BUFFER_PROPERTIES: " + buffer.getPropertyNames().toString());

							if(buffer.getPropertyNamesSet() != null && buffer.getPropertyNamesSet().size() > 0) {
								propertiesDoc = new ArrayList<Document>();

								for(String keyProp : buffer.getPropertyNamesSet()) {
									logger.debug("keyProp: " + keyProp + " - valueProp: " +buffer.getProperty(keyProp));
									propertiesDoc.add(new Document(keyProp, buffer.getProperty(keyProp)));
								}

							}
							if(buffer.getObject() instanceof org.w3c.dom.Document) {
								printXmlProps((org.w3c.dom.Document)buffer.getObject());

								//PRINT XML STRING INPUT.
								inputObject = XML2String((org.w3c.dom.Document)buffer.getObject());

								inputType	= "application/xml";
							} else if(buffer.getObject() instanceof java.lang.String) { 
								inputObject = (String)buffer.getObject();
								inputType	= "application/json";
							} else {
								logger.debug("CONTENT NOT AVAILABLE");
								inputType	= "application/octet-stream";
							}

							logger.debug("buffer: " + buffer);
						}
					}

					//final CountDownLatch latch = new CountDownLatch(1);

					String threadName = Thread.currentThread().getName();
					logger.debug("Thread.currentThread().getName(): " + Thread.currentThread().getName());

					MongoCollection<Document> collection = MongoDBClientStub.getMongoDBCollection();

					Document doc = new Document(GVC_SERVICE_INSTANCE_ID, serviceInstanceId)
					.append(GVC_SERVICE_NAME, context.getService())
					.append(GVC_OPERATION_NAME, context.getOperation())
					.append(GVC_SUBOPERATION_NAME, context.getSubOperation())
					.append(GVC_SYSTEM, context.getSystem())
					.append(GVC_SUBSYSTEM, context.getSubSystem())
					.append(GVC_THREAD_NAME, threadName)
					.append(GVC_START_DATE, new Date())
					.append(GVC_END_DATE, null);

					if(GVConsoleConstants.GVC_TRACE_LEVEL_DEBUG.equals(traceLevelUsed)) {

						doc.append(GVC_INPUT_OBJECT_TYPE, inputType)
						.append(GVC_GV_BUFFER_OBJECT, inputObject);

						if(propertiesDoc != null) {
							doc.append(GVC_GV_BUFFER_PROPS, propertiesDoc);
						}

						doc.append(GVC_INPUT_OBJECT_TYPE_OUT, null)
						.append(GVC_GV_BUFFER_OBJECT_OUT, null)
						.append(GVC_GV_BUFFER_PROPS_OUT, null);
					}

					collection.insertOne(doc, new SingleResultCallback<Void>() {
						//@Override
						public void onResult(final Void result, final Throwable t) {
							logger.debug("Inserted!  " + result);
							//	latch.countDown();
						}
					});

					//latch.await();
				}
			} else {
				logger.debug("NO TRACE LEVEL CONFIGURED FOR SERVICE_NAME: " + context.getService());
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}


		logger.debug("************************logGVFlowPerformBefore - END************************");
	}


	public static void logGVFlowPerformAfterReturning(Event event) {
		logger.debug("************************logGVFlowPerformAfterReturning - START************************");

		try {
			//InvocationContext context = (InvocationContext)InvocationContext.getInstance();
			InvocationContext context = event.getInvocationContext();
			String serviceInstanceId = context.getId().toString();
			logger.debug("logGVFlowPerformAfterReturning -> serviceInstanceId: " + serviceInstanceId + "-System: " + context.getSystem() + "-SubSystem: "+ context.getSubSystem() + "-Service: " + context.getService() + "-Operation: " +context.getOperation() + "-SubOperation: " + context.getSubOperation());
			
			Document docTraceLevel = new Document(GVC_TRACE_LEVEL_SERVICE_NAME, context.getService());
			com.mongodb.client.MongoCollection<Document> traceLevelCollection = MongoDBSynchClientStub.getMongoDBTraceLevelCollection();
			Document traceLevelFound = traceLevelCollection.find(docTraceLevel).first();
			logger.debug("traceLevelFound: " + traceLevelFound);

			if(traceLevelFound != null) {
				int enabled = traceLevelFound.getInteger(GVTraceLevelServiceFields.GVC_TRACE_LEVEL_ENABLED, 0);
				String traceLevelUsed = traceLevelFound.getString(GVTraceLevelServiceFields.GVC_TRACE_LEVEL_TRACE_LEVEL);

				List<Document> propertiesDoc = null;
				String outputType 	= null;
				Object outputObject = null;				

				GVBuffer buffer = null;

				if(enabled > 0) {
					if(event != null && event.getEventResult() != null && event.getEventResult().getObject() != null) {
						outputObject = event.getEventResult().getObject();
						buffer = (GVBuffer)outputObject;

						if(buffer != null) {
							logger.debug("BUFFER: " + buffer.toString());
							logger.debug("BUFFER_PROPERTIES: " + buffer.getPropertyNames().toString());

							if(buffer.getPropertyNamesSet() != null && buffer.getPropertyNamesSet().size() > 0) {
								propertiesDoc = new ArrayList<Document>();

								for(String keyProp : buffer.getPropertyNamesSet()) {
									logger.debug("keyProp: " + keyProp + " - valueProp: " +buffer.getProperty(keyProp));
									propertiesDoc.add(new Document(keyProp, buffer.getProperty(keyProp)));
								}

							}
							if(buffer.getObject() instanceof org.w3c.dom.Document) {
								printXmlProps((org.w3c.dom.Document)buffer.getObject());

								//PRINT XML STRING INPUT.
								outputObject = XML2String((org.w3c.dom.Document)buffer.getObject());

								outputType	= "application/xml";
							} else if(buffer.getObject() instanceof java.lang.String) { 
								outputObject = (String)buffer.getObject();
								outputType	= "application/json";
							} else {
								logger.debug("CONTENT NOT AVAILABLE");
								outputType	= "application/octet-stream";
							}
						}
					}
				}

				String threadName = Thread.currentThread().getName();
				logger.debug("Thread.currentThread().getName(): " + Thread.currentThread().getName());

				//final CountDownLatch latch = new CountDownLatch(1);

				MongoCollection<Document> collection = MongoDBClientStub.getMongoDBCollection();

				Date endService = new Date();

				Document fieldsDoc = new Document()
				.append(GVC_END_DATE, endService);

				if(GVConsoleConstants.GVC_TRACE_LEVEL_DEBUG.equals(traceLevelUsed)) {
					fieldsDoc.append(GVC_INPUT_OBJECT_TYPE_OUT, outputType).append(GVC_GV_BUFFER_OBJECT_OUT, outputObject).append(GVC_GV_BUFFER_PROPS_OUT, propertiesDoc);
				}
				Document updateFieldsDoc = new Document("$set", fieldsDoc);

				logger.debug("Document to Update -> [serviceName: " + context.getService() + "-operationName: " + context.getOperation() + "-serviceInstanceId: " + serviceInstanceId + "-threadName: " + threadName);
				collection.updateOne(and(eq(GVC_SERVICE_NAME, context.getService()), eq(GVC_OPERATION_NAME, context.getOperation()), eq(GVC_SUBOPERATION_NAME, context.getSubOperation()), eq(GVC_SERVICE_INSTANCE_ID, serviceInstanceId), eq(GVC_THREAD_NAME, threadName) ), updateFieldsDoc,
						new SingleResultCallback<UpdateResult>() {
					//@Override
					public void onResult(final UpdateResult result, final Throwable t) {
						logger.debug("UPDATED n° elements(ModifiedCount): " + result.getModifiedCount());
						//	            latch.countDown();
					}
				});

				/*
//				logger.debug("Document to Update -> [serviceName: " + serviceName + "-operationName: " + operationName + "-serviceInstanceId: " + serviceInstanceId + "-threadName: " + threadName);
//				collection.updateOne(and(eq(GVC_SERVICE_NAME, serviceName), eq(GVC_OPERATION_NAME, operationName), eq(GVC_SUBOPERATION_NAME, subOperationName), eq(GVC_SERVICE_INSTANCE_ID, serviceInstanceId), eq(GVC_THREAD_NAME, threadName) ), new Document("$set", new Document(GVC_END_DATE, endService)),
//						new SingleResultCallback<UpdateResult>() {
//					//@Override
//					public void onResult(final UpdateResult result, final Throwable t) {
//						logger.debug("UPDATED n° elements(ModifiedCount): " + result.getModifiedCount());
//						//	            latch.countDown();
//					}
//				});
				 */
				//latch.await();

			} else {
				logger.debug("NO TRACE LEVEL CONFIGURED FOR SERVICE_NAME: " + context.getService());
			}

		} 
		catch (Exception exc) {
			exc.printStackTrace();
		} 

		logger.debug("************************logGVFlowPerformAfter - END************************");
	}

	/****************************************************************/
	/********************** UTILITY METHODS *************************/
	/****************************************************************/

	private static void printXmlProps(org.w3c.dom.Document input) {
		logger.debug("printXmlProps - START... " + input.getTextContent());

		// get the first element
		Element element = input.getDocumentElement();

		// get all child nodes
		NodeList nodes = element.getChildNodes();

		// print the text content of each child
		for (int i = 0; i < nodes.getLength(); i++) {
			logger.debug("" + nodes.item(i).getTextContent());
		}
	}

	private static String XML2String(org.w3c.dom.Document inputDoc ) throws TransformerException {
		logger.debug("XML2String - START ");

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(inputDoc), new StreamResult(writer));

		logger.debug("XML2String inputDoc value is: \n" + writer.toString());

		logger.debug("XML2String - END ");

		return writer.toString();
	}
}
