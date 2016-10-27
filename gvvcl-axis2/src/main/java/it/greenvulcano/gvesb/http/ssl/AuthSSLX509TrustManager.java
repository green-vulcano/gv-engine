/*
 * $HeadURL$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.http.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * AuthSSLX509TrustManager can be used to extend the default {@link X509TrustManager}
 * with additional trust decisions.
 * </p>
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * for use without additional customization.
 * </p>
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class AuthSSLX509TrustManager implements X509TrustManager
{
    private X509TrustManager    defaultTrustManager = null;

    /** Log object for this class. */
    private static final Logger logger              = LoggerFactory.getLogger(AuthSSLX509TrustManager.class);

    /**
     * Constructor for AuthSSLX509TrustManager.
     *
     * @param defaultTrustManager
     */
    public AuthSSLX509TrustManager(final X509TrustManager defaultTrustManager)
    {
        super();
        if (defaultTrustManager == null) {
            throw new IllegalArgumentException("Trust manager may not be null");
        }
        this.defaultTrustManager = defaultTrustManager;
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],
     *      String)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException
    {
        if (logger.isInfoEnabled() && certificates != null) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                String log =" Client certificate " + (c + 1) + ":\n  Subject DN: " + cert.getSubjectDN()+"\n  Signature Algorithm: " + cert.getSigAlgName()+"  Valid from: " + cert.getNotBefore()+"\n  Valid until: " + cert.getNotAfter()+"\n  Issuer: " + cert.getIssuerDN();
				logger.info(log);
            }
        }
        defaultTrustManager.checkClientTrusted(certificates, authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],
     *      String)
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException
    {
        if (logger.isInfoEnabled() && certificates != null) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                logger.info(" Server certificate " + (c + 1) + ":");
                logger.info("  Subject DN: " + cert.getSubjectDN());
                logger.info("  Signature Algorithm: " + cert.getSigAlgName());
                logger.info("  Valid from: " + cert.getNotBefore());
                logger.info("  Valid until: " + cert.getNotAfter());
                logger.info("  Issuer: " + cert.getIssuerDN());
            }
        }
        defaultTrustManager.checkServerTrusted(certificates, authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        return this.defaultTrustManager.getAcceptedIssuers();
    }
}
