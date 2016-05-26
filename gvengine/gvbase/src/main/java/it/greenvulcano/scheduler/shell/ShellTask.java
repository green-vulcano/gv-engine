/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.scheduler.shell;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.scheduler.Task;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.shell.StreamPumper;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implements a task able to invoke a shell script.
 * 
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class ShellTask extends Task
{

    private static final Logger logger        = org.slf4j.LoggerFactory.getLogger(ShellTask.class);

    /**
     * The directory in which the shell command must be executed.
     * This value can contains placeholders which will be resolved at runtime.
     */
    private String              baseDirectory = null;

    /**
     * The command (or command sequence) to be executed.
     * This value contains placeholders which will be resolved at runtime.
     */
    private String[]            commandList   = null;

    /**
     * The environment properties to be set before executing the command(s).
     * This value contains placeholders which will be resolved at runtime.
     */
    private String[]            propsList     = null;

    private boolean             isArray       = false;

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#getLogger()
     */
    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#initTask(org.w3c.dom.Node)
     */
    @Override
    protected void initTask(Node node) throws TaskException
    {
        try {
            if (XMLConfig.exists(node, "@directory")) {
                baseDirectory = XMLConfig.get(node, "@directory");
                logger.debug("Configured base directory for command execution: " + baseDirectory);
            }

            initCommand(node);
            initProperties(node);
        }
        catch (Exception exc) {
            logger.error("Error initializing Task(" + getFullName() + ")", exc);
            throw new TaskException("Error initializing Task(" + getFullName() + ")", exc);
        }
    }

    /**
     * Initializes the string command.
     * 
     * @param node
     *        the configuration node.
     * 
     * @throws XMLConfigException
     *         if an error occurs reading the configuration.
     */
    private void initCommand(Node node) throws XMLConfigException
    {
        if (XMLConfig.exists(node, "cmd")) {
            commandList = new String[1];
            String currCommand = XMLConfig.get(node, "cmd/text()", "").trim();
            commandList[0] = currCommand;
            logger.debug("Configured command: " + currCommand);
        }
        else {
            isArray = true;
            NodeList list = XMLConfig.getNodeList(node, "cmd-array-elem");
            int size = list.getLength();
            commandList = new String[size];
            StringBuilder cmdL = new StringBuilder();
            for (int i = 0; i < size; i++) {
                String currCommand = XMLConfig.get(list.item(i), "text()", "").trim();
                commandList[i] = currCommand;
                cmdL.append(currCommand).append(" ");
            }
            logger.debug("Configured command: " + cmdL);
        }
    }

    /**
     * Initializes the environment properties to be set before invoking the
     * shell command.
     * 
     * @param node
     *        the configuration node.
     * 
     * @throws XMLConfigException
     *         if an error occurs reading the configuration.
     */
    private void initProperties(Node node) throws XMLConfigException
    {
        NodeList list = XMLConfig.getNodeList(node, "env-property");
        int size = list.getLength();
        if (size > 0) {
            propsList = new String[size];
            for (int i = 0; i < size; i++) {
                Node item = list.item(i);
                String name = XMLConfig.get(item, "@name");
                String value = XMLConfig.get(item, "@value");
                propsList[i] = name + "=" + value;
                logger.debug("Configured environment property: " + propsList[i]);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#executeTask(java.lang.String, Date, java.util.Map<java.lang.String, java.lang.String>, booolean)
     */
    @Override
    protected boolean executeTask(String name, Date fireTime, Map<String, String> locProperties, boolean isLast)
    {
        boolean success = false;
        try {
            logger.debug("Executing the task: (" + getFullName() + ") - (" + name + ")");
            Map<String, Object> props = MapUtils.convertToHMStringObject(locProperties);
            String[] realCommand = null;
            String[] realProps = null;
            File realDirectory = null;

            if (baseDirectory != null) {
                realDirectory = new File(PropertiesHandler.expand(baseDirectory, props));
            }

            if (realDirectory != null) {
                if (!realDirectory.exists()) {
                    throw new Exception("Configured execution directory " + realDirectory.getAbsolutePath()
                            + " does NOT exist.");
                }
                else if (!realDirectory.isDirectory()) {
                    throw new Exception("Configured execution directory " + realDirectory.getAbsolutePath()
                            + " is NOT a directory.");
                }
            }

            realCommand = new String[commandList.length];
            for (int i = 0; i < commandList.length; i++) {
                realCommand[i] = PropertiesHandler.expand(commandList[i], props);
            }

            if (propsList != null) {
                realProps = new String[propsList.length];
                for (int i = 0; i < propsList.length; i++) {
                    realProps[i] = PropertiesHandler.expand(propsList[i], props);
                }
            }

            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (String currCommand : realCommand) {
                    sb.append(currCommand).append(' ');
                }
                logger.debug("Executing the shell command: " + sb.toString());
                logger.debug("within "
                        + (realDirectory != null
                                ? "directory " + realDirectory.getAbsolutePath()
                                : "current working directory"));

                if (realProps != null) {
                    logger.debug("Environment property settings:");
                    for (String realProp : realProps) {
                        logger.debug(realProp);
                    }
                }
            }

            Process proc = null;
            if (isArray) {
                proc = Runtime.getRuntime().exec(realCommand, realProps, realDirectory);
            }
            else {
                proc = Runtime.getRuntime().exec(realCommand[0], realProps, realDirectory);
            }

            StreamPumper inputPumper = new StreamPumper(proc.getInputStream());
            StreamPumper errorPumper = new StreamPumper(proc.getErrorStream());

            inputPumper.start();
            errorPumper.start();

            try {
                proc.waitFor();
                inputPumper.join();
                errorPumper.join();
            }
            catch (InterruptedException ie) {
                logger.warn("Interrupted exception", ie);
            }

            logger.debug("Execution terminated");
            success = proc.exitValue() == 0;
            if (!success) {
                String stderr = errorPumper.getOutput();
                logger.warn("An error occurs executing the shell task - ExitCode: " + proc.exitValue()
                        + " + StdError: " + stderr);
            }
            logger.debug("Shell output: " + inputPumper.getOutput());
        }
        catch (Exception exc) {
            logger.error("An error occurs executing the shell Task(" + getFullName() + ") - (" + name + ")", exc);
        }
        return success;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#destroyTask()
     */
    @Override
    protected void destroyTask()
    {
        logger.debug("Destroying the task: " + getFullName());
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.scheduler.Task#sendHeartBeat()
     */
    @Override
    protected boolean sendHeartBeat()
    {
        return true;
    }
}
