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
package it.greenvulcano.gvesb.gvdte.controller;

/**
 * Single element of a Sequence Transformation.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class SequenceElement
{
    private String transformerName;
    private String inputName;
    private String bufferName;
    private String outputName;

    /**
     * @param transformerName
     * @param inputName
     * @param bufferName
     * @param outputName
     */
    public SequenceElement(String transformerName, String inputName, String bufferName, String outputName)
    {
        this.transformerName = transformerName;
        this.inputName = inputName;
        this.bufferName = bufferName;
        this.outputName = outputName;
    }

    /**
     * @return the transformer name
     */
    public String getTransformerName()
    {
        return transformerName;
    }

    /**
     * @return the input name
     */
    public String getInputName()
    {
        return inputName;
    }

    /**
     * @return the buffer name
     */
    public String getBufferName()
    {
        return bufferName;
    }

    /**
     * @return the output name
     */
    public String getOutputName()
    {
        return outputName;
    }

}
