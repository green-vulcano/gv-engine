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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupBase;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class XmlSchemaParameterDescrBuilder extends ParamDescBuilder
{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(XmlSchemaParameterDescrBuilder.class);

    XmlSchemaDescriptionBuilder builder_;

    /**
     * Creates a new instance of XmlSchemaParameterDescrBuilder
     *
     * @param defNamespace
     */
    public XmlSchemaParameterDescrBuilder(String defNamespace)
    {
        builder_ = new XmlSchemaDescriptionBuilder(defNamespace);
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescBuilder#getParameterDescForPart(javax.wsdl.Part)
     */
    protected ParamDescription getParameterDescForPart(Part p) throws WSDLException
    {

        // getting QN of element
        QName partQN = p.getElementName();

        if (logger.isDebugEnabled()) {
            logger.debug("#! getParameterDescsForPart(): " + p);
        }

        ParamDescription parameter = new ParamDescription(null);
        XmlSchemaType type=null;
        XmlSchemaElement element = null;

        if (partQN != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("ELEMENT: " + partQN);
            }

            // we have an element
            element = builder_.getXmlSchemaElement(partQN);

            if (element == null) {
                logger.warn("ELEMENT IS NULL: ERROR: " + partQN);
            }
            else {
              parameter.xmlName_ = element.getQName();
              parameter.array_ = element.getMaxOccurs() > 1;
              type = builder_.getXmlSchemaType(element);
            }
        }
        else {

            // we have a type
            partQN = p.getTypeName();
            type = builder_.getXmlSchemaType(partQN);
            parameter.xmlName_ = new QName(builder_.defNamespaceURI_, p.getName());
            parameter.typeXmlName_ = partQN;
        }

        // get all the parameters for type (ComplexType, SimpleType)
        // ParamDescription[] parameters = getParameterDescs4Type(type);
        // we have for example: <wsdl:part xmlName_="parameters"
        // type="xsd:string"/>
        // => so we have a simple type defined by the schema itself
        if (type != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("TYPE => not null, QName is: " + type.getQName());
            }

            Map<String, ParamDescription> parameters = builder_.getParameterDescs4Type(type, parameter);
            QName name = type.getQName();

            if (name == null) {
                if ((parameters == null) || (parameters.size() == 0)) {
                    name = ParamDescription.VOID;
                }
                else {
                    name = ParamDescription.EMBEDDING;
                }
            }

            parameter.typeXmlName_ = name;
            parameter.setDescriptions(parameters);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("TYPE => null use Part as Element: " + p.getName() + " and Type: " + partQN);
            }
        }

        // we have for example: <wsdl:part xmlName_="parameters"
        // type="xsd:string"/>
        // here we can ignore ComplexTypes because only ElementTypes will here
        // occur
        return parameter;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ParamDescBuilder#resolveSchema(javax.wsdl.Definition,
     *      java.lang.String)
     */
    public void resolveSchema(Definition def, String wsdlLocation) throws WSDLException
    {
        Map<String, List<XmlSchema>> schemaMap = new LinkedHashMap<String, List<XmlSchema>>();

        XmlSchemaResolver.evaluateXmlSchemas(def, wsdlLocation, schemaMap);
        builder_.setSchemaMap(schemaMap);

        if (logger.isInfoEnabled()) {
            logger.debug("Schemas imported: " + schemaMap.size());
        }
    }
}


class XmlSchemaDescriptionBuilder
{
    private static final Logger          logger                 = LoggerFactory.getLogger(XmlSchemaDescriptionBuilder.class.getName());

    final String                         defNamespaceURI_;

    final Map<String, Boolean>           elementQualifiedForms_ = new LinkedHashMap<String, Boolean>();
    private Map<String, List<XmlSchema>> schemaMap_;

    /**
     * Constructor
     *
     * @param defNamespaceURI
     */
    protected XmlSchemaDescriptionBuilder(String defNamespaceURI)
    {
        this.defNamespaceURI_ = defNamespaceURI;
    }

    /**
     * A SchemaObject can be everything but this method assumes it to be a
     * simple element or any element
     *
     * @param object
     * @param parent
     *
     * @return the parameter description for the schema
     */
    protected ParamDescription getParameterDesc4SchemaObject(XmlSchemaObject object, ParamDescription parent)
    {
        ParamDescription parameter = null;

        // Here never types will occur because they are resolved to
        // the containing elements with the
        // ParamDescription[] getParameterDescs(List parts, QName operationName)
        // method
        if (logger.isDebugEnabled()) {
            logger.debug("#! getParameterDescs4SchemaObject(): " + object);
        }

        if (object != null) {
            parameter = new ParamDescription(parent);

            if (object.getClass() == XmlSchemaElement.class) {
                XmlSchemaElement element = (XmlSchemaElement) object;

                if (logger.isDebugEnabled()) {
                    logger.debug("Element cast: " + element.getQName());
                }

                XmlSchemaType type = getXmlSchemaType(element);
                Map<String, ParamDescription> parameters = null;

                if (type != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Type found, QName given:"
                                + ((type.getQName() == null) ? "NO" : type.getQName().toString()));
                    }

                    parameter.typeXmlName_ = type.getQName();
                    parameter.array_ = element.getMaxOccurs() > 1;

                    QName name = element.getQName();

                    if (name == null) {
                        parameter.xmlName_ = new QName("", "null");
                    }
                    else {
                        if (elementQualifiedForms_.containsKey(name.getNamespaceURI())) {
                            parameter.xmlName_ = name;
                        }
                        else {
                            parameter.xmlName_ = new QName(name.getLocalPart());
                        }
                    }

                    // parent is parameter
                    parameters = getParameterDescs4Type(type, parameter);
                    parameter.setDescriptions(parameters);

                    // check for <xsd:element ref="" />
                    if (logger.isDebugEnabled()) {
                        logger.debug("ParameterName: " + parameter.xmlName_);
                    }
                }
                else {

                    // check for <xsd:element ref="" />
                    if (logger.isDebugEnabled()) {
                        logger.debug("Element ref found: " + element.getRefName());
                    }

                    XmlSchemaElement ref = getXmlSchemaElement(element.getRefName());

                    parameter = getParameterDesc4SchemaObject(ref, parent);
                }
            }
            else if (object.getClass() == XmlSchemaAny.class) {
                if (logger.isInfoEnabled()) {
                    logger.debug("Any Element found: ParameterName and -Type of "
                            + " ParameterDesc are null to mark them as any.");
                }
            }
            else {
                parameter = null;
                logger.warn("Sequence or other group element found instead of Element: " + object.getClass());
            }
        }

        return parameter;
    }

    /**
     * Method for handling XmlSchemaChoice, XmlSchemaAll, or XmlSchemaSequence
     *
     *
     * @param ref
     * @param parent
     *
     * @return the parameter description for the group reference
     */
    protected Map<String, ParamDescription> getParameterDescs4GroupRef(XmlSchemaGroupRef ref, ParamDescription parent)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("#! getParameterDescs4GroupRef(): " + ref.getRefName());
        }

        XmlSchemaGroupBase group = ref.getParticle();
        Map<String, ParamDescription> map = new LinkedHashMap<String, ParamDescription>();

        if (group != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("XmlSchemaGroupBase found in XmlSchemaGroupRef.");
            }

            map.putAll(getParameterDescs4Group(group, parent));
        }

        QName refName = ref.getRefName();

        List<XmlSchema> schemas = schemaMap_.get(refName.getNamespaceURI());

        if (schemas != null) {
            out : for (int i = 0; i < schemas.size(); i++) {
                XmlSchema schema = schemas.get(i);
                XmlSchemaObjectTable table = schema.getGroups();

                for (Iterator<?> it = table.getValues(); it.hasNext();) {
                    XmlSchemaGroup g = (XmlSchemaGroup) it.next();

                    if (g.getName().equals(refName.getLocalPart())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("XmlSchemaGroup found to evaulate in XmlSchemaGroupRef: " + g.getName());
                        }

                        map.putAll(getParameterDescs4Group(g.getParticle(), parent));

                        break out;
                    }
                }
            }
        }

        return map;
    }

    private static boolean checkParameterDescriptionCycle(ParamDescription description)
    {
        boolean isCyclic = false;

        if (description != null) {
            QName name = description.getXmlName();
            ParamDescription tmp = description.getParent();

            while (tmp != null) {
                if (name.equals(tmp.getXmlName())) {
                    isCyclic = true;
                    description.cyclic_ = isCyclic;

                    break;
                }

                tmp = tmp.getParent();
            }
        }

        return isCyclic;
    }

    protected Map<String, ParamDescription> getParameterDescs4Group(XmlSchemaGroupBase seq, ParamDescription parent)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("#! getParameterDescs4Group()");
        }

        ParamDescription.GroupType type;

        if (seq.getClass() == XmlSchemaSequence.class) {
            type = ParamDescription.GroupType.Sequence;
        }
        else if (seq.getClass() == XmlSchemaAll.class) {
            type = ParamDescription.GroupType.All;
        }
        else if (seq.getClass() == XmlSchemaChoice.class) {
            type = ParamDescription.GroupType.Choice;
        }
        else {
            type = ParamDescription.GroupType.Unknown;
            logger.error("Unidentified SchemaGroup: " + seq.toString());
        }

        Map<String, ParamDescription> map = new LinkedHashMap<String, ParamDescription>();
        XmlSchemaObjectCollection items = seq.getItems();

        if (logger.isDebugEnabled()) {
            logger.debug("Found group type: " + type.toString() + " nItems: " + items.getCount());
        }

        for (int i = 0, n = items.getCount(); i < n; i++) {
            ParamDescription parameter = getParameterDesc4SchemaObject(items.getItem(i), parent);

            if (parameter == null) {

                // we must have a group in a group like <sequence><sequence>
                if (items.getItem(i) instanceof XmlSchemaGroupBase) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found group in a group!");
                    }

                    map.putAll(getParameterDescs4Group((XmlSchemaGroupBase) items.getItem(i), parent));
                }
                else if (items.getItem(i).getClass() == XmlSchemaGroupRef.class) {
                    map.putAll(getParameterDescs4GroupRef((XmlSchemaGroupRef) items.getItem(i), parent));
                }
                else {
                    // assert false: "We have found sth others than a group: "
                    // + items.getItem(i);
                    throw new RuntimeException("We have found sth others than a group: " + items.getItem(i));
                }
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found parameter in a group: " + parameter.getXmlName());
                }

                parameter.setGroupType(type);
                if(parameter.getXmlName() != null) {
                    map.put(parameter.getXmlName().getLocalPart(), parameter);
                }
            }
        }

        return map;
    }

    /**
     *
     * Gets for a type all ParameterDescriptions
     *
     *
     * @param type
     * @param parent
     *
     * @return the parameter description for the type
     */
    protected Map<String, ParamDescription> getParameterDescs4Type(XmlSchemaType type, ParamDescription parent)
    {
        Map<String, ParamDescription> parameters = null;

        if (logger.isDebugEnabled()) {
            logger.debug("#! getParameterDescs4Type()");
        }

        // check if parent is already in parameter path to find out cycle
        if (type != null) {
            if (checkParameterDescriptionCycle(parent)) {
                logger.warn("********************************************\n" + "FOUND CYCLE specified in: "
                        + parent.getParent().getXmlName() + "********************************************");

                // so we return parameters variable with null
            }
            else if (type.getClass() == XmlSchemaComplexType.class) {
                XmlSchemaComplexType t = (XmlSchemaComplexType) type;
                XmlSchemaParticle p = t.getParticle();

                if (logger.isDebugEnabled()) {
                    logger.debug("ComplexType found, QName given: "
                            + ((t.getQName() == null) ? "NO" : t.getQName().toString()));
                }

                if (p != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Particle found.");
                    }

                    // TODO SchemaGroupRef not supported
                    // handle xsd:sequence
                    if (p instanceof XmlSchemaGroupBase) {
                        parameters = getParameterDescs4Group((XmlSchemaGroupBase) p, parent);
                    }
                    else if (p.getClass() == XmlSchemaGroupRef.class) {
                        parameters = getParameterDescs4GroupRef((XmlSchemaGroupRef) p, parent);
                    }
                    else {
                        logger.error("###############################################\n" + "Particle Type: "
                                + p.getClass() + "\n###############################################");
                    }

                    // TODO; else we can also have choice elements
                }

                XmlSchemaContentModel model = t.getContentModel();

                // check if the previous <sequence> elemnt is embedded in a
                // contentModel
                if (model != null) {

                    // We have found <xsd:simpleContent>
                    XmlSchemaContent content = model.getContent();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Model found with content.");
                    }

                    // DONT handle any simple content stuff!!! its not necessary
                    // for invocation

                    if (content.getClass() == XmlSchemaComplexContentExtension.class) {

                        // We have found <complexContent><extension base=""
                        // ...>...
                        XmlSchemaComplexContentExtension ext = (XmlSchemaComplexContentExtension) content;

                        if (logger.isDebugEnabled()) {
                            logger.debug("ContentExtension found with base: " + ext.getBaseTypeName());
                        }

                        XmlSchemaType extType = getXmlSchemaType(ext.getBaseTypeName());
                        Map<String, ParamDescription> parTmp1 = getParameterDescs4Type(extType, parent);
                        Map<String, ParamDescription> parTmp2 = parameters;

                        if (parTmp2 != null) {
                            parameters = new LinkedHashMap<String, ParamDescription>();
                            parameters.putAll(parTmp1);
                            parameters.putAll(parTmp2);
                        }
                        else {
                            parameters = parTmp1;
                        }
                    }
                    else if (content.getClass() == XmlSchemaSimpleContentExtension.class) {

                        // Other model found than ComplexContentExtension:
                        // class
                        // org.apache.ws.commons.schema.XmlSchemaSimpleContent
                        XmlSchemaSimpleContentExtension ext = (XmlSchemaSimpleContentExtension) content;
                        QName baseName = ext.getBaseTypeName();

                        logger.debug("Parent type is restricted:");
                        parent.setTypeXmlName(baseName);
                    }
                    else {
                        logger.error("Other model found than ComplexContentExtension: " + model.getClass()
                                + " content: " + model.getContent());
                    }
                }

                // we have <xsd:complexType /> -> VOID method parameter
            }
            else {
                if (!(type.getClass() == XmlSchemaSimpleType.class)) {
                    throw new RuntimeException("No Simple type is: " + type.getClass());
                }
            }
        }

        return parameters;
    }

    /**
     * Get the SchemaElement with a given xmlName_ using the given Schemas to
     * resolve it
     *
     *
     * @param name
     * @return The found SchemaElement
     */
    public XmlSchemaElement getXmlSchemaElement(QName name)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("#! getXmlSchemaElement() for QName: " + name);
        }

        XmlSchemaElement element = null;

        List<XmlSchema> list = schemaMap_.get(name.getNamespaceURI());

        if (list != null) {
            for (XmlSchema schema : list) {
                element = schema.getElementByName(name);
                if (element != null) {
                    break;
                }
            }
        }

        return element;
    }

    /**
     * Get the SchemaType with a given xmlName_ using the given Schemas to
     * resolve it returns simple xsd as well as complex types
     *
     *
     * @param name
     * @return The SchemaType
     */
    public XmlSchemaType getXmlSchemaType(QName name)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("#! getXmlSchemaType() for QName: " + name);
        }

        XmlSchemaType t = null;

        List<XmlSchema> list = schemaMap_.get(name.getNamespaceURI());

        if (list != null) {
            for (XmlSchema schema : list) {
                t = schema.getTypeByName(name);
                if (t != null) {
                    break;
                }
            }
        }

        return t;
    }

    /**
     * Get the SchemaType for the SchemaElement using the given XmlSchemas to
     * resolve it
     *
     * @param element
     *        The element
     * @return the SchemaType of the element
     */
    public XmlSchemaType getXmlSchemaType(XmlSchemaElement element)
    {
        XmlSchemaType type = element.getSchemaType();

        if (logger.isDebugEnabled()) {
            logger.debug("#! getXmlSchemaType() for Element: " + element.getQName());
        }

        if (type == null) {

            // we have a complex or simple type which is defined seperately from
            // element
            // <xsd:element ... type="tns:lala""/>
            // <xsd:complexType xmlName_="lala"">....</xsd:complexType>
            QName name = element.getSchemaTypeName();

            logger.debug("************************************");
            logger.debug("*" + name);
            logger.debug("************************************");
            // we can cast a null object to a specific object which is in fact
            // null again :)
            if (name != null) {
                type = getXmlSchemaType(name);

                if (type == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("================= create tmp XmlSchemaSimpleType: " + name);
                    }

                    // tmp type so that no ref element is used by
                    // getParameterDescs4SchemaObject()
                    type = new XmlSchemaSimpleType(new XmlSchema(name.getNamespaceURI(), null));
                    type.setName(name.getLocalPart());
                }
            }
        } // else we have a complex or simple type which is defined in the
        // element

        logger.debug("Found: " + type);
        return type;
    }

    public void setSchemaMap(Map<String, List<XmlSchema>> schemaMap)
    {
        schemaMap_ = schemaMap;

        for (Entry<String, List<XmlSchema>> e : schemaMap.entrySet()) {
            elementQualifiedForms_.put(e.getKey(), e.getValue().get(0).getElementFormDefault().getValue().equals(
                    XmlSchemaForm.QUALIFIED));
        }
    }
}
