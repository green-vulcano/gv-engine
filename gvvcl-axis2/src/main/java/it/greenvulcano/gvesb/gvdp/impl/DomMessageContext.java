package it.greenvulcano.gvesb.gvdp.impl;

import org.apache.axis2.context.MessageContext;
import org.w3c.dom.Element;

public class DomMessageContext extends MessageContext {

	
		public void setBodyContent(Element bodyContent){
			try {
				getEnvelope().getBody().addChild(org.apache.axis2.util.XMLUtils.toOM(bodyContent));
			} catch (Exception e) {
				org.slf4j.LoggerFactory.getLogger(getClass()).error("Can not set the specified content "+bodyContent, e);
			}
		}
}
