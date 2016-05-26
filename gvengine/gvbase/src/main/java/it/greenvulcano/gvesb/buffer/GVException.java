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
package it.greenvulcano.gvesb.buffer;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.net.URL;

import org.w3c.dom.Node;

/**
 * <code>GVException</code> is the base exception raised by GV components
 * errors.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVException extends Exception
{
    private static final long serialVersionUID   = -2627602453538726591L;

    /**
     *
     */
    public static final int   DEFAULT_ERROR_CODE = -1;

    private String            idMessage;
    private String[][]        params;
    private int               errorCode;
    private String            message;
    private String            actions;
    private String            causeDescription;
    private String            className;

    /**
     * This flag indicates if the catalog has been accessed read.
     */
    private boolean           catRead;

    /**
     * Creates a new GVException with identifier message
     * 
     * @param idMessage
     *        identifier of an error message in the error catalog
     */
    public GVException(String idMessage)
    {
        this.idMessage = idMessage;
        params = null;
        errorCode = DEFAULT_ERROR_CODE;
        catRead = false;
    }

    /**
     * Creates a new GVException with given message id and params
     * used to enrich the message.
     * 
     * @param idMessage
     *        identifier of an error message in the error catalog
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public GVException(String idMessage, String[][] params)
    {
        this.idMessage = idMessage;
        this.params = params;
        errorCode = DEFAULT_ERROR_CODE;
        catRead = false;
    }

    /**
     * Return the error code.
     * 
     * @return error code found in the error catalog.
     */
    public int getErrorCode()
    {
        if (!catRead) {
            retreiveErrorInformation();
        }
        return errorCode;
    }

    /**
     * Return the description of the error.
     * 
     * @return description found in the error catalog.
     */
    public String getDescription()
    {
        if (!catRead) {
            retreiveErrorInformation();
        }

        return causeDescription;
    }

    /**
     * Return the action.
     * 
     * @return action found in the error catalog
     */
    public String getActions()
    {
        if (!catRead) {
            retreiveErrorInformation();
        }

        return actions;
    }

    /**
     * Return the error message.
     * 
     * @return message found in the error catalog
     */
    @Override
    public String getMessage()
    {
        if (!catRead) {
            retreiveErrorInformation();
        }
        return message;
    }

    private void retreiveErrorInformation()
    {
        catRead = true;
        Node errorNode = findGVErrorNode();
        if (errorNode == null) {
            appliesDefaults();
            return;
        }
        appliesNode(errorNode);
    }

    private void appliesDefaults()
    {
        errorCode = DEFAULT_ERROR_CODE;
        message = calculateDefaultMessage();
        actions = "N/A";
        causeDescription = "N/A";
    }

    private String calculateDefaultMessage()
    {
        StringBuilder msg = new StringBuilder();

        msg.append(idMessage).append(" [").append(className).append("]: ");

        if (params != null) {
            for (int i = 0; i < params.length; ++i) {
                msg.append(params[i][0]).append("=").append(params[i][1]);
                if (i < (params.length - 1)) {
                    msg.append(", ");
                }
            }
        }
        /*Throwable cause = getCause();
        if (cause != null) {
            msg.append(", Cause=").append(cause);
        }*/
        return msg.toString();
    }

    private void appliesNode(Node errorNode)
    {
        errorCode = XMLConfig.getInteger(errorNode, "@code", DEFAULT_ERROR_CODE);
        message = XMLConfig.get(errorNode, "@message", "N/A");
        message = translateMsg(message, params);
        message = idMessage + " [" + className + "]: " + message;
        actions = XMLConfig.get(errorNode, "action", "N/A");
        causeDescription = XMLConfig.get(errorNode, "cause", "N/A");
    }

    /**
     * Find catalog for the given message id.
     * 
     * @return GVErrorNode in the error catalog
     */
    private Node findGVErrorNode()
    {

        String xPath = "/GVErrorCatalog/GVError[@id='" + idMessage + "']";

        Class<?> cls = this.getClass();
        className = cls.getName();

        Package previousPackage = null;
        while (cls != null) {
            ClassLoader loader = cls.getClassLoader();
            Package currentPackage = cls.getPackage();
            if (previousPackage != currentPackage) {
                previousPackage = currentPackage;
                String catalogName = getCatalogName(currentPackage);
                URL url;
                if (loader != null) {
                    url = loader.getResource(catalogName);
                }
                else {
                    url = ClassLoader.getSystemResource(catalogName);
                }
                try {
                    if (url != null) {
                        Node node = XMLConfig.getNode(catalogName, xPath);
                        if (node != null) {
                            return node;
                        }
                    }
                }
                catch (XMLConfigException exc) {
                    // Default message will be provided.
                }
            }
            cls = cls.getSuperclass();
        }

        return null;
    }

    /**
     * Create the package name of Errors.xml file.
     * 
     * @return fully qualified name of Errors.xml file
     */

    private String getCatalogName(Package pkg)
    {
        String packageName = pkg.getName();
        String catalogName = "catalogs/" + packageName + ".Errors.xml";
        return catalogName;
    }

    /**
     * Translates the message identified by <code>errorId</code> using the
     * values contained in <code>params</code>.
     * 
     * @param message
     * @param params
     * 
     * @return the cause, or <code>null</code> if there are not a cause.
     */
    protected String translateMsg(String message, String[][] params)
    {
        if (params != null) {
            String beginParam = "${";
            String endParam = "}";

            for (String[] param : params) {
                String paramStr = beginParam + param[0] + endParam;
                if (message.indexOf(paramStr) != -1) {
                    message = replaceParam(message, paramStr, param[1]);
                }
            }
        }
        return message;
    }

    private String replaceParam(String msg, String param, String value)
    {
        int beginIndex = msg.indexOf(param);

        String retString;

        if (beginIndex == -1) {
            retString = msg;
        }
        else {
            int endIndex = beginIndex + param.length();
            retString = msg.substring(0, beginIndex) + value + msg.substring(endIndex);
        }

        return retString;
    }
}
