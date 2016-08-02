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
package it.greenvulcano.gvesb.statistics;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.statistics.datawrapper.ExtendedDataWrapper;
import it.greenvulcano.gvesb.statistics.plugin.DUMMYStatisticsWriter;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class get some statistics informations for every GreenVulcano ESB
 * service invoked and store it.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */

public class StatisticsDataManager implements ConfigurationListener
{
    private static Logger   logger                = LoggerFactory.getLogger(StatisticsDataManager.class);
    public static final String                      DEFAULT_CONF_FILE_NAME = "GVStatisticsConfig.xml";

    private IStatisticsWriter                       statisticsWriter       = null;
    private boolean                                 confChangedFlag        = false;

    private static Map<String, ExtendedDataWrapper> extendedDataWrappers   = null;

    /**
     */
    public StatisticsDataManager()
    {
        XMLConfig.addConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
    }

    /**
     * 
     * @throws GVStatisticsException
     */
    public void init() throws GVStatisticsException
    {
        try {
            Node swNode = XMLConfig.getNode(DEFAULT_CONF_FILE_NAME,
                    "/GVStatisticsConfig/Writers/*[@type='stat-writer' and @id=/GVStatisticsConfig/Writers/@default]");
            
            if (swNode!=null) {            
	            statisticsWriter = (IStatisticsWriter) Class.forName(XMLConfig.get(swNode, "@class")).newInstance();
	            statisticsWriter.init(swNode);
	            initExtDataWrappers();
	            
            } else {
            	statisticsWriter = new DUMMYStatisticsWriter();
            }
            confChangedFlag = false;
        }
        catch (GVStatisticsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new GVStatisticsException("Error initializing GVStatisticsDataManager", exc);
        }
    }

    /**
     * Gets the extended data wrapper for the given system / service pair.
     * 
     * @param systemServicePair
     *        the system / service pair.
     * 
     * @return the extended data wrapper
     */
    public static ExtendedDataWrapper getWrapper(String systemServicePair)
    {
        ExtendedDataWrapper wrapper = null;
        if (extendedDataWrappers != null) {
            wrapper = extendedDataWrappers.get(systemServicePair);
        }
        return wrapper;
    }


    /**
     * This method get the following statistics information : <li>startTime</li>
     * 
     * @param gvBuffer
     *        GVBuffer object
     * @param packageName
     *        The GreenVulcanoESB package
     * @param processName
     *        The process name into the package
     * @return statisticsData StatisticsData object
     */
    public StatisticsData startStatistics(GVBuffer gvBuffer, String packageName, String processName)
    {
        StatisticsData statisticsData = startStatistics(gvBuffer);
        if (packageName != null) {
            statisticsData.setPackageName(packageName);
        }
        if (processName != null) {
            statisticsData.setProcessName(processName);
        }
        return statisticsData;
    }

    /**
     * This method get the following statistics information : <li>startTime</li>
     * 
     * @param gvBuffer
     *        GVBuffer object
     * @param processName
     *        process name
     * @return statisticsData StatisticsData object
     */
    public StatisticsData startStatistics(GVBuffer gvBuffer, String processName)
    {
        StatisticsData statisticsData = startStatistics(gvBuffer);
        if (processName != null) {
            statisticsData.setProcessName(processName);
        }
        return statisticsData;
    }

    /**
     * This method get the following statistics information : <li>startTime</li>
     * 
     * @param gvBuffer
     *        GVBuffer object
     * @return statisticsData StatisticsData object
     */
    public StatisticsData startStatistics(GVBuffer gvBuffer)
    {
        logger.debug("Start Statistics.");
        if (confChangedFlag) {
            try {
                init();
            }
            catch (Exception exc) {
                logger.error("Error reloading the configuration from configuration file '" + DEFAULT_CONF_FILE_NAME
                        + "'.", exc);
            }
        }
        long currentTime = System.currentTimeMillis();
        StatisticsData statisticsData = new StatisticsData(gvBuffer);
        statisticsData.setStartTime(currentTime);
        logger.debug("Start Statistics, StatisticsData is: " + statisticsData.toString());
        return statisticsData;
    }

    /**
     * This method get the following statistics information : <li>stopTime of
     * GreenVulcanoESB process</li> The time from the Client request and the
     * client getReply
     * 
     * @param statisticsData
     *        StatisticsData object
     * @param gvBuffer
     *        GVBuffer object
     * @param status
     *        the status of service. Terminated with SUCCESS, FAILURE or
     *        UNDEFINED.
     * @throws GVStatisticsException
     */
    public void stopStatistics(StatisticsData statisticsData, GVBuffer gvBuffer, int status)
            throws GVStatisticsException
    {
        if (status != StatisticsData.SERVICE_STATUS_UNDEFINED) {
            statisticsData.setServiceStatus(status);
            int retCode = gvBuffer.getRetCode();
            statisticsData.setErrorCode(retCode);
        }
        stopStatistics(statisticsData, gvBuffer);
    }

    /**
     * This method get the following statistics information : <li>stopTime of
     * GreenVulcanoESB process</li> The time from the Client request and the
     * client getReply
     * 
     * @param statisticsData
     *        StatisticsData object
     * @param gvBuffer
     *        GVBuffer object
     * @throws GVStatisticsException
     */
    public void stopStatistics(StatisticsData statisticsData, GVBuffer gvBuffer) throws GVStatisticsException
    {
        logger.debug("Stop Statistics with success.");
        if (gvBuffer != null) {
            statisticsData.setID(gvBuffer.getId());
        }
        long currentTime = System.currentTimeMillis();
        statisticsData.setStopTime(currentTime);
        logger.debug("Stop Statistics with success. StatisticsData is: " + statisticsData.toString());
        storeStatisticsData(statisticsData);
    }

    /**
     * This method get the following statistics information : <li>stopTime</li>
     * The time from the Client request and the client getReply
     * 
     * @param statisticsData
     *        StatisticsData object
     * @param gvException
     *        GVException object
     * @param status
     *        the status of service. Terminated with SUCCESS, FAILURE or
     *        UNDEFINED.
     * @throws GVStatisticsException
     */
    public void stopStatistics(StatisticsData statisticsData, GVException gvException, int status)
            throws GVStatisticsException
    {
        if (status != StatisticsData.SERVICE_STATUS_UNDEFINED) {
            statisticsData.setServiceStatus(status);
            int errorCode = gvException.getErrorCode();
            statisticsData.setErrorCode(errorCode);
        }
        stopStatistics(statisticsData, gvException);
    }

    /**
     * This method get the following statistics information : <li>stopTime</li>
     * The time from the Client request and the client getReply
     * 
     * @param statisticsData
     *        StatisticsData object
     * @param gvException
     *        GVException object
     * @throws GVStatisticsException
     */
    public void stopStatistics(StatisticsData statisticsData, GVException gvException) throws GVStatisticsException
    {
        logger.debug("Stop Statistics with exception.");
        long currentTime = System.currentTimeMillis();
        statisticsData.setStopTime(currentTime);
        logger.debug("Stop Statistics with exception. StatisticsData is: " + statisticsData.toString());
        storeStatisticsData(statisticsData);
    }

    /**
     * This method the writer methods of the WriterPlugIn.
     * 
     * @param statisticsData
     *        StatisticsData object
     */
    private void storeStatisticsData(StatisticsData statisticsData) throws GVStatisticsException
    {
        statisticsWriter.writeStatisticsData(statisticsData);
    }

    /**
     * This method implements the hot reloading method.
     * 
     * @param evt
     *        ConfigurationEvent object
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        logger.debug("BEGIN - Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(DEFAULT_CONF_FILE_NAME)) {
            confChangedFlag = true;
            if (extendedDataWrappers != null) {
                extendedDataWrappers.clear();
                extendedDataWrappers = null;
            }
        }
        logger.debug("END - Operation(reload Configuration)");
    }

    /**
     * Destroy the internal objects
     */
    public void destroy()
    {
        XMLConfig.removeConfigurationListener(this, DEFAULT_CONF_FILE_NAME);
        if (statisticsWriter != null) {
            statisticsWriter.destroy();
        }
        statisticsWriter = null;
    }

    /**
     * Initializes extended data wrapper.
     * 
     * @throws GVStatisticsException
     *         if an error occurs.
     */
    private static synchronized void initExtDataWrappers() throws GVStatisticsException
    {
        if (extendedDataWrappers == null) {
            extendedDataWrappers = new Hashtable<String, ExtendedDataWrapper>();
            try {
                NodeList extDataList = XMLConfig.getNodeList(DEFAULT_CONF_FILE_NAME,
                        "/GVStatisticsConfig/ExtendedDataMappings/ExtendedData");
                for (int i = 0; i < extDataList.getLength(); i++) {
                    Node extData = extDataList.item(i);
                    String system = XMLConfig.get(extData, "@system");
                    String service = XMLConfig.get(extData, "@service");
                    logger.debug("Setting extended data for: " + system + "::" + service);
                    extendedDataWrappers.put(system + "::" + service, new ExtendedDataWrapper(extData));
                }
            }
            catch (Exception exc) {
                logger.error("Error loading Extended Data Wrappers", exc);
                throw new GVStatisticsException("GVSTATISTICS_CONFIG_ERROR", new String[][]{
                        {"cause", "for Extended Data Wrappers."}, {"exception", exc.getMessage()}}, exc);
            }
        }
    }

}