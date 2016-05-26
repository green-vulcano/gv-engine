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
package tests.unit.gvesb.buffer;

import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVBufferTestCase extends TestCase
{
    private static final String SYSTEM_NAME    = "TEST_SYSTEM";
    private static final String SERVICE_NAME   = "TEST_SERVICE";
    private static final int    RETCODE        = 210;
    private static final String PROPERTY_NAME  = "test_prop";
    private static final String PROPERTY_VALUE = "prop_value";
    private static final Object TEST_BUFFER    = "Object buffer";

    /**
     * @throws Exception
     */
    public void testGVBuffer() throws Exception
    {
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        gvBuffer.setRetCode(RETCODE);
        gvBuffer.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
        gvBuffer.setObject(TEST_BUFFER);

        assertEquals(SYSTEM_NAME, gvBuffer.getSystem());
        assertEquals(SERVICE_NAME, gvBuffer.getService());
        assertEquals(id, gvBuffer.getId());
        assertEquals(RETCODE, gvBuffer.getRetCode());
        assertEquals(TEST_BUFFER, gvBuffer.getObject());
        assertEquals(PROPERTY_VALUE, gvBuffer.getProperty(PROPERTY_NAME));
    }

    /**
     * @throws Exception
     */
    public void testGVBufferAccessWithOGNL() throws Exception
    {
        ExpressionEvaluator evaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE);
        Id id = new Id();
        GVBuffer gvBuffer = new GVBuffer(SYSTEM_NAME, SERVICE_NAME, id);
        evaluator.getValue("setRetCode('" + RETCODE + "')", gvBuffer);
        evaluator.getValue("setProperty('" + PROPERTY_NAME + "','" + PROPERTY_VALUE + "')", gvBuffer);
        evaluator.getValue("setObject('" + TEST_BUFFER + "')", gvBuffer);

        assertEquals(SYSTEM_NAME, evaluator.getValue("system", gvBuffer));
        assertEquals(SERVICE_NAME, evaluator.getValue("service", gvBuffer));
        assertEquals(id, evaluator.getValue("id", gvBuffer));
        assertEquals(RETCODE, evaluator.getValue("retCode", gvBuffer));
        assertEquals(TEST_BUFFER, evaluator.getValue("object", gvBuffer));
        assertEquals(PROPERTY_VALUE, evaluator.getValue("getProperty('" + PROPERTY_NAME + "')", gvBuffer));
    }

}
