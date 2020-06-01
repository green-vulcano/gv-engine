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
package tests.unit.vcl.internal.json;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.internal.json.JSONValidationCallOperation;
import it.greenvulcano.util.xml.XMLUtils;
import junit.framework.TestCase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @version 4.1.0 Jun 01, 2020
 * @author GreenVulcano Developer Team
 *
 */
public class JSONValidationCallTestCase extends TestCase
{
    private static final String CONF_VALIDATOR            = "<ChangeGVBufferNode class=\"it.greenvulcano.gvesb.core.flow.ChangeGVBufferNode\" op-type=\"change GVBuffer\""
                                                                  + " id=\"validateJSON\" input=\"json\" next-node-id=\"check_status\" output=\"validjson\" type=\"flow-node\">"
                                                                  + "<OutputServices>"
                                                                  + "<json-validation-service critical=\"yes\" internal=\"yes\" remove-fields=\"yes\" type=\"service\">"
                                                                  + "<json-validation-call class=\"it.greenvulcano.gvesb.virtual.internal.json.JSONValidationCallOperation\""
                                                                  + " name=\"validation\" type=\"call\" jsd-version=\"V4\" jsd-name=\""
                                                                  +  JSONValidationCallTestCase.class.getClassLoader().getResource(".").getPath().toString()+"jsds/fstab.jsd"
                                                                  + "\">"
                                                                  + "</json-validation-call>"
                                                                  + "</json-validation-service>"
                                                                  + "</OutputServices></ChangeGVBufferNode>";

    private static final String TEST_JSON_VALID            = "fstab-good.json";
    private static final String TEST_JSON_NOT_VALID_1      = "fstab-bad.json";
    private static final String TEST_JSON_NOT_VALID_2      = "fstab-bad2.json";

    /**
     * @throws Exception
     */
    public void testValid() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "JSON_VALIDATION");
        CallOperation validationCall = createValidationCall(CONF_VALIDATOR, TEST_JSON_VALID, false, gvBuffer);
        validationCall.perform(gvBuffer);
    }

    /**
     * @throws Exception
     */
    public void testValid_a() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "JSON_VALIDATION");
        CallOperation validationCall = createValidationCall(CONF_VALIDATOR, TEST_JSON_VALID, true, gvBuffer);
        validationCall.perform(gvBuffer);
    }

    /**
     * @throws Exception
     */
    public void testNotValid1() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "JSON_VALIDATION");
        CallOperation validationCall = createValidationCall(CONF_VALIDATOR, TEST_JSON_NOT_VALID_1, false, gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid JSON successful!");
        }
        catch (CallException exc) {            
            assertTrue(exc.getCause() instanceof IllegalArgumentException);
        }
    }

    /**
     * @throws Exception
     */
    public void testNotValid1a() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "JSON_VALIDATION");
        CallOperation validationCall = createValidationCall(CONF_VALIDATOR, TEST_JSON_NOT_VALID_1, true, gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid JSON successful!");
        }
        catch (CallException exc) {
        	assertTrue(exc.getCause() instanceof IllegalArgumentException);
        }
    }

    /**
     * @throws Exception
     */
    public void testNotValid2() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "JSON_VALIDATION");
        CallOperation validationCall = createValidationCall(CONF_VALIDATOR, TEST_JSON_NOT_VALID_2, false, gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid JSON successful!");
        }
        catch (CallException exc) {
            assertTrue(exc.getCause() instanceof IllegalArgumentException);
        }
    }

    /**
     * @throws Exception
     */
    public void testNotValid2a() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer("GVESB", "JSON_VALIDATION");
        CallOperation validationCall = createValidationCall(CONF_VALIDATOR, TEST_JSON_NOT_VALID_2, true, gvBuffer);
        try {
            validationCall.perform(gvBuffer);
            fail("Validation on non valid JSON successful!");
        }
        catch (CallException exc) {        	
            assertTrue(exc.getCause() instanceof IllegalArgumentException);
        }
    }

    private CallOperation createValidationCall(String confStr, String json, boolean parse, GVBuffer gvBuffer)
            throws Exception
    {
        Document conf = XMLUtils.parseDOM_S(confStr, false, false);
        Node node = XMLConfig.getNode(conf,
                "/ChangeGVBufferNode/OutputServices/json-validation-service/json-validation-call");
        CallOperation validationCall = new JSONValidationCallOperation();
        validationCall.init(node);
        Path filePath  = Paths.get(ClassLoader.getSystemResource(json).toURI());
        String jsondata = new String(Files.readAllBytes(filePath));
       
        gvBuffer.setObject(parse ? new JSONObject(jsondata): jsondata);
        return validationCall;
    }

}
