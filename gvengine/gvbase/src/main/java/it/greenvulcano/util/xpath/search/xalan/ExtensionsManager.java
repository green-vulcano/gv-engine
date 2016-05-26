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
package it.greenvulcano.util.xpath.search.xalan;

import it.greenvulcano.util.xpath.search.XPathFunction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.NodeSet;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;

/**
 * Handle the XPath extensions.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
@SuppressWarnings("rawtypes")
class ExtensionsManager implements ExtensionsProvider
{
    /**
     * Singleton reference.
     */
    private static ExtensionsManager                _instance          = null;

    /**
     * Installed functions. <br/>
     * Map[String name, XPathFunction]
     */
    private Map<String, Map<String, XPathFunction>> installedFunctions = new HashMap<String, Map<String, XPathFunction>>();

    /**
     * Register for every Thread the context node from wich start XPath evaluation.
     * The XPathAPI class handle the insertion/extraction of the context when the
     * evaluation start/end<br/>
     * Map[Thread, Stack[Node]]
     */
    private Map<Thread, Stack<Node>>                contextNodes       = new HashMap<Thread, Stack<Node>>();

    /**
     * Return the singleton instance.
     */
    static synchronized ExtensionsManager instance()
    {
        if (_instance == null) {
            _instance = new ExtensionsManager();
        }
        return _instance;
    }

    private ExtensionsManager()
    {
        // do nothing
    }

    /**
     * Register an XPath extension function.
     *
     * @param name function name.
     * @param function function implementation.
     */
    synchronized void installFunction(String namespace, String name, XPathFunction function)
    {
        Map<String, XPathFunction> functions = installedFunctions.get(namespace);
        if (functions == null) {
            functions = new HashMap<String, XPathFunction>();
            installedFunctions.put(namespace, functions);
        }
        functions.put(name, function);
    }

    /**
     * Associate the current Thread to the context node.
     *
     */
    synchronized void startEvaluation(Node contextNode)
    {
        Thread thread = Thread.currentThread();
        Stack<Node> stack = contextNodes.get(thread);
        if (stack == null) {
            stack = new Stack<Node>();
            contextNodes.put(thread, stack);
        }
        stack.push(contextNode);
    }

    /**
     * Remove the association from the current Thread and the context.
     */
    synchronized void endEvaluation()
    {
        Thread thread = Thread.currentThread();
        Stack<Node> stack = contextNodes.get(thread);
        if (stack != null) {
            stack.pop();
            if (stack.empty()) {
                contextNodes.remove(thread);
            }
        }
    }

    /**
     * Return the current Thread's context node.
     *
     */
    private synchronized Node getContextNode()
    {
        Thread thread = Thread.currentThread();
        Stack<Node> stack = contextNodes.get(thread);
        if (stack == null) {
            return null;
        }
        return stack.peek();
    }

    public boolean elementAvailable(String ns, String elemName)
    {
        return false;
    }

    public Object extFunction(String ns, String funcName, Vector argVec, Object methodKey) throws TransformerException
    {
        Map<String, XPathFunction> functions = installedFunctions.get(ns);
        if (functions == null) {
            throw new TransformerException("Invalid namespace: " + ns);
        }

        XPathFunction function = functions.get(funcName);
        if (function == null) {
            throw new TransformerException("Invalid function: " + funcName + " for namespace " + ns);
        }

        Object[] params = new Object[argVec.size()];
        for (int i = 0; i < params.length; ++i) {
            XObject xobject = (XObject) argVec.elementAt(i);
            switch (xobject.getType()) {

                case XObject.CLASS_STRING :
                    params[i] = xobject.str();
                    break;

                case XObject.CLASS_NODESET :
                    params[i] = xobject.nodelist();
                    break;

                case XObject.CLASS_NUMBER :
                    params[i] = new Double(xobject.num());
                    break;

                case XObject.CLASS_BOOLEAN :
                    params[i] = Boolean.valueOf(xobject.bool());
                    break;

                case XObject.CLASS_NULL :
                    params[i] = null;
                    break;

                case XObject.CLASS_RTREEFRAG :
                    params[i] = xobject.rtree();
                    break;

                default :
                    params[i] = xobject.object();
                    break;
            }
        }

        Node contextNode = getContextNode();
        Object object = function.evaluate(contextNode, params);

        if (object == null) {
            return null;
        }

        if (object instanceof List) {
            NodeSet nodeSet = new NodeSet();
            List list = (List) object;
            Iterator i = list.iterator();
            while (i.hasNext()) {
                nodeSet.addNode((Node) i.next());
            }
            return nodeSet;
        }
        else if (object instanceof Set) {
            NodeSet nodeSet = new NodeSet();
            Set set = (Set) object;
            Iterator i = set.iterator();
            while (i.hasNext()) {
                nodeSet.addNode((Node) i.next());
            }
            return nodeSet;
        }
        else {
            Class<?> cls = object.getClass();
            if (cls.isArray()) {
                NodeSet nodeSet = new NodeSet();
                Node[] nodes = (Node[]) object;
                for (int i = 0; i < nodes.length; ++i) {
                    nodeSet.addNode(nodes[i]);
                }
                return nodeSet;
            }
        }

        return object;
    }

    public Object extFunction(FuncExtFunction function, Vector params) throws TransformerException
    {
        return extFunction(function.getNamespace(), function.getFunctionName(), params, null);
    }

    public boolean functionAvailable(String ns, String funcName)
    {
        return installedFunctions.containsKey(funcName);
    }
}
