/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.util.ldap;

import it.greenvulcano.util.xml.XMLUtils;

import java.util.Base64;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class for LDAP context dump to XML.
 *
 * @version 3.2.0 Set 24, 2011
 * @author GreenVulcano Developer Team
 */
public class LDAPContextXmlSerializer
{
    private SearchControls srchCtls     = null;
    private String         filterDef    = "(objectClass=*)";
    private String         filter       = filterDef;
    private boolean        withSchema   = false;
    private Document       rootDocument = null;

    public LDAPContextXmlSerializer()
    {
        srchCtls = new SearchControls();
        srchCtls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    }

    public boolean isWithSchema()
    {
        return this.withSchema;
    }

    public void setWithSchema(boolean withSchema)
    {
        this.withSchema = withSchema;
    }

    public String getFilter()
    {
        return this.filter;
    }

    public void setFilter(String filter)
    {
        this.filter = this.filterDef;

        if ((filter != null) && !"".equals(filter)) {
            this.filter = filter;
        }
    }

    public Document serializeContext(String root, LdapContext ctx, boolean deep) throws LDAPUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Document doc = parser.newDocument("LDAPContext", LDAPCommons.LDAP_NS_PRE, LDAPCommons.LDAP_NS_URI);

            LdapContext base = (LdapContext) ctx.lookup(root);
            Element entry = parser.insertElement(doc.getDocumentElement(), "Entry");
            Name name = base.getNameParser("").parse(base.getNameInNamespace());
            parser.setAttribute(entry, "id", name.get(name.size() - 1));
            parser.setAttribute(entry, "full-id", base.getNameInNamespace());
            if (withSchema) {
                try {
                    DirContext dc = base.getSchema("");
                    describeSchema(dc, entry, parser);
                }
                catch (Exception exc) {
                    // do nothing
                    exc.printStackTrace();
                }
            }
            dumpAttributes(base.getAttributes(""), entry, parser);

            if (deep) {
                NamingEnumeration<SearchResult> list = ctx.search(root, filter, srchCtls);

                while (list.hasMoreElements()) {
                    SearchResult sri = list.nextElement();
                    dumpContext(sri, ctx, entry, parser, true);
                }
            }
            return doc;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    public Document serializeContext(LdapContext ctx, boolean deep) throws LDAPUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Document doc = parser.newDocument("LDAPContext", LDAPCommons.LDAP_NS_PRE, LDAPCommons.LDAP_NS_URI);
            dumpContext(ctx, doc.getDocumentElement(), parser, deep);

            return doc;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    public Document serializeContext(SearchResult sr, LdapContext ctx, boolean deep) throws LDAPUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            Document doc = parser.newDocument("LDAPContext", LDAPCommons.LDAP_NS_PRE, LDAPCommons.LDAP_NS_URI);
            if ((sr == null) || (ctx == null)) {
                return doc;
            }
            dumpContext(sr, ctx, doc.getDocumentElement(), parser, deep);

            return doc;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    /*
     * Conversational metods
     */

    /**
     *
     * @throws LDAPUtilsException
     */
    public void startRootDocument() throws LDAPUtilsException
    {
        XMLUtils parser = null;
        try {
            parser = XMLUtils.getParserInstance();
            rootDocument = parser.newDocument("LDAPContext", LDAPCommons.LDAP_NS_PRE, LDAPCommons.LDAP_NS_URI);
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    public Document getRootDocument()
    {
        return this.rootDocument;
    }

    public void resetRootDocument()
    {
        rootDocument = null;
    }

    public Document addToRootDocument(SearchResult sr, LdapContext ctx, boolean deep) throws LDAPUtilsException
    {
        XMLUtils parser = null;
        try {
            if ((sr == null) || (ctx == null)) {
                return rootDocument;
            }
            parser = XMLUtils.getParserInstance();
            dumpContext(sr, ctx, rootDocument.getDocumentElement(), parser, deep);

            return rootDocument;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    private void dumpContext(LdapContext ctx, Element parent, XMLUtils parser, boolean deep) throws LDAPUtilsException
    {
        if (ctx == null) {
            return;
        }
        try {
            Element entry = parser.insertElement(parent, "Entry");
            Name name = ctx.getNameParser("").parse(ctx.getNameInNamespace());
            parser.setAttribute(entry, "id", name.get(name.size() - 1));
            parser.setAttribute(entry, "full-id", ctx.getNameInNamespace());
            if (withSchema) {
                try {
                    DirContext dc = ctx.getSchema("");
                    describeSchema(dc, entry, parser);
                }
                catch (Exception exc) {
                    // do nothing
                    exc.printStackTrace();
                }
            }
            dumpAttributes(ctx.getAttributes(""), entry, parser);

            if (deep) {
                NamingEnumeration<SearchResult> list = ctx.search("", filter, srchCtls);
                while (list.hasMoreElements()) {
                    SearchResult sri = list.nextElement();
                    dumpContext(sri, ctx, entry, parser, true);
                }
            }
        }
        catch (LDAPUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
    }

    private void dumpContext(SearchResult sr, LdapContext ctx, Element parent, XMLUtils parser, boolean deep)
            throws LDAPUtilsException
    {
        if ((sr == null) || (ctx == null)) {
            return;
        }
        try {
            Element entry = parser.insertElement(parent, "Entry");
            parser.setAttribute(entry, "id", sr.getName());
            parser.setAttribute(entry, "full-id", sr.getNameInNamespace());
            if (withSchema) {
                try {
                    DirContext dc = ((DirContext) ctx.lookup(sr.getNameInNamespace())).getSchema("");
                    describeSchema(dc, entry, parser);
                }
                catch (Exception exc) {
                    // do nothing
                    exc.printStackTrace();
                }
            }
            dumpAttributes(sr.getAttributes(), entry, parser);

            if (deep) {
                NamingEnumeration<SearchResult> list = ctx.search(sr.getNameInNamespace(), filter, srchCtls);
                while (list.hasMoreElements()) {
                    SearchResult sri = list.nextElement();
                    dumpContext(sri, ctx, entry, parser, true);
                }
            }
        }
        catch (LDAPUtilsException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new LDAPUtilsException(exc);
        }
    }

    /*
     * private void dumpContext(String base, LdapContext ctx, Node parent)
     * throws Exception
     * {
     * NamingEnumeration<SearchResult> list = ctx.search(base, filter,
     * srchCtls);
     *
     * while (list.hasMoreElements()){
     * SearchResult a = list.nextElement();
     * System.out.println(deep + " -------------------\nName: " +
     * a.getNameInNamespace());
     *
     * printAttributes(a.getAttributes());
     *
     * dumpContext(a.getNameInNamespace(), ctx, deep+1);
     * System.out.println(deep + " -------------------\n");
     * }
     * }
     */

    private void dumpAttributes(Attributes result, Element entry, XMLUtils parser) throws Exception
    {
        if ((result != null) && (result.size() > 0)) {
            Element attListN = parser.insertElement(entry, "AttributeList");

            NamingEnumeration<? extends Attribute> attributes = result.getAll();
            while (attributes.hasMore()) {
                Attribute attr = attributes.next();
                Element attN = parser.insertElement(attListN, "Attribute");
                parser.setAttribute(attN, "id", attr.getID());
                if (withSchema) {
                    try {
                        //DirContext dc = attr.getAttributeSyntaxDefinition();
                        DirContext dc = attr.getAttributeDefinition();
                        describeSchema(dc, attN, parser);
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }

                NamingEnumeration<?> values = attr.getAll();
                while (values.hasMoreElements()) {
                    Element valN = parser.insertElement(attN, "Value");
                    String value = "";
                    Object valO = values.nextElement();
                    if (valO != null) {
                        if (valO instanceof String) {
                            value = (String) valO;
                        }
                        else if (valO instanceof byte[]) {
                            value = new String(Base64.getEncoder().encode((byte[]) valO));
                        }
                        else {
                            value = "" + valO;
                        }
                    }
                    parser.insertText(valN, value);
                }
            }
        }
    }

    private void describeSchema(DirContext schema, Element entry, XMLUtils parser) throws Exception
    {
        Attributes result = schema.getAttributes("");
        if ((result != null) && (result.size() > 0)) {
            Element schN = parser.insertElement(entry, "Schema");

            NamingEnumeration<? extends Attribute> attributes = result.getAll();
            while (attributes.hasMore()) {
                Attribute attr = attributes.next();

                NamingEnumeration<?> values = attr.getAll();
                if (values.hasMoreElements()) {
                    parser.setAttribute(schN, attr.getID().toLowerCase(), "" + values.nextElement());
                }
            }
        }
    }
}
