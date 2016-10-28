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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * @version 3.0.0 Jul 28, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HttpServer extends Thread
{
    private static final String BASE_PATH             = System.getProperty("user.dir") + File.separator + "target"
                                                        + File.separator + "test-classes";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length: ";
    private static final long   DEF_TIMEOUT           = 6000;

    private int                 listenerPort;


    enum HttpMethodName {
        OPTIONS, GET, HEAD, POST, PUT, DELETE
    }

    /**
     * @param port
     */
    public HttpServer(int port)
    {
        listenerPort = port;
        start();
    }

    /**
     * @see java.lang.Thread#run()
     */
    public void run()  {
        try(ServerSocket serversocket = new ServerSocket(listenerPort)) {    

            while (true) {
                Socket connectionsocket = serversocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
                DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());

                handle(input, output);
            }
        }  catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void handle(BufferedReader input, DataOutputStream output)
    {
        String path = new String();
        HttpMethodName method = null;
        String buffer = null;
        try {
            String tmp = input.readLine();
            System.out.println("HTTP SERVER REQUEST: " + tmp);
            String tmp2 = new String(tmp);
            tmp.toUpperCase();
            method = HttpMethodName.valueOf(tmp.substring(0, tmp.indexOf(' ')));

            int start = tmp2.indexOf('/');
            int end = tmp2.indexOf(' ', start);
            path = tmp2.substring(start, end);

            String line = null;
            int contentLength = 0;
            boolean startBody = false;
            while (!startBody) {
                line = input.readLine();
                if (line.equals("")) {
                    startBody = true;
                }
                else if (line.startsWith(CONTENT_LENGTH_HEADER)) {
                    contentLength = Integer.parseInt(line.substring(CONTENT_LENGTH_HEADER.length()));
                }
            }
            if (contentLength > 0) {
                char[] cbuf = new char[contentLength];
                input.read(cbuf);
                buffer = new String(cbuf);
                System.out.println("RECEIVED BUFFER: " + buffer);
            }
        }
        catch (Exception e) {
            try {
                output.writeBytes(getResponseStatus(400, -1));
                output.close();
            }
            catch (Exception e2) {
            }
            e.printStackTrace();
            return;
        }

        try {
            if (path.endsWith("timeout.html")) {
                try {
                    Thread.sleep(DEF_TIMEOUT);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(BASE_PATH + path));
            int fileType = 99;
            if (method == HttpMethodName.POST) {
                if (!buffer.startsWith("--")) {
                    fileType = 4;
                }
                else {
                    fileType = 3;
                }
            }
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                fileType = 0;
            }
            else if (path.endsWith(".gif")) {
                fileType = 1;
            }
            else if (path.endsWith(".zip") || path.endsWith(".tar")) {
                fileType = 2;
            }
            output.writeBytes(getResponseStatus(200, fileType));

            switch (method) {
                case GET :
                    output.writeBytes("\r\n");
                    byte[] bArr = new byte[128];
                    int read = -1;
                    while ((read = is.read(bArr)) != -1) {
                        output.write(bArr, 0, read);
                    }
                    break;
                case POST :
                    if (fileType == 4) {
                        String resp = "<html><body>RESPONSE TO BUFFER: " + buffer + "</body></html>";
                        output.writeBytes(CONTENT_LENGTH_HEADER + resp.length());
                        output.writeBytes("\r\n\r\n");
                        output.writeBytes(resp);
                    }
                    else {
                        Message message = createMimeMessage();
                        message.writeTo(output);
                    }
                    break;
                default :
                    break;
            }
            output.close();
            is.close();
        }
        catch (Exception e) {
            try {
                output.writeBytes(getResponseStatus(404, -1));
                output.close();
            }
            catch (Exception e2) {
            }
            e.printStackTrace();
        }
    }

    private Message createMimeMessage() throws Exception
    {
        MimeBodyPart mainBody = new MimeBodyPart();
        mainBody.setText("This is the a multipart message in MIME format");

        MimeBodyPart attachment = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource("GVESB HTTP RESPONSE MULTIPART TEST MESSAGE".getBytes(),
                "text/plain");
        attachment.setDataHandler(new DataHandler(source));
        attachment.setFileName("response.txt");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mainBody);
        multipart.addBodyPart(attachment);

        MimeMessage message = new MimeMessage((Session) null);
        message.setContent(multipart);
        return message;
    }

    private String getResponseStatus(int status, int fileType)
    {
        StringBuilder sb = new StringBuilder("HTTP/1.0 ").append(status);
        switch (status) {
            case 200 :
                sb.append(" OK");
                break;
            case 400 :
                sb.append(" Bad Request");
                break;
            case 403 :
                sb.append(" Forbidden");
                break;
            case 404 :
                sb.append(" Not Found");
                break;
            case 500 :
                sb.append(" Internal Server Error");
                break;
            case 501 :
                sb.append(" Not Implemented");
                break;
        }

        sb.append("\r\nConnection: close\r\nServer: SimpleGVTestServer\r\n");

        switch (fileType) {
            case -1 :
                break;
            case 0 :
                sb.append("Content-Type: image/jpeg\r\n");
                break;
            case 1 :
                sb.append("Content-Type: image/gif\r\n");
                break;
            case 2 :
                sb.append("Content-Type: application/x-zip-compressed\r\n");
                break;
            case 3 :
                // sb.append("Content-Type: multipart/mixed; boundary=gvtest\r\n");
                break;
            default :
                sb.append("Content-Type: text/html\r\n");
                break;
        }

        return sb.toString();
    }
}
