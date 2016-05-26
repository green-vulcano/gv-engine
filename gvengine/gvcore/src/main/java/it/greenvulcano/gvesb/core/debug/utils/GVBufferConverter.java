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
package it.greenvulcano.gvesb.core.debug.utils;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVBuffer.Field;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;
import it.greenvulcano.gvesb.core.debug.model.Variable;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 3.3.0 Feb 17, 2013
 * @author GreenVulcano Developer Team
 */
public class GVBufferConverter
{
    public static final void toDebugger(Variable envVar, GVBuffer gvBuffer)
    {
        Variable v = new Variable(Field.SYSTEM, String.class, gvBuffer.getSystem());
        envVar.addVar(v);
        v = new Variable(Field.SERVICE, String.class, gvBuffer.getService());
        envVar.addVar(v);
        v = new Variable(Field.ID, Id.class, gvBuffer.getId());
        envVar.addVar(v);
        v = new Variable(Field.RETCODE, Integer.class, gvBuffer.getRetCode());
        envVar.addVar(v);

        v = null;
        Object obj = gvBuffer.getObject();
        if (obj == null) {
            v = new Variable(Field.OBJECT, null, null);
        }
        else {
            if (obj instanceof byte[]) {
                Dump dump = new Dump(((byte[]) gvBuffer.getObject()), -1);
                v = new Variable(Field.OBJECT, obj.getClass(), dump.toString());
            }
            else if (obj instanceof Node) {
                try {
                    StringBuilder val = new StringBuilder(XMLUtils.serializeDOM_S((Node) obj));
                    v = new Variable(Field.OBJECT, obj.getClass(), val);
                }
                catch (Exception exc) {
                    v = new Variable(Field.OBJECT, obj.getClass(), "[\nDUMP ERROR!!!!!\n].");
                }
            }
            else {
                try {
                    StringBuilder val = new StringBuilder("" + obj);
                    v = new Variable(Field.OBJECT, obj.getClass(), val);
                }
                catch (Exception exc) {
                    v = new Variable(Field.OBJECT, obj.getClass(), "[\nDUMP ERROR!!!!!\n].");
                }
            }
        }
        envVar.addVar(v);

        Iterator<String> iter = gvBuffer.getPropertyNamesIterator();
        if (iter.hasNext()) {
            v = new Variable(Field.PROPERTY, Map.class, null);
            while (iter.hasNext()) {
                String currFieldName = iter.next();
                String currFieldValue = gvBuffer.getProperty(currFieldName);
                Variable prop = new Variable(Field.PROPERTY, currFieldName, String.class, currFieldValue);
                v.addVar(prop);
            }
        }
        envVar.addVar(v);
    }

    public static final void toESB(Variable envVar, String varID, String varValue) throws GVException
    {
        Variable variable = varID.startsWith(Variable.PROPERTY_PFX) ? envVar.getVar(
                Variable.GVFIELD_PFX + Field.PROPERTY).getVar(varID) : envVar.getVar(varID);
        GVBuffer origGVBuffer = envVar.getGVBuffer();
        switch (variable.getGVBufferField()) {
            case SYSTEM :
                origGVBuffer.setSystem(varValue);
                break;
            case SERVICE :
                origGVBuffer.setService(varValue);
                break;
            case RETCODE :
                origGVBuffer.setRetCode(Integer.parseInt(varValue));
                break;
            case OBJECT :
                origGVBuffer.setObject(varValue);
                break;
            case PROPERTY :
                origGVBuffer.setProperty(variable.getName(), varValue);
                break;
			case ID:
				break;
			default:
				break;
        }
        variable.setVar(varID, varValue);
    }
}
