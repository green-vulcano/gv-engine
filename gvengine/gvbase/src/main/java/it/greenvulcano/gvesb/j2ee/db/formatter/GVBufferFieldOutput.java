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
package it.greenvulcano.gvesb.j2ee.db.formatter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.formatter.base.BaseFormatter;
import it.greenvulcano.gvesb.j2ee.db.formatter.base.BaseFormatterFactory;
import it.greenvulcano.gvesb.j2ee.db.utils.ParameterType;
import java.lang.reflect.Method;
import java.sql.CallableStatement;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class encapsulates informations concerning a parameter of CallSPFormatter.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVBufferFieldOutput
{
    private static final Logger logger        = org.slf4j.LoggerFactory.getLogger(GVBufferFieldOutput.class);

    /**
     * GVBuffer attribute considered
     */
    private String              fieldName     = null;

    /**
     * Type of GVBuffer attribute considered
     */
    private String              type          = null;

    /**
     * Method used to set the GVBuffer attribute considered
     */
    private String              setterMethod  = null;

    /**
     * Property to set
     */
    private String              property      = null;

    /**
     * BaseFormatter used to format Stored Procedure output parameter
     */
    private BaseFormatter       baseFormatter = null;


    /**
     * Constructor
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public GVBufferFieldOutput(Node node) throws GVDBException
    {
        logger.debug("Constructor start");

        try {
            fieldName = XMLConfig.get(node, "@name");
            logger.debug("Field name: " + fieldName);
            type = XMLConfig.get(node, "@type");
            logger.debug("Field type : " + type);
            setterMethod = XMLConfig.get(node, "@setterMethod");
            logger.debug("Method to use : " + setterMethod);
            property = XMLConfig.get(node, "@property");
            logger.debug("Property to set : " + property);
            baseFormatter = BaseFormatterFactory.create(XMLConfig.getNode(node, "*[@type='base_formatter']"));
        }
        catch (XMLConfigException exc) {
            logger.error("init - Error while accessing configuration informations via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg",
                    "Error while accessing configuration informations via XMLConfig" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg", exc.toString()}}, exc);
        }
    }

    /**
     * @param callStmt
     *        The value to set.
     * @param gvBuffer
     *        the GVBuffer object
     * @throws GVDBException
     *         if an error occurred
     */
    public void set(CallableStatement callStmt, GVBuffer gvBuffer) throws GVDBException
    {
        Method method = null;
        Object[] argumentValue = null;
        Class<?>[] argumentDef = null;
        int iNext = 0;

        try {
            Object objValue = baseFormatter.getValue(callStmt, type);
            if (objValue != null) {
                logger.debug("objValue: " + objValue + " objValue class: " + objValue.getClass());
            }
            if (getProperty() != null) {
                argumentValue = new Object[2];
                argumentValue[iNext] = getProperty();
                argumentDef = new Class[2];
                argumentDef[iNext] = String.class;
                iNext++;
            }
            else {
                argumentValue = new Object[1];
                argumentDef = new Class[1];
            }

            if (type.equals(ParameterType.JAVA_STRING)) {
                argumentDef[iNext] = String.class;
            }
            else if (type.equals(ParameterType.JAVA_LONG)) {
                argumentDef[iNext] = Long.TYPE;
            }
            else if (type.equals(ParameterType.JAVA_INT)) {
                argumentDef[iNext] = Integer.TYPE;
            }
            else if (type.equals(ParameterType.JAVA_BYTE_ARRAY)) {
                argumentDef[iNext] = Object.class;
            }
            else {
                logger.error("set - Error while setting parameters for GVBuffer method: paramter type not supported");
                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                        {"msg", "Error while setting parameters for GVBuffer method: paramter type not supported"},
                        {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                        {"id", gvBuffer.getId().toString()}}, null);

            }
            argumentValue[iNext] = objValue;
            if (setterMethod.equals("setObject")) {
                argumentDef[iNext] = Object.class;
                if (! ((objValue instanceof byte[]) || (objValue instanceof String))) {
                    argumentValue[iNext] = "" + objValue;
                }
            }
            method = (gvBuffer.getClass()).getMethod(setterMethod, argumentDef);
            logger.debug("object method : " + method);
            method.invoke(gvBuffer, argumentValue);
        }
        catch (Throwable exc) {
            logger.error("set - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"msg", exc.toString()},
                    {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                    {"id", gvBuffer.getId().toString()}}, exc);
        }
    }

    /**
     * Set the GVBuffer field considered.
     *
     * @param fieldName
     *        The value to set.
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * Set the type.
     *
     * @param type
     *        The value to set.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Set the GVBuffer method used to set the field considered.
     *
     * @param setterMethod
     *        The value to set.
     */
    public void setSetterMethod(String setterMethod)
    {
        this.setterMethod = setterMethod;
    }

    /**
     * Set the GVBuffer Property to set.
     *
     * @param property
     *        The value to set.
     */
    public void setProperty(String property)
    {
        this.property = property;
    }

    /**
     * Get the GVBuffer field name.
     *
     * @return the field name
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * Get the type attribute.
     *
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Get the GVBuffer method used to set the field considered.
     *
     * @return the setterMethod
     */
    public String getSetterMethod()
    {
        return setterMethod;
    }

    /**
     * Get the GVBuffer Property to set.
     *
     * @return the property
     */
    public String getProperty()
    {
        return property;
    }
}
