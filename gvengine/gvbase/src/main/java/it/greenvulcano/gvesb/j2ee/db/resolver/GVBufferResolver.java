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
package it.greenvulcano.gvesb.j2ee.db.resolver;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.utils.ParameterType;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The resolver class for the GVBuffer parameters.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVBufferResolver implements ParamResolver
{
    private static final Logger     logger                = org.slf4j.LoggerFactory.getLogger(GVBufferResolver.class);

    private GVBufferResolverParam[] gvBufferResolverParam = null;

    public GVBufferResolver()
    {
        logger.debug("Constructor start");
    }

    /**
     * Initializes ParamResolver
     *
     * @param node
     *        the configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        logger.debug("init of GVBufferResolver");
        NodeList nlParameters = null;
        try {
            nlParameters = XMLConfig.getNodeList(node, "GVBufferParam");
            int iCountParam = nlParameters.getLength();
            logger.debug("variables to bind " + iCountParam);
            gvBufferResolverParam = new GVBufferResolverParam[iCountParam];
            for (int i = 0; i < iCountParam; i++) {
                gvBufferResolverParam[i] = new GVBufferResolverParam(nlParameters.item(i));
            }
        }
        catch (XMLConfigException exc) {
            logger.error("init - Error while accessing configuration informations(<GVBufferParam>) via XMLConfig: ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while accessing configuration informations(<GVBufferParam>) via XMLConfig:" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("init - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.toString()}}, exc);
        }
    }

    /**
     * Resolves SQL parameters
     *
     * @param stmt
     *        the statement
     * @param gvBuffer
     *        the GVBuffer object
     * @throws GVDBException
     *         if an error occurred
     */
    public void resolve(Statement stmt, GVBuffer gvBuffer) throws GVDBException
    {
        for (GVBufferResolverParam param : gvBufferResolverParam) {
            try {
                int posiz = param.getPosition();
                String type = param.getType().trim();
                String gvDataMethodName = param.getGVBufferMethod();
                logger.debug("resolve - parameter (" + posiz + ") GVBufferMethod: " + gvDataMethodName + " Type: "
                        + type);

                int gvDataMethodRetType = detectGVBufferAccessorReturnType(gvDataMethodName);
                Object objParam = readValue(gvBuffer, param);

                if (objParam != null) {
                    logger.debug("objParam " + objParam);
                    if (type.equals(ParameterType.DB_INT)) {
                        int intValue = 0;
                        switch (gvDataMethodRetType) {
                            case STRING_FIELD :
                                intValue = Integer.parseInt((String) objParam);
                                break;
                            case INT_FIELD :
                                intValue = ((Integer) objParam).intValue();
                                break;
                            case OBJECT_FIELD :
                                DataInputStream dis = new DataInputStream(new ByteArrayInputStream((byte[]) objParam));
                                intValue = dis.readInt();
                                break;
                            case ID_FIELD :
                                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                                        "GVBuffer field 'Id' cannot be converted to 'int'"}});
                        }

                        ((PreparedStatement) stmt).setInt(posiz, intValue);
                        logger.debug("Value : " + objParam);
                    }
                    else if (type.equals(ParameterType.DB_LONG)) {
                        long longValue = 0;
                        switch (gvDataMethodRetType) {
                            case STRING_FIELD :
                                longValue = Long.parseLong((String) objParam);
                                break;
                            case INT_FIELD :
                                longValue = ((Integer) objParam).intValue();
                                break;
                            case OBJECT_FIELD :
                                DataInputStream dis = new DataInputStream(new ByteArrayInputStream((byte[]) objParam));
                                longValue = dis.readLong();
                                break;
                            case ID_FIELD :
                                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                                        "GVBuffer field 'Id' cannot be converted to 'long'"}});
                        }

                        ((PreparedStatement) stmt).setLong(posiz, longValue);
                        logger.debug("Value : " + objParam);
                    }
                    else if (type.equals(ParameterType.DB_FLOAT)) {
                        float floatValue = 0;
                        switch (gvDataMethodRetType) {
                            case STRING_FIELD :
                                floatValue = Float.valueOf((String) objParam);
                                break;
                            case INT_FIELD :
                                floatValue = ((Integer) objParam).intValue();
                                break;
                            case OBJECT_FIELD :
                                DataInputStream dis = new DataInputStream(new ByteArrayInputStream((byte[]) objParam));
                                floatValue = dis.readFloat();
                                break;
                            case ID_FIELD :
                                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message",
                                        "GVBuffer field 'Id' cannot be converted to 'float'"}});
                        }

                        ((PreparedStatement) stmt).setFloat(posiz, floatValue);
                        logger.debug("Value : " + objParam);
                    }
                    else if (type.equals(ParameterType.DB_DATE)) {
                        String format = param.getFormat();
                        java.util.Date date = null;
                        if (format != null) {
                            date = DateUtils.stringToDate((String) objParam, format);
                        }
                        else {
                            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                                    {"message", "DateFormat not specified"}, {"system", gvBuffer.getSystem()},
                                    {"service", gvBuffer.getService()}, {"id", gvBuffer.getId().toString()}}, null);
                        }

                        ((PreparedStatement) stmt).setTimestamp(posiz, new java.sql.Timestamp(date.getTime()));
                    }
                    else if (type.equals(ParameterType.DB_STRING)) {
                        String strValue = null;
                        switch (gvDataMethodRetType) {
                            case STRING_FIELD :
                                strValue = ((String) objParam);
                                break;
                            case INT_FIELD :
                                strValue = ((Integer) objParam).toString();
                                break;
                            case OBJECT_FIELD :
                                strValue = new String((byte[]) objParam);
                                break;
                            case ID_FIELD :
                                strValue = ((Id) objParam).toString();
                                break;
                        }

                        ((PreparedStatement) stmt).setString(posiz, strValue);
                        logger.debug("Value : " + objParam);
                    }
                    else if (type.equals(ParameterType.DB_LONG_RAW)) {
                        byte[] byteArrValue = null;
                        switch (gvDataMethodRetType) {
                            case STRING_FIELD :
                                byteArrValue = ((String) objParam).getBytes();
                                break;
                            case INT_FIELD :
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                DataOutputStream dos = new DataOutputStream(baos);
                                dos.writeInt(((Integer) objParam).intValue());
                                byteArrValue = baos.toByteArray();
                                break;
                            case OBJECT_FIELD :
                                byteArrValue = (byte[]) objParam;
                                break;
                            case ID_FIELD :
                                byteArrValue = ((Id) objParam).toString().getBytes();
                                break;
                        }

                        ByteArrayInputStream bais = new ByteArrayInputStream(byteArrValue);
                        ((PreparedStatement) stmt).setBinaryStream(posiz, bais, byteArrValue.length);
                    }
                    else if (type.equals(ParameterType.DB_BLOB)) {
                        byte[] byteArrValue = null;
                        switch (gvDataMethodRetType) {
                            case STRING_FIELD :
                                byteArrValue = ((String) objParam).getBytes();
                                break;
                            case INT_FIELD :
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                DataOutputStream dos = new DataOutputStream(baos);
                                dos.writeInt(((Integer) objParam).intValue());
                                byteArrValue = baos.toByteArray();
                                break;
                            case OBJECT_FIELD :
                                byteArrValue = (byte[]) objParam;
                                break;
                            case ID_FIELD :
                                byteArrValue = ((Id) objParam).toString().getBytes();
                                break;
                        }

                        ByteArrayInputStream bais = new ByteArrayInputStream(byteArrValue);
                        ((PreparedStatement) stmt).setBinaryStream(posiz, bais, byteArrValue.length);
                    }
                    else if (type.equals(ParameterType.DB_CLOB)) {
                        byte[] byteArrValue = null;
                        switch (gvDataMethodRetType) {
                            case STRING_FIELD :
                                byteArrValue = ((String) objParam).getBytes();
                                break;
                            case INT_FIELD :
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                DataOutputStream dos = new DataOutputStream(baos);
                                dos.writeInt(((Integer) objParam).intValue());
                                byteArrValue = baos.toByteArray();
                                break;
                            case OBJECT_FIELD :
                                byteArrValue = (byte[]) objParam;
                                break;
                            case ID_FIELD :
                                byteArrValue = ((Id) objParam).toString().getBytes();
                                break;
                        }

                        ByteArrayInputStream bais = new ByteArrayInputStream(byteArrValue);
                        ((PreparedStatement) stmt).setAsciiStream(posiz, bais, byteArrValue.length);
                    }
                    else {
                        logger.error("resolve - "
                                + "Error while setting parameters for CallableStatement: parameter type not supported");
                        throw new GVDBException(
                                "GV_GENERIC_ERROR",
                                new String[][]{
                                        {"message",
                                                "Error while setting parameters for CallableStatement: parameter type not supported"},
                                        {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                                        {"id", gvBuffer.getId().toString()}}, null);
                    }
                }
                else {
                    throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", "Invalid input Parameter"},
                            {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                            {"id", gvBuffer.getId().toString()}}, null);
                }
            }
            catch (SQLException exc) {
                logger.error("resolve - Error while setting parameters for CallableStatement", exc);
                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{
                        {"message", "Error while setting parameters for CallableStatement" + exc},
                        {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                        {"id", gvBuffer.getId().toString()}}, exc);
            }
            catch (Throwable exc) {
                logger.error("resolve - Generic Error : ", exc);
                throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.toString()},
                        {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()},
                        {"id", gvBuffer.getId().toString()}}, exc);
            }
        }
    }

    /**
     * Read the value
     *
     * @param gvBuffer
     *        the GVBuffer object
     * @param index
     *        the index
     * @return the object
     * @throws GVDBException
     *         if an error occurred
     */
    private Object readValue(GVBuffer gvBuffer, GVBufferResolverParam param) throws GVDBException
    {
        Method method = null;
        Object[] argumentValue = null;
        String methodName = param.getGVBufferMethod();
        boolean isObject = false;
        try {
            String propName = param.getPropertyName();
            if (propName != null) {
                logger.debug("property : " + propName);
                Class<?>[] argumentDef = {"".getClass()};
                method = (gvBuffer.getClass()).getMethod(methodName, argumentDef);
                argumentValue = new Object[1];
                argumentValue[0] = propName;
            }
            else {
                isObject = true;
                method = (gvBuffer.getClass()).getMethod(methodName, (Class<?>[]) null);
            }
            logger.debug("object method : " + method);
            Object val = method.invoke(gvBuffer, argumentValue);
            if (isObject) {
                if (val instanceof byte[]) {
                    return val;
                }
                else if (val instanceof String) {
                    return ((String) val).getBytes();
                }
                else if (val instanceof Node) {
                    return XMLUtils.serializeDOMToByteArray_S((Node) val);
                }
            }
            return val;
        }
        catch (Throwable exc) {
            logger.error("resolve - Generic Error : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", new String[][]{{"message", exc.toString()},
                    {"system", gvBuffer.getSystem()}, {"service", gvBuffer.getService()}, {"id", gvBuffer.getId().toString()}},
                    exc);
        }
    }

    /**
     * An enumeration of the available types of GVBuffer field
     */
    private final static int STRING_FIELD = 0; // Returned by getSystem, getService, getProperty
    private final static int INT_FIELD    = 1; // Returned by getRetCode
    private final static int ID_FIELD     = 2; // Returned by getId
    private final static int OBJECT_FIELD = 3; // Returned by getObject

    /**
     * Detect the return type of an GVBuffer accessor method, given the method's
     * name.
     *
     * @param accessorMethodName
     * @return
     */
    private int detectGVBufferAccessorReturnType(String accessorMethodName)
    {
        int result = STRING_FIELD;

        if (accessorMethodName.equals("getObject")) {
            result = OBJECT_FIELD;
        }
        else if (accessorMethodName.equals("getId")) {
            result = ID_FIELD;
        }
        else if (accessorMethodName.equals("getRetCode")) {
            result = INT_FIELD;
        }

        return result;
    }

}
