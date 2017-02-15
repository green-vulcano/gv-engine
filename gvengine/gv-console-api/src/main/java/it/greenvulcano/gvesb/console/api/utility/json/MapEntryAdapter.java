package it.greenvulcano.gvesb.console.api.utility.json;

import java.math.BigDecimal;
import java.math.BigInteger;
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

public class MapEntryAdapter extends XmlAdapter<Element, MapEntry> {
	private ClassLoader classLoader;
	private DocumentBuilder documentBuilder;
	private JAXBContext jaxbContext;

	public MapEntryAdapter() {
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	public MapEntryAdapter(JAXBContext jaxbContext) {
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

	public Element marshal(MapEntry parameter)
			throws ParserConfigurationException, JAXBException {
		if (null == parameter) {
			return null;
		}

		QName rootElement = new QName("mapEntry");
		Object value = parameter.getValue();
		Class type = value.getClass();

		JAXBElement jaxbElement = new JAXBElement(rootElement, type, value);

		Document document = getDocumentBuilder().newDocument();
		Marshaller marshaller = getJAXBContext(type).createMarshaller();
		marshaller.marshal(jaxbElement, document);
		Element element = document.getDocumentElement();

		element.setAttribute("type", type.getName());
		element.setAttribute("name", parameter.getKey().toString());

		element.setAttribute("typeName", parameter.getKey().getClass()
				.getName());
		return element;
	}

	public MapEntry unmarshal(Element element) throws ClassNotFoundException,
			JAXBException {
		if (null == element) {
			return null;
		}

		Class type = this.classLoader.loadClass(element.getAttribute("type"));

		String key = element.getAttribute("name");
		String classKey = element.getAttribute("typeName");

		DOMSource source = new DOMSource(element);
		Unmarshaller unmarshaller = getJAXBContext(type).createUnmarshaller();
		JAXBElement jaxbElement = unmarshaller.unmarshal(source, type);

		MapEntry parameter = new MapEntry();

		if (classKey.equals(String.class.getName())) {
			parameter.setKey(key);
		} else if (classKey.equals(Integer.class.getName())) {
			parameter.setKey(Integer.valueOf(Integer.parseInt(key)));
		} else if (classKey.equals(Long.class.getName())) {
			parameter.setKey(Long.valueOf(Long.parseLong(key)));
		} else if (classKey.equals(BigInteger.class.getName())) {
			parameter.setKey(new BigInteger(key));
		} else if (classKey.equals(BigDecimal.class.getName())) {
			parameter.setKey(new BigDecimal(key));
		}

		parameter.setValue(jaxbElement.getValue());
		return parameter;
	}
}
