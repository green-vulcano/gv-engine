package it.greenvulcano.gvesb.datahandling.utils.dao;

import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.factory.DHFactory;
import it.greenvulcano.gvesb.datahandling.factory.pool.DHFactoryPool;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DataAccessObject
{
    /**
     *
     */
    private DataAccessObject()
    {
        // do nothing
    }

    public static byte[] getDataAsBytes(String service, Map<String, String> params) throws DataHandlerException,
            InterruptedException {
        return db2xml(service, null, params);
    }

    public static XmlObject getDataAsXML(String service, Map<String, String> params) throws DataHandlerException,
            XmlException, InterruptedException {
        byte[] out = db2xml(service, null, params);
        XmlObject xmlObj = XmlObject.Factory.parse(new String(out));
        return xmlObj;
    }

    public static void setData(String service, byte[] input, Map<String, String> params) throws DataHandlerException,
            InterruptedException {
        xml2db(service, input, params);
    }

    /**
     * Execute an operation on DataHandler (usually a select count) and returns
     * the first instance of '/RowSet/data/col/row' on returned xml.
     *
     * @param service
     *        Type of operation to execute.
     * @param props
     *        Conditions for where.
     * @return the first instance of '/RowSet/data/col/row' on returned xml, or
     *         0.
     * @throws Exception
     *         If service is invalid or DataHandler exception.
     */
    public static int getSingleInt(String service, Map<String, String> params) throws DataHandlerException,
            InterruptedException {
        int toReturn = -1;
        byte[] out = getDataAsBytes(service, params);
        if (out != null) {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Document doc = xml.parseDOM(out);
                toReturn = Integer.parseInt(xml.get(doc, "/RowSet/data[1]/row[1]/col[1]", "0"));
            }
            catch (Exception exc) {
                // nothing to do
            }
            finally {
                if (xml != null) {
                    XMLUtils.releaseParserInstance(xml);
                }
            }
        }
        return toReturn;
    }

    public static String getSingleString(String service, Map<String, String> params) throws DataHandlerException,
            XMLUtilsException, InterruptedException {
        byte[] out = getDataAsBytes(service, params);
        String toReturn = "";
        if (out != null) {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Document doc = xml.parseDOM(out);
                toReturn = xml.get(doc, "/RowSet/data[1]/row[1]/col[1]", "");
            }
            finally {
                if (xml != null) {
                    XMLUtils.releaseParserInstance(xml);
                }
            }
        }
        return toReturn;
    }

    public static String[] getStringArray(String service, Map<String, String> params) throws DataHandlerException,
            XMLUtilsException, InterruptedException {
        byte[] out = getDataAsBytes(service, params);
        String[] toReturn = null;
        if (out != null) {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Document doc = xml.parseDOM(out);
                NodeList nl = xml.selectNodeList(doc, "/RowSet/data[1]/row");
                toReturn = new String[nl.getLength()];
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = nl.item(i);
                    toReturn[i] = xml.get(node, "col[1]", "0");
                }
            }
            finally {
                if (xml != null) {
                    XMLUtils.releaseParserInstance(xml);
                }
            }
        }
        return toReturn;
    }

    public static String[] getSingleLineAsStringArray(String service, Map<String, String> params)
            throws DataHandlerException, XMLUtilsException, InterruptedException {
        byte[] out = getDataAsBytes(service, params);
        String[] toReturn = null;
        if (out != null) {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Document doc = xml.parseDOM(out);
                NodeList nl = xml.selectNodeList(doc, "/RowSet/data[1]/row[1]/col");
                toReturn = new String[nl.getLength()];
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = nl.item(i);
                    toReturn[i] = xml.getNodeContent(node);
                }
            }
            finally {
                if (xml != null) {
                    XMLUtils.releaseParserInstance(xml);
                }
            }
        }
        return toReturn;
    }


    public static Map<String, String> getStringMap(String service, Map<String, String> params)
            throws DataHandlerException, XMLUtilsException, InterruptedException {
        byte[] out = getDataAsBytes(service, params);
        Map<String, String> toReturn = new HashMap<String, String>();
        if (out != null) {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Document doc = xml.parseDOM(out);
                NodeList nl = xml.selectNodeList(doc, "/RowSet/data[1]/row");
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = nl.item(i);
                    toReturn.put(xml.get(node, "col[1]"), xml.get(node, "col[2]"));
                }
            }
            finally {
                if (xml != null) {
                    XMLUtils.releaseParserInstance(xml);
                }
            }
        }
        return toReturn;
    }

    public static Map<String, List<String>> getStringMapArray(String service, Map<String, String> params)
            throws DataHandlerException, XMLUtilsException, InterruptedException {
        byte[] out = getDataAsBytes(service, params);
        Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
        if (out != null) {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Document doc = xml.parseDOM(out);
                NodeList nl = xml.selectNodeList(doc, "/RowSet/data[1]/row");
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = nl.item(i);
                    String key = xml.get(node, "col[1]");
                    List<String> values = toReturn.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        toReturn.put(key, values);
                    }
                    values.add(xml.get(node, "col[2]"));
                }
            }
            finally {
                if (xml != null) {
                    XMLUtils.releaseParserInstance(xml);
                }
            }
        }
        return toReturn;
    }

    public static byte[] db2xml(String service, byte[] input, Map<String, String> params) throws DataHandlerException,
            InterruptedException {
        Map<String, Object> locParams = null;
        if (params == null) {
            locParams = new HashMap<String, Object>();
        }
        else {
            locParams = MapUtils.convertToHMStringObject(params);
        }
        byte[] out = null;
        DHFactory dhf = null;
        DHFactoryPool pool = DHFactoryPool.instance();
        try {
            dhf = pool.getDHFactory(service);
            out = dhf.getDBOBuilder(service).DB2XML(service, input, locParams);
        }
        finally {
            pool.releaseDHFactory(dhf);
        }
        return out;
    }

    public static void xml2db(String service, byte[] input, Map<String, String> params) throws DataHandlerException,
            InterruptedException {
        Map<String, Object> locParams = null;
        if (params == null) {
            locParams = new HashMap<String, Object>();
        }
        else {
            locParams = MapUtils.convertToHMStringObject(params);
        }
        DHFactory dhf = null;
        DHFactoryPool pool = DHFactoryPool.instance();
        try {
            dhf = pool.getDHFactory(service);
            dhf.getDBOBuilder(service).XML2DB(service, input, locParams);
        }
        finally {
            pool.releaseDHFactory(dhf);
        }
    }

    public static DHResult execute(String service, Object input, Map<String, String> params)
            throws DataHandlerException, InterruptedException {
        Map<String, Object> locParams = null;
        if (params == null) {
            locParams = new HashMap<String, Object>();
        }
        else {
            locParams = MapUtils.convertToHMStringObject(params);
        }
        DHResult out = null;
        DHFactory dhf = null;
        DHFactoryPool pool = DHFactoryPool.instance();
        try {
            dhf = pool.getDHFactory(service);
            out = dhf.getDBOBuilder(service).EXECUTE(service, input, locParams);
        }
        finally {
            pool.releaseDHFactory(dhf);
        }
        return out;
    }

    public static byte[] callsp(String service, byte[] input, Map<String, String> params) throws DataHandlerException,
            InterruptedException {
        Map<String, Object> locParams = null;
        if (params == null) {
            locParams = new HashMap<String, Object>();
        }
        else {
            locParams = MapUtils.convertToHMStringObject(params);
        }
        byte[] out = null;
        DHFactory dhf = null;
        DHFactoryPool pool = DHFactoryPool.instance();
        try {
            dhf = pool.getDHFactory(service);
            out = dhf.getDBOBuilder(service).CALL(service, input, locParams);
        }
        finally {
            pool.releaseDHFactory(dhf);
        }
        return out;
    }

}
