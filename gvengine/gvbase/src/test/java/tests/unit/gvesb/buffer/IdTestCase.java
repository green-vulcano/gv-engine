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

import it.greenvulcano.gvesb.buffer.Id;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class IdTestCase extends TestCase
{
    /**
     * @throws Exception
     */
    public void testId() throws Exception
    {
        Set<String> ids = new HashSet<String>();
        for (int i = 0; i < 100; ++i) {
            Id id = new Id();
            String idStr = id.toString();
            assertFalse(ids.contains(idStr));
            ids.add(idStr);
        }

        Id id = new Id();
        Id id2 = new Id(id);
        assertEquals(id, id2);
    }

}
