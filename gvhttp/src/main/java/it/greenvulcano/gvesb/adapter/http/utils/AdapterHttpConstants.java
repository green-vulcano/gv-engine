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
package it.greenvulcano.gvesb.adapter.http.utils;

/**
 * This class contains the constants used across the Adapter.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public final class AdapterHttpConstants
{
    public static final String SUBSYSTEM                         = "HttpInboundGateway";

    public static final String CFG_FILE                          = "GVHttpInboundGateway.xml";

    /**
     * Http MIME-Type value "text/html".
     */
    public final static String TEXTHTML_MIMETYPE_NAME            = "text/html";

    /**
     * Http MIME-Type value "text/xml".
     */
    public final static String TEXTXML_MIMETYPE_NAME             = "text/xml";

    /**
     * Http MIME-Type value "text/json".
     */
    public final static String TEXTJSON_MIMETYPE_NAME            = "text/json";

    /**
     * Http MIME-Type value "text/javascript".
     */
    public final static String TEXTJS_MIMETYPE_NAME              = "text/javascript";

    /**
     * Http MIME-Type value "application/octet-stream".
     */
    public final static String OCTETSTREAM_MIMETYPE_NAME         = "application/octet-stream";

    /**
     * Http MIME-Type value "application/x-www-form-urlencoded".
     */
    public final static String URLENCODED_MIMETYPE_NAME          = "application/x-www-form-urlencoded";

    /**
     * Http MIME-Type value "application/xml".
     */
    public final static String APPXML_MIMETYPE_NAME              = "application/xml";

    /**
     * Http MIME-Type value "application/json".
     */
    public final static String APPJSON_MIMETYPE_NAME             = "application/json";

    /**
     *
     */
    public static final int    RECOVERABLE_EXCEPTION_RETURN_CODE = 999;

    /**
     *
     */
    public static final String ENV_KEY_ACTION_ID                 = "Action_Id";

    /**
     *
     */
    public static final String ENV_KEY_AUTH_PARAMS               = "AuthenticationParams";

    /**
     *
     */
    public static final String ENV_KEY_AUTH_RETRIES              = "AuthenticationRetries";

    /**
     *
     */
    public static final String ENV_KEY_AUTHENTICATION_PLUGIN     = "AuthenticationPlugin";

    /**
     *
     */
    public static final String ENV_KEY_COMMUNICATION_TIMEOUT     = "CommunicationTimeout";

    /**
     *
     */
    public static final String ENV_KEY_CURRENT_RETRY             = "CurrentRetry";

    /**
     *
     */
    public static final String ENV_KEY_EXECUTION_RETRIES         = "ExecutionRetries";

    /**
     *
     */
    public static final String ENV_KEY_FORMATTER                 = "Formatter";

    /**
     *
     */
    public static final String ENV_KEY_HOST_CONFIGURATION        = "HostConfiguration";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_CLIENT               = "HttpClient";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_METHOD               = "HttpMethod";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_METHOD_TYPE          = "HttpMethodType";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_METHOD_URL           = "HttpMethodUrl";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_SERVLET_REQUEST      = "HttpServletRequest";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_SERVLET_RESPONSE     = "HttpServletResponse";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_STATE                = "HttpState";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_STATUS_CODE          = "HttpStatusCode";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_USER                 = "HttpUser";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_VERSION              = "HttpVersion";

    /**
     *
     */
    public static final String ENV_KEY_GVBUFFER_INPUT            = "GVBufferInput";

    /**
     *
     */
    public static final String ENV_KEY_GVBUFFER_OUTPUT           = "GVBufferOutput";

    /**
     *
     */
    public static final String ENV_KEY_MARSHALL_ENCODING         = "MarshallEncoding";

    /**
     *
     */
    public static final String ENV_KEY_OP_TYPE                   = "OperationType";

    /**
     *
     */
    public static final String ENV_KEY_REG_EXP_TO_ACTION_ID      = "RegExpToActionID";

    /**
     *
     */
    public static final String ENV_KEY_REGULAR_EXPRESSIONS       = "RegularExpressions";

    /**
     *
     */
    public static final String ENV_KEY_REQUEST_CONTENT_TYPE      = "RequestContentType";

    /**
     *
     */
    public static final String ENV_KEY_REQUEST_BODY              = "RequestBody";

    /**
     *
     */
    public static final String ENV_KEY_REQUEST_HEADER            = "RequestHeader";

    /**
     *
     */
    public static final String ENV_KEY_RESPONSE_STRING           = "ResponseString";

    /**
     *
     */
    public static final String ENV_KEY_RESPONSE_BODY             = "ResponseBody";

    /**
     *
     */
    public static final String ENV_KEY_RESPONSE_PARAMS           = "ResponseParams";

    /**
     *
     */
    public static final String ENV_KEY_RESPONSE_CONTENT_TYPE     = "ResponseContentType";

    /**
     *
     */
    public static final String ENV_KEY_RESPONSE_STATUS           = "ResponseStatus";

    /**
     *
     */
    public static final String ENV_KEY_RET_CODE_HANDLER          = "RetCodeHandler";

    /**
     *
     */
    public static final String ENV_KEY_SERVICE_NAME              = "ServiceName";

    /**
     *
     */
    public static final String ENV_KEY_SYSTEM                    = "System";

    /**
     *
     */
    public static final String ENV_KEY_TRANS_INFO                = "GVTransactionInfo";

    /**
     *
     */
    public static final String ENV_KEY_UNMARSHALL_ENCODING       = "UnmarshallEncoding";

    /**
     *
     */
    public static final String ENV_KEY_DUMP_ON_ERROR             = "DumpOnError";

    /**
     *
     */
    public static final String ENV_KEY_HTTP_HEADER               = "HTTPHeader";

    /**
     *
     */
    public static final String ACTION_ID_ADD_AUTH_INFO           = "AddAuthenticationInfo";

    /**
     *
     */
    public static final String ACTION_ID_DUMP                    = "Dump";

    /**
     *
     */
    public static final String ACTION_ID_EXECUTE                 = "Execute";

    /**
     *
     */
    public static final String ACTION_ID_FAIL                    = "Fail";

    /**
     *
     */
    public static final String ACTION_ID_LOGIN                   = "Login";

    /**
     *
     */
    public static final String ACTION_ID_LOGOUT                  = "Logout";

    /**
     *
     */
    public static final String ACTION_ID_LOGOUT_AND_FAIL         = "LogoutAndFail";

    /**
     *
     */
    public static final String ACTION_ID_MANAGE_REDIRECTIONS     = "ManageRedirections";

    /**
     *
     */
    public static final String ACTION_ID_MANAGE_RETURN_CODE      = "ManageReturnCode";

    /**
     *
     */
    public static final String ACTION_ID_MARSHAL                 = "Marshal";

    /**
     *
     */
    public static final String ACTION_ID_RETRY                   = "Retry";

    /**
     *
     */
    public static final String ACTION_ID_RETRY_AND_LOGOUT        = "RetryAndLogout";

    /**
     *
     */
    public static final String ACTION_ID_UNMARSHAL               = "UnMarshal";

    /**
     *
     */
    public static final String ACTION_ID_UPDATE_AUTH_INFO        = "UpdateAuthenticationInfo";

}
