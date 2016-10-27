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
package it.greenvulcano.gvesb.http;

import it.greenvulcano.configuration.XMLConfig;

import java.lang.reflect.Constructor;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @version 3.0.0 Jul 27, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ProtocolFactory
{

    /**
     * @param protocolConfig
     * @return
     * @throws Exception
     */
    public static Protocol create(Node protocolConfig) throws Exception
    {
        String scheme = XMLConfig.get(protocolConfig, "@protocol-scheme");
        String socketFactoryClassName = XMLConfig.get(protocolConfig, "@protocol-socket-factory");
        int defaultPort = XMLConfig.getInteger(protocolConfig, "@protocol-default-port");
        Class<?> socketFactoryClass = Class.forName(socketFactoryClassName);
        Object socketFactoryInstance = null;
        Node constructorNode = XMLConfig.getNode(protocolConfig, "constructor-args");
        if (constructorNode != null) {
            socketFactoryInstance = createObjectUsingConstructor(socketFactoryClass, constructorNode);
        }
        else {
            socketFactoryInstance = socketFactoryClass.newInstance();
        }

        return new Protocol(scheme, (ProtocolSocketFactory) socketFactoryInstance, defaultPort);
    }

    /**
     *
     * @param objectClass
     *        Object class to instantiate
     * @param node
     *        The configuration of constructor parameters.
     * @return
     */
    private static Object createObjectUsingConstructor(Class<?> objectClass, Node node) throws Exception
    {
        NodeList paramList = XMLConfig.getNodeList(node, "constructor-param");
        Class<?>[] types = new Class[paramList.getLength()];
        Object[] params = new Object[types.length];

        for (int i = 0; i < types.length; ++i) {
            Node paramNode = paramList.item(i);
            String type = XMLConfig.get(paramNode, "@type");
            String value = XMLConfig.getDecrypted(paramNode, "@value");
            Class<?> cls = null;
            if (type.equals("byte")) {
                cls = Byte.TYPE;
            }
            else if (type.equals("boolean")) {
                cls = Boolean.TYPE;
            }
            else if (type.equals("char")) {
                cls = Character.TYPE;
            }
            else if (type.equals("double")) {
                cls = Double.TYPE;
            }
            else if (type.equals("float")) {
                cls = Float.TYPE;
            }
            else if (type.equals("int")) {
                cls = Integer.TYPE;
            }
            else if (type.equals("long")) {
                cls = Long.TYPE;
            }
            else if (type.equals("short")) {
                cls = Short.TYPE;
            }
            else if (type.equals("String")) {
                cls = String.class;
            }
            types[i] = cls;
            params[i] = cast(value, cls);
        }

        Constructor<?> constr = objectClass.getConstructor(types);
        return constr.newInstance(params);
    }

    /**
     *
     * @param value
     * @param cls
     * @return
     */
    private static Object cast(String value, Class<?> cls) throws Exception
    {
        Class<?>[] types = {String.class};
        Constructor<?> constr = cls.getConstructor(types);
        Object[] params = {value};
        return constr.newInstance(params);
    }

}
