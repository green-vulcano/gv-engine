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
package it.greenvulcano.gvesb.statistics.utils;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.statistics.GVStatisticsException;
import it.greenvulcano.gvesb.statistics.IStatisticsWriter;
import it.greenvulcano.gvesb.statistics.StatisticsData;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;

import java.io.Serializable;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */

public class JMSStatisticsMessageListener implements ConfigurationListener, MessageListener
{
    private static Logger   logger                = LoggerFactory.getLogger(JMSStatisticsMessageListener.class);

    private IStatisticsWriter         statisticsWriter       = null;
    private String                    targetWriter           = null;
    private boolean                   confChangedFlag        = false;

    /**
     *
     * @param targetWriter
     */
    public JMSStatisticsMessageListener(String targetWriter)throws Exception
    {
        this.targetWriter = targetWriter;
        init();
        XMLConfig.addConfigurationListener(this, StatisticsDataManager.DEFAULT_CONF_FILE_NAME);
    }

    /**
     * @param msg
     * @throws GVStatisticsException
     */
    @Override
    public void onMessage(Message msg)
    {
        try {
            if (confChangedFlag) {
                init();
            }
            if (msg instanceof ObjectMessage) {
                logger.debug("HandleMessage Start");
                Serializable object = ((ObjectMessage) msg).getObject();
                if (object instanceof StatisticsData) {
                    StatisticsData statisticsData = (StatisticsData) object;
                    logger.debug("StatisticsData Received: " + statisticsData.toString());
                    statisticsWriter.writeStatisticsData(statisticsData);
                }
                else {
                    logger.error("MessageObject Received is not a StatisticsData object: " + object.getClass());
                }
            }
        }
        catch (Exception exc) {
            logger.error("An error occurred when processing JMS Statistics Message.", exc);
            throw new RuntimeException("An error occurred when processing JMS Statistics Message.", exc);
        }
    }

    /**
     * This method implements the hot reloading method.
     *
     * @param evt
     *        ConfigurationEvent object
     */
    public void configurationChanged(ConfigurationEvent evt)
    {
        logger.debug("BEGIN - Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) &&
            (evt.getFile().equals(StatisticsDataManager.DEFAULT_CONF_FILE_NAME))) {
            confChangedFlag = true;
        }
        logger.debug("END - Operation(reload Configuration)");
    }

    /**
     *
     */
    public void destroy() {
        XMLConfig.removeConfigurationListener(this);
        statisticsWriter.destroy();
    }

    /**
     *
     * @throws GVStatisticsException
     */
    private void init() throws GVStatisticsException
    {
        try {
            Node writerNode = XMLConfig.getNode(StatisticsDataManager.DEFAULT_CONF_FILE_NAME,
                    "/GVStatisticsConfig/Writers/*[@type='stat-writer' and @id='" + targetWriter + "']");
            statisticsWriter = (IStatisticsWriter) Class.forName(XMLConfig.get(writerNode, "@class")).newInstance();
            statisticsWriter.init(writerNode);
            confChangedFlag = false;
        }
        catch (Exception exc) {
            logger.error("Error initializing JMSStatisticsMessageListener", exc);
            throw new GVStatisticsException("GVSTATISTICS_CONFIG_ERROR", new String[][]{
                    {"cause", "for JMSStatisticsMessageListener"}, {"exception", exc.getMessage()}});
        }
    }

}