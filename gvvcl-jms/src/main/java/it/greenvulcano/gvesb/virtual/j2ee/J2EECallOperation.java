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
package it.greenvulcano.gvesb.virtual.j2ee;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.VCLException;
import it.greenvulcano.util.clazz.ClassUtils;
import it.greenvulcano.util.clazz.ClassUtilsException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * This class realizes call mechanism for a J2EE EJB.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class J2EECallOperation extends J2EEOperation implements CallOperation
{
	private static final Logger logger = LoggerFactory.getLogger(J2EECallOperation.class);

    private static final String EJB_2                  = "2";
    private static final String EJB_3                  = "3";

    private String              jndiName               = null;
    private String              methodName             = null;
    private String              parameterTypes         = null;
    private String              inputRefDP             = null;
    private String              outputRefDP            = null;
    private String              homeInterfaceClassName = null;
    private String              interfaceClassName     = null;
    private String              ejbVersion             = null;

    /**
     * Remote or local interface of the EJB to call.
     */
    private Object              ejbObject              = null;

    /**
     * Invoked method.
     */
    private Method              publicMethod           = null;

    /**
     * Signature of called method.
     */
    private Class<?>            signature[]            = null;

    /**
     * Array used to pass parameters to the called method.
     */
    private Object              params[]               = null;

    /**
     * Completes operation specific initialization.
     */
    @Override
    protected void j2eeInit(Node node) throws InitializationException, XMLConfigException
    {
        jndiName = XMLConfig.get(node, "@jndi-name");
        checkAttribute("jndi-name", jndiName);
        logger.debug("JNDI name..............: " + jndiName);
        methodName = XMLConfig.get(node, "@method");
        checkAttribute("method", methodName);
        logger.debug("method name............: " + methodName);
        parameterTypes = XMLConfig.get(node, "@parameterTypes");
        checkAttribute("parameterTypes", parameterTypes);
        logger.debug("parameterTypes.........: " + parameterTypes);
        inputRefDP = XMLConfig.get(node, "@input-ref-dp", "");
        logger.debug("input ref dp...........: " + inputRefDP);
        outputRefDP = XMLConfig.get(node, "@output-ref-dp", "");
        logger.debug("output ref dp..........: " + outputRefDP);
        ejbVersion = XMLConfig.get(node, "@version", "3");
        logger.debug("EJB version............: " + ejbVersion);
        if (!(EJB_2.equals(ejbVersion) || EJB_3.equals(ejbVersion))) {
            throw new InitializationException("GVVCL_PARAMETER_ERROR", new String[][]{{"param", "version"},
                    {"value", ejbVersion}});
        }
        homeInterfaceClassName = XMLConfig.get(node, "@home-interface", "");
        if (EJB_2.equals(ejbVersion)) {
            checkAttribute("home-interface", homeInterfaceClassName);
        }
        logger.debug("home interface.........: " + homeInterfaceClassName);
        interfaceClassName = XMLConfig.get(node, "@remote-or-local-interface");
        checkAttribute("remote-or-local-interface", interfaceClassName);
        logger.debug("interface..............: " + interfaceClassName);
    }

    /**
     * Called when the connection is established.
     */
    @Override
    protected void j2eeConnectionEstablished(Context context) throws Exception
    {
        logger.debug("Lookup for " + jndiName);
        if (EJB_2.equals(ejbVersion)) {
            Object home = context.lookup(jndiName);
            Class<?> homeClass = Class.forName(homeInterfaceClassName);
            Object ejbHome = PortableRemoteObject.narrow(home, homeClass);
            Method createMethod = ejbHome.getClass().getMethod("create", (Class<?>[]) null);
            Object ejbInterface = createMethod.invoke(ejbHome, (Object[]) null);
            Class<?> interfaceClass = Class.forName(interfaceClassName);
            ejbObject = PortableRemoteObject.narrow(ejbInterface, interfaceClass);
        }
        else if (EJB_3.equals(ejbVersion)) {
            Object ejbInterface = context.lookup(jndiName);
            Class<?> interfaceClass = Class.forName(interfaceClassName);
            ejbObject = PortableRemoteObject.narrow(ejbInterface, interfaceClass);
        }
    }

    /**
     * Reset the object.
     */
    @Override
    protected void j2eeConnectionClosing()
    {
        ejbObject = null;
        signature = null;
    }

    /**
     * Retreives the EJB method to call with the given segnature.
     */
    private Method getMethod() throws J2EECallException
    {
        if (publicMethod == null) {
            String pt = null;
            try {
                List<Class<?>> ptA = new ArrayList<Class<?>>();
                StringTokenizer st = new StringTokenizer(parameterTypes, ", ");
                while (st.hasMoreTokens()) {
                    pt = st.nextToken();
                    ptA.add(ClassUtils.getRealClass(pt));
                }
                signature = new Class<?>[ptA.size()];
                signature = ptA.toArray(signature);
            }
            catch (ClassUtilsException exc) {
                throw new J2EECallException("GVVCL_J2EE_INVALID_METHOD_PARAMETERS", new String[][]{
                        {"method", methodName}, {"parameterTypes", parameterTypes}, {"parameter", pt}});
            }

            try {
                publicMethod = ejbObject.getClass().getMethod(methodName, signature);
            }
            catch (NoSuchMethodException exc) {
                throw new J2EECallException("GVVCL_J2EE_INVALID_METHOD_ERROR", new String[][]{{"method", methodName},
                        {"parameterTypes", parameterTypes}});
            }

            /*Class<?> returnClass = publicMethod.getReturnType();
            if (!GVBuffer.class.isAssignableFrom(returnClass)) {
                throw new J2EECallException("GVVCL_J2EE_INVALID_RETURN_TYPE_ERROR", new String[][]{
                        {"method", methodName}, {"parameterTypes", parameterTypes},
                        {"returnType", GVBuffer.class.getName()}});
            }*/
        }
        return publicMethod;
    }


    /**
     * This method just delegates to the startPerform() method of the superclass
     * in order to perform the operation using the J2EEOperation facilities
     * (connection management)
     * 
     * @param gvBuffer
     * @return the result of the call
     * @throws J2EEConnectionException
     * @throws J2EECallException
     * @throws InvalidDataException
     */
    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws J2EEConnectionException, J2EECallException, InvalidDataException
    {
        try {
            return startPerform(gvBuffer);
        }
        catch (J2EECallException exc) {
            throw exc;
        }
        catch (J2EEConnectionException exc) {
            throw exc;
        }
        catch (InvalidDataException exc) {
            throw exc;
        }
        catch (VCLException exc) {
            throw new J2EECallException("GVVCL_J2EE_INTERNAL_ERROR", exc);
        }
    }


    /**
     * Execute the actual invocation to the EJB
     */
    @Override
    protected GVBuffer j2eePerform(GVBuffer data) throws ConnectionException, CallException, InvalidDataException,
            RemoteException
    {
        Method method = getMethod();
        return j2eeCall(data, method);
    }

    /**
     * Called if the perform fails.
     */
    @Override
    protected GVBuffer j2eePerformFailed(GVBuffer data, Exception exc) throws J2EECallException
    {
        throw new J2EECallException("GVVCL_J2EE_INVOCATION_ERROR", new String[][]{{"exc", exc.toString()}}, exc);
    }

    /**
     * Executes the actual invocation to the EJB.
     */
    private GVBuffer j2eeCall(GVBuffer data, final Method method) throws ConnectionException, CallException,
            InvalidDataException, RemoteException
    {
        try {
            try {
                Object dataOut = null;

                DataProviderManager dataProviderManager = DataProviderManager.instance();
                if ((inputRefDP != null) && (inputRefDP.length() > 0)) {
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(inputRefDP);
                    try {
                        logger.debug("Working on Input array data provider: " + inputRefDP);
                        dataProvider.setObject(data);
                        params = (Object[]) dataProvider.getResult();
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(inputRefDP, dataProvider);
                    }
                }
                else {
                    params = new Object[]{data};
                }

                if (subjectBuilder != null) {
                    logger.debug("Calling with SubjectBuilder: " + subjectBuilder);
                    try {
                        dataOut = subjectBuilder.runAs(new PrivilegedExceptionAction<Object>() {
                            @Override
                            @SuppressWarnings("synthetic-access")
                            public Object run() throws RemoteException, InvocationTargetException,
                                    IllegalAccessException, GVException
                            {
                                return method.invoke(ejbObject, params);
                            }
                        });
                    }
                    catch (PrivilegedActionException paex) {
                        Exception exc = paex.getException();
                        throw exc;
                    }
                }
                else {
                    dataOut = method.invoke(ejbObject, params);
                }

                if ((outputRefDP != null) && (outputRefDP.length() > 0)) {
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(outputRefDP);
                    try {
                        logger.debug("Working on Output data provider: " + outputRefDP);
                        dataProvider.setContext(data);
                        dataProvider.setObject(dataOut);
                        data = (GVBuffer) dataProvider.getResult();
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(outputRefDP, dataProvider);
                    }
                    return data;
                }
                return (GVBuffer) dataOut;
            }
            catch (InvocationTargetException exc) {
                logger.error("EJB Invocation error", exc);
                throw exc.getTargetException();
            }
        }
        catch (RemoteException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            throw new J2EECallException("GVVCL_J2EE_TARGET_ERROR", new String[][]{{"method", method.toString()},
                    {"exc", "" + exc}}, exc);
        }
    }

    /**
     * Does nothing for this operation.
     */
    @Override
    protected void j2eeCleanUp()
    {
        params = null;
    }

    /**
     * Does nothing for this operation.
     */
    @Override
    protected void j2eeDestroy()
    {
        // do nothing
    }

    /**
     * String to use with logs.
     */
    @Override
    public String getDescription()
    {
        return "call EJB " + jndiName + " to " + initialContext.getProviderURL();
    }
}
