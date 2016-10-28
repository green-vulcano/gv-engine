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
package it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer;

import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * OMDocLiteralWriter class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class OMDocLiteralWriter implements IOMParameterWriter
{
    private static final Matcher NS_MATCHER;
    private static final Logger  logger    = LoggerFactory.getLogger(OMDocLiteralWriter.class.getName());
    private static String        NS1       = "ns1";
    private static String        NS2       = "ns2";

    static {
        Pattern pattern = Pattern.compile("\\{(.*)\\}:(.*)");

        NS_MATCHER = pattern.matcher("");
    }

    private OperationDescription desc;
    private SOAPFactory          omFactory = OMAbstractFactory.getSOAP11Factory();
    private OMElement            root;

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#initialize(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription)
     */
    public void initialize(OperationDescription desc)
    {
        this.desc = desc;
    }

    /**
     * Writes a simple type
     *
     *
     * @param description
     * @param value
     */
    public void write(ParamDescription description, String value)
    {
        QName name = description.getXmlName();
        OMNode node;

        if (logger.isDebugEnabled()) {
            logger.debug("Writing Simple Parameter: " + name);
        }

        // we have no any
        if (name != null) {
            OMNamespace ns2 = null;

            if (description.getXmlType().getLocalPart().equals("QName")) {
                NS_MATCHER.reset(value);

                while (NS_MATCHER.find()) {
                    value = NS2 + ":" + NS_MATCHER.group(2);
                    ns2 = omFactory.createOMNamespace(NS_MATCHER.group(1), NS2);
                }
            }

            OMNamespace ns1 = omFactory.createOMNamespace(name.getNamespaceURI(), NS1);
            OMElement element = omFactory.createOMElement(name.getLocalPart(), ns1);

            OMText text = omFactory.createOMText(element, value);

            element.addChild(text);
            node = element;

            if (ns2 != null) {
                element.declareNamespace(ns2);
            }
        }
        else {
            node = omFactory.createOMText(root, value);
        }

        if (root == null) {
            // node should never a OMText because <any/> can never appear as
            // root node
            root = (OMElement) node;
        }
        else {
            root.addChild(node);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeArrayEnd()
     */
    public void writeArrayEnd()
    {
        // only for encoded style important to close array type definition but
        // not for doc/lit
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeArrayStart(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription,int)
     */
    public void writeArrayStart(ParamDescription desc, int size)
    {
        // only for encoded style important to open array type definition but
        // not for doc/lit
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeComplexEnd()
     */
    public void writeComplexEnd()
    {
        OMElement tmp = (OMElement) root.getParent();

        if (tmp != null) {
            root = tmp;
        }
    }

    /**
     * Writes the name of an element in fact. Not for a <complexType because
     * these are resolved to Elements again
     *
     * @param description
     */
    public void writeComplexStart(ParamDescription description)
    {

        if (logger.isDebugEnabled()) {
            logger.debug("Writing Complex Parameter: " + description.getXmlName());
        }

        QName name = description.getXmlName();
        OMNamespace ns1 = omFactory.createOMNamespace(name.getNamespaceURI(), NS1);
        OMElement element = omFactory.createOMElement(name.getLocalPart(), ns1);

        if (root != null) {
            root.addChild(element);
        }

        root = element;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter#getOMElement()
     */
    public OMElement getOMElement()
    {
        if (root == null) {
            Iterator<ParamDescription> itr = desc.getDescriptions().values().iterator();
            ParamDescription pd = null;
            if (itr.hasNext()) {
                pd = itr.next();
            }
            QName name = pd.getXmlName();
            OMNamespace ns1 = omFactory.createOMNamespace(name.getNamespaceURI(), NS1);

            root = omFactory.createOMElement(name.getLocalPart(), ns1);
        }

        return root;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeText(java.lang.String,
     *      int)
     */
    public void writeText(String value, int type)
    {
        if (root != null) {
            OMText text = omFactory.createOMText(root, value, type);

            root.addChild(text);
        }
        else {
            // this can only appear if user has no idea of WSDL parameter
            logger.error("Root Element of Writer is null!!!");
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter#writeOMNode(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription,
     *      org.apache.axiom.om.OMNode)
     */
    public void writeOMNode(ParamDescription description, OMNode node)
    {
        if (description == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("OMDocLIteral Writer: Description is null for OMNode");
            }

            if (root == null) {

                // here we implicit say that the user is smart enough
                // to give us an element and not a OMText
                // else it would be error anyway
                root = (OMElement) node;
            }
            else {
                root.addChild(node);
            }
        }
        else {
            QName name = description.getXmlName();

            if (logger.isDebugEnabled()) {
                logger.debug("Writing OM Parameter: " + name);
            }

            OMNamespace ns1 = omFactory.createOMNamespace(name.getNamespaceURI(), NS1);
            OMElement element = omFactory.createOMElement(name.getLocalPart(), ns1);

            addChildren(element, node);

            if (root == null) {
                root = element;
            }
            else {
                root.addChild(element);
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter#getOMEnvelope()
     */
    public SOAPEnvelope getOMEnvelope()
    {
        SOAPEnvelope envelope = omFactory.getDefaultEnvelope();

        envelope.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        envelope.getBody().addChild(root);

        return envelope;
    }

    private void addChildren(OMElement root, OMNode node)
    {
        if (node instanceof OMElement) {
            OMElement element = (OMElement) node;
            if (element.getQName().toString().equals(IOMParameterWriter.MY_ROOT_NODE_CONST)) {
                Iterator<?> itr = element.getChildren();
                while (itr.hasNext()) {
                    OMNode child = (OMNode) itr.next();
                    root.addChild(child);
                }
            }
            else {
                root.addChild(node);
            }
        }
        else {
            root.addChild(node);
        }
    }
}
