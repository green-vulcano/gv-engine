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
package it.greenvulcano.gvesb.adapter.http.formatters.handlers;

import it.greenvulcano.configuration.XMLConfig;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class implements an handler factory: given the format of the http
 * parameter to be handled, it instantiates the right object implementing the
 * <tt>InterfaceParametersHandler</tt> interface. The associations between
 * format and classnames can be read from the HTTP Adapter configuration file.
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
 */
public class InterfaceParametersHandlerFactory {
    private static Logger logWriter = org.slf4j.LoggerFactory.getLogger(InterfaceParametersHandlerFactory.class);

    /**
     * Reads XML configuration info about an handler and return a correctly
     * configured <tt>InterfaceParametersHandler</tt> object.
     *
     * @param handlerConfig
     *            a DOM element containing handler configuration info.
     * @return a correctly configured <tt>InterfaceParametersHandler</tt>
     *         object.
     * @throws ParameterHandlerFactoryException
     *             if any error occurs.
     */
    public static synchronized InterfaceParametersHandler getHandler(Node handlerConfig)
            throws ParameterHandlerFactoryException {
        logWriter.debug("getHandler start");
        String handlerClassname = null;
        try {
            handlerClassname = XMLConfig.get(handlerConfig, "@Class");
            logWriter.debug("getHandler - this handler is an instance of " + handlerClassname);
            InterfaceParametersHandler handler = (InterfaceParametersHandler) Class.forName(handlerClassname)
                    .newInstance();
            handler.init(handlerConfig);
            logWriter.debug("getHandler stop");
            return handler;
        }
        catch (ClassNotFoundException ex) {
            logWriter.error("getHandler - Error while loading InterfaceParametersHandler's Java class: " + ex);
            throw new ParameterHandlerFactoryException("GVHA_CLASS_INSTANTIATION_ERROR", new String[][] {
                    { "className", handlerClassname }, { "errorName", "" + ex } }, ex);
        }
        catch (InstantiationException ex) {
            logWriter.error("getHandler - Error while instantiating InterfaceParametersHandler's Java class: " + ex);
            throw new ParameterHandlerFactoryException("GVHA_CLASS_INSTANTIATION_ERROR", new String[][] {
                    { "className", handlerClassname }, { "errorName", "" + ex } }, ex);
        }
        catch (IllegalAccessException ex) {
            logWriter.error("getHandler - Access error while instantiating InterfaceParametersHandler's Java class: "
                    + ex);
            throw new ParameterHandlerFactoryException("GVHA_CLASS_INSTANTIATION_ERROR", new String[][] {
                    { "className", handlerClassname }, { "errorName", "" + ex } }, ex);
        }
        catch (Exception ex) {
            logWriter.error("getHandler - Error while configuring InterfaceParametersHandler object: " + ex);
            throw new ParameterHandlerFactoryException("GVHA_HANDLER_INITIALIZATION_ERROR", new String[][] { {
                    "errorName", "" + ex } }, ex);
        }
    }

}
