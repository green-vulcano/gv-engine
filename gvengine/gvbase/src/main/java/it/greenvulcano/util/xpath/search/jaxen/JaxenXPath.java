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

package it.greenvulcano.util.xpath.search.jaxen;

import java.util.List;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.BaseXPath;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Nov 1, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JaxenXPath
{

    private String           xpathExpr;
    private FunctionContext  functionContext;
    private NamespaceContext namespaceContext;
    private BaseXPath        xpath;

    /**
     * @param xpathExpr
     * @throws JaxenException
     */
    protected JaxenXPath(String xpathExpr) throws JaxenException
    {
        this.xpathExpr = xpathExpr;
    }

    /**
     * @param contextNode
     * @return
     * @throws JaxenException
     */
    @SuppressWarnings("rawtypes")
	public List selectNodes(Object contextNode) throws JaxenException
    {
        return getXPath(contextNode).selectNodes(contextNode);
    }

    /**
     * @param instance
     */
    public void setFunctionContext(FunctionContext fc)
    {
        this.functionContext = fc;
    }

    /**
     * @param instance
     */
    public void setNamespaceContext(NamespaceContext nc)
    {
        this.namespaceContext = nc;
    }

    private BaseXPath getXPath(Object context) throws JaxenException
    {
        if (xpath == null) {
            if (context instanceof OMNode) {
                xpath = new AXIOMXPath(xpathExpr);
            }
            else if (context instanceof Node) {
                xpath = new DOMXPath(xpathExpr);
            }
            xpath.setFunctionContext(functionContext);
            xpath.setNamespaceContext(namespaceContext);
        }
        return xpath;
    }

}
