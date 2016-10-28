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
package it.greenvulcano.gvesb.ws.wsdl;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.extensions.soap.SOAPConstants;
import com.ibm.wsdl.util.xml.DOMUtils;

/**
 * @version 3.2.0 Feb 10, 2012
 * @author GreenVulcano Developer Team
 */
@SuppressWarnings("rawtypes")
public class JMSBindingSerializer implements ExtensionSerializer, ExtensionDeserializer, Serializable
{
    public static final long serialVersionUID = 1;
    
	@Override
    public void marshall(Class parentType, QName elementType, ExtensibilityElement extension, PrintWriter pw,
            Definition def, ExtensionRegistry extReg) throws WSDLException
    {
        JMSBinding jmsBinding = (JMSBinding) extension;

        if (jmsBinding != null) {
            String tagName = DOMUtils.getQualifiedValue(SOAPConstants.NS_URI_SOAP, Constants.ELEM_BINDING, def);

            pw.print("    <" + tagName);

            DOMUtils.printAttribute(SOAPConstants.ATTR_STYLE, jmsBinding.getStyle(), pw);
            DOMUtils.printAttribute(SOAPConstants.ATTR_TRANSPORT, jmsBinding.getTransportURI(), pw);

            Boolean required = jmsBinding.getRequired();

            if (required != null) {
                DOMUtils.printQualifiedAttribute(Constants.Q_ATTR_REQUIRED, required.toString(), def, pw);
            }
            pw.println("/>");
            if (jmsBinding.getJndiContextParameter() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS, JMSBindingImpl.JNDICONTEXTPARAMETER,
                        def);
                pw.print("    <" + tagName);
                DOMUtils.printAttribute("name", jmsBinding.getJndiContextParameter().toString(), pw);
                DOMUtils.printAttribute("value", jmsBinding.getJndiContextParameter().toString(), pw);
                pw.println("/>");
            }
            if (jmsBinding.getJndiConnectionFactoryName() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS,
                        JMSBindingImpl.JNDICONNECTIONFACTORYNAME, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getJndiConnectionFactoryName());
                pw.println("  </" + tagName + ">");
            }
            if (jmsBinding.getJndiInitialContextFactory() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS,
                        JMSBindingImpl.JNDIINITIALCONTEXTFACTORY, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getJndiInitialContextFactory());
                pw.println("  </" + tagName + ">");
            }
            if (jmsBinding.getJndiURL() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS, JMSBindingImpl.JNDIURL, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getJndiURL());
                pw.println("  </" + tagName + ">");
            }
            if (jmsBinding.getDeliveryMode() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS, JMSBindingImpl.DELIVERYMODE, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getDeliveryMode());
                pw.println("  </" + tagName + ">");
            }
            if (jmsBinding.getPriority() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS, JMSBindingImpl.PRIORITY, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getPriority());
                pw.println("  </" + tagName + ">");
            }
            if (jmsBinding.getTimeToLive() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS, JMSBindingImpl.TIMETOLIVE, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getTimeToLive());
                pw.println("  </" + tagName + ">");
            }
            if (jmsBinding.getReplyToName() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS, JMSBindingImpl.REPLYTONAME, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getReplyToName());
                pw.println("  </" + tagName + ">");
            }
            if (jmsBinding.getTopicReplyToName() != null) {
                tagName = DOMUtils.getQualifiedValue(JMSBindingImpl.NS_URI_JMS, JMSBindingImpl.TOPICREPLYTONAME, def);
                pw.print("    <" + tagName + ">");
                pw.print(jmsBinding.getTopicReplyToName());
                pw.println("  </" + tagName + ">");
            }

        }

    }

    @Override
    public ExtensibilityElement unmarshall(Class parentType, QName elementType, Element el, Definition def,
            ExtensionRegistry extReg) throws WSDLException
    {
        JMSBinding jmsBinding = (JMSBinding) extReg.createExtension(parentType, elementType);
        String transportURI = DOMUtils.getAttribute(el, SOAPConstants.ATTR_TRANSPORT);
        String style = DOMUtils.getAttribute(el, SOAPConstants.ATTR_STYLE);
        String requiredStr = DOMUtils.getAttributeNS(el, Constants.NS_URI_WSDL, Constants.ATTR_REQUIRED);

        if (transportURI != null) {
            jmsBinding.setTransportURI(transportURI);
        }

        if (style != null) {
            jmsBinding.setStyle(style);
        }

        if (requiredStr != null) {
            jmsBinding.setRequired(new Boolean(requiredStr));
        }

        return jmsBinding;
    }
}