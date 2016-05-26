/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package tests.unit.gvesb.utils;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.metadata.PropertiesHandler;
import junit.framework.TestCase;


/**
 *
 * @version 3.0.0 10/giu/2010
 * @author GreenVulcano Developer Team
 */
public class GVESBPropertyHandlerTestCase extends TestCase
{
    private static final String EXPECTED_RESULT         = "hello_world_expander";
    private static final String EXPECTED_RESULT_REVERSE = "expander_world_hello";

    
    @Override
    protected void setUp() throws Exception {    	
    	super.setUp();
    	
    	System.setProperty("gv.app.home", "target/test-classes");
        
    }
    
    /**
     * @throws Exception
     */
    public final void testGVBuffer() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer();
        String inputA = "ognl{{#object.property['prop1']}}_ognl{{#object.property['prop2']}}_ognl{{#object.property['prop3']}}";

        gvBuffer.setProperty("prop1", "hello");
        gvBuffer.setProperty("prop2", "world");
        gvBuffer.setProperty("prop3", "expander");
        String result = PropertiesHandler.expand(inputA, null, gvBuffer);
        assertEquals(EXPECTED_RESULT, result);

        String inputB = "ognl{{#appo=#object.property['prop1'],#object.property['prop1']=#object.property['prop3'],#object.property['prop3']=#appo}}";
        PropertiesHandler.expand(inputB, null, gvBuffer);
        assertEquals(EXPECTED_RESULT_REVERSE, PropertiesHandler.expand(inputA, null, gvBuffer));
    }

    /**
     * @throws Exception
     */
    public final void testGVBuffer2() throws Exception
    {
        GVBuffer gvBuffer1 = new GVBuffer("SYS1", "SRV1");
        gvBuffer1.setProperty("prop1", "SRV1_value1");
        gvBuffer1.setProperty("prop2", "SRV1_value2");

        GVBuffer gvBuffer2 = new GVBuffer("SYS2", "SRV2");
        gvBuffer2.setProperty("prop1", "SRV2_value1");
        gvBuffer2.setProperty("prop2", "SRV2_value2");

        String input = "ognl{{#object.property['prop1']=#extra.property['prop1'],#object.property['prop2']=#extra.property['prop2']}}";

        PropertiesHandler.expand(input, null, gvBuffer1, gvBuffer2);

        System.out.println("testGVBuffer2: " + gvBuffer1);
        System.out.println("testGVBuffer2: " + gvBuffer2);

        assertEquals(gvBuffer2.getProperty("prop1"), gvBuffer1.getProperty("prop1"));
        assertEquals(gvBuffer2.getProperty("prop2"), gvBuffer1.getProperty("prop2"));
    }
}
