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
package it.greenvulcano.gvesb.j2ee.jndi.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.j2ee.jndi.JNDIBuilder;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 05/lug/2010
 * @author GreenVulcano Developer Team
 */
public class Rebind implements JNDIBuilder
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Rebind.class);
    private String              name;
    private String              oldJndiName;
    private String              newJndiName;
    private JNDIHelper          jndiHelper;

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.j2ee.jndi.JNDIBuilder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws Exception
    {
        try {
            name = XMLConfig.get(node, "@name");
            oldJndiName = XMLConfig.get(node, "@oldJndiName");
            newJndiName = XMLConfig.get(node, "@newJndiName");
            jndiHelper = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
        }
        catch (Exception exc) {
            logger.error("Error initializing Rebind[" + name + "]", exc);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.j2ee.jndi.JNDIBuilder#build()
     */
    @Override
    public void build() throws Exception
    {
        Context localCtx = null;
        try {
            logger.debug("Prepare to bind from [" + oldJndiName + "] to [" + newJndiName + "]");
            Object toBind = jndiHelper.lookup(oldJndiName);

            localCtx = new InitialContext();
            Context ctx = localCtx;
            CompositeName cname = new CompositeName(newJndiName);

            // Create sub-context
            //
            for (int i = 0; i < cname.size() - 1; ++i) {
                String subcontextName = cname.get(i);
                try {
                    Object obj = ctx.lookup(subcontextName);
                    if (obj instanceof Context) {
                        ctx = (Context) obj;
                        System.out.println("------------------- found " + ctx);
                    }
                    else {
                        throw new Exception("Cannot create context: " + subcontextName);
                    }
                }
                catch (NamingException exc) {
                    ctx = ctx.createSubcontext(subcontextName);
                    System.out.println("------------------- create " + ctx);
                }
            }

            ctx.rebind(cname.get(cname.size() - 1), toBind);
            System.out.println("------------------- " + ctx);
            logger.debug("Binding from [" + oldJndiName + "] to [" + newJndiName + "]");
        }
        catch (Exception exc) {
            logger.error("Error binding from [" + oldJndiName + "] to [" + newJndiName + "]", exc);
        }
        finally {
            if (localCtx != null) {
                try {
                    localCtx.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            try {
                jndiHelper.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.j2ee.jndi.JNDIBuilder#destroy()
     */
    @Override
    public void destroy()
    {
        // TODO Auto-generated method stub

    }

}
