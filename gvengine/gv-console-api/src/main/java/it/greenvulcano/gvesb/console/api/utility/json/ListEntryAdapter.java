package it.greenvulcano.gvesb.console.api.utility.json;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ListEntryAdapter extends XmlAdapter<Element, ListEntry>
{
	private ClassLoader classLoader;
	private DocumentBuilder documentBuilder;
	private JAXBContext jaxbContext;

	public ListEntryAdapter() {
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	public ListEntryAdapter(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	private DocumentBuilder getDocumentBuilder()
			throws ParserConfigurationException {
		if (null == this.documentBuilder) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			this.documentBuilder = dbf.newDocumentBuilder();
		}
		return this.documentBuilder;
	}

	private JAXBContext getJAXBContext(Class<?> type) throws JAXBException {
		if (null == this.jaxbContext) {
			return JAXBContext.newInstance(new Class[] { type });
		}
		return this.jaxbContext;
	}

	public Element marshal(ListEntry parameter) throws JAXBException,
			ParserConfigurationException {
		if (null == parameter) {
			return null;
		}

		QName rootElement = new QName("listEntry");
		Object value = parameter.getValue();
		Class type = value.getClass();
		JAXBElement jaxbElement = new JAXBElement(rootElement, type, value);

		Document document = getDocumentBuilder().newDocument();
		Marshaller marshaller = getJAXBContext(type).createMarshaller();
		marshaller.marshal(jaxbElement, document);
		Element element = document.getDocumentElement();

		element.setAttribute("type", type.getName());
		return element;
	}

	public ListEntry unmarshal(Element element) throws ClassNotFoundException,
			JAXBException {
		if (null == element) {
			return null;
		}

		Class type = this.classLoader.loadClass(element.getAttribute("type"));

		DOMSource source = new DOMSource(element);
		Unmarshaller unmarshaller = getJAXBContext(type).createUnmarshaller();
		JAXBElement jaxbElement = unmarshaller.unmarshal(source, type);

		ListEntry parameter = new ListEntry();
		parameter.setValue(jaxbElement.getValue());
		return parameter;
	}
}
