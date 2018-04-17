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
package it.greenvulcano.configuration;

/**
 * Exception raised by configuration classes. This exception is a
 * <code>RuntimeException</code>, so you NEED NOT to catch
 * <code>XMLConfigException</code>, but you are encouraged to do this.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XMLConfigException extends Exception
{
    /**
     *
     */
    private static final long serialVersionUID = 300L;

    /**
     * Creates a new XMLConfigException with the given message.
     *
     * @param msg
     *        Message associated to the exception
     */
    public XMLConfigException(String msg)
    {
        super(msg);
    }
    
    public XMLConfigException(String msg, Exception cause){
        super(msg, cause);
    }

    /**
     * Creates a new XMLConfigException with a message and a cause.
     *
     * @param msg
     *        Message associated to the exception
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public XMLConfigException(String msg, Throwable exc)
    {
        super(msg, exc);
    }

    /**
     * Returns a string representing the exception description.
     *
     * @return the exception description
     */
    @Override
    public final String toString()
    {
        Throwable throwable = getCause();
        if (throwable == null) {
            return "XMLConfigException[" + getMessage() + "]";
        }
        return "XMLConfigException[" + getMessage() + ", " + throwable + "]";
    }

}
