/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.internal.json;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Lists;

/**
 * 
 * JSONValidationCallOperation class
 * 
 * @version 3.5.0 Sep 16, 2014
 * @author GreenVulcano Developer Team
 * 
 */
@SuppressWarnings("unused")
public class JSONValidationCallOperation implements CallOperation {

	private static final Logger               logger        = org.slf4j.LoggerFactory.getLogger(JSONValidationCallOperation.class);
	
	private static final String               NAMESPACE     = "file:${{gv.app.home}}/xmlconfig/jsds/";

	private static URITranslatorConfiguration translatorCfg = null;
	private static LoadingConfiguration       cfg           = null;
	private static JsonSchemaFactory          factory       = null;

	/**
	 * The operation key.
	 */
	protected OperationKey      key       = null;

	/**
	 * Used to validate a JSON.
	 */
	private JsonSchema          schema;

	private String              schemaName;

	static {
		try {
			//translatorCfg = URITranslatorConfiguration.newBuilder().setNamespace(PropertiesHandler.expand(NAMESPACE)).freeze();
			//cfg = LoadingConfiguration.newBuilder().setURITranslatorConfiguration(translatorCfg).freeze();
			//factory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(cfg).setReportProvider(new ListReportProvider(LogLevel.WARNING, LogLevel.NONE)).freeze();
			factory = JsonSchemaFactory.newBuilder().setReportProvider(new ListReportProvider(LogLevel.WARNING, LogLevel.NONE)).freeze();
		} catch (Exception exc) {
			logger.error("Error initializing JsonSchemaFactory", exc);
		}
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
	 */
	public void init(Node node) throws InitializationException {
		try {
			schemaName = XMLConfig.get(node, "@jsd-name");
			logger.debug("JSD name: " + schemaName);
			String schemaFile = getJSDPath(schemaName);
			logger.debug("JSD file: " + schemaFile);
			
			if (factory == null) {
				throw new InitializationException("GVCORE_VCL_JSONVALID_INIT_ERROR", new String[][] {{"node", node.getLocalName()},
								{"message", "JsonSchemaFactory not initialized"}});
			}

			schema = factory.getJsonSchema(JsonLoader.fromPath(schemaFile));
		} catch (InitializationException exc) {
			throw exc;
		} catch (Exception exc) {
			throw new InitializationException("GVCORE_VCL_JSONVALID_INIT_ERROR", new String[][] {{"node", node.getLocalName()}}, exc);
		}
	}

	/**
	 * Executes the operation.
	 * 
	 * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
	 */
	public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {
		try {
			JsonNode json = null;
			Object object = gvBuffer.getObject();
	        if (object == null) {
	            throw new InvalidDataException("Null JSON document");
	        }

	        if (object instanceof String) {
	            json = JsonLoader.fromString((String) object);
	        }
	        else if (object instanceof byte[]) {
	        	json = JsonLoader.fromString(new String((byte[]) object));
            }
	        else if (object instanceof JSONObject) {
	            json = JsonLoader.fromString(object.toString());
	        }
	        else {
	        	throw new InvalidDataException("Invalid input type: " + object.getClass());
	        }

	        ProcessingReport report = schema.validate(json, true);
	        
	        if (!report.isSuccess()) {
	        	String msg = buildValidationReport(report);
	        	logger.error(msg);
	        	throw new ProcessingException(msg);
	        }

			return gvBuffer;
		} catch (Exception exc) {
			throw new CallException("GV_JSON_VALIDATION_ERROR", new String[][] {
					{ "service", gvBuffer.getService() },
					{ "system", gvBuffer.getSystem() },
					{ "id", gvBuffer.getId().toString() },
					{ "message", exc.getMessage() } }, exc);
		}
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
	 */
	public void cleanUp() {
		// Do nothing
	}

	/**
	 * Called when an operation is discarded from cache.
	 */
	public void destroy() {
		// Do nothing
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
	 */
	public void setKey(OperationKey key) {
		this.key = key;
	}

	/**
	 * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
	 */
	public OperationKey getKey() {
		return key;
	}

	/**
	 * Return the alias for the given service
	 * 
	 * @param gvBuffer
	 *            the input service data
	 * @return the configured alias
	 */
	public String getServiceAlias(GVBuffer gvBuffer) {
		return gvBuffer.getService();
	}
	
	private String getJSDPath(String jsdFile) throws Exception
    {
        if (jsdFile == null) {
            return null;
        }
        String jsdResource = "jsds/" + jsdFile;

        logger.debug("looking in classpath for " + jsdResource);

        URL url = JSONValidationCallOperation.class.getClassLoader().getResource(jsdResource);

        if (url == null) {
            jsdResource =  PropertiesHandler.expand(jsdFile, null);
            logger.debug("looking in filesystem for " + jsdResource);

            File jsd = new File(jsdResource);
            if (jsd.exists() && jsd.isFile()) {
                return jsd.getAbsolutePath();
            }
        }
        else {
            return url.getPath();
        }

        throw new IOException("JSD " + jsdFile + " not found");
    }
	
    private String buildValidationReport(ProcessingReport report) {
        StringBuilder sb = new StringBuilder("Validation report:\n");
        List<ProcessingMessage> messages = Lists.newArrayList(report);

        if (!messages.isEmpty()) {
            for (ProcessingMessage message: messages)
	            sb.append(message);
        }
        return sb.toString();
    }
}
