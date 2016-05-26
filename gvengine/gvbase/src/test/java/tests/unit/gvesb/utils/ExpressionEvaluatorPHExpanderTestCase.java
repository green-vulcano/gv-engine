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
import it.greenvulcano.gvesb.utils.ExpressionEvaluatorPlaceholderExpander;
import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class ExpressionEvaluatorPHExpanderTestCase extends TestCase
{

    private static final String EXPECTED_RESULT = "hello_world_expander";

    /**
     * @throws Exception
     */
    public final void testExpander() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer();
        ExpressionEvaluatorPlaceholderExpander ph = new ExpressionEvaluatorPlaceholderExpander();
        String input = "%{ognl:property['prop4'] = property['prop1'], property['prop1']}_%{ognl:property['prop2']}_%{ognl:property['prop3']}";
        gvBuffer.setProperty("prop1", "hello");
        gvBuffer.setProperty("prop2", "world");
        gvBuffer.setProperty("prop3", "expander");
        assertEquals(EXPECTED_RESULT, ph.expand(input, gvBuffer));
        assertEquals(gvBuffer.getProperty("prop1"), gvBuffer.getProperty("prop4"));
    }
}
