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
package it.greenvulcano.gvesb.buffer;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * <code>GVBuffer</code> is used in order to transport data in GreenVulcano.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVBuffer implements Serializable, Cloneable
{
    /**
     * @version 3.0.0 Feb 27, 2010
     * @author GreenVulcano Developer Team
     *
     */
    public enum Field {
        /**
         *
         */
        SYSTEM,
        /**
         *
         */
        SERVICE,
        /**
         *
         */
        ID,
        /**
         *
         */
        RETCODE,
        /**
         *
         */
        OBJECT,
        /**
         *
         */
        PROPERTY
    }

    /**
     *
     */
    public static final String  GVBUFFER         = "GVBuffer";

    private static final long   serialVersionUID = 1356337373910L;
    private String              system;
    private String              service;
    private Id                  id;
    private int                 retCode;
    private Object              object           = null;

    /**
     *
     */
    public static final String  DEFAULT_SRVC     = "DEFAULT_SRVC";

    /**
     *
     */
    public static final String  DEFAULT_SYS      = "DEFAULT_SYS";

    /**
     *
     */
    public static final int     RET_OK           = 1;

    private Map<String, String> properties       = null;

    /**
     * Check parameter in order to avoid null value.
     *
     * @param param
     * @param value
     *
     * @exception GVException
     *            if the value is not correct
     */
    protected void checkNull(Field param, Object value) throws GVException
    {
        if (value == null) {
            throw new GVException("GV_NULL_PARAMETER_ERROR", new String[][]{{"param", param.toString()}});
        }
    }

    /**
     * Check parameter in order to avoid null value.
     *
     * @param param
     * @param value
     *
     * @exception GVException
     *            if the value is not correct
     */
    protected void checkNull(String param, Object value) throws GVException
    {
        if (value == null) {
            throw new GVException("GV_NULL_PARAMETER_ERROR", new String[][]{{"param", param}});
        }
    }

    /**
     * Check if parameter implements <code>java.io.Serializable</code>.
     *
     * @param param
     * @param value
     *
     * @exception GVException
     *            if the value is not correct
     */
    protected void checkSerializable(Field param, Object value) throws GVException
    {
        if (!(value instanceof Serializable)) {
            throw new GVException("GV_NOT_SERIALIZABLE_PARAMETER_ERROR", new String[][]{{"param", param.toString()}});
        }
    }

    /**
     * Constructs a GVBuffer with all fields.
     *
     * @param system
     * @param service
     * @param id
     * @param retCode
     * @param object
     * @throws GVException
     */
    public GVBuffer(String system, String service, Id id, int retCode, Object object) throws GVException
    {
        checkNull(Field.SYSTEM, system);
        checkNull(Field.SERVICE, service);
        checkNull(Field.ID, id);

        this.system = system;
        this.service = service;
        this.id = id;
        this.retCode = retCode;
        this.object = object;
    }

    /**
     * Construct a GVBuffer with system, service, id.
     *
     * @param system
     * @param service
     * @param id
     * @throws GVException
     */
    public GVBuffer(String system, String service, Id id) throws GVException
    {
        this(system, service, id, RET_OK, null);
    }

    /**
     * Construct a GVBuffer with system and service.
     *
     * @param system
     * @param service
     * @throws GVException
     */
    public GVBuffer(String system, String service) throws GVException
    {
        this(system, service, new Id(), RET_OK, null);
    }

    /**
     * Default constructor.
     *
     * @throws GVException
     */
    public GVBuffer() throws GVException
    {
        this(DEFAULT_SYS, DEFAULT_SRVC, new Id(), RET_OK, null);
    }

    /**
     * Copy constructor.
     *
     * @param toCopy
     */
    public GVBuffer(GVBuffer toCopy)
    {
        this(toCopy, true);
    }
    
    /**
     * Copy constructor.
     *
     * @param toCopy
     *      GVBuffer instance to copy
     * @param copyBody
     *      if true is copied also the object filed content
     */
    public GVBuffer(GVBuffer toCopy, boolean copyBody)
    {
        if (toCopy != null) {
            system = toCopy.system;
            service = toCopy.service;
            id = toCopy.id;
            retCode = toCopy.retCode;

            if (copyBody && (toCopy.object != null)) {
                if (toCopy.object instanceof byte[]) {
                    object = new byte[((byte[]) toCopy.object).length];
                    System.arraycopy(toCopy.object, 0, object, 0, ((byte[]) toCopy.object).length);
                }
                else if (toCopy.object instanceof String) {
                    object = toCopy.object;
                }
                else {
                    object = toCopy.object;
                }
            }

            if (toCopy.properties != null) {
                getProperties().putAll(toCopy.properties);
            }
        }
    }

    /**
     * @return the system
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * @return the service
     */
    public String getService()
    {
        return service;
    }

    /**
     * @return the id
     */
    public Id getId()
    {
        return id;
    }

    /**
     * @return the return code
     */
    public int getRetCode()
    {
        return retCode;
    }

    /**
     * @return the internal object
     */
    public Object getObject()
    {
        return object;
    }

    /**
     * @param system
     * @throws GVException
     */
    public void setSystem(String system) throws GVException
    {
        checkNull(Field.SYSTEM, system);
        this.system = system;
    }

    /**
     * @param service
     * @throws GVException
     */
    public void setService(String service) throws GVException
    {
        checkNull(Field.SERVICE, service);
        this.service = service;
    }

    /**
     * @param id
     * @throws GVException
     */
    public void setId(Id id) throws GVException
    {
        checkNull(Field.ID, id);
        this.id = id;
    }

    /**
     * @param retCode
     */
    public void setRetCode(int retCode)
    {
        this.retCode = retCode;
    }

    /**
     * @param object
     * @throws GVException
     */
    public void setObject(Object object) throws GVException
    {
        this.object = object;
    }

    /**
     * @return properties.
     */
    private Map<String, String> getProperties()
    {
        if (properties == null) {
            properties = new TreeMap<String, String>();
        }
        return properties;
    }

    /**
     * @param name
     * @return the property value
     */
    public String getProperty(String name)
    {
        return getProperties().get(name);
    }

    /**
     * @return the property names iterator
     */
    public Iterator<String> getPropertyNamesIterator()
    {
        return getProperties().keySet().iterator();
    }

    /**
     * @return the property names set
     */
    public Set<String> getPropertyNamesSet()
    {
        return getProperties().keySet();
    }

    /**
     * @return the property names as array of string
     */
    public String[] getPropertyNames()
    {
        Set<String> namesSet = getProperties().keySet();
        String[] names = new String[namesSet.size()];
        namesSet.toArray(names);
        return names;
    }

    /**
     * @param property
     * @param value
     * @throws GVException
     */
    public void setProperty(String property, String value) throws GVException
    {
        checkNull(Field.PROPERTY + " '" + property + "'", property);
        checkNull(property, value);

        getProperties().put(property, value);
    }

    /**
     * @param property
     *
     */
    public void removeProperty(String property)
    {
        getProperties().remove(property);
    }

    /**
     * @param props
     */
    public void removeProperties(Map<String, String> props)
    {
        if (properties != null) {
            for (Entry<String, String> entry : props.entrySet()) {
                properties.remove(entry.getKey());
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder("[GVBuffer]\n");
        buf.append("\tsystem  = ").append(system).append("\n");
        buf.append("\tservice = ").append(service).append("\n");
        buf.append("\tid      = ").append(id).append("\n");
        buf.append("\tretCode = ").append(retCode).append("\n");
        buf.append("\tobject  = ").append(object).append("\n");

        if (properties != null) {
            Iterator<String> iter = getPropertyNamesIterator();
            while (iter.hasNext()) {
                String currFieldName = iter.next();
                String currFieldValue = getProperty(currFieldName);
                buf.append("\t").append(currFieldName).append(" = ").append(currFieldValue).append("\n");
            }
        }
        buf.append("[GVBuffer]\n");

        return buf.toString();
    }
}
