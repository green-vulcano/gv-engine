package it.greenvulcano.gvesb.core.flow.iteration.controller;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.config.GVServiceConf;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.config.ServiceConfigManager;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreSecurityException;
import it.greenvulcano.gvesb.core.flow.GVFlow;
import it.greenvulcano.gvesb.core.flow.GVSubFlow;
import it.greenvulcano.gvesb.core.flow.iteration.LoopController;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.policy.ACLManager;
import it.greenvulcano.gvesb.policy.impl.GVCoreServiceKey;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.VCLOperationKey;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xpath.XPathFinder;

/**
 * An abstract base implementation for {@link LoopController} inlcuding commons ops.
 * 
 * The abstract method <b>doLoop</b> implemented in subclasses will contains the specific iteration logic
 * and tipically will must invokes <b>performAction</b> to perform per-item actions.
 * 
 * 
 * @version 4.0.0 20160603
 * @author GreenVulcano Developer Team
 * 
 */
public abstract class BaseLoopController implements LoopController {

	protected final Logger LOG = LoggerFactory.getLogger(getClass());

	final static int ITERATOR_OPTYPE_CALL = 1;
	final static int ITERATOR_OPTYPE_ENQUEUE = 2;
	final static int ITERATOR_OPTYPE_CALLSERVICE = 3;
	final static int ITERATOR_OPTYPE_CALLSUBFLOW = 4;

	private int operationType = 0;
	private Operation operation = null;

	private Map<String, GVSubFlow> subFlows = new HashMap<String, GVSubFlow>();

	private String service = null;
	private String system = null;
	private String flowOp = null;
	private String subflow = null;

	private boolean isFlowSysSvcOpDynamic = false;
	private boolean changeLogContext = true;	
	private boolean changeLogMasterService = false;
	private boolean onDebug = false;
	
	private InvocationContext gvContext = null;

	private Node defNode = null;
	private String id = null;

	public static final LoopController create(Type controllerType, Node node) throws InitializationException{
		switch (controllerType) {
			case JAVA_COLLECTION:
				//TODO: A LoopController implementation to handle a Collection
				break;
			case JSON_ARRAY:
				//TODO: A LoopController implementation to handle a JSONArray
				break;
			case JSON_OBJECT:
				BaseLoopController loopController = new JSONObjectLoopController();
				loopController.init(node);
				return loopController;
			case XML_NODE:
				//TODO: A LoopController implementation to handle XML nodes
				break;
			default:
				break;
		
		}
		
		return null;
	}
		
	protected void init(Node node) throws InitializationException {

		try {

			defNode = node;

			if (XMLConfig.exists(node, "*[@type='call']")) {
				operationType = ITERATOR_OPTYPE_CALL;
				LOG.debug("operationType=ITERATOR_OPTYPE_CALL");
				OperationKey opKey = new VCLOperationKey(XMLConfig.getNode(node, "*[@type='call']"));

				operation = OperationFactory.createCall(opKey);
			} else if (XMLConfig.exists(node, "*[@type='enqueue']")) {
				operationType = ITERATOR_OPTYPE_ENQUEUE;
				LOG.debug("operationType=ITERATOR_OPTYPE_ENQUEUE");
				OperationKey opKey = new VCLOperationKey(XMLConfig.getNode(node, "*[@type='enqueue']"));
				operation = OperationFactory.createEnqueue(opKey);
			} else if (XMLConfig.exists(node, "CoreCall")) {
				operationType = ITERATOR_OPTYPE_CALLSERVICE;
				LOG.debug("operationType=ITERATOR_CORE_CALL");
				changeLogContext = XMLConfig.getBoolean(node, "CoreCall/@change-log-context", true);
				changeLogMasterService = changeLogContext
						&& XMLConfig.getBoolean(defNode, "CoreCall/@change-log-master-service", false);
				system = XMLConfig.get(node, "CoreCall/@id-system", GVBuffer.DEFAULT_SYS);
				LOG.debug("system=" + system);
				service = XMLConfig.get(node, "CoreCall/@id-service");
				LOG.debug("service=" + service);
				flowOp = XMLConfig.get(node, "CoreCall/@operation");
				LOG.debug("flowOp=" + flowOp);
				isFlowSysSvcOpDynamic = XMLConfig.getBoolean(node, "CoreCall/@dynamic", false);
				LOG.debug("isFlowSysSvcOpDynamic  = " + isFlowSysSvcOpDynamic);

				gvContext = new InvocationContext();
			} else if (XMLConfig.exists(node, "SubFlowCall")) {
				operationType = ITERATOR_OPTYPE_CALLSUBFLOW;
				LOG.debug("operationType=ITERATOR_SUBFLOW_CALL");
				changeLogContext = XMLConfig.getBoolean(node, "SubFlowCall/@change-log-context", true);
				subflow = XMLConfig.get(node, "SubFlowCall/@subflow");
				LOG.debug("subflow=" + subflow);
				isFlowSysSvcOpDynamic = XMLConfig.getBoolean(node, "SubFlowCall/@dynamic", false);
				LOG.debug("isFlowSysSvcOpDynamic  = " + isFlowSysSvcOpDynamic);

				gvContext = new InvocationContext();
			} else {
				LOG.error("An error occurred while configuring the " + getClass().getName()
						+ " plug-in: unsupported iterator operation type");
				throw new InitializationException("GV_CONFIGURATION_ERROR",
						new String[][] { { "message", "Unsupported iterator operation type" } });
			}
		} catch (InitializationException exc) {
			throw exc;

		} catch (XMLConfigException exc) {
			LOG.error("An error occurred while configuring the " + getClass().getName() + " plug-in: ", exc);
			throw new InitializationException("GV_CONFIGURATION_ERROR",
					new String[][] { { "message", exc.getMessage() } }, exc);
		} catch (Exception exc) {
			LOG.error("A generic error occurred while initializing the " + getClass().getName() + " plug-in: ", exc);
			throw new InitializationException("GV_CONFIGURATION_ERROR",
					new String[][] { { "message", exc.getMessage() } }, exc);
		}
	}

	@Override
	public GVBuffer executeLoop(GVBuffer inputData, boolean onDebug) throws GVException{
		this.onDebug = onDebug;
		return doLoop(inputData);
	}
			
	protected abstract GVBuffer doLoop(GVBuffer inputCollection) throws GVException;
	
	/**
	 * Perform the requested call on a collection item.
	 * Tipically must be invoked inside {@link doLoop}
	 * 
	 * @param inputCollectionItem a GVBuffer builded over the collection item
	 * @return A GVBuffer for the result
	 */
	protected GVBuffer performAction(GVBuffer inputCollectionItem) {
		GVBuffer currIterOutput = null;
		try {      

			if (operationType == ITERATOR_OPTYPE_CALLSERVICE) {
				currIterOutput = executeCoreCall(inputCollectionItem, onDebug);
			}
			else if (operationType == ITERATOR_OPTYPE_CALLSUBFLOW) {
				currIterOutput = executeSubFlow(inputCollectionItem, onDebug);
			}
			else {
				currIterOutput = operation.perform(inputCollectionItem);
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("currIterOutput=" + currIterOutput.toString());
			}

		} catch (Exception exc) {
			LOG.error("Performing iteration  caused a " + exc.getClass().getName());

		}

		return currIterOutput;

	}

	private GVBuffer executeCoreCall(GVBuffer internalGVBuffer, boolean onDebug) throws GVException, InterruptedException
	{
		GVBuffer result = null;

		try {
			String localSystem = (GVBuffer.DEFAULT_SYS.equals(system) ? internalGVBuffer.getSystem() : system);
			String localService = service;
			String localFlowOp = flowOp;

			if (isFlowSysSvcOpDynamic) {
				Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(internalGVBuffer, true);
				localSystem = PropertiesHandler.expand(localSystem, props, internalGVBuffer);
				localService = PropertiesHandler.expand(localService, props, internalGVBuffer);
				localFlowOp = PropertiesHandler.expand(localFlowOp, props, internalGVBuffer);
			}

			internalGVBuffer.setService(localService);
			internalGVBuffer.setSystem(localSystem);
			GVServiceConf gvsConfig = null;
			InvocationContext mainCtx = (InvocationContext) InvocationContext.getInstance();
			gvContext.setContext(localFlowOp, internalGVBuffer);
			gvContext.setGVServiceConfigManager(mainCtx.getGVServiceConfigManager());
			gvContext.setStatisticsDataManager(mainCtx.getStatisticsDataManager());
			gvContext.setExtraField("DTE_CONTROLLER", mainCtx.getExtraField("DTE_CONTROLLER"));

			ServiceConfigManager svcMgr = gvContext.getGVServiceConfigManager();
			gvsConfig = svcMgr.getGVSConfig(internalGVBuffer);
			if (!ACLManager.canAccess(new GVCoreServiceKey(gvsConfig.getGroupName(), gvsConfig.getServiceName(),
					localFlowOp))) {
				throw new GVCoreSecurityException("GV_SERVICE_POLICY_ERROR", new String[][]{
					{"service", internalGVBuffer.getService()}, {"system", internalGVBuffer.getSystem()},
					{"id", internalGVBuffer.getId().toString()}, {"user", GVIdentityHelper.getName()}});
			}
			GVFlow flow = gvsConfig.getGVOperation(internalGVBuffer, localFlowOp);

			NMDC.push();
			try {
				gvContext.push();
				if (changeLogContext) {
					GVBufferMDC.put(internalGVBuffer);
					NMDC.setOperation(localFlowOp);
					NMDC.put(GVBuffer.Field.SERVICE.toString(), localService);
					NMDC.put(GVBuffer.Field.SYSTEM.toString(), localSystem);
				}

				String masterService = null;
				try {
					if (changeLogMasterService) {
						masterService = GVBufferMDC.changeMasterService(localService);
					}


					result = flow.perform(internalGVBuffer, onDebug);
				}
				finally {

					if (changeLogMasterService) {
						GVBufferMDC.changeMasterService(masterService);
					}
				}
			}
			finally {
				NMDC.pop();
				try {
					gvContext.pop();
					gvContext.cleanup();
				}
				catch (Exception exc) {
					LOG.warn("Failed cleanUp() of InvocationContext", exc);
				}
			}
		} catch (Exception exc) {
			LOG.error("Error performing operation", exc);
			ThreadUtils.checkInterrupted(exc);
			throw new GVException("Error performing operation: " + exc);
		}
		return result;
	}

	private GVBuffer executeSubFlow(GVBuffer internalGVBuffer, boolean onDebug)	throws GVException, InterruptedException {
		GVBuffer result = null;

		try {
			String localSubFlow = subflow;

			if (isFlowSysSvcOpDynamic) {
				Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(internalGVBuffer, true);
				localSubFlow = PropertiesHandler.expand(subflow, props, internalGVBuffer);
			}

			InvocationContext mainCtx = (InvocationContext) InvocationContext.getInstance();
			gvContext.setContext(localSubFlow, internalGVBuffer);
			gvContext.setGVServiceConfigManager(mainCtx.getGVServiceConfigManager());
			gvContext.setStatisticsDataManager(mainCtx.getStatisticsDataManager());

			GVSubFlow subFlow = getSubFlow(localSubFlow);

			NMDC.push();
			try {
				gvContext.push();
				if (changeLogContext) {
					NMDC.setOperation(localSubFlow);
					GVBufferMDC.put(internalGVBuffer);
				}
				result = subFlow.perform(internalGVBuffer, onDebug);
			} finally {
				NMDC.pop();
				try {
					gvContext.pop();
					gvContext.cleanup();
				} catch (Exception exc) {
					LOG.warn("Failed cleanUp() of InvocationContext", exc);
				}
			}
		} catch (Exception exc) {
			LOG.error("Error performing operation", exc);
			ThreadUtils.checkInterrupted(exc);
			throw new GVException("Error performing operation: " + exc);
		}
		return result;
	}

	private GVSubFlow getSubFlow(String name) throws GVCoreConfException {
		GVSubFlow subFlow = subFlows.get(name);
		if (subFlow == null) {
			try {
				Node fNode = XMLConfig.getNode(defNode, "ancestor::Operation/SubFlow[@name='" + name + "']");
				if (fNode == null) {
					throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR",
							new String[][] { { "name", "'subflow'" }, { "node", XPathFinder.buildXPath(defNode) } });
				}
				subFlow = new GVSubFlow();
				subFlow.init(fNode, true);
				subFlows.put(name, subFlow);
			} catch (GVCoreConfException exc) {
				throw exc;
			} catch (Exception exc) {
				throw new GVCoreConfException("GVCORE_SUB_FLOW_INIT_ERROR",
						new String[][] { { "id", id }, { "node", XPathFinder.buildXPath(defNode) } }, exc);
			}
		}
		return subFlow;
	}

}
