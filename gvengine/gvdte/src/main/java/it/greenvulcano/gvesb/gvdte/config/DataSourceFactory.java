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
package it.greenvulcano.gvesb.gvdte.config;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.util.UtilsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class acts as a factory of Java objects implementing the DataSource
 * interface. It instantiates the right object for a resource repository located
 * on a specific medium based on elements read from the main configuration file
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DataSourceFactory
{
    private static final Logger                  logger                 = org.slf4j.LoggerFactory.getLogger(DataSourceFactory.class);

    private String                               configurationFileName  = "";

    private Map<String, Map<String, DataSource>> dataSourcesSets        = new HashMap<String, Map<String, DataSource>>();

    private boolean                              configChangeFlag       = false;

    public DataSourceFactory()
    {
        // do nothing
    }

    private void init() throws UtilsException
    {
        logger.debug("Loading Configuration");

        try {
            logger.debug("Loading DataSourceSet NodeList...");
            NodeList dataSourceSetNodes = XMLConfig.getNodeList(configurationFileName, "/GVDataTransformation/DataSourceSets/DataSourceSet");

            for (int i = 0; i < dataSourceSetNodes.getLength(); i++) {
                Node dataSourceSetNode = dataSourceSetNodes.item(i);
                String dataSourceName = XMLConfig.get(dataSourceSetNode, "@name");
                Map<String, DataSource> dataSourcesMap = getDataSourcesMap(dataSourceSetNode);
                dataSourcesSets.put(dataSourceName, dataSourcesMap);
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Can't initialize DataSourceFactory: ", exc);
            throw new UtilsException("GVDTE_XML_CONFIG_ERROR", exc);
        }
        catch (UtilsException exc) {
            throw exc;
        }
    }

    private Map<String, DataSource> getDataSourcesMap(Node node) throws UtilsException
    {
        String className = "";
        String dataSourceName = "";

        try {
            String dataSourceSetName = XMLConfig.get(node, "@name");
            logger.debug("Loading DataSource NodeList for DataSourceSet '" + dataSourceSetName + "'");
            NodeList dataSourceNodes = XMLConfig.getNodeList(node, "*[@type='datasource']");
            Map<String, DataSource> dataSourcesMap = new HashMap<String, DataSource>();

            for (int i = 0; i < dataSourceNodes.getLength(); i++) {
                Node dataSourceNode = dataSourceNodes.item(i);
                dataSourceName = XMLConfig.get(dataSourceNode, "@name");
                String formatHandled = XMLConfig.get(dataSourceNode, "@formatHandled");

                if ((formatHandled == null) || (formatHandled.trim().equals(""))) {
                    throw new UtilsException("GVDTE_DATASOURCE_FORMAT_ERROR", new String[][]{{
                            "format",
                            "formatHandled attribute for node " + dataSourceNode.getNodeName() + " (dataSource:"
                                    + dataSourceName + ") must be present and not empty "}});
                }

                List<String> formatHdlList = getFormatHandled(formatHandled);

                className = XMLConfig.get(dataSourceNode, "@class");

                DataSource theDataSource = (DataSource) Class.forName(className).newInstance();
                theDataSource.init(dataSourceNode);

                for (String format : formatHdlList) {
                    dataSourcesMap.put(format, theDataSource);
                }
            }
            logger.debug("DataSource NodeList loaded for DataDourceSet '" + dataSourceSetName + "'");
            return dataSourcesMap;
        }
        catch (XMLConfigException exc) {
            logger.error("Can't initialize DataSourceFactory: ", exc);
            throw new UtilsException("GVDTE_XML_CONFIG_ERROR", exc);
        }
        catch (ClassNotFoundException exc) {
            throw new UtilsException("GVDTE_CLASS_NOT_FOUND", new String[][]{{"className", className},
                    {"cause", "DataSource: " + dataSourceName}}, exc);
        }
        catch (IllegalAccessException exc) {
            throw new UtilsException("GVDTE_ACCESS_ERROR", new String[][]{{"className", className},
                    {"msg", "DataSource: " + dataSourceName}}, exc);
        }
        catch (InstantiationException exc) {
            throw new UtilsException("GVDTE_INSTANCE_ERROR", new String[][]{{"className", className},
                    {"cause", "DataSource: " + dataSourceName}}, exc);
        }
    }

    /**
     * The method returns a object implementing DataSource interface.
     *
     * @param resourceName
     *        resource type handled from the desired Datasource
     * @return the object implementing the {@link DataSource} interface for the
     *         specific resource
     * @throws UtilsException
     */
    public DataSource getDataSource(String resourceName) throws UtilsException
    {
        return getDataSource("Default", resourceName);
    }

    /**
     * The method returns a object implementing DataSource interface.
     *
     * @param dataSourceSet
     *        the DataSourceSet holding the DataSource
     * @param resourceName
     *        resource type handled from the desired Datasource
     * @return the object implementing the {@link DataSource} interface for the
     *         specific resource
     * @throws UtilsException
     */
    public DataSource getDataSource(String dataSourceSet, String resourceName) throws UtilsException
    {
        if (configChangeFlag) {
            logger.debug("getDataSource - configuration is changed, the internal chache is removed");
            configurationClean();
            logger.debug("getDataSource - reload all DataSources");
            init();
        }

        String format = resourceName.substring(resourceName.lastIndexOf(".") + 1);
        Map<String, DataSource> dataSourcesMap = dataSourcesSets.get(dataSourceSet);
        if (dataSourcesMap == null) {
            throw new UtilsException("GVDTE_DATASOURCE_FORMAT_ERROR", new String[][]{{"format", format},
                    {"set", dataSourceSet}});
        }
        DataSource ds = dataSourcesMap.get(format);
        if (ds == null) {
            throw new UtilsException("GVDTE_DATASOURCE_FORMAT_ERROR", new String[][]{{"format", format},
                    {"set", dataSourceSet}});
        }
        return ds;
    }

    /**
     * @param name
     * @throws UtilsException
     */
    public void setMainConfigurationName(String name) throws UtilsException
    {
        if ((name == null) || (name.trim().equals(""))) {
            throw new UtilsException("GVDTE_INVALID_CONFIGURATION_FILE", new String[][]{{"cause",
                    "Name parameter can't be null or empty"}});
        }
        configurationFileName = name;
        configurationClean();
        init();
    }

    /**
     * @return the configuration file name
     */
    public String getMainConfigurationName()
    {
        return configurationFileName;
    }

    /**
     * Returns an IConfigLoader object that can load GVDTE Configuration
     * informations from a string source storing them in the format specified by
     * the String configInfoFormat. The IConfigLoader object can be browsed
     * using XPath
     *
     * @param dataSource
     * @return an {@link IConfigLoader} object that can load GVDTE Configuration
     *         informations
     * @throws UtilsException
     */
    public IConfigLoader getConfigLoader(DataSource dataSource) throws UtilsException
    {
        String handlerClassname = dataSource.getFormatHandlerClass();
        try {
            if ((handlerClassname != null) && !handlerClassname.equals("")) {
                IConfigLoader handler = (IConfigLoader) Class.forName(handlerClassname).newInstance();

                return handler;
            }
            throw new UtilsException("GVDTE_DATASOURCE_HANDLER_CLASS_UNKNOWN", new String[][]{
                    {"className", handlerClassname}, {"dataSourceName", dataSource.getName()}});
        }
        catch (ClassNotFoundException exc) {
            throw new UtilsException("GVDTE_DATASOURCE_HANDLER_CLASS_NOT_FOUND", new String[][]{
                    {"className", handlerClassname}, {"dataSourceName", dataSource.getName()}}, exc);
        }
        catch (IllegalAccessException exc) {
            throw new UtilsException("GVDTE_ACCESS_ERROR", new String[][]{{"className", handlerClassname},
                    {"msg", "DataSource: " + dataSource.getName()}}, exc);
        }
        catch (InstantiationException exc) {
            throw new UtilsException("GVDTE_INSTANCE_ERROR", new String[][]{{"className", handlerClassname},
                    {"cause", "DataSource: " + dataSource.getName()}}, exc);
        }
    }

    /**
     * Returns an IConfigLoader object that can load GVDTE Configuration
     * informations from a string source storing them in the format specified by
     * the String configInfoFormat. The IConfigLoader object can be browsed
     * using XPath
     *
     * @param dataSourceSet
     * @param resourceName
     * @return an {@link IConfigLoader} object that can load GVDTE Configuration
     *         informations
     * @throws UtilsException
     */
    public IConfigLoader getConfigLoader(String dataSourceSet, String resourceName) throws UtilsException
    {
        try {
            DataSource ds = getDataSource(dataSourceSet, resourceName);
            String rs = ds.getResourceAsString(resourceName);
            IConfigLoader cl = getConfigLoader(ds);
            cl.init(rs);
            return cl;
        }
        catch (ConfigException exc) {
            throw new UtilsException("GVDTE_CREATE_ERROR",
                    new String[][]{{"obj", "DataSource:  '" + resourceName + "'"}}, exc);
        }
    }

    private List<String> getFormatHandled(String formatList)
    {
        StringTokenizer strTok = new StringTokenizer(formatList, ",");
        List<String> formatHdlList = new ArrayList<String>();
        while (strTok.hasMoreTokens()) {
            formatHdlList.add(strTok.nextToken());
        }

        return formatHdlList;
    }


    /**
     * Configuration cleanup.
     */
    public void configurationClean()
    {
        logger.debug("BEGIN - Configuration cleanup");
        dataSourcesSets.clear();
        configChangeFlag = false;
        logger.debug("END - Configuration cleanup");
    }
}
