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
package it.greenvulcano.gvesb.adapter.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.output.TeeOutputStream;

/**
 *
 * @version 3.4.0 27/mar/2014
 * @author GreenVulcano Developer Team
 *
 */
public class MultiReadHttpServletResponse extends HttpServletResponseWrapper
{
    private HttpServletResponse   response;    
    private ByteArrayOutputStream body;
    private ServletOutputStream   sos = null;
    private int                   sc  = 200;
    private String                sm  = "OK";
    private Map<String, List<String>> headers = new HashMap<String, List<String>>();

    public MultiReadHttpServletResponse(HttpServletResponse response) throws IOException {
        super(response);
        this.response = response;
        body = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (sos == null) {
            sos = new ServletOutputStreamImpl(response.getOutputStream(), body);
        }
        return sos;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        String enc = getCharacterEncoding();
        if(enc == null) enc = "UTF-8";
        return new PrintWriter(new OutputStreamWriter(getOutputStream(), enc));
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        addHeaderLoc(name, String.valueOf(date));
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        addHeaderLoc(name, String.valueOf(value));
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        addHeaderLoc(name, value);
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        setHeaderLoc(name, String.valueOf(date));
    }
    
    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        setHeaderLoc(name, String.valueOf(value));
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        setHeaderLoc(name, value);
    }

    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    public Collection<String> getHeaders(String name) {
        return headers.get(name);
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.sc = sc;
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void setStatus(int sc, String sm) {
        super.setStatus(sc, sm);
        this.sc = sc;
        this.sm = sm;
    }
    
    @Override
    public void sendError(int sc) throws IOException {
        super.sendError(sc);
        this.sc = sc;
    }
    
    @Override
    public void sendError(int sc, String sm) throws IOException {
        super.sendError(sc, sm);
        this.sc = sc;
        this.sm = sm;
    }

    public int getStatus() {
        return this.sc;
    }

    public int getStatusCode() {
        return this.sc;
    }

    public String getStatusMsg() {
        return this.sm;
    }

    public String getBody() {
        return body.toString();
    }

    private class ServletOutputStreamImpl extends ServletOutputStream {

        private final TeeOutputStream targetStream;

        public ServletOutputStreamImpl(OutputStream one, OutputStream two ) {
            targetStream = new TeeOutputStream(one, two);
        }

        @Override
        public void write(int c) throws IOException {
            this.targetStream.write(c);
        }

        public void flush() throws IOException {
            super.flush();
            this.targetStream.flush();
        }

        public void close() throws IOException {
            super.close();
            this.targetStream.close();
        }

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			// TODO Auto-generated method stub
			
		}               
    }
    
    private void setHeaderLoc(String name, String value) {
        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }
        else {
            values.clear();
        }
        values.add(value);
    }
    
    private void addHeaderLoc(String name, String value) {
        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }
        values.add(value);
    }
}
