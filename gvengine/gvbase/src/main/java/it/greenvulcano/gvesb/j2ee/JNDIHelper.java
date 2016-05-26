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
package it.greenvulcano.gvesb.j2ee;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * JNDIHelper class
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class JNDIHelper
{
    // Basic Properties
    private static final String       INITIAL_CONTEXT_FACTORY = "initial-context-factory";
    private static final String       PROVIDER_URL            = "provider-url";
    private static final String       SECURITY_PRINCIPAL      = "security-principal";
    private static final String       SECURITY_CREDENTIALS    = "security-credentials";

    // Advanced Properties
    private static final String       OBJECT_FACTORIES        = "object-factories";
    private static final String       STATE_FACTORIES         = "state-factories";
    private static final String       URL_PKG_PREFIXES        = "url-pkg-prefixes";
    private static final String       DNS_URL                 = "dns-url";
    private static final String       AUTHORITATIVE           = "authoritative";
    private static final String       BATCHSIZE               = "batchsize";
    private static final String       REFERRAL                = "referral";
    private static final String       SECURITY_PROTOCOL       = "security-protocol";
    private static final String       SECURITY_AUTHENTICATION = "security-authentication";
    private static final String       LANGUAGE                = "language";

    private Hashtable<String, String> properties              = null;
    private InitialContext            context                 = null;

    /**
     * Constructor.
     */
    public JNDIHelper()
    {
        properties = new Hashtable<String, String>();
    }

    /**
     * Constructor.
     *
     * @param node
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if error occurs
     */
    public JNDIHelper(Node node) throws XMLConfigException
    {
        properties = new Hashtable<String, String>();

        if (node != null) {
            setBasicProperties(node);
            setAdvancedProperties(node);
            setExtendedProperties(node);
        }
    }

    /**
     * Create the InitialContext.
     *
     * @return the created context
     * @throws NamingException
     *         if error occurs
     */
    public final InitialContext getInitialContext() throws NamingException
    {
        if (context == null) {
            context = new InitialContext(properties);
        }
        return context;
    }

    /**
     * Create the InitialLdapContext.
     *
     * @return the created context
     * @throws NamingException
     *         if error occurs
     */
    public final InitialLdapContext getInitialLdapContext() throws NamingException
    {
        if (context == null) {
            context = new InitialLdapContext(properties, null);
        }
        return (InitialLdapContext) context;
    }

    /**
     * Perform a lookup operation.
     *
     * @param name
     *        the binding name
     * @return the found object
     * @throws NamingException
     *         if error occurs
     */
    public final Object lookup(String name) throws NamingException
    {
        getInitialContext();
        return context.lookup(name);
    }

    /**
     * Close the Context.
     *
     * @throws NamingException
     *         if error occurs
     */
    public final void close() throws NamingException
    {
        if (context != null) {
            try {
                context.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            context = null;
        }
    }

    /**
     * @return the configured provider URL
     */
    public final String getProviderURL()
    {
        return properties.get(Context.PROVIDER_URL);
    }

    /**
     * Get a context property.
     *
     * @param property
     *        the property name
     * @return
     *         the property value
     */
    public final String getProperty(String property)
    {
        return properties.get(property);
    }

    /**
     * Sets a context property.
     *
     * @param property
     *        the property name
     * @param value
     *        the property value
     * @throws PropertiesHandlerException 
     */
    public final void setProperty(String property, String value) throws PropertiesHandlerException
    {
        properties.put(property, PropertiesHandler.expand(value));
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString()
    {
        return properties.toString();
    }

    /**
     * Sets the basic context properties.
     *
     * @param node
     *        the node from which reads configuration data
     * @throws XMLConfigException
     *         if error occurs
     */
    private void setBasicProperties(Node node) throws XMLConfigException
    {
        setProperty(node, INITIAL_CONTEXT_FACTORY, Context.INITIAL_CONTEXT_FACTORY, false);
        setProperty(node, PROVIDER_URL, Context.PROVIDER_URL, false);
        setProperty(node, SECURITY_PRINCIPAL, Context.SECURITY_PRINCIPAL, false);
        setProperty(node, SECURITY_CREDENTIALS, Context.SECURITY_CREDENTIALS, true);
    }

    /**
     * Sets the advanced context properties.
     *
     * @param node
     *        the node from which reads configuration data
     * @throws XMLConfigException
     *         if error occurs
     */
    private void setAdvancedProperties(Node node) throws XMLConfigException
    {
        Node advanced = XMLConfig.getNode(node, "Advanced");
        if (advanced == null) {
            return;
        }

        setProperty(advanced, OBJECT_FACTORIES, Context.OBJECT_FACTORIES, false);
        setProperty(advanced, STATE_FACTORIES, Context.STATE_FACTORIES, false);
        setProperty(advanced, URL_PKG_PREFIXES, Context.URL_PKG_PREFIXES, false);
        setProperty(advanced, DNS_URL, Context.DNS_URL, false);
        setProperty(advanced, AUTHORITATIVE, Context.AUTHORITATIVE, false);
        setProperty(advanced, BATCHSIZE, Context.BATCHSIZE, false);
        setProperty(advanced, REFERRAL, Context.REFERRAL, false);
        setProperty(advanced, SECURITY_PROTOCOL, Context.SECURITY_PROTOCOL, false);
        setProperty(advanced, SECURITY_AUTHENTICATION, Context.SECURITY_AUTHENTICATION, false);
        setProperty(advanced, LANGUAGE, Context.LANGUAGE, false);
    }

    /**
     * Sets the extended context properties.
     *
     * @param node
     *        the node from which reads configuration data
     * @throws XMLConfigException
     *         if error occurs
     */
    private void setExtendedProperties(Node node) throws XMLConfigException
    {
        NodeList extendedList = XMLConfig.getNodeList(node, "Extended");
        if ((extendedList == null) || (extendedList.getLength() == 0)) {
            return;
        }

        int num = extendedList.getLength();
        for (int i = 0; i < num; i++) {
            Node propertyNode = extendedList.item(i);
            String name = XMLConfig.get(propertyNode, "@name");
            if (name != null) {
                setProperty(propertyNode, "value", name, false);
            }
        }
    }

    /**
     * Reads an attribute from the given node and set the value as properties.
     *
     * @param node
     *        the node from which read the attribute
     * @param attribute
     *        the attribute name
     * @param property
     *        the property name
     * @param encrypted
     *        if true the attribute value must be decrypted
     * @throws XMLConfigException
     *         if error occurs
     */
    private void setProperty(Node node, String attribute, String property, boolean encrypted) throws XMLConfigException
    {
        String value = null;
        if (encrypted) {
            value = XMLConfig.getDecrypted(node, "@" + attribute);
        }
        else {
            value = XMLConfig.get(node, "@" + attribute);
        }
        if (value != null) {
        	try {
                properties.put(property, PropertiesHandler.expand(value));
        	}
        	catch (PropertiesHandlerException exc) {
                throw new XMLConfigException("Error processing metadata", exc);
           }
        }
    }
}
