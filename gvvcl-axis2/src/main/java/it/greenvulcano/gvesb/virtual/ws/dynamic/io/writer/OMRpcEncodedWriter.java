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
package it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer;

import it.greenvulcano.gvesb.virtual.ws.dynamic.Constants;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

/**
 * OMRpcEncodedWriter class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class OMRpcEncodedWriter implements IOMParameterWriter
{
    private SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
    private OMElement   root;
    OMNamespace         xsi;
    OMNamespace         xsd;

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#initialize(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription)
     */
    public void initialize(OperationDescription desc)
    {

        // Operation Body
        QName name = desc.getXmlName();
        OMNamespace ns0 = omFactory.createOMNamespace(name.getNamespaceURI(), "");

        xsi = omFactory.createOMNamespace(Constants.NS_URI_1999_SCHEMA_XSI, "xsi");
        xsd = omFactory.createOMNamespace(Constants.NS_URI_1999_SCHEMA_XSD, "xsd");
        root = omFactory.createOMElement(name.getLocalPart(), ns0);

        // xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance"
        // xmlns:xsd="http://www.w3.org/1999/XMLSchema"
        // SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"
        root.declareNamespace(xsi);
        root.declareNamespace(xsd);

        // soapenv="http://schemas.xmlsoap.org/soap/envelope/
        OMNamespace soap = omFactory.createOMNamespace(Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                Constants.SOAP_DEFAULT_NAMESPACE_PREFIX);

        OMAttribute attribute = omFactory.createOMAttribute("encodingStyle", soap,
                "http://schemas.xmlsoap.org/soap/encoding/");

        root.addAttribute(attribute);
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

        if (name != null) {

            // we have no any
            OMNamespace ns0 = omFactory.createOMNamespace(name.getNamespaceURI(), "");
            OMElement element = omFactory.createOMElement(name.getLocalPart(), ns0);

            // !!!! overwrite QName of name with that of type
            name = description.getXmlType();

            String prefix = name.getPrefix();

            if (name.getNamespaceURI().equals(Constants.NS_URI_1999_SCHEMA_XSD)
                    || name.getNamespaceURI().equals(Constants.NS_URI_2000_SCHEMA_XSD)
                    || name.getNamespaceURI().equals(Constants.NS_URI_2001_SCHEMA_XSD)) {
                prefix = xsd.getPrefix();
            }
            else {
                OMNamespace ns1 = omFactory.createOMNamespace(name.getNamespaceURI(), "ns1");

                prefix = "ns1";
                element.declareNamespace(ns1);
            }

            OMAttribute attribute = omFactory.createOMAttribute("type", xsi, prefix + ":" + name.getLocalPart());

            element.addAttribute(attribute);

            OMText text = omFactory.createOMText(element, value);

            element.addChild(text);
            root.addChild(element);
        }
        else {
            OMText text = omFactory.createOMText(root, value);

            root.addChild(text);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeArrayEnd()
     */
    public void writeArrayEnd()
    {
        throw new UnsupportedOperationException("Arrays in Rpc style not supported at the moment.");

        // only for encoded style important to close array type definition but
        // not for doc/lit
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeArrayStart(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription,int)
     */
    public void writeArrayStart(ParamDescription desc, int size)
    {
        throw new UnsupportedOperationException("Arrays in Rpc style not supported at the moment.");

        // only for encoded style important to open array type definition but
        // not for doc/lit
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeComplexEnd()
     */
    public void writeComplexEnd()
    {
        root = (OMElement) root.getParent();
    }

    /**
     * Writes the name of an element in fact. Not for a <complexType> because
     * these are resolved to Elements again
     *
     * @param description
     */
    public void writeComplexStart(ParamDescription description)
    {
        QName name = description.getXmlName();
        OMNamespace ns0 = omFactory.createOMNamespace(name.getNamespaceURI(), "");
        OMElement element = omFactory.createOMElement(name.getLocalPart(), ns0);

        // !!!!! overwrite QName of name with that of type
        name = description.getXmlType();

        String prefix = name.getPrefix();

        if (name.getNamespaceURI().equals(Constants.NS_URI_1999_SCHEMA_XSD)
                || name.getNamespaceURI().equals(Constants.NS_URI_2000_SCHEMA_XSD)
                || name.getNamespaceURI().equals(Constants.NS_URI_2001_SCHEMA_XSD)) {
            prefix = xsd.getPrefix();
        }
        else {
            OMNamespace ns1 = omFactory.createOMNamespace(name.getNamespaceURI(), "ns1");

            prefix = "ns1";
            element.declareNamespace(ns1);
        }

        OMAttribute attribute = omFactory.createOMAttribute("type", xsi, prefix + ":" + name.getLocalPart());

        element.addAttribute(attribute);
        root.addChild(element);
        root = element;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter#getOMElement()
     */
    public OMElement getOMElement()
    {
        return root;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IParameterWriter#writeText(java.lang.String,
     *      int)
     */
    public void writeText(String value, int type)
    {
        OMText text = omFactory.createOMText(root, value, type);

        root.addChild(text);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter#writeOMNode(it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescription,
     *      org.apache.axiom.om.OMNode)
     */
    public void writeOMNode(ParamDescription desc, OMNode node)
    {
        if (desc == null) {
            addChildren(root, node);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.io.writer.IOMParameterWriter#getOMEnvelope()
     */
    public SOAPEnvelope getOMEnvelope()
    {
        SOAPEnvelope envelope = omFactory.getDefaultEnvelope();

        envelope.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        envelope.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        envelope.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        envelope.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
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
