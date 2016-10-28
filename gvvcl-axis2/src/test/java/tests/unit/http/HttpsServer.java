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
package tests.unit.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * @version 3.0.0 Jul 27, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HttpsServer extends Thread
{
    /**
     *
     */
    public static int       SERVER_PORT            = 9888;

    /**
     *
     */
    public static int       SERVER_RESPONSE_STATUS = 200;

    /**
     *
     */
    public static String    SERVER_RESPONSE        = "<html><body>GV ESB Test!</body></html>";

    private SSLServerSocket sslServerSocket;

    /**
     *
     */
    public HttpsServer()
    {
    	
    	
        InputStream ksInputStream = getClass().getClassLoader().getResourceAsStream("keystores/server.jks");
                    
        char ksPass[] = "server".toCharArray();
        char ctPass[] = "test_server_pwd".toCharArray();
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(ksInputStream, ksPass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            sslServerSocket = (SSLServerSocket) ssf.createServerSocket(SERVER_PORT);
            System.out.println("Server started:");
            printServerSocketInfo(sslServerSocket);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see java.lang.Thread#run()
     */
    public void run()
    {
        while (true) {
            try {
                // Listening to the port
                SSLSocket c = (SSLSocket) sslServerSocket.accept();
                printSocketInfo(c);
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                String m = r.readLine();
                System.out.println("Received request: [" + m + "].");
                w.write("HTTP/1.0 " + SERVER_RESPONSE_STATUS + " OK");
                w.newLine();
                w.write("Content-Type: text/html");
                w.newLine();
                w.newLine();
                w.write(SERVER_RESPONSE);
                w.newLine();
                w.flush();
                w.close();
                r.close();
                c.close();
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private void printSocketInfo(SSLSocket s)
    {
        System.out.println("Socket class: " + s.getClass());
        System.out.println("   Remote address = " + s.getInetAddress().toString());
        System.out.println("   Remote port = " + s.getPort());
        System.out.println("   Local socket address = " + s.getLocalSocketAddress().toString());
        System.out.println("   Local address = " + s.getLocalAddress().toString());
        System.out.println("   Local port = " + s.getLocalPort());
        System.out.println("   Need client authentication = " + s.getNeedClientAuth());
        SSLSession ss = s.getSession();
        System.out.println("   Cipher suite = " + ss.getCipherSuite());
        System.out.println("   Protocol = " + ss.getProtocol());
    }

    private void printServerSocketInfo(SSLServerSocket s)
    {
        System.out.println("Server socket class: " + s.getClass());
        System.out.println("   Socket address = " + s.getInetAddress().toString());
        System.out.println("   Socket port = " + s.getLocalPort());
        System.out.println("   Need client authentication = " + s.getNeedClientAuth());
        System.out.println("   Want client authentication = " + s.getWantClientAuth());
        System.out.println("   Use client mode = " + s.getUseClientMode());
    }
}
