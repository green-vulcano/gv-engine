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
package it.greenvulcano.util.ldap;

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.search.XPathAPI;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for LDAP context building from XML.
 *
 * @version 3.2.0 25/set/2011
 * @author GreenVulcano Developer Team
 */
public class LDAPContextXmlBuilder
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LDAPContextXmlBuilder.class);

    static {
        XPathAPI.installNamespace(LDAPCommons.LDAP_NS_PRE, LDAPCommons.LDAP_NS_URI);
    }

    public LDAPContextXmlBuilder()
    {
        // do nothing
    }

    public void buildContext(Document doc, LdapContext ctx, String root) throws LDAPUtilsException
    {
        XMLUtils parser = null;
        logger.debug("BEGIN updating Context: " + root);
        try {
            parser = XMLUtils.getParserInstance();

            LdapContext base = (LdapContext) ctx.lookup(root);
            NodeList entryL = parser.selectNodeList(doc, "/ldapc:LDAPContext/ldapc:Entry");
            for (int i = 0; i < entryL.getLength(); i++) {
                Node entryN = entryL.item(i);

                processEntry(entryN, base, parser);
            }
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
            logger.debug("END updating Context: " + root);
        }
    }

    private void processEntry(Node entryN, LdapContext base, XMLUtils parser) throws LDAPUtilsException
    {
        String id = "";
        String mode = "";
        String ctxName = "";
        try {
            id = parser.get(entryN, "@id");
            mode = parser.get(entryN, "@mode", "add");
            ctxName = base.getNameInNamespace();
            logger.debug("BEGIN updating Entry[" + id + "] mode[" + mode + "] on Context[" + ctxName + "]");
            LdapContext entry = null;
            try {
                entry = (LdapContext) base.lookup(id);
                if (entry != null) {
                    if (mode.equals("add") || mode.equals("modify")) {
                        processAttributes(entryN, entry, parser);
                    }
                    else if ((entry != null) && mode.equals("remove")) {
                        removeEntry(entryN, base, parser);
                        return;
                    }
                }
            }
            catch (Exception exc) {
                // do nothing
            }
            
            if ((entry == null) && (mode.equals("add") || mode.equals("modify"))) {
                entry = createEntryAndAttributes(entryN, base, parser);
            }
            
            NodeList entryL = parser.selectNodeList(entryN, "ldapc:Entry");
            for (int i = 0; i < entryL.getLength(); i++) {
                Node subEntryN = entryL.item(i);

                processEntry(subEntryN, entry, parser);
            }
        }
        catch (LDAPUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            logger.debug("END updating Entry[" + id + "] mode[" + mode + "] on Context[" + ctxName + "]");
        }
    }

    private void processAttributes(Node entryN, LdapContext entry, XMLUtils parser) throws LDAPUtilsException
    {
        String ctxName = "";
        try {
            ctxName = entry.getNameInNamespace();
            logger.debug("BEGIN updating Attributes on Context[" + ctxName + "]");
            
            List<ModificationItem> miL = new ArrayList<ModificationItem>();

            NodeList attrRemL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[@mode='remove']");
            if (attrRemL.getLength() > 0) {
                for (int i = 0; i < attrRemL.getLength(); i++) {
                    Node attrN = attrRemL.item(i);
                    String id = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(id);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".", "");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.getDecoder().decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    miL.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, ba));
                }
            }

            NodeList attrRepL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[@mode='replace']");
            if (attrRepL.getLength() > 0) {
                for (int i = 0; i < attrRepL.getLength(); i++) {
                    Node attrN = attrRepL.item(i);
                    String id = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(id);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.getDecoder().decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    miL.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, ba));
                }
            }

            NodeList attrAddL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[not(@mode) or @mode='add']");
            if (attrAddL.getLength() > 0) {
                for (int i = 0; i < attrAddL.getLength(); i++) {
                    Node attrN = attrAddL.item(i);
                    String id = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(id);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.getDecoder().decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    miL.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, ba));
                }
            }

            dumpAttributes("PRE Update", entry.getAttributes(""));
            
            if (miL.size() > 0) {
                ModificationItem[] miA = miL.toArray(new ModificationItem[0]);
                entry.modifyAttributes("", miA);
            }
            
            dumpAttributes("POST Update", entry.getAttributes(""));
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            logger.debug("END updating Attributes on Context[" + ctxName + "]");
        }
    }

    private LdapContext createEntryAndAttributes(Node entryN, LdapContext base, XMLUtils parser) throws LDAPUtilsException
    {
        String id = "";
        String mode = "";
        String ctxName = "";
        try {
            id = parser.get(entryN, "@id");
            mode = parser.get(entryN, "@mode", "add");
            ctxName = base.getNameInNamespace();
            logger.debug("BEGIN adding Entry[" + id + "] mode[" + mode + "] on Context[" + ctxName + "]");
            BasicAttributes bas = new BasicAttributes();

            NodeList attrAddL = parser.selectNodeList(entryN, "ldapc:AttributeList/ldapc:Attribute[not(@mode) or @mode='add' or mode='replace']");
            if (attrAddL.getLength() > 0) {
                for (int i = 0; i < attrAddL.getLength(); i++) {
                    Node attrN = attrAddL.item(i);
                    String idA = parser.get(attrN, "@id");
                    String encoding = parser.get(attrN, "@encoding", "string");
                    BasicAttribute ba = new BasicAttribute(idA);

                    NodeList attrVL = parser.selectNodeList(attrN, "ldapc:Value");
                    for (int v = 0; v < attrVL.getLength(); v++) {
                        Node valN = attrVL.item(v);
                        String val = parser.get(valN, ".");
                        if ("base64".equals(encoding)) {
                            ba.add(Base64.getDecoder().decode(val));
                        }
                        else {
                            ba.add(val);
                        }
                    }

                    bas.put(ba);
                }
            }

            LdapContext entry = (LdapContext) base.createSubcontext(id, bas);
            dumpAttributes("POST Insert", entry.getAttributes(""));
            
            return entry;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            logger.debug("END updating Attributes on Context[" + ctxName + "]");
        }
    }
    
    private void removeEntry(Node entryN, LdapContext base, XMLUtils parser) throws LDAPUtilsException
    {
        String id = "";
        String ctxName = "";
        try  {
            id = parser.get(entryN, "@id");
            ctxName = base.getNameInNamespace();
            logger.debug("BEGIN removing Entry[" + id + "] on Context[" + ctxName + "]");
            @SuppressWarnings("unused")
			LdapContext entry = (LdapContext) base.lookup(id);
            base.destroySubcontext(id);
        }
        catch (NameNotFoundException exc) {
            logger.debug("Entry[" + id + "] not found on Context[" + ctxName + "]");
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            logger.debug("END removing Entry[" + id + "] on Context[" + ctxName + "]");
        }
    }
    
    private void dumpAttributes(String header, Attributes result) throws NamingException
    {
        if (header != null) {
            logger.debug(header);
        }
        if (result == null) {
            logger.debug("No attributes present");
        }
        else {
            logger.debug("Attributes:");
            NamingEnumeration<? extends Attribute> attrs = result.getAll();
            while (attrs.hasMore()) {
                Attribute at = attrs.next();
                logger.debug("name: " + at.getID());
                NamingEnumeration<?> vals = at.getAll();
                while (vals.hasMoreElements()) {
                    logger.debug("\tvalue: " + vals.nextElement());
                }
            }
        }
    }
}
