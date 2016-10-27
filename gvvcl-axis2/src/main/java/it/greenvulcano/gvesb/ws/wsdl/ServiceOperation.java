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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;


/**
 * ServiceOperation class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class ServiceOperation
{

    private String              operationQName;
    private String              operationName;
    private String              soapAction      = null;
    private Map<String, String> attributes      = new HashMap<String, String>();
    private String              targetNameSpace = null;
    private String              policyData      = null;

    /**
     * @param operationQName
     */
    public ServiceOperation(String operationQName)
    {
        this.operationQName = operationQName;
        this.operationName = getLocalName(operationQName);
    }

    /**
     * @param operationConf
     * @throws Exception
     */
    public ServiceOperation(Node operationConf) throws Exception
    {
        operationQName = XMLConfig.get(operationConf, "@operation-qname");
        operationName = getLocalName(operationQName);
        setTargetNameSpace(getNameSpace(operationQName));
        soapAction = XMLConfig.get(operationConf, "@soap-action", null);

        policyData = XMLConfig.get(operationConf, "PolicyData", null);
    }

    private String getLocalName(String qname)
    {
        if (qname.startsWith("{") && (qname.indexOf('}') > 0)) {
            qname = qname.substring(qname.indexOf('}') + 1);
        }
        return qname;
    }

    private String getNameSpace(String qname)
    {
        if (qname == null) {
            return "";
        }
        if (qname.startsWith("{") && (qname.indexOf('}') > 0)) { //$NON-NLS-1$
            return qname.substring(1, qname.indexOf('}'));
        }
        return "";
    }

    /**
     * @return Returns the operation QName.
     */
    public String getOperationQName()
    {
        return operationQName;
    }

    /**
     * @return Returns the operation local name.
     */
    public String getOperationName()
    {
        return operationName;
    }

    /**
     * @param operationQName
     *        The operation QName to set.
     */
    public void setOperationName(String operationQName)
    {
        this.operationQName = operationQName;
        operationName = getLocalName(operationQName);
    }

    /**
     * @return Returns the soapAction.
     */
    public String getSoapAction()
    {
        return soapAction;
    }

    /**
     * @param soapAction
     *        The soapAction to set.
     */
    public void setSoapAction(String soapAction)
    {
        this.soapAction = soapAction;
    }

    /**
     * @param attribute
     * @param value
     */
    public void setAttribute(String attribute, String value)
    {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        attributes.put(attribute, value);
    }

    /**
     * @param attribute
     * @return the attribute value
     */
    public String getAttribute(String attribute)
    {
        if (attributes == null) {
            return null;
        }
        return attributes.get(attribute);
    }

    /**
     * @param attribute
     */
    public void removeAttribute(String attribute)
    {
        if (attributes == null) {
            return;
        }
        attributes.remove(attribute);
        if (attributes.size() == 0) {
            attributes = null;
        }
    }


    public String getPolicyData()
    {
        return this.policyData;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("\nService Operation: \n");

        sb.append("\toperationName '").append(operationName).append("' \n\tsoapAction '").append(soapAction).append(
                "\n");

        // Attributes
        Set<String> keyDataSet = attributes.keySet();
        Iterator<String> iterDataSet = keyDataSet.iterator();
        String key = "";
        String data;
        if (iterDataSet.hasNext()) {
            sb.append("\tThe attributes:\n");
            while (iterDataSet.hasNext()) {
                key = iterDataSet.next();
                try {
                    data = attributes.get(key);
                }
                catch (IllegalArgumentException e) {
                    data = "IllegalArgumentException";
                }
                sb.append("\t\t").append(key).append("\t=\t").append(data).append("\n");
            }
        }
        else {
            sb.append("\tThere aren't attributes.\n ");
        }
        sb.append("\n");

        return sb.toString();
    }

    public String getTargetNameSpace()
    {
        return targetNameSpace;
    }

    public void setTargetNameSpace(String targetNameSpace)
    {
        this.targetNameSpace = targetNameSpace;
    }
}
