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
package it.greenvulcano.configuration;

import it.greenvulcano.event.EventHandler;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.crypto.CryptoHelperException;
import it.greenvulcano.util.crypto.CryptoUtilsException;
import it.greenvulcano.util.txt.PropertiesFileReader;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.search.XPath;
import it.greenvulcano.util.xpath.search.XPathAPI;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * This class is used to access the XML configuration files. <br>
 * The application access the nodes using the static methods of this class.
 * <p>
 * The <code>XMLConfig</code> methods can throw <code>XMLConfigException</code>.
 * <p>
 * Access to the configuration is synchronized among threads, so this class is
 * thread safe. To optimize the performances, however, it is recommended that
 * client classes reads the configuration only once at initialization time. <br>
 * To be notified about configuration changes they can use configuration
 * listener mechanism. Clients can be registered as listeners on a single
 * configuration file, on a set of files or on all files loaded by
 * <tt>XMLConfig</tt>.
 * <p>
 * <b>The XMLConfig does not use the GVLogger mechanism to log exception because
 * the GVLogger uses XMLConfig during initialization: we want to avoid loops in
 * case of initialization error. The XMLConfig uses the standard
 * printStackTrace() API and the standard error in order to make evidence of
 * exceptions. </b>
 * <p>
 * The XMLConfig object can be in two states: 1) normal work, 2) firing events.
 * <p>
 * During the normal work (state 1) the XMLConfig serves requests to the client
 * classes. <br>
 * During firing events (state 2) the XMLConfig notifies listeners about
 * configuration changes.
 * <p>
 * XMLConfig switches from state 1 to state 2 when a file is loaded or removed,
 * switches from state 1 to state 2 when all events are raised. During firing
 * events no requests are served to the client classes.
 * 
 * 
 * <hr>
 * <h3>Splitting configuration files</h3>
 * It is possible to split configuration files in multiple fragments:
 * transparently XMLConfig will read all fragments and will merge them together.
 * <p>
 * In order to split a configuration file in multiple fragments, you must
 * provide to XMLConfig an XML file described by the following DTD:
 * 
 * <pre>
 *     &lt;!ELEMENT config (document*)&gt;
 *     &lt;!ATTLIST config xmlns CDATA #FIXED &quot;http://config.eai.it&quot;&gt;
 *     &lt;!ELEMENT document (merging*)&gt;
 *     &lt;!ATTLIST document document CDATA #REQUIRED&gt;
 *     &lt;!ELEMENT merging EMPTY&gt;
 *     &lt;!ATTLIST merging src CDATA #REQUIRED&gt;
 *     &lt;!ATTLIST merging dest CDATA #REQUIRED&gt;
 * </pre>
 * 
 * <p>
 * When XMLConfig loads a configuration file it check if the root of the XML is
 * a <b>config</b> element in the <b>http://gvesb.it</b> namespace.
 * <p>
 * If not, XMLConfig will return the loaded XML and the load operation
 * terminates.
 * <p>
 * If yes, XMLConfig will perform the following steps:
 * <ul>
 * <li>iterate over the <b>document</b> elements
 * <li>read <b>document</b> attribute and load the XML.<br>
 * The value of the <b>document</b> attribute is relative to the URL specified
 * by the application.
 * <li>The first <b>document</b> will be the base XML, following
 * <b>document</b>s will be merged to the base XML in this way:
 * <ul>
 * <li>read the <b>merging</b> elements
 * <li>from each <b>merging</b> read the <b>src</b> and <b>dest</b> attributes
 * <li><b>src</b>'s value is a XPath applied to the current <b>document</b> in
 * order to select a list of nodes to merge to the base XML
 * <li><b>dest</b>'s value is a XPath applied to the base XML in order to
 * specify the parent element
 * <li>the <b>src</b> nodes will be imported and appended to the <b>dest</b>
 * node
 * </ul>
 * </ul>
 * The resulting base XML will be returned as the configuration file.
 * <p>
 * <b>Example:</b><br>
 * Suppose to have following files:
 * <p>
 * <code>frag1.xml</code>:
 * 
 * <pre>
 *     &lt;root&gt;
 *         &lt;elem id=&quot;A&quot;/&gt;
 *         &lt;elem id=&quot;B&quot;/&gt;
 *     &lt;/root&gt;
 * </pre>
 * 
 * <p>
 * <code>frag2.xml</code>:
 * 
 * <pre>
 *     &lt;root&gt;
 *         &lt;elem id=&quot;C&quot;/&gt;
 *         &lt;elem id=&quot;D&quot;/&gt;
 *     &lt;/root&gt;
 * </pre>
 * 
 * <p>
 * <code>config.xml</code>:
 * 
 * <pre>
 *     &lt;config xmlns=&quot;http://eai.it&quot;&gt;
 *      &lt;document document=&quot;frag1.xml&quot;/&gt;
 *      &lt;document document=&quot;frag2.xml&quot;&gt;
 *          &lt;merging src=&quot;/root/elem&quot; dest=&quot;/root&quot;/&gt;
 *      &lt;/document&gt;
 *     &lt;/config&gt;
 * </pre>
 * 
 * <p>
 * The statement
 * 
 * <pre>
 * XMLConfig.getDocument(&quot;config.xml&quot;)
 * </pre>
 * 
 * will result in the following XML:
 * 
 * <pre>
 *     &lt;root&gt;
 *         &lt;elem id=&quot;A&quot;/&gt;
 *         &lt;elem id=&quot;B&quot;/&gt;
 *         &lt;elem id=&quot;C&quot;/&gt;
 *         &lt;elem id=&quot;D&quot;/&gt;
 *     &lt;/root&gt;
 * </pre>
 * 
 * and the following statement will be true:
 * 
 * <pre>
 * XMLConfig.get(&quot;config.xml&quot;, &quot;/root/elem[3]&quot;).equals(&quot;C&quot;)
 * </pre>
 * 
 * <hr>
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public final class XMLConfig {
	
	private final static Logger LOG = LoggerFactory.getLogger(XMLConfig.class);
	
	public static final String DEFAULT_FOLDER = "xmlconfig";
	
    /**
     * XMLConfig events source.
     */
    public static final String EVENT_SOURCE   = "XMLConfig";
    /**
     * default keiId for configuration cipher key.
     */
    public static final String DEFAULT_KEY_ID = CryptoHelper.DEFAULT_KEY_ID;

    /**
     * The XPath API implementation.
     */
    public static final String CONFIG_NS      = "http://gvesb.it";

    /**
     * Default entity resolver used by XMLConfig. This entity resolver does not
     * resolve any entity. To use an actual entity resolver use the
     * <code>setEntityResolver()</code> method of <code>XMLConfig</code>.
     * 
     * @see #setDefaultEntityResolver()
     * @see #setEntityResolver(org.xml.sax.EntityResolver)
     */
    static class DefaultEntityResolver implements EntityResolver
    {
        /**
         * @param publicId
         * @param systemId
         * @return an InputSource for an empty string.
         */
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
        {
            return new InputSource(new StringReader(""));
        }
    }

    static class SplitConfig
    {
        private String fileSrc = "";
        private String xpath   = "";

        public SplitConfig(String fileSrc, String xpath)
        {
            this.fileSrc = fileSrc;
            this.xpath = xpath;
        }

        public String getFileSrc()
        {
            return fileSrc;
        }

        public String getXpath()
        {
            return xpath;
        }

        @Override
        public String toString()
        {
            return fileSrc + " -> " + xpath;
        }
    }

    private static String                         baseConfigPath      = "";

    /**
     * Entity resolver used to resolve entity contained in the configuration
     * file.
     */
    private static EntityResolver                 entityResolver      = new DefaultEntityResolver();

    /**
     * Cache for loaded configuration files.
     */
    private static Map<String, Document>          documents           = new HashMap<String, Document>();

    /**
     * Cache for reload-able configuration files.
     */
    private static Map<String, Document>          reloadableDocuments = new HashMap<String, Document>();

    /**
     * Cache for already loaded configuration files.
     */
    private static Map<String, Document>          splitDocuments      = new HashMap<String, Document>();

    /**
     * The XPath API implementation.
     */
    private static XPathAPI                       xpathAPI            = new XPathAPI();

    /**
     * Caches for URLs used to load the configuration files.
     */
    private static Map<String, URL>               urls                = new HashMap<String, URL>();

    /**
     * Properties defined in XMLConfig.properties file.
     */
    private static Map<String, String>            baseProps           = null;

    /**
     * Properties defined in XMLConfigSplit.properties file.
     */
    private static Map<String, List<String>>      mainToSplitFile     = null;

    /**
     * Properties defined in XMLConfigSplit.properties file.
     */
    private static Map<String, SplitConfig>       splitConfig         = null;

    /**
     * True if XMLConfig is firing events. During firing events no requests are
     * served to the clients.
     */
    private static boolean                        isFiringEvents      = false;

    /**
     * List of ConfigurationEvents to fire.
     */
    private static LinkedList<ConfigurationEvent> configurationEvents = new LinkedList<ConfigurationEvent>();

    /**
     * Constructor
     */
    private XMLConfig()
    {
        // do nothing
    }

    /**
     * Set the entity resolver used to resolve entities into the configuration
     * files.
     * 
     * @param entityResolver
     *        EntityResolver to use in order to resolve entity. If
     *        <code>null</code> is specified then the default XML mechanism is
     *        used.
     * 
     * @see #setDefaultEntityResolver()
     */
    public static synchronized void setEntityResolver(EntityResolver entityResolver)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        XMLConfig.entityResolver = entityResolver;
    }

    /**
     * Use the default entity resolver to resolve entities into the
     * configuration files. The default entity resolver does not resolve
     * anything.
     * 
     * @see #setEntityResolver(org.xml.sax.EntityResolver)
     */
    public static synchronized void setDefaultEntityResolver()
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        XMLConfig.entityResolver = new DefaultEntityResolver();
    }

    /**
     * Return the entity resolver for the configuration.
     * 
     * @return the entity resolver for the configuration
     */
    public static synchronized EntityResolver getEntityResolver()
    {
        return entityResolver;
    }

    /**
     * Load a configuration file and, if necessary, notifies registered
     * listeners. This method can be used in order to preload the configuration
     * file.
     * 
     * @param file
     *        the file to read
     * @return the complete URL used to load the file.
     * @exception XMLConfigException
     *            if error occurs
     */
    public static synchronized URL load(String file) throws XMLConfigException
    {
        return load(file, null, false, true);
    }

    /**
     * Load a configuration file and, if necessary, notifies registered
     * listeners. This method can be used in order to preload the configuration
     * file.
     * 
     * @param file
     *        the file to read
     * @param classLoader
     *        the class loader to use to retrieve the file to read
     * @param force
     *        force the reload of file if already present in cache
     * @param canBeReloaded
     *        flag that indicates if this file can be changed and can be
     *        reloaded
     * @return the complete URL used to load the file.
     * @exception XMLConfigException
     *            if error occurs
     */
    public static synchronized URL load(String file, ClassLoader classLoader, boolean force, boolean canBeReloaded)
            throws XMLConfigException
    {
        // Load a configuration file and caches it.
        // If the file is already loaded, noting happens.
        //
        getDocument(file, classLoader, force, canBeReloaded);

        // Return the complete used URL
        //
        return urls.get(file);
    }

    /**
     * Discard all cached configuration files, notifies all registered
     * listeners, reload discarded files and notifies all registered listeners.
     * 
     * @exception XMLConfigException
     *            if error occurs
     */
    public static synchronized void reloadAll() throws XMLConfigException
    {
        baseProps = null;

        xpathAPI.reset();

        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        // Obtains loaded configuration file names
        //
        Set<String> keys = reloadableDocuments.keySet();
        String[] arr = new String[keys.size()];
        keys.toArray(arr);

        // Discard all and notify listeners
        //
        discardAll();

        // Reload files
        //
        for (int i = 0; i < arr.length; ++i) {
            load(arr[i]);
        }
    }

    /**
     * If loaded discard the given file and notifies all registered listeners,
     * then reload the given file (only if it was previously loaded) and
     * notifies all registered listeners.
     * 
     * @param file
     *        the file to reload
     * @exception XMLConfigException
     *            if error occurs
     */
    public static synchronized void reload(String file) throws XMLConfigException
    {
        baseProps = null;

        xpathAPI.reset();

        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        boolean mustReload = reloadableDocuments.containsKey(file);
        if (mustReload) {
            discard(file);
            load(file);
        }
    }

    /**
     * Discard all cached files and notifies all listeners.
     */
    public static synchronized void discardAll()
    {
        baseProps = null;

        xpathAPI.reset();

        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        Set<String> keys = reloadableDocuments.keySet();
        String[] arr = new String[keys.size()];
        keys.toArray(arr);
        Map<String, URL> oldUrls = urls;
        reloadableDocuments = new HashMap<String, Document>();
        splitDocuments = new HashMap<String, Document>();
        urls = new HashMap<String, URL>();

        for (int i = 0; i < arr.length; ++i) {
            ConfigurationEvent event = new ConfigurationEvent(ConfigurationEvent.EVT_FILE_REMOVED, arr[i],
                    oldUrls.get(arr[i]));
            prepareConfigurationEvent(event, false);

            List<String> splits = mainToSplitFile.get(arr[i]);
            if (splits != null) {
                for (String split : splits) {
                    ConfigurationEvent eventS = new ConfigurationEvent(ConfigurationEvent.EVT_FILE_REMOVED, split,
                            oldUrls.get(arr[i]));
                    prepareConfigurationEvent(eventS, false);
                }
            }
        }
    }

    /**
     * Discard the given file. If the file was previously loaded then the
     * listeners will be notified, otherwise no action will be taken.
     * 
     * @param file
     *        the file to discard
     */
    public static synchronized void discard(String file)
    {
        baseProps = null;

        xpathAPI.reset();

        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        if (reloadableDocuments.containsKey(file)) {
            URL url = urls.get(file);
            reloadableDocuments.remove(file);
            urls.remove(file);
            documents.remove(file);
            ConfigurationEvent event = new ConfigurationEvent(ConfigurationEvent.EVT_FILE_REMOVED, file, url);
            prepareConfigurationEvent(event, true);

            List<String> splits = mainToSplitFile.get(file);
            if (splits != null) {
                for (String split : splits) {
                    splitDocuments.remove(split);
                    ConfigurationEvent eventS = new ConfigurationEvent(ConfigurationEvent.EVT_FILE_REMOVED, split, url);
                    prepareConfigurationEvent(eventS, false);
                }
            }
        }
    }

    /**
     * Returns an array of <tt>String</tt> s containing the names of the files
     * currently loaded into <tt>XMLConfig</tt> private cache.
     * 
     * @return an array of <tt>String</tt> s containing the names of the files
     *         currently loaded into <tt>XMLConfig</tt> private cache.
     */
    public static synchronized String[] getLoadedFiles()
    {
        Set<String> keys = reloadableDocuments.keySet();
        String[] arr = new String[keys.size()];
        keys.toArray(arr);
        return arr;
    }

    /**
     * Return the value for a node.
     * 
     * @param node
     *        input Node.
     * 
     * @return the node value. The value for an Element is the concatenation of
     *         children values. For other nodes the value is
     *         <code>node.getNodeValue()</code>.
     */
    public static String getNodeValue(Node node)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        if (node instanceof Element) {
            StringBuilder buf = new StringBuilder();
            Node child = node.getFirstChild();
            while (child != null) {
                String val = getNodeValue(child);
                if (val != null) {
                    buf.append(val);
                }
                child = child.getNextSibling();
            }
            return buf.toString();
        }
        return node.getNodeValue();
    }

    /**
     * Return the value of a NodeList as concatenation of values of all nodes
     * contained in the list.
     * 
     * @param node
     *        the node list
     * @return the nodes value
     */
    public static String getNodeValue(NodeList node)
    {
        StringBuilder buf = new StringBuilder();
        NodeList list = node;
        int n = list.getLength();
        for (int i = 0; i < n; ++i) {
            String val = getNodeValue(list.item(i));
            if (val != null) {
                buf.append(val);
            }
        }
        return buf.toString();
    }

    /**
     * Reads a value. If the XPath selects many nodes the values are appended
     * together.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * 
     * @return the searched value or <code>null</code> if the XPath select no
     *         node.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     * @see #getNodeValue(org.w3c.dom.NodeList)
     * @see #getNodeList(java.lang.String, java.lang.String)
     */
    public static String get(String file, String xpath) throws XMLConfigException
    {
        NodeList list = getNodeList(file, xpath);
        if (list.getLength() == 0) {
            return null;
        }
        return getNodeValue(list);
    }

    /**
     * Reads a value. If the XPath selects many nodes the values are appended
     * together.
     * 
     * @param node
     *        base for XPath
     * @param xpath
     *        parameter to read specified as relative path to the node.
     * 
     * @return the searched value or <code>null</code> if the XPath select no
     *         node.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     * @see #getNodeValue(org.w3c.dom.NodeList)
     * @see #getNodeList(org.w3c.dom.Node, java.lang.String)
     */
    public static String get(Node node, String xpath) throws XMLConfigException
    {
        NodeList list = getNodeList(node, xpath);
        if (list.getLength() == 0) {
            return null;
        }
        return getNodeValue(list);
    }

    /**
     * Reads a value. If the XPath selects many nodes the values are appended
     * together.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #get(java.lang.String, java.lang.String)
     */
    public static String get(String file, String xpath, String defaultValue)
    {
        try {
            String val = get(file, xpath);
            if (val == null) {
                return defaultValue;
            }
            return val;
        }
        catch (Exception exc) {
            return defaultValue;
        }
    }

    /**
     * Reads a value. If the XPath selects many nodes the values are appended
     * together.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #get(org.w3c.dom.Node, java.lang.String)
     */
    public static String get(Node node, String xpath, String defaultValue)
    {
        try {
            String val = get(node, xpath);
            if (val == null) {
                return defaultValue;
            }
            return val;
        }
        catch (Exception exc) {
            return defaultValue;
        }
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * @param keyId
     *        the key id to be used for decryption, if null or empty is used
     *        DEFAULT_KEY_ID
     * @param canBeClear
     *        if true the data can be unencrypted
     * 
     * @return the searched value or <code>null</code> if the XPath select no
     *         node.
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     * @see #getNodeValue(org.w3c.dom.NodeList)
     * @see #getNodeList(java.lang.String, java.lang.String)
     */
    public static String getDecrypted(String file, String xpath, String keyId, boolean canBeClear)
            throws XMLConfigException
    {
        NodeList list = getNodeList(file, xpath);
        if (list.getLength() != 1) {
            return null;
        }
        String value = getNodeValue(list);
        if ("".equals(keyId)) {
            keyId = DEFAULT_KEY_ID;
        }
        try {
            value = CryptoHelper.decrypt(keyId, value, canBeClear);
        }
        catch (CryptoUtilsException exc) {
            throw new XMLConfigException("Error occurred decrypting value (XPath " + xpath + " - keyId " + keyId
                    + ") : " + exc.getMessage(), exc);
        }
        catch (CryptoHelperException exc) {
            if (canBeClear) {
                return value;
            }
            throw new XMLConfigException("Error occurred decrypting value (XPath " + xpath + " - keyId " + keyId
                    + ") : " + exc.getMessage(), exc);
        }
        return value;
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param node
     *        base for XPath
     * @param xpath
     *        parameter to read specified as relative path to the node.
     * @param keyId
     *        the key id to be used for decryption, if null or empty is used
     *        DEFAULT_KEY_ID
     * @param canBeClear
     *        if true the data can be unencrypted
     * 
     * @return the searched value or <code>null</code> if the XPath select no
     *         node.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     * @see #getNodeValue(org.w3c.dom.NodeList)
     * @see #getNodeList(org.w3c.dom.Node, java.lang.String)
     */
    public static String getDecrypted(Node node, String xpath, String keyId, boolean canBeClear)
            throws XMLConfigException
    {
        NodeList list = getNodeList(node, xpath);
        if (list.getLength() != 1) {
            return null;
        }
        String value = getNodeValue(list);
        if ("".equals(keyId)) {
            keyId = DEFAULT_KEY_ID;
        }
        try {
            value = CryptoHelper.decrypt(keyId, value, canBeClear);
        }
        catch (CryptoUtilsException exc) {
            throw new XMLConfigException("Error occurred decrypting value (XPath " + xpath + " - keyId " + keyId
                    + ") : " + exc.getMessage(), exc);
        }
        catch (CryptoHelperException exc) {
            if (canBeClear) {
                return value;
            }
            throw new XMLConfigException("Error occurred decrypting value (XPath " + xpath + " - keyId " + keyId
                    + ") : " + exc.getMessage(), exc);
        }
        return value;
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * @param keyId
     *        the key id to be used for decryption, if null or empty is used
     *        DEFAULT_KEY_ID
     * @param canBeClear
     *        if true the data can be unencrypted
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #getDecrypted(java.lang.String, java.lang.String)
     */
    public static String getDecrypted(String file, String xpath, String keyId, boolean canBeClear, String defaultValue)
    {
        try {
            String val = getDecrypted(file, xpath, keyId, canBeClear);
            if (val == null) {
                return defaultValue;
            }
            return val;
        }
        catch (Exception exc) {
            return defaultValue;
        }
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param keyId
     *        the key id to be used for decryption, if null or empty is used
     *        DEFAULT_KEY_ID
     * @param canBeClear
     *        if true the data can be unencrypted
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #getDecrypted(org.w3c.dom.Node, java.lang.String)
     */
    public static String getDecrypted(Node node, String xpath, String keyId, boolean canBeClear, String defaultValue)
    {
        try {
            String val = getDecrypted(node, xpath, keyId, canBeClear);
            if (val == null) {
                return defaultValue;
            }
            return val;
        }
        catch (Exception exc) {
            return defaultValue;
        }
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * 
     *        Sets the key id as DEFAULT_KEY_ID and canBeClear at true
     * 
     * @return the searched value or <code>null</code> if the XPath select no
     *         node.
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     * @see #getDecrypted(java.lang.String, java.lang.String, java.lang.String,
     *      boolean)
     */
    public static String getDecrypted(String file, String xpath) throws XMLConfigException
    {
        return getDecrypted(file, xpath, DEFAULT_KEY_ID, true);
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param node
     *        base for XPath
     * @param xpath
     *        parameter to read specified as relative path to the node.
     * 
     *        Sets the key id as DEFAULT_KEY_ID and canBeClear at true
     * 
     * @return the searched value or <code>null</code> if the XPath select no
     *         node.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     * @see #getDecrypted(org.w3c.dom.Node, java.lang.String, java.lang.String,
     *      boolean)
     */
    public static String getDecrypted(Node node, String xpath) throws XMLConfigException
    {
        return getDecrypted(node, xpath, DEFAULT_KEY_ID, true);
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * @param defaultValue
     *        default value
     * 
     *        Sets the key id as DEFAULT_KEY_ID and canBeClear at true
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #getDecrypted(java.lang.String, java.lang.String, java.lang.String,
     *      boolean, java.lang.String)
     */
    public static String getDecrypted(String file, String xpath, String defaultValue)
    {
        return getDecrypted(file, xpath, DEFAULT_KEY_ID, true, defaultValue);
    }

    /**
     * Reads a single encrypted value.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param defaultValue
     *        default value
     * 
     *        Sets the key id as DEFAULT_KEY_ID and canBeClear at true
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #getDecrypted(org.w3c.dom.Node, java.lang.String, java.lang.String,
     *      boolean, java.lang.String)
     */
    public static String getDecrypted(Node node, String xpath, String defaultValue)
    {
        return getDecrypted(node, xpath, DEFAULT_KEY_ID, true, defaultValue);
    }

    /**
     * Decrypt a string encrypted by default XMLConfig key.
     * 
     * @param value
     *        the value to decrypt
     * 
     * @return the decrypted value or value if not encrypted
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     */
    public static String getDecrypted(String value) throws XMLConfigException
    {
        String out = value;
        try {
            out = CryptoHelper.decrypt(DEFAULT_KEY_ID, value, true);
        }
        catch (Exception exc) {
            throw new XMLConfigException("Error occurred decrypting value [" + value + "]", exc);
        }
        return out;
    }

    /**
     * Encrypt a string using default XMLConfig key.
     * 
     * @param value
     *        the value to encrypt
     * 
     * @return the encrypted value
     * @throws XMLConfigException
     *         if some error occurs.
     * 
     */
    public static String getEncrypted(String value) throws XMLConfigException
    {
        String out = value;
        try {
            out = CryptoHelper.encrypt(DEFAULT_KEY_ID, value, true);
        }
        catch (Exception exc) {
            throw new XMLConfigException("Error occurred encrypting value [" + value + "]", exc);
        }
        return out;
    }

    /**
     * Return an integer parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to an integer.
     * 
     * @param file
     * @param xpath
     * @return the searched integer value from the configuration.
     * 
     * @see #get(java.lang.String, java.lang.String)
     * 
     * @throws XMLConfigException
     *         if any error occurs
     */
    public static int getInteger(String file, String xpath) throws XMLConfigException
    {
        try {
            String v = get(file, xpath);
            return Integer.parseInt(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }

    }

    /**
     * Return an integer parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to an integer.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * 
     * @return the searched integer value from the configuration.
     * 
     * @throws XMLConfigException
     *         if an error occurs
     */
    public static int getInteger(Node node, String xpath) throws XMLConfigException
    {
        try {
            String v = get(node, xpath);
            return Integer.parseInt(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Return an integer parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to an integer.
     * 
     * @param file
     *        the configuration file
     * @param xpath
     *        the XPath to search
     * @param defaultValue
     *        value to return if XPath not match
     * @return the parameter value. If the parameter does not exists or an error
     *         occurs then the specified default value will be returned.
     * 
     * @see #get(java.lang.String, java.lang.String)
     */
    public static int getInteger(final String file, final String xpath, final int defaultValue)
    {
        try {
            return getInteger(file, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Return an integer parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to an integer.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #get(org.w3c.dom.Node, java.lang.String)
     */
    public static int getInteger(Node node, String xpath, int defaultValue)
    {
        try {
            return getInteger(node, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Return a long parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a long.
     * 
     * @param file
     * @param xpath
     * @return the searched long value from the configuration.
     * 
     * @see #get(java.lang.String, java.lang.String)
     * 
     * @throws XMLConfigException
     *         if an error occurs
     */
    public static long getLong(String file, String xpath) throws XMLConfigException
    {
        try {
            String v = get(file, xpath);
            return Long.parseLong(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Return a long parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a long.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * 
     * @return the searched long value from the configuration.
     * 
     * @throws XMLConfigException
     *         if an error occurs
     */
    public static long getLong(Node node, String xpath) throws XMLConfigException
    {
        try {
            String v = get(node, xpath);
            return Long.parseLong(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Return a long parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a long.
     * 
     * @param file
     * @param xpath
     * @param defaultValue
     * 
     * @return the parameter value. If the parameter does not exists or an error
     *         occurs then the specified default value will be returned.
     * 
     * @see #get(java.lang.String, java.lang.String)
     */
    public static long getLong(String file, String xpath, long defaultValue)
    {
        try {
            return getLong(file, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Return a long parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a long.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param defaultValue
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #get(org.w3c.dom.Node, java.lang.String)
     */
    public static long getLong(Node node, String xpath, long defaultValue)
    {
        try {
            return getLong(node, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Return a double parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a double.
     * 
     * @param file
     * @param xpath
     * @return the searched double value from the configuration.
     * 
     * @see #get(java.lang.String, java.lang.String)
     * 
     * @throws XMLConfigException
     *         if an error occurs
     */
    public static double getDouble(String file, String xpath) throws XMLConfigException
    {
        try {
            String v = get(file, xpath);
            return Double.parseDouble(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Return a double parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a double.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * 
     * @return the searched double value from the configuration.
     * 
     * @throws XMLConfigException
     *         if an error occurs
     */
    public static double getDouble(Node node, String xpath) throws XMLConfigException
    {
        try {
            String v = get(node, xpath);
            return Double.parseDouble(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Return a double parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a double.
     * 
     * @param file
     * @param xpath
     * @param defaultValue
     * 
     * @return the parameter value. If the parameter does not exists or an error
     *         occurs then the specified default value will be returned.
     * 
     * @see #get(java.lang.String, java.lang.String)
     */
    public static double getDouble(String file, String xpath, double defaultValue)
    {
        try {
            return getDouble(file, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Return a double parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a double.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #get(org.w3c.dom.Node, java.lang.String)
     */
    public static double getDouble(Node node, String xpath, double defaultValue)
    {
        try {
            return getDouble(node, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Return a float parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a float.
     * 
     * @param file
     * @param xpath
     * @return the searched float value from the configuration.
     * 
     * @see #get(java.lang.String, java.lang.String)
     * 
     * @throws XMLConfigException
     *         if an error occurs
     */
    public static float getFloat(String file, String xpath) throws XMLConfigException
    {
        try {
            String v = get(file, xpath);
            return Float.parseFloat(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Return a float parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a float.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * 
     * @return the searched float value from the configuration.
     * 
     * @throws XMLConfigException
     *         if an error occurs
     */
    public static float getFloat(Node node, String xpath) throws XMLConfigException
    {
        try {
            String v = get(node, xpath);
            return Float.parseFloat(v);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Return a float parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a float.
     * 
     * @param file
     * @param xpath
     * @param defaultValue
     * 
     * @return the parameter value. If the parameter does not exists or an error
     *         occurs then the specified default value will be returned.
     * 
     * @see #get(java.lang.String, java.lang.String)
     */
    public static float getFloat(String file, String xpath, float defaultValue)
    {
        try {
            return getFloat(file, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Return a float parameter. <br>
     * This method uses <code>get()</code> to obtain the value, then convert it
     * to a float.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     * 
     * @see #get(org.w3c.dom.Node, java.lang.String)
     */
    public static float getFloat(Node node, String xpath, float defaultValue)
    {
        try {
            return getFloat(node, xpath);
        }
        catch (Exception exc) {
            // do nothing
        }
        return defaultValue;
    }

    /**
     * Returns a boolean parameter.
     * <p>
     * The value returned is <b>true</b> if and only if the parameter red is
     * equal, ignoring case, to "true" or "yes" or "on". Otherwise, it returns
     * <b>false</b>.
     * <p>
     * 
     * @param file
     * @param xpath
     * @return the boolean value of the parameter.
     * @throws XMLConfigException
     */
    public static boolean getBoolean(String file, String xpath) throws XMLConfigException
    {
        try {
            String s = get(file, xpath);
            return (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on"));
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Returns a boolean parameter.
     * <p>
     * The value returned is <b>true</b> if and only if the parameter red is
     * equal, ignoring case, to "true" or "yes" or "on". Otherwise, it returns
     * <b>false</b>.
     * <p>
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * 
     * @return the searched boolean value from the configuration.
     * 
     * @throws XMLConfigException
     */
    public static boolean getBoolean(Node node, String xpath) throws XMLConfigException
    {
        try {
            String s = get(node, xpath);
            return (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on"));
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("" + exc, exc);
        }
    }

    /**
     * Returns a boolean parameter.
     * <p>
     * The value returned is <b>true</b> if and only if the parameter red is
     * equal, ignoring case, to "true" or "yes" or "on". Otherwise, it returns
     * <b>false</b>.
     * <p>
     * 
     * @param file
     * @param xpath
     * @param defaultValue
     * @return the boolean value of the parameter. If the parameter does not
     *         exists or an error occurs then the specified default value will
     *         be returned.
     */
    public static boolean getBoolean(String file, String xpath, boolean defaultValue)
    {
        try {
            return getBoolean(file, xpath);
        }
        catch (Exception exc) {
            // Do nothing
        }
        return defaultValue;
    }

    /**
     * Returns a boolean parameter.
     * <p>
     * The value returned is <b>true</b> if and only if the parameter red is
     * equal, ignoring case, to "true" or "yes" or "on". Otherwise, it returns
     * <b>false</b>.
     * <p>
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to node.
     * @param defaultValue
     *        default value
     * 
     * @return the searched value or the specified default value if the XPath
     *         select no node or an error occurs.
     */
    public static boolean getBoolean(Node node, String xpath, boolean defaultValue)
    {
        try {
            return getBoolean(node, xpath);
        }
        catch (Exception exc) {
            // Do nothing
        }
        return defaultValue;
    }

    /**
     * Checks if the given parameter exists. <br>
     * It checks that <code>get()</code> method does not return
     * <code>null</code>.
     * 
     * @param file
     * @param xpath
     * @return if the given parameter exists.
     * 
     * @see #get(java.lang.String, java.lang.String)
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static boolean exists(String file, String xpath) throws XMLConfigException
    {
        return get(file, xpath) != null;
    }

    /**
     * Checks if the given parameter exists. <br>
     * It checks that <code>get()</code> method does not return
     * <code>null</code>.
     * 
     * @param node
     * @param xpath
     * @return if the given parameter exists
     * 
     * @see #get(org.w3c.dom.Node, java.lang.String)
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static boolean exists(Node node, String xpath) throws XMLConfigException
    {
        return get(node, xpath) != null;
    }

    /**
     * Obtains a list of nodes that match the given XPath.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * @return a list of nodes that match the given XPath.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static NodeList getNodeList(String file, String xpath) throws XMLConfigException
    {

        synchronized (XMLConfig.class) {
            xpathAPI.reset();
        }

        Document doc = getDocument(file);
        try {
            synchronized (doc) {
                return (NodeList) xpathAPI.selectNodeList(doc, new XPath(xpath));
            }
        }
        catch (Throwable thr) {
            thr.printStackTrace();

            throw new XMLConfigException("XML XMLConfig error (File:" + file + ", Node:-, XPath:" + xpath + ")", thr);
        }
    }

    /**
     * Obtains a list of nodes that match the given XPath.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to the node
     * @return a list of nodes that match the given XPath.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static NodeList getNodeList(Node node, String xpath) throws XMLConfigException
    {
        synchronized (XMLConfig.class) {
            xpathAPI.reset();
        }

        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        if (node == null) {
            throw new XMLConfigException("Context node cannot be null");
        }

        try {
            synchronized (getOwnerDocument(node)) {
                return (NodeList) xpathAPI.selectNodeList(node, new XPath(xpath));
            }
        }
        catch (Throwable thr) {
            thr.printStackTrace();

            throw new XMLConfigException("XML XMLConfig error (File:-, Node:" + node.getNodeName() + ", XPath:" + xpath
                    + ")", thr);
        }
    }

    /**
     * Obtains a list of nodes that match the given XPath as a
     * <code>Collection</code>.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * @return a list of nodes that match the given XPath as a
     *         <code>Collection</code>.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static Collection<Node> getNodeListCollection(String file, String xpath) throws XMLConfigException
    {
        NodeList nl = getNodeList(file, xpath);
        Collection<Node> returnList = new ArrayList<Node>();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                returnList.add(nl.item(i));
            }
        }
        return returnList;
    }

    /**
     * Obtains a list of nodes that match the given XPath as a
     * <code>Collection</code>.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to the node
     * @return a list of nodes that match the given XPath as a
     *         <code>Collection</code>.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static Collection<Node> getNodeListCollection(Node node, String xpath) throws XMLConfigException
    {
        NodeList nl = getNodeList(node, xpath);
        Collection<Node> returnList = new ArrayList<Node>();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                returnList.add(nl.item(i));
            }
        }
        return returnList;
    }

    /**
     * Obtains a single node that matches the given XPath.
     * 
     * @param file
     *        file to read
     * @param xpath
     *        parameter to read specified as absolute path to the root of the
     *        file.
     * @return a single node that matches the given XPath.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static Node getNode(String file, String xpath) throws XMLConfigException
    {
        Document doc = getDocument(file);
        if (doc!=null) {
	        try {
	            synchronized (doc) {
	                return (Node) xpathAPI.selectSingleNode(doc, new XPath(xpath));
	            }
	        }
	        catch (Throwable thr) {
	            thr.printStackTrace();
	
	            throw new XMLConfigException("XML XMLConfig error (File:" + file + ", Node:-, XPath:" + xpath + ")", thr);
	        }
        } else {
        	return null;
        }
    }

    /**
     * Obtains a single node that matches the given XPath.
     * 
     * @param node
     *        base node for XPath
     * @param xpath
     *        parameter to read specified as relative path to the node
     * @return a single node that matches the given XPath.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static Node getNode(Node node, String xpath) throws XMLConfigException
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        if (node == null) {
            throw new XMLConfigException("Context node cannot be null");
        }

        try {
            synchronized (getOwnerDocument(node)) {
                return (Node) xpathAPI.selectSingleNode(node, new XPath(xpath));
            }
        }
        catch (Throwable thr) {
            thr.printStackTrace();

            throw new XMLConfigException("XML XMLConfig error (File:-, Node:" + node.getNodeName() + ", XPath:" + xpath
                    + ")", thr);
        }
    }

    /**
     * Add a ConfigurationListener.
     * 
     * @param listener
     */
    public static void addConfigurationListener(ConfigurationListener listener)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        try {
            EventHandler.addEventListener(listener, ConfigurationListener.class, new ConfigurationEventSelector(),
                    EVENT_SOURCE);
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Add a ConfigurationListener listening for events related to a single
     * particular file.
     * 
     * @param listener
     *        a <tt>ConfigurationListener</tt> object
     * @param filename
     *        a <tt>String</tt> containing the name of a file whose changes must
     *        be notified to the given listener
     */
    public static void addConfigurationListener(ConfigurationListener listener, String filename)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        try {
            EventHandler.addEventListener(listener, ConfigurationListener.class, new ConfigurationEventSelector(
                    filename), EVENT_SOURCE);
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Add a ConfigurationListener listening for events related to a particular
     * set of files.
     * 
     * @param listener
     *        a <tt>ConfigurationListener</tt> object
     * @param fileList
     *        a <tt>List</tt> of <tt>String</tt> s containing the name of files
     *        whose changes must be notified to the given listener
     */
    public static void addConfigurationListener(ConfigurationListener listener, List<String> fileList)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        try {
            EventHandler.addEventListener(listener, ConfigurationListener.class, new ConfigurationEventSelector(
                    fileList), EVENT_SOURCE);
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Remove a ConfigurationListener
     * 
     * @param listener
     */
    public static void removeConfigurationListener(ConfigurationListener listener)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        EventHandler.removeEventListener(listener, ConfigurationListener.class, EVENT_SOURCE);
    }

    /**
     * Remove a ConfigurationListener listening for changes on a single file
     * 
     * @param listener
     *        a <tt>ConfigurationListener</tt> object
     * @param filename
     *        a <tt>String</tt> containing the name of a file whose changes must
     *        be notified to the given listener
     */
    public static void removeConfigurationListener(ConfigurationListener listener, String filename)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        EventHandler.removeEventListener(listener, ConfigurationListener.class,
                new ConfigurationEventSelector(filename), EVENT_SOURCE);
    }

    /**
     * Remove a ConfigurationListener listening for changes on a subset of files
     * 
     * @param listener
     *        a <tt>ConfigurationListener</tt> object
     * @param fileList
     *        a <tt>List</tt> of <tt>String</tt> s containing the name of files
     *        whose changes must be notified to the given listener
     */
    public static void removeConfigurationListener(ConfigurationListener listener, List<String> fileList)
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        EventHandler.removeEventListener(listener, ConfigurationListener.class,
                new ConfigurationEventSelector(fileList), EVENT_SOURCE);
    }

    /**
     * Fires a ConfigurationEvent to all registered ConfigurationListener.
     * 
     * @param event
     *        event to fire
     * @param immediate
     *        if true the event is fired immediately
     */
    protected static synchronized void prepareConfigurationEvent(ConfigurationEvent event, boolean immediate)
    {
        isFiringEvents = true;

        configurationEvents.add(event);

        if (immediate) {
            fireConfigurationEvents();
        }
    }

    /**
     * Fires the ConfigurationEvents to all registered ConfigurationListener.
     * 
     */
    protected static synchronized void fireConfigurationEvents()
    {
        if (!isFiringEvents) {
            return;
        }

        while (!configurationEvents.isEmpty()) {
            try {
                //EventHandler.fireEventSync("configurationChanged", configurationEvents.remove(0));
                EventHandler.fireEvent("configurationChanged", configurationEvents.remove(0));
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        isFiringEvents = false;
    }

    /**
     * Return the URL to be used in order to load the given file.
     * 
     * @param file
     *        the file to read
     * @return the URL to be used in order to load the given file.
     * @exception XMLConfigException
     *            if the file could not be found.
     */
    public static synchronized URL getURL(String file) throws XMLConfigException
    {
        return getURL(file, null, false, true);
    }

    /**
     * Return the URL to be used in order to load the given file.
     * 
     * @param file
     *        the file to read
     * @param classLoader
     *        the class loader to use to retrieve the file to read
     * @param force
     *        force the reload of file if already present in cache
     * @param canBeReloaded
     *        flag that indicates if this file can be changed and can be
     *        reloaded
     * @return the URL to be used in order to load the given file.
     * @exception XMLConfigException
     *            if the file could not be found.
     */
    public static synchronized URL getURL(String file, ClassLoader classLoader, boolean force, boolean canBeReloaded)
            throws XMLConfigException
    {
        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        // It is already in cache?
        //
        URL url = urls.get(file);
        if ((url != null) && !force) {
            return url;
        }

        try {
            if ((XMLConfig.baseConfigPath != null) && !("".equals(XMLConfig.baseConfigPath))) {
                File f = new File(baseConfigPath + File.separatorChar + file);
                if (f.exists()) {
                    url = new URL("file", null, f.getAbsolutePath());
                }
            }
        }
        catch (MalformedURLException e) {
            // do nothing
        }

        if (url == null) {
            if (classLoader == null) {
                classLoader = XMLConfig.class.getClassLoader();
            }
            // Tries to obtain the URL from the class loader.
            // If not found, then throws an exception.
            //
            url = classLoader.getResource(file);
            if (url == null) {
                System.err.println("XMLConfig: file not found: " + file);
                throw new XMLConfigException("XML configuration error (File:" + file + ", Node:-, XPath:-)");
            }
        }

        // Caches the URL.
        urls.put(file, url);

        return url;
    }

    /**
     * Reads a configuration file and caches it.
     * <p>
     * The file is searched into the Java class path as another Java resource.
     * See Java class loader documentation to understand this mechanism.
     * 
     * @param file
     *        the file to read
     * @return the read configuration as {@link org.w3c.dom.Document Document}.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static synchronized Document getDocument(String file) throws XMLConfigException
    {
        return getDocument(file, null, false, true);
    }

    /**
     * Reads a configuration file and caches it.
     * <p>
     * The file is searched into the Java class path as another Java resource.
     * See Java class loader documentation to understand this mechanism.
     * 
     * @param file
     *        the file to read
     * @param classLoader
     *        the class loader to use to retrieve the file to read
     * @param force
     *        force the reload of file if already present in cache
     * @param canBeReloaded
     *        flag that indicates if this file can be changed and can be
     *        reloaded
     * @return the read configuration as {@link org.w3c.dom.Document Document}.
     * 
     * @throws XMLConfigException
     *         if some error occurs.
     */
    public static synchronized Document getDocument(String file, ClassLoader classLoader, boolean force,
            boolean canBeReloaded) throws XMLConfigException
    {
        String mainFile = file;

        if (isFiringEvents) {
            fireConfigurationEvents();
        }
        if (file == null) {
            System.err.println("Invalid argument: no file specified");

            throw new IllegalArgumentException("No file specified");
        }

        if (splitConfig == null) {
            try {
                readSplitConfig();
            }
            catch (Exception exc) {
                LOG.error("Error reading: XMLConfigSplit.properties ", exc);
                splitConfig = new HashMap<String, SplitConfig>();
                mainToSplitFile = new HashMap<String, List<String>>();
            }
        }

        Document document = documents.get(file);
        if ((document != null) && !force) {
            return document;
        }

        if (isSplitFile(file)) {
            document = splitDocuments.get(file);
            if ((document != null) && !force) {
                return document;
            }

            SplitConfig sCfg = splitConfig.get(file);
            mainFile = sCfg.getFileSrc();
        }

        URL currentUrl = getURL(mainFile, classLoader, force, canBeReloaded);
        LOG.debug("Retrieved Document URL: " + currentUrl);

        try {
            document = readDocument(currentUrl, mainFile);

            documents.put(mainFile, document);
            urls.put(mainFile, currentUrl);
            if (canBeReloaded) {
                reloadableDocuments.put(mainFile, document);

                ConfigurationEvent event = new ConfigurationEvent(ConfigurationEvent.EVT_FILE_LOADED, mainFile,
                        urls.get(mainFile));
                prepareConfigurationEvent(event, true);
            }

            if (isSplitFile(file)) {
            	
                document = initSplitFile(file, mainFile, document);
            }
            return document;
        }
        catch (Throwable thr) {
            thr.printStackTrace();

            throw new XMLConfigException("XML XMLConfig error (File:" + file + ", Node:-, XPath:-)", thr);
        }
    }

    /**
     * Checks if configuration is composed by multiple files.
     * 
     * @param document
     * @return if configuration is composed by multiple files.
     */
    public static boolean isCompositeXMLConfig(Document document)
    {
        Element root = document.getDocumentElement();
        String namespace = root.getNamespaceURI();
        String localName = root.getLocalName();

        return CONFIG_NS.equals(namespace) && "config".equals(localName);
    }

    /**
     * @param baseUrl
     * @param document
     * @return the read configuration as {@link org.w3c.dom.Document Document}
     * @throws XMLConfigException
     */
    public static synchronized Document readCompositeXMLConfig(URL baseUrl, Document document)
            throws XMLConfigException
    {
        xpathAPI.reset();

        try {
            if (!isCompositeXMLConfig(document)) {
                throw new XMLConfigException("given document is not a composite configuration");
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            if (entityResolver != null) {
                db.setEntityResolver(entityResolver);
            }

            Element root = document.getDocumentElement();

            return readCompositeXMLConfig(baseUrl, db, root);
        }
        catch (XMLConfigException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new XMLConfigException("Cannot read composite configuration", exc);
        }
    }

    /**
     * @param masterURL
     * @param xml
     * @return the input with replaced properties
     */
    public static synchronized byte[] replaceXMLProperties(URL masterURL, byte[] xml) {
        byte[] result = xml;

        if (baseProps == null) {
            try {            	
                Properties props = PropertiesFileReader.readFile("XMLConfig.properties",baseConfigPath);
                baseProps = PropertiesFileReader.propertiesToMap(props);
            }
            catch (Exception exc) {
                LOG.error("Error reading XMLConfig.properties " + exc);
                baseProps = new HashMap<String, String>();
            }
        }

        Hashtable<String, String> fullProps = new Hashtable<String, String>(baseProps);

        String file = masterURL.getPath();
        String propFile = null;
        try {
            propFile = file.substring(0, file.lastIndexOf(".xml")) + ".properties";          
            Properties props = PropertiesFileReader.readFile(propFile, null);
            fullProps.putAll(PropertiesFileReader.propertiesToMap(props));
        }
        catch (Exception exc) {
        	LOG.error("Error reading " + propFile , exc);
        }
         
        if (!fullProps.isEmpty()) {
            result = TextUtils.replacePlaceholder(new String(xml), "xmlp{{", "}}", fullProps).getBytes();
        }

        return result;
    }

    /**
     * @param baseConfigPath
     */
    public static void setBaseConfigPath(String baseConfigPath)
    {
        XMLConfig.baseConfigPath = baseConfigPath;
    }

    /**
     * @return the base configuration path
     */
    public static String getBaseConfigPath()
    {
        return XMLConfig.baseConfigPath;
    }

    private static Document readDocument(URL url, String file) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        if (entityResolver != null) {
            db.setEntityResolver(entityResolver);
        }
        Document document = readXML(url, file, db);

        Element root = document.getDocumentElement();
        String namespace = root.getNamespaceURI();
        String localName = root.getLocalName();

        if (CONFIG_NS.equals(namespace) && "config".equals(localName)) {
            return readCompositeXMLConfig(url, db, root);
        }

        return document;
    }

    private static Document readCompositeXMLConfig(URL baseUrl, DocumentBuilder db, Element config) throws Exception
    {
        NodeList ldocuments = config.getElementsByTagNameNS(CONFIG_NS, "document");
        Document document = null;
        for (int i = 0; i < ldocuments.getLength(); ++i) {
            Element documentElement = (Element) ldocuments.item(i);
            Document currentDocument = readXML(baseUrl, db, documentElement);
            if (document == null) {
                document = currentDocument;
            }
            else {
                mergeXML(document, currentDocument, documentElement);
            }
        }
        return document;
    }

    private static void mergeXML(Document global, Document current, Element currentElement) throws Exception
    {
        NodeList fragments = currentElement.getElementsByTagNameNS(CONFIG_NS, "merging");
        for (int i = 0; i < fragments.getLength(); ++i) {
            Element fragmentElement = (Element) fragments.item(i);
            mergeFragment(global, current, fragmentElement);
        }
    }

    private static void mergeFragment(Document global, Document current, Element fragmentElement) throws Exception
    {
        String src = fragmentElement.getAttribute("src");
        String dest = fragmentElement.getAttribute("dest");
        NodeList sources = (NodeList) xpathAPI.selectNodeList(current, new XPath(src));
        if (sources.getLength() > 0) {
            Node destNode = (Node) xpathAPI.selectSingleNode(global, new XPath(dest));
            for (int i = 0; i < sources.getLength(); ++i) {
                Node node = sources.item(i);
                node = global.importNode(node, true);
                destNode.appendChild(node);
            }
        }
    }

    private static Document readXML(URL baseUrl, DocumentBuilder db, Element documentElement) throws Exception
    {
        String documentName = documentElement.getAttribute("document");
        return readXML(baseUrl, db, documentName);
    }

    private static Document readXML(URL baseUrl, DocumentBuilder db, String relativeLocation) throws Exception
    {
        URL url = new URL(baseUrl, relativeLocation);
        return readXML(url, relativeLocation, db);
    }

    private static synchronized Document readXML(URL url, String file, DocumentBuilder db) throws Exception {
        LOG.debug("Reading: " + url);
        
        byte[] xml = TextUtils.readFileFromURL(url).getBytes();        
        InputStream xmlInputStream = new ByteArrayInputStream(replaceXMLProperties(url, xml));
        InputSource source = new InputSource(xmlInputStream);

        source.setSystemId(url.toString());
        Document document = db.parse(source);
        return document;
    }

    @SuppressWarnings("unchecked")
    private static void readSplitConfig() throws Exception
    {
        splitConfig = new HashMap<String, SplitConfig>();
        mainToSplitFile = new HashMap<String, List<String>>();
        
        Properties props = PropertiesFileReader.readFile("XMLConfigSplit.properties", baseConfigPath);

        Enumeration<String> dests = (Enumeration<String>) props.propertyNames();
        while (dests.hasMoreElements()) {
            String dest = dests.nextElement();
            String[] cfg = props.getProperty(dest).split("\\|");
            splitConfig.put(dest, new SplitConfig(cfg[0], cfg[1]));
            List<String> splits = mainToSplitFile.get(cfg[0]);
            if (splits == null) {
                splits = new ArrayList<String>();
                mainToSplitFile.put(cfg[0], splits);
            }
            splits.add(dest);
        }

    }

    private static boolean isSplitFile(String file)
    {
        return splitConfig.containsKey(file);
    }

    /**
     * @param file
     * @param document
     * @param sCfg
     * @return
     * @throws XMLConfigException
     */
    private static Document initSplitFile(String file, String mainFile, Document document) throws XMLConfigException
    {
        try {
            List<String> splits = mainToSplitFile.get(mainFile);

            XMLUtils xmlU = XMLUtils.getParserInstance();

            for (String split : splits) {
                SplitConfig sCfg = splitConfig.get(split);
                
                Node root = (Node) xpathAPI.selectSingleNode(document, new XPath(sCfg.getXpath()));
                if (root != null) {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document splitDoc = db.newDocument();
                    splitDoc.appendChild(splitDoc.adoptNode(root.cloneNode(true)));
                    splitDocuments.put(split, splitDoc);

                    ConfigurationEvent event = new ConfigurationEvent(ConfigurationEvent.EVT_FILE_LOADED, split,
                            urls.get(mainFile));
                    prepareConfigurationEvent(event, true);
                }
            }

            XMLUtils.releaseParserInstance(xmlU);

            return splitDocuments.get(file);
        }
        catch (Exception exc) {
            throw new XMLConfigException("XML XMLConfig error (File:" + file + ", Node:-, XPath:-)", exc);
        }
    }

    private static Document getOwnerDocument(Node node)
    {
        if (node == null) {
            return null;
        }
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) node;
        }
        return node.getOwnerDocument();
    }

}
