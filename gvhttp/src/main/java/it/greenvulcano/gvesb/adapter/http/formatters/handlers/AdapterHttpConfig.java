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
package it.greenvulcano.gvesb.adapter.http.formatters.handlers;


/**
 * This class contains static methods to check AdapterHttp configuration and
 * static fields containing tag names and possible values for attributes within
 * AdapterHttp configuration XML file.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class AdapterHttpConfig
{
    /**
     * Application-wide token separator char (it will be used to concatenate
     * tokens into a single string).
     */
    public static final String TOKEN_SEPARATOR                                     = ":";

    /**
     * Prefix used to indicate that a keyword refers to an GVBuffer field
     * (standard or extended).
     */
    public static final String GVBUFFER_FIELD_KEYWORD_PREFIX                       = "GVBuffer.";

    /**
     * Prefix used to indicate that a keyword refers to an GVTransactionInfo
     * field (standard or extended).
     */
    public static final String GVTRANSINFO_FIELD_KEYWORD_PREFIX                    = "GVTransInfo.";

    /**
     * Default separator char between parameter entries within a query string
     */
    public static final String DEFAULT_PARAM_ENTRY_SEPARATOR                       = "&";

    /**
     * Default separator char between parameter's name and value within a query
     * string
     */
    public static final String DEFAULT_PARAM_NAMEVALUE_SEPARATOR                   = "=";

    /**
     * HTTP Request method <code>GET</code>
     */
    public static final String HTTP_REQUEST_METHOD_GET                             = "GET";

    /**
     * HTTP Request method <code>POST</code>
     */
    public static final String HTTP_REQUEST_METHOD_POST                            = "POST";

    /**
     * <tt>ItemType</tt> attribute value <i>'Handler'</i> within AdapterHTTP XML
     * configuration file
     */
    public static final String ITEM_TYPE_HANDLER_VALUE                             = "Handler";

    /**
     * <tt>ItemType</tt> attribute value <i>'ACKHandler'</i> within AdapterHTTP
     * XML configuration file
     */
    public static final String ITEM_TYPE_ACK_HANDLER_VALUE                         = "ACKHandler";

    /**
     * <tt>ItemType</tt> attribute value <i>'ErrorHandler'</i> within
     * AdapterHTTP XML configuration file
     */
    public static final String ITEM_TYPE_ERROR_HANDLER_VALUE                       = "ErrorHandler";


    /**
     * <tt>Handler output type</tt> value "GVBuffer" within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_OUTPUT_TYPE_VALUE_GVDATA                    = "GVBuffer";

    /**
     * <tt>Handler output type</tt> value "OpType" within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_OUTPUT_TYPE_VALUE_OPTYPE                    = "OpType";

    /**
     * <tt>Handler output type</tt> value "HttpParam" within AdapterHTTP XML
     * configuration file
     */
    public static final String HANDLER_OUTPUT_TYPE_VALUE_HTTPPARAM                 = "HttpParam";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.system</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_SYSTEM                       = "GVBuffer.system";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.service</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_SERVICE                      = "GVBuffer.service";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.id</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_ID                           = "GVBuffer.id";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.retCode</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_RETCODE                      = "GVBuffer.retCode";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.object</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_GVBUFFER                     = "GVBuffer.object";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.property</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_PROPERTY                     = "GVBuffer.property";

    /**
     * <tt>Variable</tt> attribute value <tt>GVBuffer.property.list</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_PROPERTYLIST                 = "GVBuffer.property.list";

    /**
     * <tt>Variable</tt> attribute value <tt>OpType</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_OPTYPE                       = "OpType";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.system</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOSYSTEM              = "GVTransInfo.system";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.service</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOSERVICE             = "GVTransInfo.service";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.id</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOID                  = "GVTransInfo.id";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.errorCode</tt> within
     * <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOERRORCODE           = "GVTransInfo.errorCode";

    /**
     * <tt>Variable</tt> attribute value <tt>GVTransInfo.errorMessage</tt>
     * within <tt>(any)ParamMapping</tt> tag of AdapterHTTP XML configuration
     * file
     */
    public static final String MAPPING_VARIABLE_VALUE_TRANSINFOERRORMESSAGE        = "GVTransInfo.errorMessage";

}
