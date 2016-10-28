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

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.xml.DOMWriter;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.wsdl.extensions.http.HTTPConstants;
import com.ibm.wsdl.extensions.mime.MIMEConstants;
import com.ibm.wsdl.extensions.mime.MIMEContentImpl;
import com.ibm.wsdl.extensions.soap.SOAPConstants;
import com.ibm.wsdl.extensions.soap12.SOAP12Constants;

/**
 * WSDLGenerator class.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public abstract class WSDLGenerator
{

    private static final Logger      logger                 = org.slf4j.LoggerFactory.getLogger(WSDLGenerator.class);

    /**
     *
     */
    public static final String       CONFIGURATION_FILE     = "GVAdapters.xml";

    /**
     *
     */
    public static final String       GV_NS                  = "http://www.greenvulcano.it/greenvulcano";
    /**
     *
     */
    public static final String       GV_PREFIX              = "gv";

    /**
     *
     */
    public static final String       SCHEMA_NS              = "http://www.w3.org/2001/XMLSchema";
    /**
     *
     */
    public static final String       SCHEMA_PREFIX          = "xsd";
    /**
     *
     */
    public static final String       SCHEMA_INSTANCE_NS     = "http://www.w3.org/2001/XMLSchema-instance";
    /**
     *
     */
    public static final String       SCHEMA_INSTANCE_PREFIX = "xsi";
    /**
     *
     */
    public static final String       SOAP_ENCODING_NS       = "http://schemas.xmlsoap.org/soap/encoding/";
    /**
     *
     */
    public static final String       SOAP_ENCODING_PREFIX   = "soapenc";
    /**
     *
     */
    public static final String       SCHEMA_SCHEMA          = "schema";
    /**
     *
     */
    public static final String       SCHEMA_ELEMENT         = "element";
    /**
     *
     */
    public static final String       SCHEMA_COMPLEX         = "complexType";
    /**
     *
     */
    public static final String       SCHEMA_SEQUENCE        = "sequence";

    /**
     *
     */
    public static final String       SOAP_NS                = SOAPConstants.NS_URI_SOAP;
    public static final String       SOAP12_NS              = SOAP12Constants.NS_URI_SOAP12;
    public static final String       JMSSOAP_NS             = "http://www.w3.org/2010/soapjms/";
    /**
     * 
     */
    public static final String       SOAP_PREFIX            = "soap";
    public static final String       SOAP12_PREFIX          = "soap12";
    public static final String       HTTP_PREFIX            = "http";
    public static final String       JMS_PREFIX             = "soapjms";
    public static final String       MIME_PREFIX            = "mime";
    public static final String       SOAP_OVER_HTTP         = "http://schemas.xmlsoap.org/soap/http";
    public static final QName        HTTP_BINDING           = new QName(HTTPConstants.NS_URI_HTTP, "binding");
    public static final QName        HTTP_OPERATION         = new QName(HTTPConstants.NS_URI_HTTP, "operation");
    public static final QName        HTTP_ADDRESS           = new QName(HTTPConstants.NS_URI_HTTP, "address");
    public static final QName        MIME_CONTENT           = new QName(MIMEConstants.NS_URI_MIME, "mime");

    /**
     *
     */
    public static final QName        SOAP_ADDRESS           = new QName(SOAP_NS, "address");
    /**
     *
     */
    public static final QName        SOAP_BINDING           = new QName(SOAP_NS, "binding");
    /**
     *
     */
    public static final QName        SOAP_OPERATION         = new QName(SOAP_NS, "operation");
    /**
     *
     */
    public static final QName        SOAP_BODY              = new QName(SOAP_NS, "body");
    public static final QName        SOAP12_ADDRESS         = new QName(SOAP12_NS, "address");
    /**
	 * 
	 */
    public static final QName        SOAP12_BINDING         = new QName(SOAP12_NS, "binding");
    /**
	 * 
	 */
    public static final QName        SOAP12_OPERATION       = new QName(SOAP12_NS, "operation");
    /**
	 * 
	 */
    public static final QName        SOAP12_BODY            = new QName(SOAP12_NS, "body");

    /**
     *
     */
    public static final List<String> ENCODING_STYLES        = new ArrayList<String>();

    private static final String      CUSTOM_TNS_PREFIX      = "tns";

    static {
        ENCODING_STYLES.add("http://schemas.xmlsoap.org/soap/encoding/");
    }

    private WSDLFactory              wsdlFactory;
    private ExtensionRegistry        extensionRegistry;
    private Definition               definition             = null;
    private Types                    types                  = null;
    private Element                  schemaElement;
    private boolean                  encoded                = false;
    private String                   httpSoapAddress        = "";
    private String                   httpsSoapAddress       = "";
    private File                     wsdlDirectory          = null;

    private String                   targetNamespace        = null;

    /**
     * @throws WSDLException
     *
     */
    public WSDLGenerator() throws WSDLException
    {
        wsdlFactory = WSDLFactory.newInstance();
        extensionRegistry = wsdlFactory.newPopulatedExtensionRegistry();
    }

    /**
     * @param encoded
     */
    public void setEncoded(boolean encoded)
    {
        this.encoded = encoded;
    }

    /**
     * @return if is encoded style WSDL
     */
    public boolean isEncoded()
    {
        return encoded;
    }

    /**
     * @return the Web Service configuration
     */
    protected abstract List<Node> getWebServiceConfig();

    /**
     * @throws Exception
     */
    public void generateAllWSDL() throws Exception
    {
        List<Node> wsListConfig = getWebServiceConfig();

        DOMWriter writer = new DOMWriter();
        writer.setPreferredWidth(100);
        for (Node node : wsListConfig) {
            GVWebService service = new GVWebService(node);
            generateWSDL(service);
        }
    }

    /**
     * @param service
     * @throws WSDLException
     * @throws IOException
     */
    public void generateWSDL(GVWebService service) throws WSDLException, IOException
    {
        generateWSDL(service, (File) null);
    }

    /**
     * @param service
     * @param wsdlDir
     * @throws WSDLException
     * @throws IOException
     */
    public void generateWSDL(GVWebService service, String wsdlDir) throws WSDLException, IOException
    {
        File f = null;
        if (wsdlDir != null) {
            f = new File(wsdlDir);
        }
        generateWSDL(service, f);
    }

    /**
     * @param service
     * @param wsdlDir
     * @throws WSDLException
     * @throws IOException
     */
    public void generateWSDL(GVWebService service, File wsdlDir) throws WSDLException, IOException
    {
        DOMWriter writer = new DOMWriter();
        writer.setPreferredWidth(100);

        if (wsdlDir == null) {
            wsdlDir = wsdlDirectory;
        }

        if (service.getSoapAddress() == null) {
            String defaultSoapAddress = httpSoapAddress;
            if (service.isForceHttps()) {
                defaultSoapAddress = httpsSoapAddress;
            }
            service.setSoapAddress(defaultSoapAddress + "/" + service.getServiceName());
        }

        Definition definition = generateDefinitionWSDL(service);
        String wsdlName = service.getServiceName() + ".wsdl";
        OutputStream outputStream = new FileOutputStream(new File(wsdlDir, wsdlName));
        writer.write(getDocument(definition), outputStream);
        outputStream.close();
    }

    /**
     * @param webService
     * @return the generated WSDL definition
     * @throws WSDLException
     */
    public Definition generateDefinitionWSDL(GVWebService webService) throws WSDLException
    {
        definition = wsdlFactory.newDefinition();
        definition.addNamespace(SOAP_PREFIX, SOAP_NS);
        definition.addNamespace(SCHEMA_PREFIX, SCHEMA_NS);
        definition.addNamespace(SCHEMA_INSTANCE_PREFIX, SCHEMA_INSTANCE_NS);
        definition.addNamespace(SOAP_ENCODING_PREFIX, SOAP_ENCODING_NS);
        definition.addNamespace(HTTP_PREFIX, HTTPConstants.NS_URI_HTTP);
        definition.addNamespace(SOAP12_PREFIX, SOAP12_NS);
        definition.addNamespace(JMS_PREFIX, JMSSOAP_NS);
        definition.addNamespace(MIME_PREFIX, MIMEConstants.NS_URI_MIME);

        types = definition.createTypes();
        definition.setTypes(types);

        Schema schema = (Schema) extensionRegistry.createExtension(Types.class, new QName(SCHEMA_NS, SCHEMA_SCHEMA));
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.newDocument();
            if (webService.getInputXSD() == null || webService.getInputXSD().length() == 0) {
                schemaElement = document.createElementNS(SCHEMA_NS, SCHEMA_SCHEMA);
                schemaElement.setPrefix(SCHEMA_PREFIX);
                schemaElement.setAttribute("elementFormDefault", "qualified");
                schemaElement.setAttribute("targetNamespace", GV_NS);
            }
            else {
                schemaElement = null;
                String tns = loadExternalXSD(webService.getInputXSD());
                if (webService.isTargetNSFromXSD()) {
                    definition.addNamespace(CUSTOM_TNS_PREFIX, tns);
                    targetNamespace = tns;
                }
                loadExternalXSD(webService.getOutputXSD());
                if (schemaElement != null) {
                    schemaElement.setAttribute("targetNamespace", targetNamespace);
                }
                definition.setTargetNamespace(targetNamespace);
            }
        }
        catch (Exception exc) {
            throw new WSDLException("dom", "cannot create the schema element", exc);
        }
       
        PortType portType = addPortType(webService);
        addConcreteParam(webService,portType);
        schema.setElement(schemaElement);
        if(targetNamespace==null){
          definition.setTargetNamespace(GV_NS);
          targetNamespace = GV_NS;
          definition.addNamespace(GV_PREFIX, GV_NS);
        }
        definition.setExtensionRegistry(extensionRegistry);

        return definition;
    }

    private String loadExternalXSD(String xsd) throws WSDLException
    {
        String tns = null;
        if (xsd != null && xsd.length() > 0) {
            if (!PropertiesHandler.isExpanded(xsd)) {
                try {
                    xsd = PropertiesHandler.expand(xsd);
                }
                catch (PropertiesHandlerException exc) {
                    logger.warn("Cannot expand XSD path name" + xsd, exc);
                }
            }
            File xsdFile = new File(xsd);
            if (!xsdFile.isAbsolute()) {
                try {
                    xsd = PropertiesHandler.expand("${{gv.app.home}}" + File.separatorChar +"xmlconfig"+ File.separatorChar +"xsds"
                            + File.separatorChar + xsd);
                    xsdFile = new File(xsd);
                }
                catch (PropertiesHandlerException exc) {
                    logger.warn("Cannot expand XSD relative path name" + xsd, exc);
                }
            }
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(xsdFile);
                Document xsdDOM = XMLUtils.parseDOM_S(fis, false, true);
                if (schemaElement == null) {
                    schemaElement = xsdDOM.getDocumentElement();
                    tns = schemaElement.getAttribute("targetNamespace");
                }
                else {
                    Element schema = xsdDOM.getDocumentElement();
                    tns = schema.getAttribute("targetNamespace");
                    addSchemaFragments(schema.getChildNodes());
                }
            }
            catch (Exception exc) {
                logger.error("Cannot load XSD file " + xsd, exc);
                throw new WSDLException("dom", "cannot load the XSD file " + xsd, exc);
            }
            finally {
                if (fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException exc) {
                        // we don't care
                    }
                }
            }
        }
        return tns;
    }

    /**
     * @param outputStream
     * @param definition
     * @throws WSDLException
     */
    public void writeWSDL(OutputStream outputStream, Definition definition) throws WSDLException
    {
        WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
        wsdlWriter.writeWSDL(definition, outputStream);
    }

    /**
     * @param writer
     * @param definition
     * @throws WSDLException
     */
    public void writeWSDL(Writer writer, Definition definition) throws WSDLException
    {
        WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
        wsdlWriter.writeWSDL(definition, writer);
    }

    /**
     * @param definition
     * @return the <code>org.w3c.dom.Document</code> corresponding to the WSDL
     *         definition
     * @throws WSDLException
     */
    public Document getDocument(Definition definition) throws WSDLException
    {
        WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
        return wsdlWriter.getDocument(definition);
    }

    private QName createQName(String localName)
    {
        QName name = new QName(targetNamespace, localName);
        return name;
    }

    private Operation addOperation(PortType portType, ServiceOperation serviceOperation)
    {
        String operationName = serviceOperation.getOperationName();
        System.out.println("operationName="+operationName);
        Operation operation = definition.createOperation();
        operation.setName(operationName);
        operation.setUndefined(false);

        Message request = definition.createMessage();
        request.setQName(createQName(operationName + "Request"));
        request.setUndefined(false);
        definition.addMessage(request);

        Input input = definition.createInput();
        input.setName(operationName + "Request");
        input.setMessage(request);
        operation.setInput(input);
        Part part = definition.createPart();
        part.setName("parameters");
        part.setElementName(createQName(operationName));
        request.addPart(part);

        Message response = definition.createMessage();
        response.setQName(createQName(operationName + "Response"));
        response.setUndefined(false);
        definition.addMessage(response);
        part = definition.createPart();
        part.setName("parameters");
        part.setElementName(createQName(operationName + "Response"));
        response.addPart(part);

        Output output = definition.createOutput();
        output.setName(operationName + "Response");
        output.setMessage(response);
        operation.setOutput(output);

        portType.addOperation(operation);

        return operation;
    }


    private void addSchemaFragments(NodeList configs)
    {
        if (configs == null) {
            return;
        }

        Document doc = schemaElement.getOwnerDocument();
        Node newNode = null;
        for (int i = 0; i < configs.getLength(); i++) {
            Node privConf = configs.item(i);
            try {
                newNode = doc.importNode(privConf, true);
            }
            catch (DOMException e) {
                e.printStackTrace();
            }
            schemaElement.appendChild(newNode);
        }
    }

    /**
     * @return the HTTP SOAP address
     */
    public String getHttpSoapAddress()
    {
        return httpSoapAddress;
    }

    /**
     * @param httpSoapAddress
     */
    public void setHttpSoapAddress(String httpSoapAddress)
    {
        this.httpSoapAddress = httpSoapAddress;
    }

    /**
     * @return the HTTPS SOAP address
     */
    public String getHttpsSoapAddress()
    {
        return httpsSoapAddress;
    }

    /**
     * @param httpsSoapAddress
     */
    public void setHttpsSoapAddress(String httpsSoapAddress)
    {
        this.httpsSoapAddress = httpsSoapAddress;
    }

    /**
     * @return the directory containing the WSDL
     */
    public File getWsdlDirectory()
    {
        return wsdlDirectory;
    }

    /**
     * @param wsdlDirectory
     */
    public void setWsdlDirectory(File wsdlDirectory)
    {
        this.wsdlDirectory = wsdlDirectory;
    }
    private void addConcreteParam(GVWebService webService, PortType portType) throws WSDLException
    {
        Binding binding = null;
        Service service = addService(webService);
        if (webService.isSoapTransport()) {
            binding = addBinding(webService, portType);
            addBindingServiceSoap(webService, service, binding);
        }
        if (webService.isSoap12Transport()) {
            binding = addBinding12(webService, portType);
            addBindingServiceSoap12(webService, service, binding);
        }
        if (webService.isRestTransport()) {
            binding = addBindingRest(webService, portType);
            addBindingServiceRest(webService, service, binding);
        }
        if (webService.isJmsTransport()) {
            binding = addBindingJMS(webService, portType);
            addBindingServiceJms(webService, service, binding);
        }
    }
    private Service addService(GVWebService webService) throws WSDLException
    {
        Service service = definition.createService();
        service.setQName(createQName(webService.getServiceName()));
        definition.addService(service);
        service.setQName(createQName(webService.getServiceName()));
        return service;
    }

    private void addBindingServiceSoap(GVWebService webService, Service service, Binding binding)
            throws WSDLException
    {
        if (binding != null) {
            Port port = definition.createPort();
            port.setName(webService.getServiceName() + "_Soap");
            service.addPort(port);
            port.setBinding(binding);

            SOAPAddress soapAddress = (SOAPAddress) extensionRegistry.createExtension(Port.class, SOAP_ADDRESS);
            soapAddress.setLocationURI(webService.getSoapAddress());
            port.addExtensibilityElement(soapAddress);

        }
    }

    private void addBindingServiceSoap12(GVWebService webService, Service service, Binding binding)
            throws WSDLException
    {
        if (binding != null) {
        	System.out.println("binding12"+binding);
            Port port = definition.createPort();
            port.setName(webService.getServiceName() + "_Soap12");
            service.addPort(port);
            port.setBinding(binding);

            SOAP12Address soapAddress12 = (SOAP12Address) extensionRegistry.createExtension(Port.class, SOAP12_ADDRESS);
            soapAddress12.setLocationURI(webService.getSoapAddress());
            port.addExtensibilityElement(soapAddress12);
        }
    }

    private void addBindingServiceRest(GVWebService webService, Service service, Binding binding)
            throws WSDLException
    {
        if (binding != null) {
            Port port = definition.createPort();
            port.setName(webService.getServiceName() + "_HttpBinding");
            service.addPort(port);
            port.setBinding(binding);

            HTTPAddress restAddressHttp = (HTTPAddress) extensionRegistry.createExtension(Port.class, HTTP_ADDRESS);
            restAddressHttp.setLocationURI(webService.getSoapAddress());
            port.addExtensibilityElement(restAddressHttp);
        }
    }

    private void addBindingServiceJms(GVWebService webService, Service service, Binding binding)
            throws WSDLException
    {
        if (binding != null) {
            Port port = definition.createPort();
            port.setName(webService.getServiceName() + "_JmsBinding");
            service.addPort(port);
            port.setBinding(binding);

            SOAPAddress soapAddress = (SOAPAddress) extensionRegistry.createExtension(Port.class, SOAP_ADDRESS);
            soapAddress.setLocationURI("jms:jndi:" + webService.getJmsDestination() + "?targetService="
                    + webService.getServiceName() + ";replyToName=" + webService.getJmsReplyDestination());
            port.addExtensibilityElement(soapAddress);
        }
    }

    private PortType addPortType(GVWebService webService)
    {
        PortType portType = definition.createPortType();
        portType.setQName(createQName(webService.getServiceName() + "PortType"));
        portType.setUndefined(false);
        definition.addPortType(portType);
        Iterator<ServiceOperation> wsOp = webService.getServiceOperations();
        while(wsOp.hasNext()){
        	ServiceOperation operation = wsOp.next();
        	addOperation(portType, operation);
        	targetNamespace=operation.getTargetNameSpace();
        	definition.addNamespace(GV_PREFIX, targetNamespace);
        	definition.setTargetNamespace(operation.getTargetNameSpace());
        }
        return portType;
    }
    private Binding addBinding(GVWebService webService, PortType portType) throws WSDLException
    {
        Binding binding = definition.createBinding();
        binding.setQName(createQName(webService.getServiceName() + "_Soap"));
        binding.setUndefined(false);
        binding.setPortType(portType);
        definition.addBinding(binding);
        SOAPBinding soapBinding = (SOAPBinding) extensionRegistry.createExtension(Binding.class, SOAP_BINDING);
        soapBinding.setTransportURI(SOAP_OVER_HTTP);
        if (encoded) {
            soapBinding.setStyle("rpc");
        }
        else {
            soapBinding.setStyle("document");
        }
        binding.addExtensibilityElement(soapBinding);

        @SuppressWarnings("unchecked")
		Iterator<Operation> it = portType.getOperations().iterator();
        while (it.hasNext()) {
            Operation operation = it.next();
            String operationName = operation.getName();
            String soapAction = null;
            
            ServiceOperation serviceOperation = webService.getOperation(operationName);
            if (serviceOperation != null) {
                soapAction = serviceOperation.getSoapAction();
            }

            if ((soapAction == null) || "".equals(soapAction)) {
                soapAction = webService.getServiceName() + "/" + operationName;
            }

            addBindingOperation(binding, operation, soapAction);
        }

        return binding;
    }

    private BindingOperation addBindingOperation(Binding binding, Operation operation, String soapAction)
            throws WSDLException
    {
        BindingOperation bindingOperation = definition.createBindingOperation();
        bindingOperation.setName(operation.getName());
        bindingOperation.setOperation(operation);

        BindingInput bindingInput = definition.createBindingInput();
        bindingInput.setName(operation.getInput().getName());
        bindingOperation.setBindingInput(bindingInput);

        SOAPBody soapBody = (SOAPBody) extensionRegistry.createExtension(BindingInput.class, SOAP_BODY);
        if (encoded) {
            soapBody.setUse("encoded");
            soapBody.setEncodingStyles(ENCODING_STYLES);
        }
        else {
            soapBody.setUse("literal");
        }
        bindingInput.addExtensibilityElement(soapBody);

        BindingOutput bindingOutput = definition.createBindingOutput();
        bindingOutput.setName(operation.getOutput().getName());
        bindingOperation.setBindingOutput(bindingOutput);

        soapBody = (SOAPBody) extensionRegistry.createExtension(BindingOutput.class, SOAP_BODY);
        if (encoded) {
            soapBody.setUse("encoded");
            soapBody.setEncodingStyles(ENCODING_STYLES);
        }
        else {
            soapBody.setUse("literal");
        }
        bindingOutput.addExtensibilityElement(soapBody);

        binding.addBindingOperation(bindingOperation);

        SOAPOperation soapOperation = (SOAPOperation) extensionRegistry.createExtension(BindingOperation.class,
                SOAP_OPERATION);
        soapOperation.setSoapActionURI(soapAction);
        if (encoded) {
            soapOperation.setStyle("rpc");
        }
        else {
            soapOperation.setStyle("document");
        }
        bindingOperation.addExtensibilityElement(soapOperation);

        return bindingOperation;
    }

    @SuppressWarnings("unchecked")
    private Binding addBinding12(GVWebService webService, PortType portType) throws WSDLException
    {
        Binding binding = definition.createBinding();
        binding.setQName(createQName(webService.getServiceName() + "_Soap12"));
        binding.setUndefined(false);
        binding.setPortType(portType);
        definition.addBinding(binding);

        SOAP12Binding soapBinding = (SOAP12Binding) extensionRegistry.createExtension(Binding.class, SOAP12_BINDING);
        soapBinding.setTransportURI(SOAP_OVER_HTTP);
        if (encoded) {
            soapBinding.setStyle("rpc");
        }
        else {
            soapBinding.setStyle("document");
        }
        binding.addExtensibilityElement(soapBinding);

        Iterator<Operation> it = portType.getOperations().iterator();
        while (it.hasNext()) {
            Operation operation = it.next();
            String operationName = operation.getName();
            String soapAction = null;
            ServiceOperation serviceOperation = webService.getOperation(operationName);
            if (serviceOperation != null) {
                soapAction = serviceOperation.getSoapAction();
            }

            if ((soapAction == null) || "".equals(soapAction)) {
                soapAction = webService.getServiceName() + "/" + operationName;
            }

            addBindingOperation12(binding, operation, soapAction);
        }
        System.out.println("binding"+binding);
        return binding;
    }

    @SuppressWarnings("unchecked")
    private Binding addBindingJMS(GVWebService webService, PortType portType) throws WSDLException
    {
        Binding binding = definition.createBinding();
        binding.setQName(createQName(webService.getServiceName() + "_Jms"));
        binding.setUndefined(false);
        binding.setPortType(portType);
        definition.addBinding(binding);
        JMSBinding jmsBinding = null;
        try {

            JMSBindingSerializer soapBindingSer = new JMSBindingSerializer();
            extensionRegistry.registerSerializer(Binding.class, JMSBindingImpl.Q_ELEM_JMS_BINDING, soapBindingSer);
            extensionRegistry.registerDeserializer(Binding.class, JMSBindingImpl.Q_ELEM_JMS_BINDING, soapBindingSer);
            extensionRegistry.mapExtensionTypes(Binding.class, JMSBindingImpl.Q_ELEM_JMS_BINDING, JMSBindingImpl.class);
            jmsBinding = (JMSBinding) extensionRegistry.createExtension(Binding.class,
                    JMSBindingImpl.Q_ELEM_JMS_BINDING);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        jmsBinding.setTransportURI(SOAP_OVER_HTTP);
        if (encoded) {
            jmsBinding.setStyle("rpc");
        }
        else {
            jmsBinding.setStyle("document");
        }
        jmsBinding.setJndiConnectionFactoryName(webService.getJndiConnectionFactoryName());
        jmsBinding.setJndiInitialContextFactory(webService.getJndiInitialContextFactory());
        jmsBinding.setJndiURL(webService.getJndiURL());
        if (webService.getJmsReplyDestination() != null) {
            if (webService.getJmsDestinationType().equals("queue"))
                jmsBinding.setReplyToName(webService.getJmsReplyDestination());
            else
                jmsBinding.setTopicReplyToName(webService.getJmsReplyDestination());
        }

        binding.addExtensibilityElement(jmsBinding);

        Iterator<Operation> it = portType.getOperations().iterator();
        while (it.hasNext()) {
            Operation operation = it.next();
            String operationName = operation.getName();
            String soapAction = null;
            ServiceOperation serviceOperation = webService.getOperation(operationName);
            if (serviceOperation != null) {
                soapAction = serviceOperation.getSoapAction();
            }

            if ((soapAction == null) || "".equals(soapAction)) {
                soapAction = webService.getServiceName() + "/" + operationName;
            }

            addBindingOperation(binding, operation, soapAction);
        }

        return binding;
    }

    @SuppressWarnings("unchecked")
    private Binding addBindingRest(GVWebService webService, PortType portType) throws WSDLException
    {
        Binding binding = definition.createBinding();
        binding.setQName(createQName(webService.getServiceName() + "_HttpBinding"));
        binding.setUndefined(false);
        binding.setPortType(portType);
        definition.addBinding(binding);

        HTTPBinding httpBinding = (HTTPBinding) extensionRegistry.createExtension(Binding.class, HTTP_BINDING);
        httpBinding.setVerb(webService.getRestVerb());
        binding.addExtensibilityElement(httpBinding);

        Iterator<Operation> it = portType.getOperations().iterator();
        while (it.hasNext()) {
            Operation operation = it.next();
            String operationName = operation.getName();
            String soapAction = null;
            ServiceOperation serviceOperation = webService.getOperation(operationName);
            if (serviceOperation != null) {
                soapAction = serviceOperation.getSoapAction();
            }

            if ((soapAction == null) || "".equals(soapAction)) {
                soapAction = webService.getServiceName() + "/" + operationName;
            }

            addBindingOperationHttp(webService, binding, operation, soapAction);
        }

        return binding;
    }

    private BindingOperation addBindingOperationHttp(GVWebService webService, Binding binding, Operation operation,
            String soapAction) throws WSDLException
    {
        BindingOperation bindingOperation = definition.createBindingOperation();
        bindingOperation.setName(operation.getName());
        bindingOperation.setOperation(operation);

        BindingInput bindingInput = definition.createBindingInput();
        bindingInput.setName(operation.getInput().getName());
        bindingOperation.setBindingInput(bindingInput);
        MIMEContent mimeContent = new MIMEContentImpl();
        mimeContent.setType("text/xml");
        mimeContent.setPart(operation.getInput().getName());
        bindingInput.addExtensibilityElement(mimeContent);

        BindingOutput bindingOutput = definition.createBindingOutput();
        bindingOutput.setName(operation.getOutput().getName());
        bindingOperation.setBindingOutput(bindingOutput);
        bindingOutput.addExtensibilityElement(mimeContent);

        binding.addBindingOperation(bindingOperation);

        HTTPOperation httpOperation = (HTTPOperation) extensionRegistry.createExtension(BindingOperation.class,
                HTTP_OPERATION);
        httpOperation.setLocationURI(webService.getServiceName() + "/" + operation.getName());
        bindingOperation.addExtensibilityElement(httpOperation);

        return bindingOperation;
    }

    private BindingOperation addBindingOperation12(Binding binding, Operation operation, String soapAction)
            throws WSDLException
    {
        BindingOperation bindingOperation = definition.createBindingOperation();
        bindingOperation.setName(operation.getName());
        bindingOperation.setOperation(operation);

        BindingInput bindingInput = definition.createBindingInput();
        bindingInput.setName(operation.getInput().getName());
        bindingOperation.setBindingInput(bindingInput);

        SOAP12Body soapBody = (SOAP12Body) extensionRegistry.createExtension(BindingInput.class, SOAP12_BODY);
        if (encoded) {
            soapBody.setUse("encoded");
            soapBody.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
        }
        else {
            soapBody.setUse("literal");
        }
        bindingInput.addExtensibilityElement(soapBody);

        BindingOutput bindingOutput = definition.createBindingOutput();
        bindingOutput.setName(operation.getOutput().getName());
        bindingOperation.setBindingOutput(bindingOutput);

        soapBody = (SOAP12Body) extensionRegistry.createExtension(BindingOutput.class, SOAP12_BODY);
        if (encoded) {
            soapBody.setUse("encoded");
            soapBody.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
        }
        else {
            soapBody.setUse("literal");
        }
        bindingOutput.addExtensibilityElement(soapBody);

        binding.addBindingOperation(bindingOperation);

        SOAP12Operation soapOperation = (SOAP12Operation) extensionRegistry.createExtension(BindingOperation.class,
                SOAP12_OPERATION);
        soapOperation.setSoapActionURI(soapAction);
        if (encoded) {
            soapOperation.setStyle("rpc");
        }
        else {
            soapOperation.setStyle("document");
        }
        bindingOperation.addExtensibilityElement(soapOperation);

        return bindingOperation;
    }

}
