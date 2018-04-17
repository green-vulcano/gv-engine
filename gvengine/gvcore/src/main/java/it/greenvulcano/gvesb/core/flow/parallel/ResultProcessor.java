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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 21/nov/2013
 * @author GreenVulcano Developer Team
 * 
 */
public class ResultProcessor
{
    public enum ProcessorInput {
        PROCESS_ONLY_OBJECT("Process Only Object"), PROCESS_OBJECT_ERROR("Process Object and Error"), PROCESS_ONLY_GVBUFFER(
                "Process Only GVBuffer"), PROCESS_GVBUFFER_ERROR("Process GVBuffer and Error");

        private String desc;

        private ProcessorInput(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        public static ProcessorInput fromString(String name) {
            if ((name == null) || "".equals(name)) {
                return null;
            }
            if ("only-object".equals(name)) {
                return PROCESS_ONLY_OBJECT;
            }
            if ("only-gvbuffer".equals(name)) {
                return PROCESS_ONLY_GVBUFFER;
            }
            if ("object-and-error".equals(name)) {
                return PROCESS_OBJECT_ERROR;
            }
            if ("gvbuffer-and-error".equals(name)) {
                return PROCESS_GVBUFFER_ERROR;
            }
            return null;
        }
    }

    private static final Logger logger             = org.slf4j.LoggerFactory.getLogger(ResultProcessor.class);

    private ProcessorInput      processorInput     = null;
    private boolean             failOnError        = true;

    // XMLAggregate
    private String              aggregateRoot      = null;
    private String              aggregateNamespace = null;

    // Script
    private ScriptExecutor      script             = null;

    public void init(Node node) throws GVCoreConfException {
        processorInput = ProcessorInput.fromString(XMLConfig.get(node, "@processor-input", ""));
        if (processorInput == null) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'processor-input'"}, {"node", XPathFinder.buildXPath(node)}});
        }

        failOnError = XMLConfig.getBoolean(node, "@fail-on-error", true);

        // XMLAggregate
        aggregateRoot = XMLConfig.get(node, "XMLAggregate/@root", null);
        aggregateNamespace = XMLConfig.get(node, "XMLAggregate/@namespace", null);
        // Script
        try {
            Node sNode = XMLConfig.getNode(node, "Script");
            if (sNode != null) {
                script = ScriptExecutorFactory.createSE(sNode);
            }
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'Script'"}, {"node", XPathFinder.buildXPath(node)}}, exc);
        }

        logger.debug("Configured " + toString());
    }

    public GVBuffer process(GVBuffer input, List<Result> results) throws GVCoreException, InterruptedException {
        if ((aggregateRoot != null) && !"".equals(aggregateRoot)) {
            logger.debug("Using XMLAggregate Processor: root[" + aggregateNamespace + ":" + aggregateRoot + "]");
            return xmlProcessor(input, results);
        }
        if (script != null) {
            logger.debug("Using Script Processor: " + script.getEngineName() + "/" + script.getScriptName());
            return scriptProcessor(input, results);
        }
        logger.debug("Using Default Processor");
        return defaultProcessor(input, results);
    }

    @Override
    public String toString() {
        String desc = "ResultProcessor: failOnError[" + failOnError + "] - processorInput[" + processorInput + "]";
        if ((aggregateRoot != null) && !"".equals(aggregateRoot)) {
            desc += " - Use XMLAggregate Processor: root[" + aggregateNamespace + ":" + aggregateRoot + "]";
        }
        else if (script != null) {
            desc += " - Use Script Processor: [" + script.getEngineName() + "/" + script.getScriptName() + "]";
        }
        else {
            desc += " - Use Default Processor";
        }
        return desc;
    }

    private GVBuffer defaultProcessor(GVBuffer input, List<Result> results) throws GVCoreException,
            InterruptedException {
        List<Object> toProcess = new ArrayList<Object>();

        Iterator<Result> itInput = results.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            Object d = currOutput.getOutput();
            if (failOnError && (currOutput.getState() != Result.State.STATE_OK)) {
                throw new GVCoreException("GVCORE_PARALLEL_EXEC_ERROR", new String[][]{{"message", "" + d}},
                        (Throwable) d);
            }
            switch (processorInput) {
                case PROCESS_GVBUFFER_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                case PROCESS_ONLY_GVBUFFER :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                    break;
                case PROCESS_OBJECT_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                case PROCESS_ONLY_OBJECT :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(((GVBuffer) d).getObject());
                        }
                    }
                    break;
            }
        }
        try {
            input.setObject(toProcess);
        }
        catch (Exception exc) {
            // do nothing
        }
        return input;
    }

    private GVBuffer xmlProcessor(GVBuffer input, List<Result> results) throws GVCoreException, InterruptedException {
        List<Object> toProcess = new ArrayList<Object>();

        Iterator<Result> itInput = results.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            Object d = currOutput.getOutput();
            if (failOnError && (currOutput.getState() != Result.State.STATE_OK)) {
                throw new GVCoreException("GVCORE_PARALLEL_EXEC_ERROR", new String[][]{{"message", "" + d}},
                        (Throwable) d);
            }
            if (currOutput.getState() == Result.State.STATE_OK) {
                if (d != null) {
                    toProcess.add(((GVBuffer) d).getObject());
                }
            }
        }
        try {
            input.setObject(XMLUtils.aggregateXML_S(aggregateRoot, aggregateNamespace, toProcess.toArray()));
            return input;
        }
        catch (Exception exc) {
            throw new GVCoreException("GVCORE_PARALLEL_XML_AGGREGATE_ERROR", new String[][]{{"message", "" + exc}}, exc);
        }
    }

    private GVBuffer scriptProcessor(GVBuffer input, List<Result> results) throws GVCoreException, InterruptedException {
        List<Object> toProcess = new ArrayList<Object>();

        Iterator<Result> itInput = results.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            Object d = currOutput.getOutput();
            if (failOnError && (currOutput.getState() != Result.State.STATE_OK)) {
                throw new GVCoreException("GVCORE_PARALLEL_EXEC_ERROR", new String[][]{{"message", "" + d}},
                        (Throwable) d);
            }
            switch (processorInput) {
                case PROCESS_GVBUFFER_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        toProcess.add(currOutput);
                    }
                case PROCESS_ONLY_GVBUFFER :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        toProcess.add(currOutput);
                    }
                    break;
                case PROCESS_OBJECT_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                case PROCESS_ONLY_OBJECT :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(((GVBuffer) d).getObject());
                        }
                    }
                    break;
            }
        }

        try {
            script.putProperty("results", toProcess);
            script.putProperty("data", input);
            script.putProperty("logger", logger);
            script.execute(GVBufferPropertiesHelper.getPropertiesMapSO(input, true), input);
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new GVCoreException("GVCORE_PARALLEL_JS_AGGREGATE_ERROR", new String[][]{{"message", "" + exc}}, exc);
        }
        return input;
    }
}
