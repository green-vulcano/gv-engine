/**
 * 
 */
package it.greenvulcano.gvesb.core.flow.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * @author gianluca
 *
 */
public class XMLMerge {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(XMLMerge.class);
	
	private class DocMerge {
        String source;
        String xpathSrc;
        String xpathDest;

        /**
         * @param source
         * @param xpathSrc
         * @param xpathDest
         */
        public DocMerge(String source, String xpathSrc, String xpathDest) {
            this.source = source;
            this.xpathSrc = xpathSrc;
            this.xpathDest = xpathDest;
        }

        @Override
        public String toString() {
            return "Source: " + source + " - xpathSrc: " + xpathSrc + " - xpathDest: " + xpathDest;
        }
    }


    private List<DocMerge> mergeList = new Vector<DocMerge>();

	public XMLMerge(Node node) throws Exception {
		try {
			NodeList nl = XMLConfig.getNodeList(node, "DocMerge");
			for (int i = 0; i < nl.getLength(); i++) {
                Node mn = nl.item(i);
                DocMerge docMerge = new DocMerge(XMLConfig.get(mn, "@source"), XMLConfig.get(mn, "@xpath-source"),
                		XMLConfig.get(mn, "@xpath-dest"));
                mergeList.add(docMerge);
                logger.debug("Added a DocMerge[" + docMerge + "].");
            }
			if (mergeList.size() < 2) {
				throw new Exception("Is needed at least 2 DocMerge definition in ExecXMLMerge configuration");
			}
		} catch (Exception exc) {
			throw exc;
		}
	}

	public Document merge(String input, Map<String, Object> environment) throws Exception {
		List<Object> listDoc = new ArrayList<Object>();
		List<String> listXPath = new ArrayList<String>();
		
		for (int i = 0; i < mergeList.size(); i++) {
			DocMerge dm = mergeList.get(i);
			listDoc.add(((GVBuffer) environment.get(dm.source)).getObject());
			if (i > 0) {
				listXPath.add(dm.xpathSrc + "##" + dm.xpathDest);
			}
			else {
				listXPath.add("");
			}
		}
		Document docMerge = XMLUtils.mergeXML_S(listDoc.toArray(), listXPath.toArray());
		return docMerge;
	}
}
