package it.greenvulcano.gvesb.interceptor;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class DocumentBuilderTest
{

	public static void main(String[] args) {

		// create a new DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			// use the factory to create a documentbuilder
			DocumentBuilder builder = factory.newDocumentBuilder();

			// create a new document from input stream
			//FileInputStream fis = new FileInputStream("Student.xml");
			
			String xmlString = "<Radice><data>pippo</data><data>pluto</data></Radice>";
			
            InputSource xmlSource = new InputSource(new StringReader(xmlString));
            //return docBuilderNamespace.parse(xmlSource);
			
			Document doc = builder.parse(xmlSource);

			// get the first element
			Element element = doc.getDocumentElement();

			// get all child nodes
			NodeList nodes = element.getChildNodes();

			// print the text content of each child
			for (int i = 0; i < nodes.getLength(); i++) {
				System.out.println("" + nodes.item(i).getTextContent());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
