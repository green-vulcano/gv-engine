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
package it.greenvulcano.gvesb.j2ee.xmlRegistry.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.Proxy;
import it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;


/**
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class RegistryImpl implements Registry
{

    /**
     * The GreenVulcano logger utility.
     */
	private static final Logger logger = LoggerFactory.getLogger(RegistryImpl.class);

    /**
     * xpath per raggiungere il Registry desiderato in configurazione
     */
    private Node                     configConf = null;
    /**
     * URL per la pubblicazione sul registry
     */
    public String                    regUrlp    = "";
    /**
     * URL per la query sul registry
     */
    public String                    regUrli    = "";

    /**
     * Organizations Collection
     */
    private Collection<Organization> orgs;
    private RegistryConnection       registryConnection;

    /**
     * Constructor empty
     * 
     */
    public RegistryImpl()
    {

    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#init(org.w3c.dom.Node,
     *      it.greenvulcano.gvesb.j2ee.xmlRegistry.Proxy)
     */
    public void init(Node configConf, Proxy proxy)
    {
        logger.debug("BEGIN init");
        this.configConf = configConf;
        registryConnection = new RegistryConnection();
        registryConnection.init(configConf, proxy);
        logger.debug("END init");
    }

    /**
     * This method adds an Organization to the Registry server
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#saveBusiness(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public boolean saveBusiness(String businessName, String description, String email, String personName, String phone)
    {
        logger.debug("BEGIN saveBusiness");
        try {
            BusinessLifeCycleManager blm = registryConnection.getBlm();
            Organization org = blm.createOrganization(blm.createInternationalString(businessName));
            org.setDescription(blm.createInternationalString(description));

            User user = blm.createUser();
            PersonName userName = blm.createPersonName(personName);

            org.setPrimaryContact(user);

            TelephoneNumber telephoneNumber = blm.createTelephoneNumber();
            telephoneNumber.setNumber(phone);
            telephoneNumber.setType(null);

            Collection<EmailAddress> emailAddresses = new ArrayList<EmailAddress>();
            EmailAddress emailAddress = blm.createEmailAddress(email);
            emailAddresses.add(emailAddress);

            Collection<TelephoneNumber> numbers = new ArrayList<TelephoneNumber>();
            numbers.add(telephoneNumber);

            user.setPersonName(userName);
            user.setEmailAddresses(emailAddresses);
            user.setTelephoneNumbers(numbers);
            orgs = new ArrayList<Organization>();
            orgs.add(org);

            BulkResponse br = blm.saveOrganizations(orgs);
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                logger.debug("Organization Saved");
                logger.debug("END saveBusiness");
                return true;
            }
            else {
                logger.debug("One or more JAXRExceptions " + "occurred during the save operation!!");
                return false;
            }

        }
        catch (JAXRException e) {
            logger.error("Exception " + e.getMessage());
            return false;
        }
    }

    /**
     * This method saves a service under a specific Organization
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#saveService(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean saveService(String keyBusiness, String serviceName, String serviceDescription, String WSDL)
    {
        logger.debug("BEGIN saveService");
        try {
            BulkResponse br = null;
            Organization org = null;
            BusinessLifeCycleManager blm = registryConnection.getBlm();
            Service service = (Service) blm.createObject("Service");
            service.setName(blm.createInternationalString(serviceName));
            service.setDescription(blm.createInternationalString(serviceDescription));
            ServiceBinding servicebinding = (ServiceBinding) blm.createObject("ServiceBinding");
            servicebinding.setAccessURI(WSDL);
            service.addServiceBinding(servicebinding);
            org = (Organization) findBusinessByKey(keyBusiness);
            service.setProvidingOrganization(org);
            ArrayList<Service> services = new ArrayList<Service>();
            services.add(service);
            br = blm.saveServices(services);
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                logger.debug("service Saved");
                logger.debug("END saveService");
                return true;
            }
            else {
                logger.error("Error during save service!!!");
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception " + e.getMessage());
            return false;
        }
    }

    /**
     * This method finds all Organizations of the specific JAXR registry
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#findBusinessByName()
     */
    public Hashtable<String, String> findBusinessByName()
    {
        logger.debug("BEGIN findBusinessByName()");
        Collection<?> orgList = null;
        Hashtable<String, String> orgListNames = new Hashtable<String, String>();
        try {
            logger.debug("Add NAme");
            BusinessQueryManager bqm = registryConnection.getBqm();
            ArrayList<String> names = new ArrayList<String>();
            names.add("%%");
            Collection<String> fQualifiers = new ArrayList<String>();
            // fQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            fQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
            fQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            BulkResponse br = bqm.findOrganizations(fQualifiers, names, null, null, null, null);
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                orgList = br.getCollection();
                Iterator<?> iter = orgList.iterator();
                while (iter.hasNext()) {
                    Organization org = (Organization) iter.next();
                    orgListNames.put(getKeyID(org), getName(org));
                }
                logger.debug("Organization put in the hashtable");
                return orgListNames;
            }
            else {
                logger.error("One or more JAXRExceptions " + "occurred during the query operation!!");
                return null;
            }
        }
        catch (JAXRException e) {
            logger.equals("Exception " + e.getMessage());
            e.printStackTrace();
        }
        logger.debug("END findBusinessByName()");
        return orgListNames;
    }

    /**
     * This method find Organization, of the specific JAXR registry, that has
     * name as 'businessName'
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#findBusinessByName(java.lang.String)
     */
    public Hashtable<String, String> findBusinessByName(String businessName)
    {
        logger.debug("BEGIN findBusinessByName");
        Collection<?> orgList = null;
        Hashtable<String, String> orgListNames = new Hashtable<String, String>();
        try {
            BusinessQueryManager bqm = registryConnection.getBqm();
            ArrayList<String> names = new ArrayList<String>();
            names.add(businessName);
            Collection<String> fQualifiers = new ArrayList<String>();
            fQualifiers.add(FindQualifier.EXACT_NAME_MATCH);
            logger.debug("BEGIN call findOrganization '" + businessName + "'");
            BulkResponse br = bqm.findOrganizations(fQualifiers, names, null, null, null, null);
            logger.debug("END call findOrganizations");
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                orgList = br.getCollection();
                Iterator<?> iter = orgList.iterator();
                while (iter.hasNext()) {
                    Organization org = (Organization) iter.next();
                    orgListNames.put(getKeyID(org), getName(org));
                    logger.debug("Name=" + getName(org));
                }
            }
            else {
                logger.error("One or more JAXRExceptions " + "occurred during the query operation!!");
            }
        }
        catch (JAXRException e) {
            logger.error("Exception retrieving business by name " + businessName, e);
        } finally {
            logger.debug("END findBusinessByName");
        }
        return orgListNames;
    }

    /**
     * This method finds all Organizations of the specific JAXR registry
     */
    private Organization findBusinessByKey(String businessKey)
    {
        logger.debug("BEGIN findBusinessByKey");
        Organization org = null;
        try {
            BusinessQueryManager bqm = registryConnection.getBqm();
            org = (Organization) bqm.getRegistryObject(businessKey, BusinessLifeCycleManager.ORGANIZATION);
        }
        catch (JAXRException e) {
            logger.error("Exception " + e.getMessage());
        }
        logger.debug("END findBusinessByKey");
        return org;
    }

    /**
     * This method finds Services list from Registry server
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#findServiceByName(java.lang.String)
     */
    public Hashtable<String, String> findServiceByName(String businessKey)
    {
        logger.debug("BEGIN findServiceByName");
        Collection<?> serviceList = null;
        Hashtable<String, String> serviceListNames = new Hashtable<String, String>();
        try {
            BusinessQueryManager bqm = registryConnection.getBqm();
            BusinessLifeCycleManager blm = registryConnection.getBlm();
            Key key = blm.createKey(businessKey);
            ArrayList<String> names = new ArrayList<String>();
            names.add("%");
            Collection<String> fQualifiers = new ArrayList<String>();
            fQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);

            BulkResponse br = bqm.findServices(key, fQualifiers, names, null, null);

            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                serviceList = br.getCollection();
                System.out.println(br.isPartialResponse());
                Iterator<?> iter = serviceList.iterator();
                while (iter.hasNext()) {
                    Service service = (Service) iter.next();
                    serviceListNames.put(getKeyID(service), getName(service));
                }
                logger.debug("END findServiceByName");
                return serviceListNames;
            }
            else {
                logger.error("One or more JAXRExceptions " + "occurred during the query operation:");
                return null;
            }
        }
        catch (JAXRException e) {
            logger.error("Exception " + e.getMessage());
        }
        logger.debug("END findServiceByName");
        return serviceListNames;
    }

    /**
     * @param serviceKey
     * @return the <code>Service</code> corresponding to specific
     *         <i>serviceKey</i> parameter.
     */
    public Service findServiceByKey(String serviceKey)
    {
        logger.debug("BEGIN findServiceByKey");
        Service service = null;
        try {
            BusinessQueryManager bqm = registryConnection.getBqm();
            service = (Service) bqm.getRegistryObject(serviceKey, BusinessLifeCycleManager.SERVICE);
        }
        catch (JAXRException e) {
            logger.error("Exception " + e.getMessage());
        }
        logger.debug("END findServiceByKey");
        return service;
    }


    /**
     * This method deletes the registered business
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#delBusiness(java.lang.String)
     */
    public boolean delBusiness(String businessKey)
    {
        logger.debug("BEGIN delBusiness");
        try {
            BusinessLifeCycleManager blm = registryConnection.getBlm();
            Key key = blm.createKey(businessKey);
            Collection<Key> keys = new ArrayList<Key>();
            keys.add(key);
            BulkResponse br = blm.deleteOrganizations(keys);
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                logger.debug("Organization deleted ");
                logger.debug("END delBusiness");
                return true;
            }
            else {
                logger.error("One or more JAXRExceptions " + "occurred during the save operation!!");
                return false;
            }
        }
        catch (JAXRException e) {
            logger.error("Exception " + e.getMessage());
            return false;
        }
    }

    /**
     * This method deletes the specified service
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#delService(java.lang.String)
     */
    public boolean delService(String serviceKey)
    {
        logger.debug("BEGIN delService");
        BulkResponse br = null;
        try {
            BusinessLifeCycleManager blm = registryConnection.getBlm();
            Key key = blm.createKey(serviceKey);
            Collection<Key> keys = new ArrayList<Key>();
            keys.add(key);
            br = blm.deleteServices(keys);
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                logger.debug("service delete");
                logger.debug("END delService");
                return true;
            }
            else {
                logger.error("One or more JAXRExceptions " + "occurred during the save operation!!");
                return false;
            }
        }
        catch (JAXRException e) {
            logger.error("Non � stato possibile effettuare l'operazione !!!");
            return false;
        }
    }

    private String getName(RegistryObject ro)
    {
        try {
            return ro.getName().getValue();
        }
        catch (NullPointerException npe) {
            return "";
        }
        catch (JAXRException e) {
            return "";
        }
    }

    private String getKeyID(RegistryObject ro)
    {
        try {
            Key key = ro.getKey();
            return key.getId();
        }
        catch (NullPointerException npe) {
            return "";
        }
        catch (JAXRException e) {
            return "";
        }
    }

    private String getDescription(RegistryObject ro)
    {
        try {
            return ro.getDescription().getValue();
        }
        catch (NullPointerException npe) {

            return " ";
        }
        catch (JAXRException e) {
            return " ";
        }
    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#getRegistryURLPublish()
     */
    @Override
    public String getRegistryURLPublish()
    {
        logger.debug("BEGIN getRegistryURLP");
        logger.debug("END getRegistryURLP");
        return regUrlp;
    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#getRegistryURLInquiry()
     */
    public String getRegistryURLInquiry()
    {
        logger.debug("BEGIN getRegistryURLI");
        logger.debug("END getRegistryURLI");
        return regUrli;
    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#destroy()
     */
    public void destroy()
    {
        // Do nothing
    }

    /**
     * This method return the detail of business organization.
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#getBusinessDetail(java.lang.String)
     */
    public Hashtable<String, String> getBusinessDetail(String keyBusiness)
    {
        logger.debug("BEGIN getBusinessDetail");
        Organization org = null;
        Hashtable<String, String> orgDetails = new Hashtable<String, String>();
        org = findBusinessByKey(keyBusiness);
        if (org != null) {
            orgDetails.put("BusinessName", getName(org));
            orgDetails.put("Description", getDescription(org));

            String name = getContactName(org);
            String phone = getPhoneNumber(org);
            String mail = getEmailAddress(org);

            orgDetails.put("ContactName", name);
            orgDetails.put("ContactPhone", phone);
            orgDetails.put("ContactEmail", mail);
            logger.debug("END getBusinessDetail");
            return orgDetails;
        }
        else {
            logger.error("One or more JAXRExceptions " + "occurred during the save operation!!");
        }
        return orgDetails;
    }

    /*
     * Utility method for getting contact name.
     */
    private String getContactName(Organization org)
    {
        String name = "";
        try {
            User user = org.getPrimaryContact();
            if (user != null) {
                PersonName personName = user.getPersonName();
                if (personName != null) {
                    name = personName.getFullName();
                }
            }
        }
        catch (JAXRException e) {
            logger.error(" Non � stato possibile recuperare il contact name !!");
            name = "";
            return name;
        }
        return name;
    }

    /*
     * Utility method for getting contact's phone number.
     */
    private String getPhoneNumber(Organization org)
    {
        String numberString = "";
        try {
            User user = org.getPrimaryContact();
            if (user != null) {
                // use null to get all phone numbers
                Collection<?> numbers = user.getTelephoneNumbers(null);
                Iterator<?> iter = numbers.iterator();
                if (iter.hasNext()) {
                    TelephoneNumber number = (TelephoneNumber) iter.next();
                    String areaCode = null;
                    try {
                        areaCode = number.getAreaCode();
                    }
                    catch (UnsupportedCapabilityException usce) {
                        // do nothing
                    }
                    if (areaCode != null) {
                        areaCode = "(" + areaCode + ") ";
                    }
                    else {
                        areaCode = "";
                    }
                    numberString = areaCode + number.getNumber();
                }
            }
        }
        catch (JAXRException e) {
            logger.error(" Non � stato possibile recupare il contact phone number !!!");
            numberString = "";
            return numberString;
        }
        return numberString;
    }

    /*
     * Utility method for getting contact's email address.
     */
    private String getEmailAddress(Organization org)
    {
        String emailAddressString = "";
        try {
            User user = org.getPrimaryContact();
            if (user != null) {
                Collection<?> addresses = user.getEmailAddresses();
                Iterator<?> iter = addresses.iterator();
                if (iter.hasNext()) {
                    EmailAddress emailAddress = (EmailAddress) iter.next();
                    emailAddressString = emailAddress.getAddress();
                }
            }
        }
        catch (JAXRException e) {
            logger.error("Non � stato possibile recuperare il contact email address !!!");
            emailAddressString = "";
            return emailAddressString;
        }
        return emailAddressString;
    }

    /**
     * This method returns service details
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#getServiceDetail(java.lang.String)
     */
    public Hashtable<String, String> getServiceDetail(String keyService)
    {
        logger.debug("BEGIN getServiceDetail");
        Service service = null;
        String WSDL = null;
        Hashtable<String, String> serviceDetails = new Hashtable<String, String>();
        try {
            service = findServiceByKey(keyService);
            if (service != null) {
                serviceDetails.put("ServiceName", service.getName().getValue());
                serviceDetails.put("Description", service.getDescription().getValue());
                Collection<?> c = service.getServiceBindings();
                Iterator<?> it = c.iterator();
                ServiceBinding s = (ServiceBinding) it.next();
                WSDL = s.getAccessURI();
                serviceDetails.put("AccessURL", WSDL);
                return serviceDetails;
            }
        }
        catch (JAXRException e) {
            logger.error("Non e' stato possibile recuparare i dettagli del service!!!!");
            serviceDetails.put("ServiceName", "");
            serviceDetails.put("Description", "");
            serviceDetails.put("AccessURL", "");
            return serviceDetails;
        }
        return serviceDetails;
    }

    /**
     * This method return service details.
     * 
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#getRegistryID()
     */
    @Override
    public String getRegistryID()
    {
        String IDRegistry = XMLConfig.get(configConf, "@id-registry", "");
        logger.debug("IDRegistry = " + IDRegistry);
        return IDRegistry;
    }


    /**
     * @see it.greenvulcano.gvesb.j2ee.xmlRegistry.Registry#getOrganization()
     */
    public String getOrganization()
    {
        return XMLConfig.get(configConf, "@organization-name", "");
    }

}
