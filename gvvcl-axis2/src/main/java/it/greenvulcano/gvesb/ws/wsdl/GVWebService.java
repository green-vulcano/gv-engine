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
package it.greenvulcano.gvesb.ws.wsdl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * GVWebService class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVWebService
{

    private Set<ServiceOperation> serviceOperations;
    private String                serviceName;
    private String                soapAddress;
    private boolean               forceHttps;
    private String                inputXSD;
    private String                outputXSD;
    private boolean               targetNSFromXSD;
    private boolean               soapTransport             = true;
    private boolean               soap12Transport           = false;
    private boolean               restTransport             = false;
    private boolean               jmsTransport              = false;
    private String                restVerb                  = null;
    private String                jndiConnectionFactoryName = null;
    private String                jndiInitialContextFactory = null;
    private String                jndiContextParameter      = null;
    private String                jndiURL                   = null;
    private String                jmsDestination            = null;
    private String                jmsDestinationType        = null;
    private String                jmsReplyDestination       = null;
    private String                jmsContentType            = null;
    private String                jmsBytesMessage           = null;
    private String                jmsTextMessage            = null;
    private boolean               useOriginalwsdl           = true;
    private List<Module>          modules;

    /**
     * @param serviceConf
     * @throws Exception
     */
    public GVWebService(Node serviceConf) throws Exception
    {
        serviceOperations = new LinkedHashSet<ServiceOperation>();
        modules = new ArrayList<Module>();

        serviceName = XMLConfig.get(serviceConf, "@web-service");
        soapAddress = XMLConfig.get(serviceConf, "@soap-address", null);
        inputXSD = XMLConfig.get(serviceConf, "@input-xsd", "");
        outputXSD = XMLConfig.get(serviceConf, "@output-xsd", "");
        targetNSFromXSD = XMLConfig.getBoolean(serviceConf, "@targetNS-from-xsd", false);
        useOriginalwsdl = XMLConfig.getBoolean(serviceConf, "@useOriginalwsdl", true);
        Node transport = XMLConfig.getNode(serviceConf, "Transport");
        if (transport != null) {
            addTransport(transport);
        }

        NodeList operationList = XMLConfig.getNodeList(serviceConf, "WSOperation");
        for (int i = 0; i < operationList.getLength(); ++i) {
            Node operationConf = operationList.item(i);
            ServiceOperation operation = new ServiceOperation(operationConf);
            addOperation(operation);
        }

        forceHttps = XMLConfig.getBoolean(serviceConf, "@force-https", false);

        NodeList moduleList = XMLConfig.getNodeList(serviceConf, "EngageModule");
        for (int i = 0; i < moduleList.getLength(); ++i) {
            Node modConf = moduleList.item(i);
            Module module = new Module(modConf);
            modules.add(module);
        }
    }

    /**
     * @return the service name
     */
    public String getServiceName()
    {
        return serviceName;
    }

    /**
     * @param serviceName
     */
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    /**
     * @return Returns the soapAddress.
     */
    public String getSoapAddress()
    {
        return soapAddress;
    }

    /**
     * @param soapAddress
     *        The soapAddress to set.
     */
    public void setSoapAddress(String soapAddress)
    {
        this.soapAddress = soapAddress;
    }

    /**
     * @return the input XSD path
     */
    public String getInputXSD()
    {
        return inputXSD;
    }

    /**
     * @param inputXSD
     */
    public void setInputXSD(String inputXSD)
    {
        this.inputXSD = inputXSD;
    }

    /**
     * @return the output XSD path
     */
    public String getOutputXSD()
    {
        return outputXSD;
    }

    /**
     * @param outputXSD
     */
    public void setOutputXSD(String outputXSD)
    {
        this.outputXSD = outputXSD;
    }

    /**
     * @return if should be used the target name space from the XSD.
     */
    public boolean isTargetNSFromXSD()
    {
        return targetNSFromXSD;
    }

    /**
     * @param targetNSFromXSD
     */
    public void setTargetNSFromXSD(boolean targetNSFromXSD)
    {
        this.targetNSFromXSD = targetNSFromXSD;
    }

    /**
     * @return the service operations iterator
     */
    public Iterator<ServiceOperation> getServiceOperations()
    {
        return serviceOperations.iterator();
    }

    /**
     * @param serviceOperation
     */
    public void addOperation(ServiceOperation serviceOperation)
    {
        serviceOperations.add(serviceOperation);
    }

    /**
     * @param serviceOperation
     * @throws XMLConfigException
     */
    public void addTransport(Node transport) throws XMLConfigException
    {
        String soap = XMLConfig.get(transport, "soap", null);
        if (soap != null) {
            soapTransport = true;
            System.out.println("soapTransport" + soapTransport);
        }
        soap = XMLConfig.get(transport, "soap12", null);
        if (soap != null) {
            soap12Transport = true;
        }
        Node rest = XMLConfig.getNode(transport, "rest");
        if (rest != null) {
            restTransport = true;
            restVerb = XMLConfig.get(rest, "@verb", null);

        }
        Node jms = XMLConfig.getNode(transport, "jms");
        if (jms != null) {
            System.out.println("jms" + jms);
            jmsTransport = true;
            jndiConnectionFactoryName = XMLConfig.get(jms, "@connectionFactory", null);
            jmsBytesMessage = XMLConfig.get(jms, "@bytesMessage", null);
            jmsTextMessage = XMLConfig.get(jms, "@textMessage", null);
            jmsContentType = XMLConfig.get(jms, "@contentType", null);
            jmsDestination = XMLConfig.get(jms, "@destination", null);
            jmsReplyDestination = XMLConfig.get(jms, "@replyDestination", null);
            jmsDestinationType = XMLConfig.get(jms, "@destinationType", null);
            jndiInitialContextFactory = XMLConfig.get(jms, "@initialContextFactory", null);
            jndiURL = XMLConfig.get(jms, "@jndiURL", null);
            jndiContextParameter = XMLConfig.get(jms, "@contextParameter", null);

        }
    }

    /**
     * @param operationName
     * @return the <code>ServiceOperation</code> corresponding
     */
    public ServiceOperation getOperation(String operationName)
    {
        Iterator<ServiceOperation> it = serviceOperations.iterator();
        while (it.hasNext()) {
            ServiceOperation operation = it.next();
            if (operationName.equals(operation.getOperationName())) {
                return operation;
            }
        }
        return null;
    }

    /**
     * @return the service module iterator
     */
    public Iterator<Module> getServiceModules()
    {
        return modules.iterator();
    }


    /**
     * @return Returns the forceHttps.
     */
    public boolean isForceHttps()
    {
        return forceHttps;
    }

    /**
     * @param forceHttps
     *        The forceHttps to set.
     */
    public void setForceHttps(boolean forceHttps)
    {
        this.forceHttps = forceHttps;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("GV Web Service: \n");

        sb.append("\tserviceName '").append(serviceName).append("' \n\tsoapAddress '").append(soapAddress).append(
                "' \n\tforceHttps '").append(forceHttps).append("\n").append(useOriginalwsdl).append("\n");

        for (ServiceOperation svcOp : serviceOperations) {
            sb.append(svcOp);
        }

        return sb.toString();
    }

    public String getRestVerb()
    {
        return restVerb;
    }

    public void setRestVerb(String restVerb)
    {
        this.restVerb = restVerb;
    }

    public boolean isSoapTransport()
    {
        return soapTransport;
    }

    public void setSoapTransport(boolean soapTransport)
    {
        this.soapTransport = soapTransport;
    }

    public boolean isSoap12Transport()
    {
        return soap12Transport;
    }

    public void setSoap12Transport(boolean soap12Transport)
    {
        this.soap12Transport = soap12Transport;
    }

    public boolean isRestTransport()
    {
        return restTransport;
    }

    public void setRestTransport(boolean restTransport)
    {
        this.restTransport = restTransport;
    }

    public boolean isJmsTransport()
    {
        return jmsTransport;
    }

    public void setJmsTransport(boolean jmsTransport)
    {
        this.jmsTransport = jmsTransport;
    }

    public String getJmsDestination()
    {
        return jmsDestination;
    }

    public void setJmsDestination(String jmsDestination)
    {
        this.jmsDestination = jmsDestination;
    }

    public String getJmsDestinationType()
    {
        return jmsDestinationType;
    }

    public void setJmsDestinationType(String jmsDestinationType)
    {
        this.jmsDestinationType = jmsDestinationType;
    }

    public String getJmsReplyDestination()
    {
        return jmsReplyDestination;
    }

    public void setJmsReplyDestination(String jmsReplyDestination)
    {
        this.jmsReplyDestination = jmsReplyDestination;
    }

    public String getJmsBytesMessage()
    {
        return jmsBytesMessage;
    }

    public void setJmsBytesMessage(String jmsBytesMessage)
    {
        this.jmsBytesMessage = jmsBytesMessage;
    }

    public String getJmsTextMessage()
    {
        return jmsTextMessage;
    }

    public void setJmsTextMessage(String jmsTextMessage)
    {
        this.jmsTextMessage = jmsTextMessage;
    }


    public String getJndiConnectionFactoryName()
    {
        return jndiConnectionFactoryName;
    }

    public void setJndiConnectionFactoryName(String jndiConnectionFactoryName)
    {
        this.jndiConnectionFactoryName = jndiConnectionFactoryName;
    }

    public String getJmsContentType()
    {
        return jmsContentType;
    }

    public void setJmsContentType(String jmsContentType)
    {
        this.jmsContentType = jmsContentType;
    }

    public boolean isUseOriginalwsdl()
    {
        return useOriginalwsdl;
    }

    public void setUseOriginalwsdl(boolean useOriginalwsdl)
    {
        this.useOriginalwsdl = useOriginalwsdl;
    }

    public String getJndiInitialContextFactory()
    {
        return jndiInitialContextFactory;
    }

    public void setJndiInitialContextFactory(String jndiInitialContextFactory)
    {
        this.jndiInitialContextFactory = jndiInitialContextFactory;
    }

    public String getJndiURL()
    {
        return jndiURL;
    }

    public void setJndiURL(String jndiURL)
    {
        this.jndiURL = jndiURL;
    }

    public String getJndiContextParameter()
    {
        return jndiContextParameter;
    }

    public void setJndiContextParameter(String jndiContextParameter)
    {
        this.jndiContextParameter = jndiContextParameter;
    }

}
