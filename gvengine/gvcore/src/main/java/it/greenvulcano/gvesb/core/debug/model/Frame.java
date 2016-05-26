/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.debug.model;

import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.debug.DebuggerException;
import it.greenvulcano.gvesb.core.debug.ExecutionInfo;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 3.3.0 Dic 14, 2012
 * @author GreenVulcano Developer Team
 */
public class Frame extends DebuggerObject
{
    /**
     * 
     */
    private static final long       serialVersionUID = 1L;
    private Map<String, Variable>   vars             = null;
    private String                  serviceName;
    private String                  operationName;
    private String                  flowNode;
    private String                  frameName;
    private String                  subflow;
    private transient ExecutionInfo execInfo;

    public Frame(ExecutionInfo execInfo)
    {
        super();
        this.serviceName = execInfo.getService();
        this.operationName = execInfo.getOperation();
        this.flowNode = execInfo.getNodeId();
        this.subflow = execInfo.getSubflow();
        this.frameName = execInfo.getUniqueKey();
        this.execInfo = execInfo;
        vars = new LinkedHashMap<String, Variable>();
        loadInfo();
    }

    public Variable getVar(String env, String key) throws DebuggerException
    {
        Variable varEnv = vars.get(env);
        if (varEnv == null) {
            throw new DebuggerException("Environment '" + env + "' not found");
        }
        return varEnv.getVar(key);
    }

    public void setVar(String env, String varID, String varValue) throws GVException
    {
        Variable varEnv = null;
        if (env != null) {
            varEnv = vars.get(env);
        }
        if (varEnv != null) {
            varEnv.setVar(varID, varValue);
        }
    }

    public Map<String, Variable> getVars()
    {
        return vars;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(String sName)
    {
        this.serviceName = sName;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName(String opName)
    {
        this.operationName = opName;
    }

    public String getFlowNode()
    {
        return flowNode;
    }

    public void setFlowNode(String flowNode)
    {
        this.flowNode = flowNode;
    }

    public String getFrameName()
    {
        return frameName;
    }

    /**
     * @see it.greenvulcano.gvesb.core.debug.model.DebuggerObject#getXML(it.greenvulcano.util.xml.XMLUtils,
     *      org.w3c.dom.Document)
     */
    @Override
    protected Node getXML(XMLUtils xml, Document doc) throws XMLUtilsException
    {
        Element frame = xml.createElement(doc, "Frame");
        frame.setAttribute("service_name", serviceName);
        frame.setAttribute("operation_name", operationName);
        frame.setAttribute("subflow", subflow);
        frame.setAttribute("flow_node", flowNode);
        frame.setAttribute("name", frameName);
        if (vars != null && vars.size() > 0) {
            Element varEl = xml.insertElement(frame, "Variables");
            for (String e : vars.keySet()) {
                Element var = xml.insertElement(varEl, Variable.ELEMENT_TAG);
                xml.setAttribute(var, NAME_ATTR, e);
                xml.setAttribute(var, ID_ATTR, vars.get(e).getID());
                xml.setAttribute(var, TYPE_ATTR, vars.get(e).getTypeName());
            }
        }
        return frame;
    }

    private void loadInfo()
    {
        Map<String, Object> environment = execInfo.getEnvironment();
        Set<String> envEntryKeys = environment.keySet();
        if (envEntryKeys != null) {
            for (String key : envEntryKeys) {
                Object envEntry = environment.get(key);
                if (envEntry != null) {
                    Variable v = new Variable(key, envEntry.getClass(), envEntry);
                    vars.put(key, v);
                }
            }
        }
    }

}
