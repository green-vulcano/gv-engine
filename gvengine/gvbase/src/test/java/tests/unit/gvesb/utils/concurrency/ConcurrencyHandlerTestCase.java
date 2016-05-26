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
package tests.unit.gvesb.utils.concurrency;

import java.util.Map;
import java.util.Set;

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.utils.concurrency.ConcurrencyHandler;
import it.greenvulcano.gvesb.utils.concurrency.ConcurrencyHandler.SubSystem;
import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ConcurrencyHandlerTestCase extends TestCase
{
    /**
     * @throws XMLConfigException
     */
    public void testConcurrencyHandler() throws XMLConfigException
    {
        ConcurrencyHandler concurrencyHandler = ConcurrencyHandler.instance();
        Map<String, SubSystem> subSystems = concurrencyHandler.getSubSystems();
        Set<String> keySet = subSystems.keySet();
        boolean found = false;
        for (String subSystem : keySet) {
            System.out.println("SubSystem: " + subSystem);
            if (subSystem.equals("Core")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}
