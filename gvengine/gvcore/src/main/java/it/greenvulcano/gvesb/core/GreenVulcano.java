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
package it.greenvulcano.gvesb.core;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.config.GVServiceConf;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.config.ServiceConfigManager;
import it.greenvulcano.gvesb.core.exc.GVCoreCallSvcException;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreDisabledServiceException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreInputServiceException;
import it.greenvulcano.gvesb.core.exc.GVCoreOutputServiceException;
import it.greenvulcano.gvesb.core.exc.GVCoreSecurityException;
import it.greenvulcano.gvesb.core.exc.GVCoreServiceNotFoundException;
import it.greenvulcano.gvesb.core.exc.GVCoreTimeoutException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongOpException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongParameterException;
import it.greenvulcano.gvesb.core.flow.GVFlow;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.log.GVBufferDump;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.policy.ACLManager;
import it.greenvulcano.gvesb.policy.impl.GVCoreServiceKey;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;
import it.greenvulcano.gvesb.utils.concurrency.ConcurrencyHandler;
import it.greenvulcano.gvesb.utils.concurrency.ServiceConcurrencyInfo;

import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.thread.ThreadMap;

import java.util.Map;

import org.slf4j.Logger;


/**
 * The main GreenVulcano class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class GreenVulcano
{
    private static final Logger   logger                = org.slf4j.LoggerFactory.getLogger(GreenVulcano.class);

    /**
     * The subsystem name that identify this component.
     */
    public static final String    GVC_SUBSYSTEM         = "GVCore";

    /**
     * Used to get an GVServiceConf of a specific service (SYSTEM + SERVICE).
     */
    private ServiceConfigManager  gvSvcConfMgr          = null;
    /**
     * The Statistics StatisticsDataManager to be used.
     */
    private StatisticsDataManager statisticsDataManager = null;

    private DTEController         dteController         = null;

    /**
     * The ESB context instance.
     */
    private InvocationContext     gvContext             = null;

    /**
     * Identifies if the current instance is executing a flow.
     */
    private boolean               running               = false;
    /**
     * Identifies if the current instance is valid.
     */
    private boolean               valid                 = false;

    /**
     * Default constructor to build and initialize a GreenVulcano instance.
     * 
     * @exception GVCoreException
     */
    public GreenVulcano() throws GVCoreException
    {
        logger.debug("BEGIN GreenVulcano init");

        try {
            XMLConfig.load(GreenVulcanoConfig.getServicesConfigFileName());
            logger.debug("GreenVulcano Services Configuration File: " + GreenVulcanoConfig.getServicesConfigFileName());
            XMLConfig.load(GreenVulcanoConfig.getSystemsConfigFileName());
            logger.debug("GreenVulcano Systems Configuration File: " + GreenVulcanoConfig.getSystemsConfigFileName());

            statisticsDataManager = new StatisticsDataManager();
            try {
                statisticsDataManager.init();
            }
            catch (Exception exc) {
                logger.error("Error initializing Statistics Manager", exc);
            }
            String dteConfFileName = "GVDataTransformation.xml";
            logger.debug("DTE configuration file: " + dteConfFileName + ".");
            try {
                dteController = new DTEController(dteConfFileName);
            }
            catch (Exception exc) {
                logger.error("Error initializing DTEController from file: " + dteConfFileName, exc);
            }
            gvSvcConfMgr = new ServiceConfigManager();
            gvSvcConfMgr.setStatisticsDataManager(statisticsDataManager);
            gvContext = new InvocationContext();
            gvContext.setGVServiceConfigManager(gvSvcConfMgr);
            gvContext.setStatisticsDataManager(statisticsDataManager);
            gvContext.setExtraField("DTE_CONTROLLER", dteController);

            valid = true;

            logger.debug("END GreenVulcano init");
        }
        catch (Exception exc) {
            logger.error("An exception is occurred during the initialization of the GreenVulcano class", exc);
            throw new GVCoreException("GV_INITIALIZATION_ERROR", new String[][]{{"message", exc.getMessage()}});
        }
    }

    /**
     * This method is used by to perform an asynchronous request.
     * 
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @return
     * @throws GVPublicException
     */
    public GVBuffer request(GVBuffer gvBuffer) throws GVPublicException
    {
        GVBuffer outputGVBuffer = handleFlow("Request", new GVBuffer(gvBuffer));
        return outputGVBuffer;
    }

    /**
     * This method is used to perform a synchronous request.
     * 
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @return
     * @throws GVPublicException
     */
    public GVBuffer requestReply(GVBuffer gvBuffer) throws GVPublicException
    {
        GVBuffer outputGVBuffer = handleFlow("RequestReply", new GVBuffer(gvBuffer));
        return outputGVBuffer;
    }

    /**
     * This method is used to get a reply of a previous called service.
     * 
     * @param gvBuffer
     *        The GreenVulcano data coming from the client.
     * @return
     * @throws GVPublicException
     */
    public GVBuffer getReply(GVBuffer gvBuffer) throws GVPublicException
    {
        GVBuffer outputGVBuffer = handleFlow("GetReply", new GVBuffer(gvBuffer));
        return outputGVBuffer;
    }

    /**
     * This method is used by a Call-back server to send a reply of a previous
     * called service.
     * 
     * @param gvBuffer
     *        The GreenVulcano data coming from the server
     * @return
     * @throws GVPublicException
     */
    public GVBuffer sendReply(GVBuffer gvBuffer) throws GVPublicException
    {
        GVBuffer outputGVBuffer = handleFlow("SendReply", new GVBuffer(gvBuffer));
        return outputGVBuffer;
    }

    /**
     * This method is used by the client to get a request to be elaborated. It
     * is used by Polling servers.
     * 
     * @param gvBuffer
     * @return
     * @throws GVPublicException
     */
    public GVBuffer getRequest(GVBuffer gvBuffer) throws GVPublicException
    {
        GVBuffer outputGVBuffer = handleFlow("GetRequest", new GVBuffer(gvBuffer));
        return outputGVBuffer;
    }

    /**
     * This method is used by GreenVulcano to perform decoupling operation.
     * 
     * @param gvBuffer
     *        The GreenVulcano data
     * @param name
     *        The forward name
     * @return
     * @throws GVPublicException
     */
    public GVBuffer forward(GVBuffer gvBuffer, String name) throws GVPublicException
    {
        GVBuffer outputGVBuffer = handleFlow(name, new GVBuffer(gvBuffer));
        return outputGVBuffer;
    }

    /**
     * This method is used by GreenVulcano to perform decoupling operation.
     * 
     * @param gvBuffer
     *        The GreenVulcano data
     * @param name
     *        The forward name
     * @param flowSystem
     *        If not null, overwrite the gvBuffer system
     * @param flowService
     *        If not null, overwrite the gvBuffer service
     * @return
     * @throws GVPublicException
     */
    public GVBuffer forward(GVBuffer gvBuffer, String name, String flowSystem, String flowService)
            throws GVPublicException
    {
        GVBuffer outputGVBuffer = handleFlow(name, new GVBuffer(gvBuffer), flowSystem, flowService);
        return outputGVBuffer;
    }


    public GVBuffer recover(String id, String flowSystem, String flowService, String gvsOperation, String recoveryNode,
            Map<String, Object> environment) throws GVException, GVPublicException
    {
        running = true;
        try {
            GVBuffer gvBuffer = new GVBuffer(flowSystem, flowService, new Id(id));
            NMDC.push();
            GVBufferMDC.put(gvBuffer);
            NMDC.setOperation(gvsOperation);

            GVBuffer returnData = null;
            GVServiceConf gvsConfig = null;
            ServiceConcurrencyInfo serviceConcInfo = null;
            boolean success = false;
            long startTime = 0;

            gvContext.setContext(gvsOperation, gvBuffer);

            try {
                gvContext.push();
                startTime = System.currentTimeMillis();

                serviceConcInfo = ConcurrencyHandler.instance().add(GVC_SUBSYSTEM, gvBuffer);

                gvsConfig = createGVSConfig(gvBuffer, flowSystem, flowService);
                
                GVFlow gvOp = gvsConfig.getGVOperation(gvBuffer, gvsOperation);
             
                if (logger.isInfoEnabled()) {
                    GVBufferDump dump = new GVBufferDump(gvBuffer, false);
                    logger.info("INPUT GVBuffer: "+dump);                        
                }

                returnData = gvOp.recover(recoveryNode, environment);
                gvsConfig.manageAliasOutput(returnData);

                if (logger.isInfoEnabled()) {
                    GVBufferDump dump = new GVBufferDump(returnData, false);
                    logger.info("OUTPUT GVBuffer: "+dump);                        
                }
                    
                long endTime = System.currentTimeMillis();
    
                if (logger.isInfoEnabled()) {
                    logger.info(GVFormatLog.formatENDOperation(returnData, endTime - startTime).toString());
                }
    
                success = true;
            }
            catch (GVCoreCallSvcException exc) {
                throwGVPublicException("GV_CALL_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreInputServiceException exc) {
                throwGVPublicException("GV_INPUT_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreOutputServiceException exc) {
                throwGVPublicException("GV_OUTPUT_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreTimeoutException exc) {
                throwGVPublicException("GV_SERVICE_TIMEOUT_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreWrongOpException exc) {
                throwGVPublicException("GV_WRONG_PARADIGM_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreDisabledServiceException exc) {
                throwGVPublicException("GV_SERVICE_DISABLED_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreServiceNotFoundException exc) {
                throwGVPublicException("GV_SERVICE_NOT_FOUND_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreWrongInterfaceException exc) {
                throwGVPublicException("GV_WRONG_INTERFACE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreWrongParameterException exc) {
                throwGVPublicException("GV_PARAMETER_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreConfException exc) {
                throwGVPublicException("GV_CONFIGURATION_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreException exc) {
                logger.error("GVCore Internal Exception", exc);
                throwGVPublicException("GV_CALL_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVPublicException exc) {
                logExc(exc, startTime);
                throw exc;
            }
            catch (InterruptedException exc) {
                logExcST(exc);
                throwGVPublicException("GV_INTERRUPTED_ERROR", gvBuffer, exc, startTime);
            }
            catch (Exception exc) {
                logExcST(exc);
                throwGVPublicException("GV_GENERIC_ERROR", gvBuffer, exc, startTime);
            }
            finally {
                if (serviceConcInfo != null) {
                    serviceConcInfo.remove();
                }
                try {
                    if (gvsConfig != null) {
                        ServiceOperationInfo serviceInfo = ServiceOperationInfoManager.instance().getServiceOperationInfo(
                                gvsConfig.getServiceName(), true);
                        serviceInfo.flowTerminated(gvsOperation, id, success);
                    }
                }
                catch (Exception exc) {
                    logger.warn("Error on MBean registration");
                }

                gvContext.pop();
                NMDC.pop();
            }
    
            return returnData;
        }
        finally {
            running = false;
            ThreadMap.remove("IS_XA_ABORT");
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isValid() {
        return this.valid;
    }

    protected void setValid(boolean valid) {
        logger.debug("Invalidating GreenVulcano instance " + this);
        this.valid = valid;
    }

    /**
     * Destroy the internal objects
     */
    public void destroy(boolean force)
    {
        setValid(false);
        if (running && !force) {
            return;
        }
        logger.debug("Destroing GreenVulcano instance " + this);
        if (gvContext != null) {
            gvContext.destroy();
            gvContext = null;
        }
        if (statisticsDataManager != null) {
            statisticsDataManager.destroy();
            statisticsDataManager = null;
        }
        if (gvSvcConfMgr != null) {
            gvSvcConfMgr.destroy();
            gvSvcConfMgr = null;
        }
        if (dteController != null) {
            dteController.destroy();
            dteController = null;
        }
    }

    /**
     * Execute the requested Operation.
     * 
     * @param gvsOperation
     *        The name of the Operation to invoke
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @return
     * @throws GVPublicException
     */
    private GVBuffer handleFlow(String gvsOperation, GVBuffer gvBuffer) throws GVPublicException
    {

        return handleFlow(gvsOperation, gvBuffer, null, null);
    }

    /**
     * Execute the requested Operation.
     * 
     * @param gvsOperation
     *        The name of the Operation to invoke
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @param flowSystem
     *        If not null, overwrite the gvBuffer system
     * @param flowService
     *        If not null, overwrite the gvBuffer service
     * @return
     * @throws GVPublicException
     */
    private GVBuffer handleFlow(String gvsOperation, GVBuffer gvBuffer, String flowSystem, String flowService)
            throws GVPublicException
    {
        running = true;
        try {
            NMDC.push();
            GVBufferMDC.put(gvBuffer);
            NMDC.setOperation(gvsOperation);

            GVBuffer returnData = null;
            GVServiceConf gvsConfig = null;
            ServiceConcurrencyInfo serviceConcInfo = null;
            boolean success = false;
            String id = gvBuffer.getId().toString();
            long startTime = 0;

            gvContext.setContext(gvsOperation, gvBuffer);

            try {
                gvContext.push();
                startTime = System.currentTimeMillis();

                serviceConcInfo = ConcurrencyHandler.instance().add(GVC_SUBSYSTEM, gvBuffer);

                if (logger.isInfoEnabled()) {
                    logger.info(GVFormatLog.formatBEGINOperation(gvBuffer).toString());
                }

                gvsConfig = createGVSConfig(gvBuffer, flowSystem, flowService);

                gvsConfig.manageAliasInput(gvBuffer);

                if (!ACLManager.canAccess(new GVCoreServiceKey(gvsConfig.getGroupName(), gvsConfig.getServiceName(),
                        gvsOperation))) {
                    throw new GVCoreSecurityException("GV_SERVICE_POLICY_ERROR", new String[][]{
                            {"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"user", GVIdentityHelper.getName()}});
                }

                GVFlow gvOp = gvsConfig.getGVOperation(gvBuffer, gvsOperation);

                if (logger.isInfoEnabled()) {
                    GVBufferDump dump = new GVBufferDump(gvBuffer, false);
                    logger.info("INPUT GVBuffer: "+dump);                        
                }

                returnData = gvOp.perform(gvBuffer);
                gvsConfig.manageAliasOutput(returnData);

                if (logger.isInfoEnabled()) {
                    GVBufferDump dump = new GVBufferDump(returnData, false);
                    logger.info("OUTPUT GVBuffer: "+dump);
                }                
    
                long endTime = System.currentTimeMillis();
    
                if (logger.isInfoEnabled()) {
                    logger.info(GVFormatLog.formatENDOperation(returnData, endTime - startTime).toString());
                }
    
                success = true;
            }
            catch (GVCoreCallSvcException exc) {
                throwGVPublicException("GV_CALL_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreInputServiceException exc) {
                throwGVPublicException("GV_INPUT_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreOutputServiceException exc) {
                throwGVPublicException("GV_OUTPUT_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreTimeoutException exc) {
                throwGVPublicException("GV_SERVICE_TIMEOUT_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreWrongOpException exc) {
                throwGVPublicException("GV_WRONG_PARADIGM_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreDisabledServiceException exc) {
                throwGVPublicException("GV_SERVICE_DISABLED_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreServiceNotFoundException exc) {
                throwGVPublicException("GV_SERVICE_NOT_FOUND_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreWrongInterfaceException exc) {
                throwGVPublicException("GV_WRONG_INTERFACE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreWrongParameterException exc) {
                throwGVPublicException("GV_PARAMETER_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreConfException exc) {
                throwGVPublicException("GV_CONFIGURATION_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreSecurityException exc) {
                throwGVPublicException("GV_SERVICE_POLICY_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVCoreException exc) {
                logger.error("GVCore Internal Exception", exc);
                throwGVPublicException("GV_CALL_SERVICE_ERROR", gvBuffer, exc, startTime);
            }
            catch (GVPublicException exc) {
                logExc(exc, startTime);
                throw exc;
            }
            catch (InterruptedException exc) {
                logExcST(exc);
                throwGVPublicException("GV_INTERRUPTED_ERROR", gvBuffer, exc, startTime);
            }
            catch (Exception exc) {
                logExcST(exc);
                throwGVPublicException("GV_GENERIC_ERROR", gvBuffer, exc, startTime);
            }
            finally {
                if (serviceConcInfo != null) {
                    serviceConcInfo.remove();
                }
                try {
                    if (gvsConfig != null) {
                        ServiceOperationInfo serviceInfo = ServiceOperationInfoManager.instance().getServiceOperationInfo(
                                gvsConfig.getServiceName(), true);
                        serviceInfo.flowTerminated(gvsOperation, id, success);
                    }
                }
                catch (Exception exc) {
                    logger.warn("Error on MBean registration");
                }

                gvContext.pop();
                gvContext.cleanup();
                NMDC.pop();
            }

            return returnData;
        }
        finally {
            running = false;
            ThreadMap.remove("IS_XA_ABORT");
        }
    }


    /**
     * @param excID
     *        the exception id
     * @param gvBuffer
     *        the GVBuffer instance to use as data source
     * @param exc
     *        the generating exception
     * @param startTime
     *        the execution start time
     * @exception GVPublicException
     */
    private void throwGVPublicException(String excID, GVBuffer gvBuffer, Exception exc, long startTime)
            throws GVPublicException
    {
        GVPublicException excOut = new GVPublicException(excID, new String[][]{{"service", gvBuffer.getService()},
                {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()}, {"message", exc.toString()}});
        logExc(excOut, startTime);
        throw excOut;
    }

    /**
     * Returns the a GVServiceConf instance that holds the given system::service
     * configuration.
     * 
     * @param gvBuffer
     *        The GreenVulcano data coming from the client (the request buffer)
     * @return A GVServiceConf instance that holds the given system::service
     *         configuration
     * @throws GVCoreException
     *         if an error occurs at Communication Layer or core level
     */
    private GVServiceConf createGVSConfig(GVBuffer gvBuffer, String flowSystem, String flowService) throws GVException,
            GVCoreException
    {
        if ((flowSystem != null) && !flowSystem.equals("") && (flowService != null) && !flowService.equals("")) {
            gvBuffer = new GVBuffer(flowSystem, flowService, gvBuffer.getId());
        }
        GVServiceConf gvsConfig = gvSvcConfMgr.getGVSConfig(gvBuffer);
        return gvsConfig;
    }

    /**
     * Log the given exception.
     * 
     * @param exc
     * @param startTime
     */
    private void logExc(GVPublicException exc, long startTime)
    {
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.error(GVFormatLog.formatENDOperation(exc, totalTime).toString());
    }

    /**
     * Log the given exception, with the stack trace.
     * 
     * @param exc
     */
    private void logExcST(Exception exc)
    {
        logger.error("Operation Error - Error performing service call: ", exc);
    }

}