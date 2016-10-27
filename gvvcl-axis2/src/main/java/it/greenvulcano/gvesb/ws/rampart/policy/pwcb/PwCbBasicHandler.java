package it.greenvulcano.gvesb.ws.rampart.policy.pwcb;

import it.greenvulcano.configuration.XMLConfig;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PwCbBasicHandler extends AbstractPWCBHandler
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PwCbBasicHandler.class);

    public static final String TYPE    = "PwCbBasicHandler";
    public Map<String, String> mapping = new HashMap<String, String>();

    /*
     * (non-Javadoc)
     *
     * @see test.rampart.policy.PWCBHandler#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws Exception
    {
        mapping.clear();
        NodeList nl = XMLConfig.getNodeList(node, "UserDef");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            String name = XMLConfig.get(n, "@name");
            String pwd = XMLConfig.getDecrypted(n, "@password");
            logger.debug("PwCbBasicHandler - Insert entry: " + name);// + "/" + pwd);
            mapping.put(name, pwd);
        }
    }

    /* (non-Javadoc)
     * @see test.rampart.policy.pwcb.PWCBHandler#getType()
     */
    @Override
    public String getType()
    {
        return TYPE;
    }

    /*
     * (non-Javadoc)
     *
     * @see test.rampart.policy.PWCBHandler#resolve(java.lang.String)
     */
    @Override
    public String resolve(String name) throws Exception
    {
        return mapping.get(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see test.rampart.policy.PWCBHandler#destroy()
     */
    @Override
    public void destroy()
    {
        mapping.clear();
    }

}
