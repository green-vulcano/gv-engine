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
package it.greenvulcano.gvesb.internal.condition;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ExceptionDef
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(ExceptionDef.class);
    /**
     * The exception class to check
     */
    private String              exceptionClass   = "";
    /**
     * If true execute a strict class check on 'exceptionClass'
     */
    private boolean             strictClassCheck = false;
    /**
     * If true the check are performed also on chained exception
     */
    private boolean             followExcChain   = false;
    /**
     * Lower bound for exception code check
     */
    private int                 minCode;
    /**
     * Upper bound for exception code check
     */
    private int                 maxCode;
    /**
     * The filter to apply on exception message
     */
    private String              messageFilter    = "";
    /**
     * If true the filter is a regular expression
     */
    private boolean             filterIsRegExp   = false;
    /**
     * The regular expression
     */
    private Pattern             filterPattern    = null;
    /**
     * The exception class
     */
    private Class<?>            exception        = null;

    /**
     * Constructor
     */
    public ExceptionDef()
    {
        minCode = Integer.MIN_VALUE;
        maxCode = Integer.MIN_VALUE;
    }

    /**
     * Initialize the instance
     *
     * @param node
     *        the XML node containing configuration data
     * @throws XMLConfigException
     *         if @min-code > @max-code
     */
    public void init(Node node) throws XMLConfigException
    {
        exceptionClass = XMLConfig.get(node, "@exception-class", "");
        strictClassCheck = XMLConfig.getBoolean(node, "@strict-class-check", false);
        followExcChain = XMLConfig.getBoolean(node, "@follow-exc-chain", false);
        minCode = XMLConfig.getInteger(node, "@min-code", Integer.MIN_VALUE);
        maxCode = XMLConfig.getInteger(node, "@max-code", Integer.MAX_VALUE);
        messageFilter = XMLConfig.get(node, "@message-filter", "");
        filterIsRegExp = XMLConfig.get(node, "@filter-type", "text").equals("reg-exp");

        if (minCode > maxCode) {
            throw new XMLConfigException("@min-code must be < @max-code for node: " + XPathFinder.buildXPath(node));
        }
        if (!exceptionClass.equals("")) {
            try {
                exception = Class.forName(exceptionClass);
            }
            catch (Exception exc) {
                logger.error("Unable to retrieve Exception of class '" + exceptionClass + "'", exc);
                throw new XMLConfigException("EBCORE_BAD_CFG", exc);
            }
        }
        if (strictClassCheck && (exception == null)) {
            throw new XMLConfigException("@strict-class-check can be 'yes' only if 'exception-class' is valid. Node: "
                    + XPathFinder.buildXPath(node));
        }
        if (!messageFilter.equals("") && filterIsRegExp) {
            try {
                filterPattern = Pattern.compile(messageFilter);
            }
            catch (Exception exc) {
                logger.error("Bad regular expression '" + messageFilter + "'", exc);
                throw new XMLConfigException("EBCORE_BAD_CFG", exc);
            }
        }
    }

    /**
     * Perform the check
     *
     * @param exc
     *        the Exception to compare
     * @return true if the exception type match this object configuration
     */
    public boolean match(Exception exc)
    {
        boolean bExceptionClass = false;
        boolean bExceptionCode = false;
        boolean bMessageFilter = false;

        if (exc == null) {
            return false;
        }
        if (exception == null) {
            bExceptionClass = true;
        }
        else {
            if (strictClassCheck) {
                bExceptionClass = exc.getClass().getName().equals(exceptionClass);
            }
            else {
                bExceptionClass = exception.isInstance(exc);
            }
        }
        if (GVException.class.isInstance(exc)) {
            int code = ((GVException) exc).getErrorCode();
            if ((minCode <= code) && (code <= maxCode)) {
                bExceptionCode = true;
            }
        }
        else {
            bExceptionCode = true;
        }
        if (messageFilter.equals("")) {
            bMessageFilter = true;
        }
        else {
            String excMessage = exc.getMessage();
            if (filterIsRegExp) {
                Matcher matcher = filterPattern.matcher(excMessage);
                bMessageFilter = matcher.lookingAt();
            }
            else {
                bMessageFilter = (excMessage.indexOf(messageFilter) != -1);
            }
        }

        boolean result = (bExceptionClass && bExceptionCode && bMessageFilter);

        if (!result && followExcChain) {
            Throwable throwable = exc.getCause();
            if (throwable != null) {
                result = match((Exception) throwable);
            }
        }

        return result;
    }

    /**
     * @return a string representing the object description
     */
    @Override
    public String toString()
    {
        return "ExceptionDef - exceptionClass: '" + exceptionClass + "' - strictClassCheck: '" + strictClassCheck
                + "' - followExcChain: '" + followExcChain + "' - messageFilter: '" + messageFilter
                + "' - filterIsRegExp: '" + filterIsRegExp + " - range [" + minCode + "," + maxCode + "]";
    }
}