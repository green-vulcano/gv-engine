/**
 * 
 */
package it.greenvulcano.gvesb.core.flow.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

/**
 * @author gianluca
 *
 */
public class XMLAggregate {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(XMLAggregate.class);

	private String root;
	private String namespace = null;
	private List<String> docNameList = new ArrayList<String>();

	public XMLAggregate(Node node) throws Exception {
		try {
			root = XMLConfig.get(node, "@root");
			namespace = XMLConfig.get(node, "@namespace", null);

			NodeList nl = XMLConfig.getNodeList(node, "DocAggregate");
			for (int i = 0; i < nl.getLength(); i++) {
				Node ndoc = nl.item(i);
				String source = XMLConfig.get(ndoc, "@source");
				docNameList.add(source);
				logger.debug("Added a DocAggregate[" + source + "].");
			}
			/*if (docNameList.size() < 2) {
				throw new Exception("Is needed at least 2 DocAggregate definition in ExecXMLAggregate configuration");
			}*/
		} catch (Exception exc) {
			throw exc;
		}
	}

	public Document aggregate(String input, Map<String, Object> environment) throws Exception {
		List<Object> listDoc = new ArrayList<Object>();
		if (docNameList.isEmpty()) {
			addDoc(input, ((GVBuffer) environment.get(input)).getObject(), listDoc);
		}
		else {
			for (String name : docNameList) {
				addDoc(name, ((GVBuffer) environment.get(name)).getObject(), listDoc);
			}
		}
		Document docAgg = XMLUtils.aggregateXML_S(root, namespace, listDoc.toArray());
		return docAgg;
	}

	private void addDoc(String name, Object object, List<Object> listDoc) throws Exception {
		if (object == null) return;
		if (object instanceof Collection) {
			listDoc.addAll((Collection) object);
		}
		else if (object instanceof Document) {
            listDoc.add(object);
        }
        else if (object instanceof Node) {
        	listDoc.add(object);
        }
        else if (object instanceof String) {
        	listDoc.add(object);
        }
        else if (object instanceof byte[]) {
        	listDoc.add(object);
        }
        else if (object instanceof InputStream) {
        	listDoc.add(object);
        }
        else {
            throw new XMLUtilsException("Invalid input type[" + name + "]: " + object.getClass());
        }
	}
}
