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
package it.greenvulcano.gvesb.virtual.ws.module;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.virtual.ws.module.utils.ModulePropertiesMgr;

import java.io.ByteArrayInputStream;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.w3c.dom.Node;


/**
 * ModuleHandler class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public abstract class ModuleHandler
{
    private String              name        = null;

    private Policy              policy      = null;

    private String              policyKey   = null;

    private ModulePropertiesMgr propManager = null;

    /**
     * @param config
     * @throws XMLConfigException
     */
    public void init(Node config) throws XMLConfigException
    {
        try {
            name = XMLConfig.get(config, "@name");
            Node policyConfiguration = XMLConfig.getNode(config, "PolicyData");
            if (policyConfiguration != null) {
                policyKey = XMLConfig.get(policyConfiguration, "@policy_key", null);
                policy = loadPolicy(XMLConfig.get(policyConfiguration, "."));
            }
            propManager = new ModulePropertiesMgr();
            propManager.init(config);
        }
        catch (Exception exc) {
            throw new XMLConfigException("Error initializing Module", exc);
        }
    }

    /**
     * @param serviceClient
     * @param options
     * @return if the pre-send operation was successful
     */
    public final boolean preSendOperation(ServiceClient serviceClient, Options options)
    {
        getPropertyMgr().set(options);
        return preSendOperationSpecific(serviceClient, options);
    }

    /**
     * @param serviceClient
     * @param options
     * @return if the post-send operation was successful
     */
    public final boolean postSendOperation(ServiceClient serviceClient, Options options)
    {
        return postSendOperationSpecific(serviceClient, options);
    }

    /**
     * @param serviceClient
     * @param options
     * @return if the post-send fault operation was successful
     */
    public final boolean postSendFaultOperation(ServiceClient serviceClient, Options options)
    {
        return postSendFaultOperationSpecific(serviceClient, options);
    }

    /**
     * @param serviceClient
     * @param options
     * @return if the pre-send operation was successful
     */
    protected abstract boolean preSendOperationSpecific(ServiceClient serviceClient, Options options);

    /**
     * @param serviceClient
     * @param options
     * @return if the post-send operation was successful
     */
    protected abstract boolean postSendOperationSpecific(ServiceClient serviceClient, Options options);

    /**
     * @param serviceClient
     * @param options
     * @return if the post-send fault operation was successful
     */
    protected abstract boolean postSendFaultOperationSpecific(ServiceClient serviceClient, Options options);

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the policy
     */
    public Policy getPolicy()
    {
        return policy;
    }

    /**
     * @return the policy key
     */
    public String getPolicyKey()
    {
        return policyKey;
    }

    /**
     * @return the property manager
     */
    protected ModulePropertiesMgr getPropertyMgr()
    {
        return propManager;
    }

    private static Policy loadPolicy(String xml) throws Exception
    {
        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes()));
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }
}
