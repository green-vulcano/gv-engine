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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.lf5.LogLevel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 * 
 * JSONValidationCallOperation class
 * 
 * @version 4.1.0 Jun 01, 2020
 * @author GreenVulcano Developer Team
 * 
 */
@SuppressWarnings("unused")
public class JSONValidationCallOperation implements CallOperation {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JSONValidationCallOperation.class);

    private static final String NAMESPACE = "file:${{gv.app.home}}/xmlconfig/jsds/";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private JsonSchemaFactory factory = null;

    /**
     * The operation key.
     */
    protected OperationKey key = null;

    /**
     * Used to validate a JSON.
     */
    private String jsdName, jsdVersion;
    
    private boolean throwException;
    
    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException {

        try {
            jsdVersion = XMLConfig.get(node, "@jsd-version", "V4");              
            jsdName =  XMLConfig.get(node, "@jsd-name");
            throwException =   XMLConfig.getBoolean(node, "@throw-exception", true);
            
        } catch (Exception exc) {
            throw new InitializationException("GVCORE_VCL_JSONVALID_INIT_ERROR", new String[][] { { "node", node.getLocalName() } }, exc);
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
                json =  OBJECT_MAPPER.readTree((String) object);
            } else if (object instanceof byte[]) {
                json = OBJECT_MAPPER.readTree(new String((byte[]) object, StandardCharsets.UTF_8));
            } else if (object instanceof JSONObject) {
                json = OBJECT_MAPPER.readTree(object.toString());
            } else {
                throw new InvalidDataException("Invalid input type: " + object.getClass());
            }
            
            String version = PropertiesHandler.expand(jsdVersion, gvBuffer);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.valueOf(version));
            
            String jsonSchemaPath =  PropertiesHandler.expand(jsdName, gvBuffer);
            logger.debug("Loading JSON Schema {}", jsonSchemaPath);            
            
            JsonSchema schema = factory.getSchema(Files.newInputStream(Paths.get(jsonSchemaPath), StandardOpenOption.READ));
            Set<ValidationMessage> validationResults = schema.validate(json);

            if (validationResults.isEmpty()) {
                logger.debug("JSON is VALID against JSON Schema {}", jsonSchemaPath); 
            
            } else {
            
                logger.debug("JSON validation failed against JSON Schema {}", jsonSchemaPath);
                JSONArray errors = new JSONArray();
                validationResults.stream().map(ValidationMessage::getMessage).forEach(errors::put);
                
                if (throwException) {
                    throw new IllegalArgumentException(errors.toString());
                } else {                
                    gvBuffer.setProperty("JSON_VALIDATION_ERRORS", errors.toString());
                }
            
            }
            return gvBuffer;
        } catch (Exception exc) {
            throw new CallException("GV_JSON_VALIDATION_ERROR",
                                    new String[][] { { "service", gvBuffer.getService() },
                                                     { "system", gvBuffer.getSystem() },
                                                     { "id", gvBuffer.getId().toString() },
                                                     { "message", exc.getMessage() } },
                                    exc);
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
     * the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer) {

        return gvBuffer.getService();
    }
   

}
