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
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;

/**
 * This interface identifies operations used to perform dequeue.
 * <p/>
 * The <code>DequeueOperation</code> add the acknowledge() method to the
 * <code>Operation</code> interface in order to confirm consumed messages to the
 * underlying MOM.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface DequeueOperation extends Operation
{
    public static final String TYPE            = "dequeue";

    /**
     * Used in the <code>setTimeout()</code> in order to set an infinite
     * timeout.
     */
    public static final long   TO_INDEFINITELY = 0;

    /**
     * Used in the <code>setTimeout()</code> in order to set a non-blocking
     * receive.
     */
    public static final long   TO_NON_BLOCKING = -1;

    /**
     * Return <code>null</code> if no messages are available or timeout occurs.
     *
     * @param gvBuffer
     * @return a <code>GVBuffer</code> or <code>null</code> if no messages are
     *         available or timeout occurs.
     * @throws ConnectionException
     * @throws DequeueException
     * @throws InvalidDataException
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, DequeueException, InvalidDataException, InterruptedException;

    /**
     * This method acknowledge the message identified by the given Id.
     *
     * @param id
     * @throws ConnectionException
     * @throws AcknowledgeException
     */
    public void acknowledge(Id id) throws ConnectionException, AcknowledgeException;

    /**
     * This method acknowledge all the messages.
     *
     * @throws ConnectionException
     * @throws AcknowledgeException
     */
    public void acknowledgeAll() throws ConnectionException, AcknowledgeException;

    /**
     * This method roll-back the dequeue of the message identified by the given
     * Id.
     *
     * @param id
     * @throws ConnectionException
     * @throws AcknowledgeException
     */
    public void rollback(Id id) throws ConnectionException, AcknowledgeException;

    /**
     * This method roll-back the dequeue of all the messages.
     *
     * @throws ConnectionException
     * @throws AcknowledgeException
     */
    public void rollbackAll() throws ConnectionException, AcknowledgeException;

    /**
     * This method set the filter for messages to receive. <br>
     * This filter is valid only for the first perform() invocation.
     * The filter is reset also if perform() terminate with an exception. <br>
     * Despite the GVVCL is a virtual interface, the syntax for the filter is
     * like the syntax for JMS selectors. The concrete implementation must
     * adapt to the underlying MOM.
     *
     * @param filter
     * @throws FilterException
     */
    public void setFilter(String filter) throws FilterException;

    /**
     * This method set the timeout for the receive operation. <br>
     * If 0 then the receive blocks indefinitely. If <nobr>&gt;
     * 0</nobr> then this parameter specify, in milliseconds, the time that the
     * receive must wait for a message. If <nobr>&lt; 0</nobr> then executes a
     * non-blocking receive. <br>
     * This parameter is valid only for the first perform() invocation.
     * The timeout is reset also if perform() terminate with an exception.
     *
     * @param timeout
     *        Specify the receive timeout. If <nobr>&gt; 0</nobr> specifies the
     *        milliseconds for timeout. It is possible to use the
     *        <code>TO_xxx</code> constants in order to specify a non-blocking
     *        receive operation or a indefinitely blocking receive.
     *
     * @see #TO_INDEFINITELY
     * @see #TO_NON_BLOCKING
     */
    public void setTimeout(long timeout);
}
