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
package it.greenvulcano.gvesb.j2ee.xmlRegistry;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface Registry
{
    /**
     * Initialization method.
     *
     * @param configNode
     *        the configuration node
     * @param proxy
     *        the proxy
     */
    public void init(Node configNode, Proxy proxy);

    /**
     * saveBusiness.
     *
     * @param businessName
     *        String
     * @param description
     *        String
     * @param email
     *        String
     * @param personName
     *        String
     * @param phone
     *        String
     * @return boolean
     */
    public boolean saveBusiness(String businessName, String description, String email, String personName, String phone);

    /**
     * saveService.
     *
     * @param keyBusiness
     *        String
     * @param serviceName
     *        String
     * @param serviceDescription
     *        String
     * @param WSDL
     *        String
     * @return boolean
     */
    public boolean saveService(String keyBusiness, String serviceName, String serviceDescription, String WSDL);

    /**
     * delBusiness.
     *
     * @param businessKey
     *        String
     * @return boolean
     */
    public boolean delBusiness(String businessKey);

    /**
     * delService.
     *
     * @param businessKey
     *        String
     * @return boolean
     */
    public boolean delService(String businessKey);

    /**
     * findBusinessByName.
     *
     * @param businessName
     *        String
     * @return a map
     */
    public Map<String, String> findBusinessByName(String businessName);

    /**
     * findBusinessByName.
     *
     * @return a map
     */
    public Map<String, String> findBusinessByName();

    /**
     * findServiceByName.
     *
     * @param organizationKey
     *        String
     * @return a map
     */
    public Map<String, String> findServiceByName(String organizationKey);

    /**
     * getBusinessDetail.
     *
     * @param keyBusiness
     *        String
     * @return a map
     */
    public Map<String, String> getBusinessDetail(String keyBusiness);

    /**
     * getServiceDetail.
     *
     * @param keyService
     *        String
     * @return a map
     */
    public Map<String, String> getServiceDetail(String keyService);

    /**
     * getIDRegistry.
     *
     * @return String
     */
    public String getRegistryID();

    /**
     * getOrganization.
     *
     * @return String
     */
    public String getOrganization();

    /**
     * getRegistryURLP.
     *
     * @return String
     */
    public String getRegistryURLPublish();

    /**
     * getRegistryURLI.
     *
     * @return String
     */
    public String getRegistryURLInquiry();

    /**
     * /** destroy.
     */
    public void destroy();
}
