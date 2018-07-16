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
package tests.unit.gvesb.policy;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.IdentityInfo;
import it.greenvulcano.gvesb.identity.impl.DummyIdentityInfo;
import it.greenvulcano.gvesb.policy.ACLManager;
import it.greenvulcano.gvesb.policy.impl.GVCoreServiceKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version 3.2.0 Feb 02, 2012
 * @author GreenVulcano Developer Team
 */
public class GVPolicyTestCase extends TestCase
{
    Set<String> addressLocal = null;
    Set<String> addressEst   = null;

    @Override
    protected void setUp() throws Exception
    {
       
        super.setUp();
        XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
        addressLocal = new HashSet<>();
       
        addressLocal.add("127.0.0.1/32");
        addressEst = new HashSet<>();
      
        addressEst.add("192.168.100.0/30");
    }

    /**
     * @throws Exception
     */
    public void testDummyIdentityInfo() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "127.0.0.1");

        assertEquals("TestUser", id.getName());
        assertTrue(id.isInRole("ROLE_A"));
        assertTrue(id.isInRole("ROLE_B"));
        assertTrue(id.isInRole("ROLE_C"));
        assertFalse(id.isInRole("ROLE_D"));

        assertFalse(id.isInRole(""));
        assertFalse(id.isInRole((String) null));
        assertFalse(id.isInRole((String[]) null));
        assertFalse(id.isInRole((List<String>) null));
        assertFalse(id.isInRole((Set<String>) null));

        assertTrue(id.matchAddress("127.0.0.1"));
        assertFalse(id.matchAddress("192.168.100.10"));

        assertTrue(id.matchAddressMask(addressLocal));
        assertFalse(id.matchAddressMask(addressEst));

        assertFalse(id.matchAddress(""));
        assertFalse(id.matchAddress((String) null));
        assertFalse(id.matchAddress((String[]) null));
        assertFalse(id.matchAddress((List<String>) null));
        assertFalse(id.matchAddress((Set<String>) null));
        assertFalse(id.matchAddressMask(null));

        List<String> rolesL = new ArrayList<String>();
        assertFalse(id.isInRole(rolesL));
        rolesL.add("ROLE_D");
        assertFalse(id.isInRole(rolesL));
        rolesL.add("ROLE_A");
        assertTrue(id.isInRole(rolesL));
        rolesL.clear();
        rolesL.add("ROLE_B");
        assertTrue(id.isInRole(rolesL));
        rolesL.clear();
        rolesL.add("ROLE_C");
        assertTrue(id.isInRole(rolesL));

        Set<String> rolesS = new HashSet<String>();
        assertFalse(id.isInRole(rolesS));
        rolesS.add("ROLE_D");
        assertFalse(id.isInRole(rolesS));
        rolesS.add("ROLE_A");
        assertTrue(id.isInRole(rolesS));
        rolesS.clear();
        rolesS.add("ROLE_B");
        assertTrue(id.isInRole(rolesS));
        rolesS.clear();
        rolesS.add("ROLE_C");
        assertTrue(id.isInRole(rolesS));
    }

    /**
     * @throws Exception
     */
    public void testDummyIdentityInfo2() throws Exception
    {
        IdentityInfo id0 = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "192.168.100.0");
        IdentityInfo id1 = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "192.168.100.1");
        IdentityInfo id2 = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "192.168.100.2");
        IdentityInfo id3 = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "192.168.100.3");
        IdentityInfo id4 = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "192.168.100.4");

        assertTrue(id0.matchAddressMask(addressEst));
        assertTrue(id1.matchAddressMask(addressEst));
        assertTrue(id2.matchAddressMask(addressEst));
        assertTrue(id3.matchAddressMask(addressEst));
        assertFalse(id4.matchAddressMask(addressEst));
    }

    /**
     * @throws Exception
     */
    public void testGVIdentityHelper() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "127.0.0.1");

        GVIdentityHelper.push(id);
        try {
            assertEquals("TestUser", GVIdentityHelper.getName());
            assertTrue(GVIdentityHelper.isInRole("ROLE_A"));
            assertTrue(GVIdentityHelper.isInRole("ROLE_B"));
            assertTrue(GVIdentityHelper.isInRole("ROLE_C"));
            assertFalse(GVIdentityHelper.isInRole("ROLE_D"));

            assertFalse(GVIdentityHelper.isInRole(""));
            assertFalse(GVIdentityHelper.isInRole((String) null));
            assertFalse(GVIdentityHelper.isInRole((String[]) null));
            assertFalse(GVIdentityHelper.isInRole((List<String>) null));
            assertFalse(GVIdentityHelper.isInRole((Set<String>) null));

            assertTrue(GVIdentityHelper.matchAddress("127.0.0.1"));
            assertFalse(GVIdentityHelper.matchAddress("192.168.100.10"));

            assertFalse(GVIdentityHelper.matchAddress(""));
            assertFalse(GVIdentityHelper.matchAddress((String) null));
            assertFalse(GVIdentityHelper.matchAddress((String[]) null));
            assertFalse(GVIdentityHelper.matchAddress((List<String>) null));
            assertFalse(GVIdentityHelper.matchAddress((Set<String>) null));

            List<String> rolesL = new ArrayList<String>();
            assertFalse(GVIdentityHelper.isInRole(rolesL));
            rolesL.add("ROLE_D");
            assertFalse(GVIdentityHelper.isInRole(rolesL));
            rolesL.add("ROLE_A");
            assertTrue(GVIdentityHelper.isInRole(rolesL));
            rolesL.clear();
            rolesL.add("ROLE_B");
            assertTrue(GVIdentityHelper.isInRole(rolesL));
            rolesL.clear();
            rolesL.add("ROLE_C");
            assertTrue(GVIdentityHelper.isInRole(rolesL));

            Set<String> rolesS = new HashSet<String>();
            assertFalse(GVIdentityHelper.isInRole(rolesS));
            rolesS.add("ROLE_D");
            assertFalse(GVIdentityHelper.isInRole(rolesS));
            rolesS.add("ROLE_A");
            assertTrue(GVIdentityHelper.isInRole(rolesS));
            rolesS.clear();
            rolesS.add("ROLE_B");
            assertTrue(GVIdentityHelper.isInRole(rolesS));
            rolesS.clear();
            rolesS.add("ROLE_C");
            assertTrue(GVIdentityHelper.isInRole(rolesS));
        }
        finally {
            GVIdentityHelper.pop();
        }
    }

    /**
     * @throws Exception
     */
    public void testGVIdentityHelper2() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser", "ROLE_A,ROLE_B,ROLE_C", "");

        GVIdentityHelper.push(id);
        try {
            assertEquals("TestUser", GVIdentityHelper.getName());
            assertTrue(GVIdentityHelper.isInRole("ROLE_A"));
            assertTrue(GVIdentityHelper.isInRole("ROLE_B"));
            assertTrue(GVIdentityHelper.isInRole("ROLE_C"));
            assertFalse(GVIdentityHelper.isInRole("ROLE_D"));
        }
        finally {
            GVIdentityHelper.pop();
        }
        assertEquals("NONE", GVIdentityHelper.getName());
        assertFalse(GVIdentityHelper.isInRole("ROLE_A"));
        assertFalse(GVIdentityHelper.isInRole("ROLE_B"));
        assertFalse(GVIdentityHelper.isInRole("ROLE_C"));
        assertFalse(GVIdentityHelper.isInRole("ROLE_D"));
    }

    /**
     * @throws Exception
     */
    public void testACLManager1() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser_1", "ROLE_A", "");
        GVIdentityHelper.push(id);
        try {
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_2")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_1")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_2")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_2")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B1", "OPER_1")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B2", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_2")));
        }
        finally {
            GVIdentityHelper.pop();
        }
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B2", "OPER_1")));
        assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_1")));
        assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_2")));
    }

    /**
     * @throws Exception
     */
    public void testACLManager2() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser_1", "ROLE_B", "");
        GVIdentityHelper.push(id);
        try {
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_1")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_2")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_1")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_2")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_2")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B2", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_2")));
        }
        finally {
            GVIdentityHelper.pop();
        }
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B2", "OPER_1")));
        assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_1")));
        assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_2")));
    }

    /**
     * @throws Exception
     */
    public void testACLManager3() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser_1", "ROLE_A,ROLE_C", "");
        GVIdentityHelper.push(id);
        try {
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_2")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_2")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_2")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B1", "OPER_1")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B2", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_2")));
        }
        finally {
            GVIdentityHelper.pop();
        }
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A1", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A2", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_A", "SVC_A3", "OPER_2")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_B", "SVC_B2", "OPER_1")));
        assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_1")));
        assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_C", "SVC_C1", "OPER_2")));
    }

    /**
     * @throws Exception
     */
    public void testACLManager4() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser_1", "ROLE_A", "");
        GVIdentityHelper.push(id);
        try {
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D1", "OPER_1")));
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D2", "OPER_2")));
        }
        finally {
            GVIdentityHelper.pop();
        }
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D2", "OPER_2")));
    }

    /**
     * @throws Exception
     */
    public void testACLManager5() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser_1", "ROLE_A", "127.0.0.1");
        GVIdentityHelper.push(id);
        try {
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D2", "OPER_2")));
        }
        finally {
            GVIdentityHelper.pop();
        }
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D2", "OPER_2")));
    }

    /**
     * @throws Exception
     */
    public void testACLManager6() throws Exception
    {
        IdentityInfo id = new DummyIdentityInfo("TestUser_1", "", "127.0.0.1");
        GVIdentityHelper.push(id);
        try {
            assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D1", "OPER_1")));
            assertTrue(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D2", "OPER_2")));
        }
        finally {
            GVIdentityHelper.pop();
        }
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D1", "OPER_1")));
        assertFalse(ACLManager.canAccess(new GVCoreServiceKey("GRP_D", "SVC_D2", "OPER_2")));
    }

}
