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
package it.greenvulcano.gvesb.virtual.ws.transport.https;

import it.greenvulcano.gvesb.http.ssl.AuthSSLProtocolSocketFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.CommonsHTTPTransportSender;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.slf4j.Logger;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVHTTPSTransportSender extends CommonsHTTPTransportSender
{
    private static final Logger logger            = org.slf4j.LoggerFactory.getLogger(GVHTTPSTransportSender.class);

    private static final String TWOWAY_AUTH       = "2WAY_AUTH";
    private static final String KEYSTORE_ID       = "KeystoreID";
    private static final String TRUST_KEYSTORE_ID = "TrustKeystoreID";
    private static final String KEY_PASSWORD      = "KeyAliasPassword";

    private Protocol            myhttps           = null;

    /**
     * @see org.apache.axis2.transport.http.CommonsHTTPTransportSender#init(org.apache.axis2.context.ConfigurationContext,
     *      org.apache.axis2.description.TransportOutDescription)
     */
    @Override
    public void init(ConfigurationContext confContext, TransportOutDescription transportOut) throws AxisFault
    {
        logger.debug("Base configuration read...");
        super.init(confContext, transportOut);
        logger.debug("Base configuration read...OK");

        // Added for 2-way ssl authentication handling
        // <parameter name="TWOWAY_AUTH" locked="false">true</parameter> is
        // checked
        Parameter twoway_auth = transportOut.getParameter(TWOWAY_AUTH);
        if (twoway_auth != null) {
            if ("true".equals(twoway_auth.getValue())) {
                logger.info("Enabling 2-way ssl authentication");
                try {
                    String keystoreID = null;
                    String keyPwd = null;
                    String trustID = null;
                    Parameter param = transportOut.getParameter(KEYSTORE_ID);
                    if (param != null) {
                        keystoreID = param.getValue().toString();
                    }
                    param = transportOut.getParameter(KEY_PASSWORD);
                    if (param != null) {
                        keyPwd = param.getValue().toString();
                    }
                    param = transportOut.getParameter(TRUST_KEYSTORE_ID);
                    if (param != null) {
                        trustID = param.getValue().toString();
                    }
                    ProtocolSocketFactory psf = new AuthSSLProtocolSocketFactory(keystoreID, trustID, keyPwd);
                    myhttps = new Protocol("https", psf, 443);
                }
                catch (Exception exc) {
                    logger.error("Cannot read 2-way ssl authentication parameters: ", exc);
                    throw new AxisFault("Cannot read 2-way ssl authentication parameters.", exc);
                }
            }
            else {
                try {
                    String trustID = null;
                    Parameter param = transportOut.getParameter(TRUST_KEYSTORE_ID);
                    if (param != null) {
                        trustID = param.getValue().toString();
                    }
                    ProtocolSocketFactory psf = new AuthSSLProtocolSocketFactory(null, trustID, null);
                    myhttps = new Protocol("https", psf, 443);
                }
                catch (Exception exc) {
                    logger.error("Cannot read 1-way ssl authentication parameters: ", exc);
                    throw new AxisFault("Cannot read 1-way ssl authentication parameters.", exc);
                }
            }
        }
        else {
            logger.debug("Normal ssl authentication");
        }
    }

    @Override
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault
    {
        msgContext.getOptions().setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, myhttps);
        return super.invoke(msgContext);
    }

}
