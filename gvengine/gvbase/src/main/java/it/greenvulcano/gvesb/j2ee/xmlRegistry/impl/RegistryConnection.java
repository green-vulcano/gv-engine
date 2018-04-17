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
package it.greenvulcano.gvesb.j2ee.xmlRegistry.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.Proxy;

import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author nunzio
 *
 */
public class RegistryConnection
{

    /**
     * The GreenVulcano logger utility.
     */
	private static final Logger   logger = LoggerFactory.getLogger(RegistryConnection.class);

    /**
     * xpath per raggiungere il Registry desiderato in configurazione
     */
    private Node                     configConf       = null;

    /**
     * Identificativo dell'UDDI registry
     */
    public String                    IDRegistry       = "";

    /**
     * Nome dell'organizzazione UDDI
     */
    public String                    organizationName = "";
    /**
     * URL per la pubblicazione sul registry
     */
    public String                    regUrlp          = "";

    /**
     * URL per la query sul registry
     */
    public String                    regUrli          = "";

    /**
     * user name per accedere al registry
     */
    private String                   username         = "";

    /**
     * password per autenticazione al registry
     */
    private String                   password         = "";

    /**
     * proxy host per connessione al registry
     */
    private String                   httpProxyHost    = "";

    /**
     * proxy port per connessione al registry
     */
    private String                   httpProxyPort    = "";

    /**
     * proxy host per connessione sicura al registry
     */
    private String                   httpsProxyHost   = "";

    /**
     * proxy port per connessione sicura al registry
     */
    private String                   httpsProxyPort   = "";

    /**
     * Factory impl della sun
     */
    private String                   implFactory      = "";

    /**
     * Class factory
     */
    private Class<?>                 classFactory     = null;
    /**
     * Connection factory
     */
    private ConnectionFactory        factory          = null;

    /**
     * Connection Properties
     */
    Properties                       connProps        = null;

    /**
     * Registry service
     */
    private RegistryService          rs;

    /**
     * Query Business manager
     */
    private BusinessQueryManager     bqm;
    private BusinessLifeCycleManager blm;
    private Connection               conn;

    /**
     * Constructor empty
     *
     */
    public RegistryConnection()
    {

    }

    /**
     * Initializes configuration parameters
     *
     * @param configConf
     * @param proxy
     */
    public void init(Node configConf, Proxy proxy)
    {

        logger.debug("BEGIN init");
        this.configConf = configConf;
        if (proxy != null) {
            this.httpProxyHost = proxy.getHost();
            this.httpProxyPort = proxy.getPort();
        }
        connProps = new Properties();

        try {
            loadConfiguration();
            setConnectionProperties();
            factory = getConnectionFactory();
            factory.setProperties(connProps);
            conn = factory.createConnection();
            rs = conn.getRegistryService();
            bqm = rs.getBusinessQueryManager();
            blm = rs.getBusinessLifeCycleManager();
            if (username != null && !username.equals("") && password != null && !password.equals("")) {
                PasswordAuthentication thePswAuthentication = new PasswordAuthentication(username,
                        password.toCharArray());
                Set<PasswordAuthentication> theConnectionCredentials = new HashSet<PasswordAuthentication>();
                theConnectionCredentials.add(thePswAuthentication);
                logger.debug("PasswordAuthentication ok");
                conn.setCredentials(theConnectionCredentials);
                logger.debug("setCredentials ok");
            }

        }
        catch (JAXRException e) {
            logger.error("JAXRException: ", e);
        }
        catch (InstantiationException exc) {
            logger.error("InstantiationException: ", exc);
        }
        catch (IllegalAccessException exc) {
            logger.error("IllegalAccessException: ", exc);
        }
        catch (Exception exc) {
            logger.error("Unhandled Exception: ", exc);
        }
        logger.debug("END init");
    }

    /**
     * This method reads the configuration and initializes the cache of objects.
     * it is invoked from the constructor and after the cleaning of the cache.
     */
    private void loadConfiguration()
    {
        logger.debug("BEGIN loadConfiguration");
        IDRegistry = XMLConfig.get(configConf, "@id-registry", "");
        logger.debug("IDRegistry = " + IDRegistry);
        organizationName = XMLConfig.get(configConf, "@organization-name", "");
        logger.debug("organizationName = " + organizationName);
        regUrlp = XMLConfig.get(configConf, "@publish-url", "");
        logger.debug("regUrlp = " + regUrlp);
        regUrli = XMLConfig.get(configConf, "@query-url", "");
        logger.debug("regUrli = " + regUrli);
        username = XMLConfig.get(configConf, "@user-name", "");
        logger.debug("username = " + username);
        password = XMLConfig.getDecrypted(configConf, "@password", "");
        logger.debug("password = " + password);
        implFactory = XMLConfig.get(configConf, "Properties/connectionFactory/@value", "");
        logger.debug("implFactory = " + implFactory);
        try {
            if (implFactory != null && !implFactory.equals(""))
                classFactory = Class.forName(implFactory);
        }
        catch (Exception e) {
            logger.error("Exception loading UDDI connection configuration", e);
        }

        logger.debug("END loadConfiguration");
    }

    /**
     * Costruisce la ConnectionFactory. Se � configurato l'attributo
     * <code>@connection-factory</code> usa la classe specificata, altrimenti
     * usa il metodo <code>ConnectionFactory.newInstance()</code>.
     */
    private ConnectionFactory getConnectionFactory() throws JAXRException, InstantiationException,
            IllegalAccessException
    {
        logger.debug("BEGIN getConnectionFactory()");
        if (classFactory == null) {
            logger.debug("END getConnectionFactory()");
            return ConnectionFactory.newInstance();
        }
        else {
            logger.debug("Load class connection factory impl from configuration file");
            logger.debug("END getConnectionFactory()");
            return (ConnectionFactory) classFactory.newInstance();
        }
    }


    /**
     * This method create a connection properties
     */
    public void setConnectionProperties()
    {
        logger.debug("BEGIN setConnectionProperties");
        connProps.setProperty("javax.xml.registry.queryManagerURL", regUrli);
        connProps.setProperty("javax.xml.registry.lifeCycleManagerURL", regUrlp);
        try {
            NodeList propClasses = XMLConfig.getNodeList(configConf, "Properties/*[@type='classproperty']");
            for (int i = 0; i < propClasses.getLength(); i++) {
                Node propClass = propClasses.item(i);
                String nome = XMLConfig.get(propClass, "@name", "");
                String value = XMLConfig.get(propClass, "@value", "");
                connProps.setProperty(XMLConfig.get(propClass, "@name", ""), XMLConfig.get(propClass, "@value", ""));
                logger.debug("nome=" + nome + "- value = " + value);

            }
            if (httpProxyHost != null && !httpProxyHost.equals(""))
                connProps.setProperty("com.sun.xml.registry.http.proxyHost", httpProxyHost);
            if (httpProxyPort != null && !httpProxyPort.equals(""))
                connProps.setProperty("com.sun.xml.registry.http.proxyPort", httpProxyPort);
            if (httpsProxyHost != null && !httpsProxyHost.equals(""))
                connProps.setProperty("com.sun.xml.registry.https.proxyHost", httpsProxyHost);
            if (httpsProxyPort != null && !httpsProxyPort.equals(""))
                connProps.setProperty("com.sun.xml.registry.https.proxyPort", httpsProxyPort);
        }
        catch (XMLConfigException e) {
            logger.error("setConnectionProperties FAILS", e);
        }

        logger.debug("END setConnectionProperties");
    }

    /**
     * @return the <code>BusinessQueryManager</code>
     * @throws JAXRException
     */
    public BusinessQueryManager getBqm() throws JAXRException
    {
        return bqm;
    }

    /**
     * @return the <code>BusinessLifeCycleManager</code>
     * @throws JAXRException
     */
    public BusinessLifeCycleManager getBlm() throws JAXRException
    {
        return blm;
    }

    /**
     * @return the <code>RegistryService</code>
     */
    public RegistryService getRs()
    {
        return rs;
    }


}
