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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.utils.MessageFormatter;
import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * GVThrowException class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 *
 */
public class GVThrowException
{
    private static final Logger logger          = org.slf4j.LoggerFactory.getLogger(GVThrowException.class);

    /**
     * the GVException id
     */
    private String              id     = "";
    /**
     * the exception class name
     */
    private String              className       = "";
    /**
     * the GVException parameters
     */
    private String[][]          excParameters       = null;
    /**
     * the exception Constructor(String)
     */
    private Constructor<?>      excConstr       = null;
    /**
     * the exception Constructor(String, Throwable)
     */
    private Constructor<?>      excConstrExc    = null;
    /**
     * the exception Constructor(String, String[][])
     */
    private Constructor<?>      excConstrPar    = null;
    /**
     * the exception Constructor(String, String[][], Throwable)
     */
    private Constructor<?>      excConstrParExc = null;
    /**
     * the Constructor actual parameters
     */
    private Object[]            excConstrParam  = null;

    /**
     * Initialize the instance
     *
     * @param node
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if errors occurs
     */
    public void init(Node node) throws XMLConfigException
    {
        id = XMLConfig.get(node, "@exception-id");
        className = XMLConfig.get(node, "@class");

        NodeList nl = XMLConfig.getNodeList(node, "ExceptionParam");
        if ((nl != null) && (nl.getLength() > 0)) {
            excParameters = new String[nl.getLength()][2];
            for (int i = 0; i < nl.getLength(); i++) {
                excParameters[i][0] = XMLConfig.get(nl.item(i), "@name");
                excParameters[i][1] = XMLConfig.get(nl.item(i), "@value", "");
            }
        }

        try {
            Class<?> exc = Class.forName(className);
            Class<?>[] paramDef = null;
            if (excParameters == null) {
                paramDef = new Class[]{String.class};
                excConstr = exc.getConstructor(paramDef);
                paramDef = new Class[]{String.class, Throwable.class};
                excConstrExc = exc.getConstructor(paramDef);
                excConstrParam = new Object[]{id};
            }
            else {
                paramDef = new Class[]{String.class, String[][].class};
                excConstrPar = exc.getConstructor(paramDef);
                paramDef = new Class[]{String.class, String[][].class, Throwable.class};
                excConstrParExc = exc.getConstructor(paramDef);
                excConstrParam = new Object[]{id, excParameters};
            }
        }
        catch (Exception exc) {
            logger.error("Unable to retrieve Exception constructor for class '" + className + "'", exc);
            throw new XMLConfigException("Unable to retrieve Exception constructor for class '" + className + "'", exc);
        }
    }

    /**
     * Perform the GVBuffer modification
     *
     * @param nested
     *        the nested exception
     * @param gvBuffer
     * @return the created exception
     * @throws GVException
     *         if errors occurs
     */
    public Exception execute(Throwable nested, GVBuffer gvBuffer) throws GVException
    {
        Exception exception = null;
        try {
            Object[] excConstrParamDef = null;
            if (nested == null) {
                excConstrParamDef = new Object[excConstrParam.length];
                if (excParameters == null) {
                    excConstrParamDef[0] = formatString(gvBuffer, (String) excConstrParam[0]);
                    exception = (Exception) excConstr.newInstance(excConstrParamDef);
                }
                else {
                    excConstrParamDef[0] = formatString(gvBuffer, (String) excConstrParam[0]);
                    excConstrParamDef[1] = formatString(gvBuffer, (String[][]) excConstrParam[1]);
                    exception = (Exception) excConstrPar.newInstance(excConstrParamDef);
                }
            }
            else {
                excConstrParamDef = new Object[excConstrParam.length + 1];
                if (excParameters == null) {
                    excConstrParamDef[0] = formatString(gvBuffer, (String) excConstrParam[0]);
                    excConstrParamDef[1] = nested;
                    exception = (Exception) excConstrExc.newInstance(excConstrParamDef);
                }
                else {
                    excConstrParamDef[0] = formatString(gvBuffer, (String) excConstrParam[0]);
                    excConstrParamDef[1] = formatString(gvBuffer, (String[][]) excConstrParam[1]);
                    excConstrParamDef[2] = nested;
                    exception = (Exception) excConstrParExc.newInstance(excConstrParamDef);
                }
            }
        }
        catch (Exception exc) {
            logger.error("Unable to instantiate Exception of class '" + className + "'", exc);
            throw new GVException("GVCORE_BAD_CFG");
        }

        return exception;
    }

    private String formatString(GVBuffer gvBuffer, String string) throws Exception
    {
        if (gvBuffer != null) {
            MessageFormatter formatterMessage = new MessageFormatter(string, gvBuffer);
            return formatterMessage.toString();
        }
        return string;
    }

    private String[][] formatString(GVBuffer gvBuffer, String[][] strings) throws Exception
    {
        if (gvBuffer != null) {
            String[][] out = new String[strings.length][2];
            for (int i = 0; i < strings.length; i++) {
                MessageFormatter formatterMessage = new MessageFormatter(strings[i][1], gvBuffer);
                out[i][0] = strings[i][0];
                out[i][1] = formatterMessage.toString();
            }
            return out;
        }
        return strings;
    }
}