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
package it.greenvulcano.gvesb.statistics.datawrapper;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * DataMappingWrapper extracting the data from GVBuffer body by means regular expression.
 *
 * @version 3.0.0 13/giu/2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVBufferBodyWrapper implements DataMappingWrapper
{
    private static final Logger logger = LoggerFactory.getLogger(GVBufferBodyWrapper.class);

    /**
     * Default encoding.
     */
    private String                      encoding;

    /**
     * Regular expression pattern.
     */
    private Pattern                     pattern;

    /**
     * Regular expression group array.
     */
    private int[]                       groups = new int[]{0};


    /**
     *
     */
    public GVBufferBodyWrapper()
    {
        // do nothing
    }

    /**
     *
     * @param node
     *        the configuration node.
     *
     * @throws XMLConfigException
     *         if an error occurs.
     */
    @Override
    public void init(Node node) throws XMLConfigException
    {
        encoding = XMLConfig.get(node, "@encoding");
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }

        logger.debug("Encoding: " + encoding);

        String patternField = XMLConfig.get(node, "@regularExpression");
        pattern = Pattern.compile(patternField);
        logger.debug("Pattern: " + patternField);

        String groupsField = XMLConfig.get(node, "@groups");
        if (groupsField != null) {
            logger.debug("Groups: " + groupsField);
            StringTokenizer tokens = new StringTokenizer(groupsField, ",; .:", false);
            int numTokens = tokens.countTokens();
            groups = new int[numTokens];
            for (int i = 0; i < numTokens; ++i) {
                groups[i] = Integer.parseInt(tokens.nextToken());
            }
        }
    }

    /**
     * Resolves the parameter. If no match has been found returns null.
     */
    @Override
    public String resolveData(GVBuffer gvBuffer)
    {
        String value = null;
        try {
            Object obj =  gvBuffer.getObject();
            if (obj == null) {
                return null;
            }
            if (obj instanceof byte[]) {
                try {
                    obj = new String((byte[]) obj);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (obj instanceof String) {
                String buffer = (String) obj;

                Matcher matcher = pattern.matcher(buffer);
                if (matcher.find()) {
                    StringBuffer ret = new StringBuffer();
                    for (int i = 0; i < groups.length; ++i) {
                        ret.append(matcher.group(groups[i]));
                    }
                    value = ret.toString().trim();
                }
            }
        }
        catch (Exception exc) {
            logger.warn("Error extracting body data", exc);
        }
        logger.debug("GVBuffer Data resolved: " + value);
        return value;
    }

}
