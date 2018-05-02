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

import it.greenvulcano.util.xpath.search.XPathFunction;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xpath.NodeSet;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.w3c.dom.Node;

/**
 * Encapsulate a XPathFunction into a Jaxen Function.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JaxenXPathFunction implements Function
{
    /**
     * Extension function.
     */
    private XPathFunction function;

    /**
     * Creates a new Jaxen Function encapsulating a XPathFunction.
     *
     * @param function
     *        the encapsulated XPath function
     */
    public JaxenXPathFunction(XPathFunction function)
    {
        this.function = function;
    }

    /**
     * @see org.jaxen.Function#call(org.jaxen.Context, java.util.List)
     */
    public Object call(Context context, List args) throws FunctionCallException
    {
        Object[] objects = args.toArray();

        // Invoca la funzione
        //
        Node contextNode = (Node) context.getNodeSet().get(0);
        Object object;
        try {
            object = function.evaluate(contextNode, objects);
        }
        catch (TransformerException e) {
            e.printStackTrace();
            throw new FunctionCallException(e);
        }

        if (object == null) {
            return null;
        }

        // Prepara i NodeSet nel caso di collezioni di Nodi
        //
        if (object instanceof Collection) {
            NodeSet nodeSet = new NodeSet();
            Collection list = (Collection) object;
            Iterator<Object> i = list.iterator();
            while (i.hasNext()) {
                nodeSet.addNode((Node) i.next());
            }
            return nodeSet;
        }
        Class cls = object.getClass();
        if (cls.isArray()) {
            NodeSet nodeSet = new NodeSet();
            Node[] nodes = (Node[]) object;
            for (int i = 0; i < nodes.length; ++i) {
                nodeSet.addNode(nodes[i]);
            }
            return nodeSet;
        }

        return object;
    }
}
