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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import it.greenvulcano.gvesb.adapter.http.MultiReadHttpServletResponse;

/**
 *
 * @version 3.4.0 27/mar/2014
 * @author GreenVulcano Developer Team
 *
 */
public class DumpUtils
{
    public static void dump(HttpServletRequest request, StringBuffer log) throws IOException {
        String hN;

        log.append("-- DUMP HttpServletRequest START").append("\n");
        log.append("Method             : ").append(request.getMethod()).append("\n");
        log.append("RequestedSessionId : ").append(request.getRequestedSessionId()).append("\n");
        log.append("Scheme             : ").append(request.getScheme()).append("\n");
        log.append("IsSecure           : ").append(request.isSecure()).append("\n");
        log.append("Protocol           : ").append(request.getProtocol()).append("\n");
        log.append("ContextPath        : ").append(request.getContextPath()).append("\n");
        log.append("PathInfo           : ").append(request.getPathInfo()).append("\n");
        log.append("QueryString        : ").append(request.getQueryString()).append("\n");
        log.append("RequestURI         : ").append(request.getRequestURI()).append("\n");
        log.append("RequestURL         : ").append(request.getRequestURL()).append("\n");
        log.append("ContentType        : ").append(request.getContentType()).append("\n");
        log.append("ContentLength      : ").append(request.getContentLength()).append("\n");
        log.append("CharacterEncoding  : ").append(request.getCharacterEncoding()).append("\n");
        
        log.append("---- Headers START\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            hN = headerNames.nextElement();
            log.append("[" + hN + "]=");
            Enumeration<String> headers = request.getHeaders(hN);
            while (headers.hasMoreElements()) {
                log.append("[" + headers.nextElement() + "]");
            }
            log.append("\n");
        }
        log.append("---- Headers END\n");
        
        log.append("---- Body START\n");
        log.append(IOUtils.toString(request.getInputStream())).append("\n");
        log.append("---- Body END\n");
       
        log.append("-- DUMP HttpServletRequest END \n");
    }
    
    public static void dump(HttpServletResponse response, StringBuffer log) {
        String hN;

        MultiReadHttpServletResponse resp = (MultiReadHttpServletResponse) response;
        log.append("-- DUMP HttpServletResponse START").append("\n");
        log.append("Status            : ").append(resp.getStatus()).append("\n");
        log.append("ContentType       : ").append(resp.getContentType()).append("\n");
        log.append("CharacterEncoding : ").append(resp.getCharacterEncoding()).append("\n");
        log.append("---- Headers START\n");
        Iterator<String> headerNames = resp.getHeaderNames().iterator();
        while (headerNames.hasNext()) {
            hN = headerNames.next();
            log.append("[" + hN + "]=");
            Iterator<String> headers = resp.getHeaders(hN).iterator();
            while (headers.hasNext()) {
                log.append("[" + headers.next() + "]");
            }
            log.append("\n");
        }
        log.append("---- Headers END\n");

        log.append("---- Body START\n");
        log.append(resp.getBody()).append("\n");
        log.append("---- Body END\n");

        log.append("-- DUMP HttpServletResponse END \n");
    }
    
    public static void dump(HttpServletResponse response, ByteArrayOutputStream body, StringBuffer log) {
    

        log.append("-- DUMP HttpServletResponse START").append("\n");
        log.append("Status: ").append(response.getStatus()).append("\n");
        log.append("---- Headers START\n");
        
        response.getHeaderNames().stream()
        		.map(headerName -> "[" + headerName + "]=["+response.getHeaders(headerName)+"]")
        		.forEach(h -> log.append(h) );
        
        log.append("---- Headers END\n");
        
        log.append("---- Body START\n");
        log.append(body.toString()).append("\n");
        log.append("---- Body END\n");
       
        log.append("-- DUMP HttpServletResponse END \n");
    }
}
