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
package it.greenvulcano.gvesb.virtual.j2ee;

import it.greenvulcano.gvesb.j2ee.XAHelperException;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * JMSDataXASynchronization class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author Gianluca Di Maio
 *
 *
 *
 */
public class JMSDataXASynchronization implements Synchronization
{
	private static final Logger logger = LoggerFactory.getLogger(JMSConnectionManager.class);

    private JMSData             jmsData     = null;
    private Transaction         transaction = null;

    /**
     * Constructor
     *
     * @param jmsData
     *        the JMSData instance to handle
     * @throws XAHelperException
     */
    public JMSDataXASynchronization(JMSData jmsData) throws XAHelperException
    {
        this.jmsData = jmsData;
        transaction = jmsData.getXAHelper().getTransaction();
    }

    /**
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    public void beforeCompletion()
    {
        // TODO Auto-generated method stub
    }

    /**
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    public void afterCompletion(int status)
    {
        logger.debug("Closing Transaction on JMSData " + jmsData);
        JMSConnectionManager.instance().xaReleaseConnection(this);
        logger.debug("Closed Transaction on JMSData " + jmsData);
    }

    /**
     * @return Returns the jmsData.
     */
    public JMSData getJmsData()
    {
        return jmsData;
    }

    /**
     * @return Returns the transaction.
     */
    public Transaction getTransaction()
    {
        return transaction;
    }

}
