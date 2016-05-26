/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.flow.GVFlowNode;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.List;

import org.w3c.dom.Node;

/**
 *
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 *
 */
public abstract class BaseParallelNode extends GVFlowNode
{
    protected ResultProcessor resultProcessor = null;
    
    @Override
    public void init(Node defNode) throws GVCoreConfException {
        super.init(defNode);
        try {
            Node rsNode = XMLConfig.getNode(defNode, "ResultProcessor");
            resultProcessor = new ResultProcessor();
            resultProcessor.init(rsNode);
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'ResultProcessor'"}, {"node", XPathFinder.buildXPath(defNode)}});
        }
    }

    protected GVBuffer processOutput(GVBuffer inputData, List<Result> results) throws GVException, InterruptedException {
        GVBuffer outputData = resultProcessor.process(inputData, results);
        return outputData;
    }
}
