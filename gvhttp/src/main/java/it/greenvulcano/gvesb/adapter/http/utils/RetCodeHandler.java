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
package it.greenvulcano.gvesb.adapter.http.utils;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * RetCodeHandler class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */

public class RetCodeHandler
{
    private static Logger logWriter            = org.slf4j.LoggerFactory.getLogger(RetCodeHandler.class);

    private boolean       toBetrasformed       = false;
    private int           transformMode        = 0;
    private int           inverseTransformMode = 1;

    /**
     * @param configurationNode
     */
    public void init(Node configurationNode)
    {
        String retCodeConversion = XMLConfig.get(configurationNode, "@RetCodeConversion", null);
        logWriter.debug("RetCodeHandler: retCodeConversionParam is:" + retCodeConversion);
        if (retCodeConversion == null) {
            logWriter.debug("RetCodeHandler: NULL retCodeConversionParam: setting defaults values");
            toBetrasformed = false;
            return;
        }
        if (retCodeConversion.equalsIgnoreCase("0to1")) {
            logWriter.debug("RetCodeHandler: 0to1 retCodeConversionParam: setting 0to1 values");
            transformMode = 1;
            inverseTransformMode = 0;
            toBetrasformed = true;
        }
        else if (retCodeConversion.equalsIgnoreCase("1to0")) {
            logWriter.debug("RetCodeHandler: 1to0 retCodeConversionParam: setting 1to0 values");
            transformMode = 0;
            inverseTransformMode = 1;
            toBetrasformed = true;
        }
    }

    /**
     * @param input
     * @return the transformed input
     */
    public GVBuffer transformInput(GVBuffer input)
    {
        if (toBetrasformed) {
            input = transform(input, transformMode);
        }
        return input;
    }

    /**
     * @param output
     * @return the transformed output
     */
    public GVBuffer transformOutput(GVBuffer output)
    {
        if (toBetrasformed) {
            output = transform(output, inverseTransformMode);
        }
        return output;
    }

    private GVBuffer transform(GVBuffer in, int mode)
    {
        switch (mode) {
            case 0 :
                if (in.getRetCode() == 1) {
                    in.setRetCode(0);
                    logWriter.debug("RetCodeHandler: retCode set from 1 to 0");
                }
                break;
            case 1 :
                if (in.getRetCode() == 0) {
                    in.setRetCode(1);
                    logWriter.debug("RetCodeHandler: retCode set from 0 to 1");
                }
        }
        return in;
    }

}
