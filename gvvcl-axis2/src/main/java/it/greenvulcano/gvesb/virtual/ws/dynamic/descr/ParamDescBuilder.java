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
package it.greenvulcano.gvesb.virtual.ws.dynamic.descr;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.WSDLException;

/**
 * ParamDescBuilder class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public abstract class ParamDescBuilder
{
    boolean wrapped_;

    /**
     * Creates a new instance of ParamDescBuilder
     */
    public ParamDescBuilder()
    {
    }

    /**
     * Get all the ParameterDescriptions for a WSDL Part Element. In Order to
     * get the information which parameters an operation has, you have to use
     * this method with the Part list.
     *
     * @param operationName
     *
     * @param parts
     *        A list of Parts
     * @return The ParameterDescriptions
     * @throws WSDLException
     *         If an error occur
     */
    public Map<String, ParamDescription> getParameterDescs(String operationName, List<Part> parts) throws WSDLException
    {
        Map<String, ParamDescription> parameters = new LinkedHashMap<String, ParamDescription>();
        Part p = getWrappedDocLiteralPart(parts);

        if (p != null) {
            parameters.put(p.getName(), getParameterDescForPart(p));

            if (operationName != null) {
                if (operationName.equals(p.getElementName().getLocalPart())) {
                    wrapped_ = true;
                }
            }
        }
        else {
            // get parts in correct order
            for (int i = 0, n = parts.size(); i < n; ++i) {
                Part part = parts.get(i);
                ParamDescription parameter = getParameterDescForPart(part);

                parameters.put(part.getName(), parameter);
            }
        }

        return parameters;
    }

    /**
     * Gets the wrapped_ Part if this is wrapped_ document literal type
     * operation. An operation is wrapped_ if: - there is only one input or
     * output message part and that part is an element not a type (MIME means
     * there can be many parts, so all this can check is that there is only one
     * element part) - the message xmlName_ is the same as the operation
     * xmlName_ (for a response the operation xmlName_ is appended with
     * "Response")
     *
     * @param parts
     * @return the wrapped document literal part
     */
    public Part getWrappedDocLiteralPart(List<Part> parts)
    {
        boolean wrapped = (parts != null) && (parts.size() == 1);
        Part elementPart = null;

        if (wrapped) {
            Part p = (Part) parts.get(0);

            if (p.getElementName() != null) {
                if (elementPart == null) {
                    elementPart = p;
                }
            }
        }

        return elementPart;
    }

    /**
     * @return if the document is wrapped
     */
    public boolean isWrapped()
    {
        return wrapped_;
    }

    /**
     * @param p
     * @return the parameter description for the part
     * @throws WSDLException
     */
    protected abstract ParamDescription getParameterDescForPart(Part p) throws WSDLException;

    /**
     * @param def
     * @param wsdlLocation
     * @throws WSDLException
     */
    public abstract void resolveSchema(Definition def, String wsdlLocation) throws WSDLException;
}
