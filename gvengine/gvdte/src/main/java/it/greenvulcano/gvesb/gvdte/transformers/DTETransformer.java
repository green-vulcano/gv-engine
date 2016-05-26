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
package it.greenvulcano.gvesb.gvdte.transformers;

import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * Transformer Interface.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface DTETransformer
{

    /**
     * Initialize the instance.
     *
     * @param node
     * @param dsf
     *
     * @throws DTETransfException
     *         if any initialization error occurs.
     */
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException;

    /**
     * Performs the data transformation.
     * The <code>input</code> parameter contains the data to be transformed.
     * The <code>buffer</code> will contain an intermediate result of the
     * transformation. If it's <code>null</code>, the result of the
     * transformation will be the return value. If <code>buffer</code> is not
     * <code>null</code> , the result of the transformation will be stored into
     * buffer. This interface method NOT imply anyway that a particular transformer
     * must be able to handle any possible kind of input. The configuration must
     * guarantee every transformer will receive only data types it can handle.
     *
     * @param input
     *        the data to transform.
     * @param buffer
     *        the intermediate result of the transformation (if in a sequence).
     * @param mapParam
     * @return the transformation result.
     * @throws DTETransfException if error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
                  InterruptedException;

    public String getName();

    /**
     * @param validate
     */
    public void setValidate(String validate);

    /**
     * @return if input object must be validated
     */
    public boolean validate();

    /**
     * @return the map name
     */
    public String getMapName();

    /**
     * Reset the transformer. Must be called afther every transformation.
     */
    public void clean();

    /**
     * Free the transformer resurces. Must be called prior to release the instance.
     */
    public void destroy();

    public List<TransformerHelper> getHelpers();
}
