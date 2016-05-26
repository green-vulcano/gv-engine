/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.gvesb.policy.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.policy.ACL;
import it.greenvulcano.gvesb.policy.ACLException;
import it.greenvulcano.gvesb.policy.ResourceKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 01/feb/2012
 * @author GreenVulcano Developer Team
 */
public class ACLGreenVulcano implements ACL
{
    private List<ResourceKey>                                keys    = new ArrayList<ResourceKey>();
    private Map<ResourceKey, ACLResource>                    aclCfg  = new HashMap<ResourceKey, ACLResource>();
    private ConcurrentHashMap<ResourceKey, Set<ResourceKey>> aclMain = new ConcurrentHashMap<ResourceKey, Set<ResourceKey>>();
    private ResourceKey                                      defKey  = null;


    @Override
    public void init(Node node) throws ACLException
    {
        try {
            NodeList nl = XMLConfig.getNodeList(node, "ServiceRes");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                ResourceKey key = new GVCoreServiceKey();
                key.init(n);
                keys.add(key);
                ACLResource res = new ACLResource();
                res.init(n);
                aclCfg.put(key, res);
            }
        }
        catch (XMLConfigException exc) {
            throw new ACLException("Error initializing ACLGreenVulcano[ServiceRes]", new String[][]{{"exc", "" + exc}});
        }

        try {
            NodeList nl = XMLConfig.getNodeList(node, "VCLOperationRes");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                ResourceKey key = new VCLOperationKey();
                key.init(n);
                keys.add(key);
                ACLResource res = new ACLResource();
                res.init(n);
                aclCfg.put(key, res);
            }
        }
        catch (XMLConfigException exc) {
            throw new ACLException("Error initializing ACLGreenVulcano[VCLOperationRes]", new String[][]{{"exc",
                    "" + exc}});
        }

        try {
            Node n = XMLConfig.getNode(node, "DefaultRes");
            defKey = new DefaultKey();
            defKey.init(n);
            ACLResource res = new ACLResource();
            res.init(n);
            aclCfg.put(defKey, res);
        }
        catch (XMLConfigException exc) {
            throw new ACLException("Error initializing ACLGreenVulcano[DefaultRes]", new String[][]{{"exc", "" + exc}});
        }
    }

    @Override
    public boolean canAccess(ResourceKey key) throws ACLException
    {
        Set<ResourceKey> locKeys = aclMain.get(key);
        if (locKeys == null) {
            locKeys = new HashSet<ResourceKey>();
            for (ResourceKey keyL : keys) {
                if (keyL.match(key)) {
                    locKeys.add(keyL);
                }
            }
            if (locKeys.size() == 0) {
                locKeys.add(defKey);
            }
            aclMain.putIfAbsent(key, locKeys);
        }

        boolean canAccess = false;
        for (ResourceKey keyL : locKeys) {
            ACLResource res = aclCfg.get(keyL);
            canAccess = res.canAccess();
            if (!canAccess) {
                break;
            }
        }

        return canAccess;
    }

    @Override
    public void destroy()
    {
        keys.clear();
        aclCfg.clear();
        aclMain.clear();
    }
}
