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
package it.greenvulcano.gvesb.virtual.ws.dynamic.descr;

import it.greenvulcano.gvesb.virtual.ws.WSCallException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.slf4j.Logger;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ServiceDescrBuilder
{
    private final static Logger             logger = org.slf4j.LoggerFactory.getLogger(ServiceDescrBuilder.class);

    private Definition                      definition_;
    private Map<String, ServiceDescription> services_;
    private String                          wsdlLocation_;

    /**
     * @param wsdlLocation
     */
    public ServiceDescrBuilder(String wsdlLocation)
    {
        this.wsdlLocation_ = wsdlLocation;
    }

    /**
     * Get the ServiceDescription Information with all necessary information
     * about the services_ defined by the WSDL file concerning operations and
     * parameters
     * 
     * @return the Service Description Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, ServiceDescription> loadServices() throws WSCallException
    {
        Map<String, ServiceDescription> services = new LinkedHashMap<String, ServiceDescription>();

        try {
            Definition def = readWsdl(wsdlLocation_);

            if (def != null) {
                definition_ = def;

                ParamDescBuilder wsdlConverter = new XmlSchemaParameterDescrBuilder(def.getTargetNamespace());

                wsdlConverter.resolveSchema(def, wsdlLocation_);

                // ******
                // SERVICE Elements
                // ******
                Collection<Service> defSet = def.getServices().values();

                for (Service service : defSet) {

                    // ******
                    // PORT Elements
                    // ******
                    Collection<Port> portSet = service.getPorts().values();
                    for (Port port : portSet) {

                        ServiceDescription sd = new ServiceDescription();
                        sd.setXmlName(service.getQName());

                        String key = service.getQName() + "_" + port.getName();
                        services.put(key, sd);
                        logger.debug("Added service description for key: " + key);

                        Binding binding = port.getBinding();

                        retrievePortParameters(port, sd);

                        // **********
                        // PORTTYPE Element
                        // **********
                        PortType portType = binding.getPortType();

                        sd.setPortTypeXmlName(portType.getQName());

                        List<Operation> operationList = portType.getOperations();

                        // set Operations to the service
                        Map<String, OperationDescription> operations = new LinkedHashMap<String, OperationDescription>();

                        sd.setDescriptions(operations);

                        // **********
                        // OPERATION Element
                        // **********
                        for (Operation op : operationList) {

                            OperationDescription opDesc = new OperationDescription();
                            operations.put(op.getName(), opDesc);

                            Input input = op.getInput();
                            Output output = op.getOutput();

                            // the namespace uri of message element is the same
                            // as the operation uri!!
                            opDesc.setXmlName(new QName(input.getMessage().getQName().getNamespaceURI(), op.getName()));
                            retrieveBindingParameters(binding, opDesc);

                            // **************
                            // HEADER Elements
                            // **************
                            Part headerPart = null;
                            if (opDesc.isUseSOAP12Namespace()) {
                                SOAP12Header header = opDesc.getSOAP12Header();
                                if (header != null) {
                                    Message headerMsg = def.getMessage(header.getMessage());
                                    headerPart = headerMsg.getPart(header.getPart());
                                }
                            }
                            else {
                                SOAPHeader header = opDesc.getSOAPHeader();
                                if (header != null) {
                                    Message headerMsg = def.getMessage(header.getMessage());
                                    headerPart = headerMsg.getPart(header.getPart());
                                }
                            }
                            if (headerPart != null) {
                                ParamDescription pdesc = wsdlConverter.getParameterDescForPart(headerPart);
                                Map<String, ParamDescription> headerMap = new LinkedHashMap<String, ParamDescription>();
                                headerMap.put(headerPart.getName(), pdesc);
                                opDesc.setHeaderDescriptions(headerMap);
                                opDesc.setHeaderPart(new String[]{headerPart.getName()});
                            }

                            // **************
                            // MESSAGE Elements
                            // **************
                            opDesc.setInputMessageXmlName(input.getMessage().getQName());
                            opDesc.setOutputMessageXmlName(output.getMessage().getQName());

                            // **************
                            // PART Elements
                            // **************
                            List<Part> partList = input.getMessage().getOrderedParts(null);

                            Map<String, ParamDescription> p = wsdlConverter.getParameterDescs(op.getName(), partList);
                            String[] partNames = new String[partList.size()];

                            for (int z = 0, n = partList.size(); z < n; z++) {
                                partNames[z] = partList.get(z).getName();
                            }

                            opDesc.setInputPart(partNames);
                            opDesc.setDescriptions(p);
                            opDesc.setWrapped(wsdlConverter.isWrapped());

                            partList = output.getMessage().getOrderedParts(null);
                            p = wsdlConverter.getParameterDescs(null, partList);
                            partNames = new String[partList.size()];

                            for (int z = 0, n = partList.size(); z < n; z++) {
                                partNames[z] = partList.get(z).getName();
                            }

                            opDesc.setOutputPart(partNames);
                            opDesc.setOutputDescriptions(p);
                            opDesc.setWrapped(wsdlConverter.isWrapped());
                        }
                    }
                }
            }
        }
        catch (WSDLException exc) {
            throw new WSCallException("ERROR_LOADING_WSDL", new String[][]{{"cause",
                    "Error while loading WSDL " + wsdlLocation_}}, exc);
        }
        return services;
    }

    /**
     * Retrieves the <code>SOAPAdress</code> of the <code>Port</code>. We assume
     * that there is only one Port Element for each Service Element in the WSDL
     * file.
     * 
     * @param port
     *        The PortType of the WSDL file
     * @param service
     *        stores Service Information
     */
    private static void retrievePortParameters(Port port, ServiceDescription service)
    {
        List<?> list = port.getExtensibilityElements();

        for (int n = list.size() - 1; 0 <= n; n--) {
            Object o = list.get(n);

            if (o instanceof SOAPAddress) {
                SOAPAddress address = (SOAPAddress) o;
                service.setAddress(address.getLocationURI());
            }
            else if (o instanceof SOAP12Address) {
                SOAP12Address address = (SOAP12Address) o;
                service.setAddress(address.getLocationURI());
            }
            else if (o instanceof HTTPAddress) {
                HTTPAddress address = (HTTPAddress) o;
                service.setAddress(address.getLocationURI());
            }
            else {
                logger.warn("Unidentified PortListElement: " + o.getClass());
            }
        }
    }

    /**
     * Get the Style (literal or encoded) out of the Binding and stores it into
     * the given OperationDescription.
     * 
     * @param binding
     *        The Binding of the WSDL file
     * @param operation
     *        stores Operation Information
     */
    private static void retrieveBindingParameters(Binding binding, OperationDescription operation)
    {
        List<?> list = binding.getExtensibilityElements();

        for (int n = list.size() - 1; 0 <= n; n--) {
            Object o = list.get(n);

            if (o instanceof SOAPBinding) {
                SOAPBinding bind = (SOAPBinding) o;
                operation.setStyle(bind.getStyle());
                operation.setUseSOAP12Namespace(false);
            }
            else if (o instanceof SOAP12Binding) {
                SOAP12Binding bind = (SOAP12Binding) o;
                operation.setStyle(bind.getStyle());
                operation.setUseSOAP12Namespace(true);
            }
            else if (o instanceof HTTPBinding) {
                HTTPBinding bind = (HTTPBinding) o;
                operation.setVerb(bind.getVerb());
            }
            else {
                logger.warn("Unidentified BindingListElement: " + ((ExtensibilityElement) o).getElementType() + " - "
                        + o.getClass());
            }
        }

        BindingOperation bindingOperation = binding.getBindingOperation(operation.getXmlName().getLocalPart(), null,
                null);

        list = bindingOperation.getExtensibilityElements();
        if (list != null) {
            for (int n = list.size() - 1; 0 <= n; n--) {
                Object o = list.get(n);

                if (o instanceof SOAPOperation) {
                    SOAPOperation soapOp = (SOAPOperation) o;
                    operation.setSOAPAction(soapOp.getSoapActionURI());
                }
                else if (o instanceof SOAP12Operation) {
                    SOAP12Operation soapOp = (SOAP12Operation) o;
                    operation.setSOAPAction(soapOp.getSoapActionURI());
                } else {
                    logger.warn("Unidentified BindingOperationListElement: "
                            + ((ExtensibilityElement) o).getElementType() + " - " + o.getClass());
                }
            }
        }

        list = bindingOperation.getBindingInput().getExtensibilityElements();

        for (int n = list.size() - 1; 0 <= n; n--) {
            Object o = list.get(n);

            if (o instanceof SOAPBody) {
                SOAPBody body = (SOAPBody) o;
                operation.setEncoding(body.getUse());
            }
            else if (o instanceof SOAP12Body) {
                SOAP12Body body = (SOAP12Body) o;
                operation.setEncoding(body.getUse());
            }
            else if (o instanceof SOAPHeader) {
                SOAPHeader header = (SOAPHeader) o;
                operation.setSOAPHeader(header);
            }
            else if (o instanceof SOAP12Header) {
                SOAP12Header header = (SOAP12Header) o;
                operation.setSOAP12Header(header);
            }
            else if (o instanceof MIMEContent) {
                MIMEContent header = (MIMEContent) o;
                operation.setMediaType(header.getType());
            }
            else {
                logger.warn("Unidentified BindingInputOperationListElement: "
                        + ((ExtensibilityElement) o).getElementType() + " - " + o.getClass());
            }
        }
    }

    private static Definition readWsdl(String wsdlLoc) throws WSDLException  {
    	return WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlLoc);
    }

    /**
     * @return the definition
     * @throws WSDLException
     */
    public Definition getDefinition() throws WSDLException
    {
        if (definition_ == null) {
            definition_ = readWsdl(wsdlLocation_);
        }

        return definition_;
    }

    /**
     * @return the services
     * @throws WSCallException
     */
    public Map<String, ServiceDescription> getServices() throws WSCallException
    {
        if (services_ == null) {
            services_ = loadServices();
        }

        return services_;
    }

    /**
     * @return the WSDL location
     */
    public String getWsdlLocation()
    {
        return this.wsdlLocation_;
    }

    /**
     * if we set location we also read and parse it
     * 
     * 
     * @param wsdlLocation
     */
    public void setWsdlLocation(String wsdlLocation)
    {
        this.wsdlLocation_ = wsdlLocation;
        definition_ = null;
        services_ = null;
    }
}
