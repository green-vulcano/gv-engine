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
package it.greenvulcano.gvesb.j2ee.xmlRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;


/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class RegistryManager
{
    /**
     * The GreenVulcano logger utility.
     */
    private static final Logger logger = LoggerFactory.getLogger(RegistryManager.class);

    /**
     * The default configuration file
     */
    public static final String    DEFAULT_CONFIGURATION_FILE = "GVSupport.xml";

    private Node                  configConf                 = null;

    private Map<String, Registry> cacheRegistry              = null;

    private Proxy                 proxy                      = null;

    /**
     * Constructor to value the request object
     *
     * @throws RegistryException
     * @throws XMLConfigException
     */
    public RegistryManager() throws RegistryException, XMLConfigException
    {
        init();
    }


    private void init() throws RegistryException, XMLConfigException
    {
        cacheRegistry = new HashMap<String, Registry>();
        configConf = XMLConfig.getNode(DEFAULT_CONFIGURATION_FILE, "GVSupport/JAXRegistry/XMLRegistries");
        logger.debug("configConf=" + configConf.toString());
        createProxy();
        createRegistries();
    }

    /**
     * Creates the wrapper class for the required test <br>
     *
     * @return proxy <code>Proxy</code> with the plug-in object.
     * @throws RegistryException
     *         if an error occurred
     */
    protected Proxy createProxy() throws RegistryException
    {
        try {
            logger.debug("BEGIN createProxy");

            String xPath = "*[@type='proxy']";

            Node proxyConf = XMLConfig.getNode(configConf, xPath);
            if (proxyConf != null) {
                proxy = new Proxy(proxyConf);
                logger.debug("proxy=" + proxy.toString());
            }

            logger.debug("END createProxy");
        }
        catch (XMLConfigException exc) {
            throw new RegistryException("ConfigurationException", exc);
        }
        return proxy;
    }

    /**
     * @return the {@link Proxy} object
     */
    public Proxy getProxy()
    {
        return proxy;
    }

    /**
     * @throws RegistryException
     */
    protected void createRegistries() throws RegistryException
    {
        try {
            logger.debug("BEGIN createRegistries()");

            String xPath = "*[@type='xmlregistry']";
            // Node registryNode = null;
            Node registryConf = null;
            NodeList registries = XMLConfig.getNodeList(configConf, xPath);
            logger.debug("Num registries =" + registries.getLength());

            for (int i = 0; i < registries.getLength(); i++) {
                // registryNode = registries.item(i);
                registryConf = registries.item(i);

                String id_registry = XMLConfig.get(registryConf, "@id-registry");
                logger.debug("id_registry =" + id_registry);
                // Put the registry in the cache
                cacheRegistry.put(id_registry, createRegistry(registryConf));
            }
            logger.debug("END createRegistries()");
        }
        catch (Exception e) {
            logger.error("createRegistries() fails",e);
            throw new RegistryException("", e);
        }
    }

    /**
     * Creates the registry from the configuration file <br>
     *
     * @param id_registry
     *        Name of the current test
     * @return registry <code>Registry</code> with the plug-in object.
     * @throws RegistryException
     * @throws Throwable
     *         if an error occurred
     */
    private Registry createRegistry(String id_registry) throws RegistryException
    {
        Registry registry = null;
        try {
            logger.debug("BEGIN createRegistry(id_registry)");
            String xPath = "*[@type='xmlregistry'][@id-registry='" + id_registry + "']";
            Node registryConf = XMLConfig.getNode(configConf, xPath);

            registry = createRegistry(registryConf);
            logger.debug("createRegistry =" + id_registry);
            logger.debug("END createRegistry(id_registry)");
        }
        catch (Exception e) {
            logger.error("createRegistries() fails",e);
            throw new RegistryException("", e);
        }
        return registry;
    }

    private Registry createRegistry(Node registryConf) throws RegistryException
    {
        Registry registry = null;
        try {
            logger.debug("BEGIN createRegistry(Node registryNode)");


            String className = XMLConfig.get(registryConf, "@class");
            Class<?> regImpl = Class.forName(className);
            registry = (Registry) regImpl.newInstance();
            registry.init(registryConf, proxy);
            logger.debug("Create id_registry '" + registry.getRegistryID() + "' - urli '"
                    + registry.getRegistryURLInquiry() + "'");
            logger.debug("END createRegistry(Node registryNode)");
        }
        catch (Exception exc) {
            logger.error("createRegistries() fais",exc);
            throw new RegistryException("Exception: ", exc);
        }
        return registry;
    }

    /**
     * @param id_registry
     * @return the {@link Registry}
     * @throws RegistryException
     */
    public Registry getRegistry(String id_registry) throws RegistryException
    {
        Registry registry = null;
        logger.debug("BEGIN getRegistry");
        registry = cacheRegistry.get(id_registry);
        if (registry == null) {
            registry = createRegistry(id_registry);
        }
        logger.debug("cacheRegistry.get " + registry.getRegistryID() + " inquiryURL "
                + registry.getRegistryURLInquiry() + " publishURL " + registry.getRegistryURLPublish());

        return registry;
    }

    /**
     * Get the registries list in configuration
     *
     * @return ArrayList <code>getRegistriesList</code>
     * @throws RegistryException
     *         if an error occurred
     */
    public Set<String> getRegistriesSet() throws RegistryException
    {
        logger.debug("BEGIN getRegistriesSet()");
        return cacheRegistry.keySet();
    }
}
