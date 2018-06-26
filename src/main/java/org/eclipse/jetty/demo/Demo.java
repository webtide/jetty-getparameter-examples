package org.eclipse.jetty.demo;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.json.JSONArray;
import org.json.JSONObject;

public class Demo
{
    public static void main(String[] args) throws Exception
    {
        Server server = DemoServer.init();
        server.start();

        try
        {
            URI baseUri = server.getURI().resolve("/");
            performClientTests(baseUri);
        }
        finally
        {
            server.stop();
        }
    }

    private static void performClientTests(URI baseUri)
    {
        JSONObject json = new JSONObject();
        json.append("mode", "a=b");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("kiwi bird");
        jsonArray.put("kea");
        jsonArray.put("skink");
        json.append("animals", jsonArray);

        List<byte[]> bodyContents = new ArrayList<>();
        bodyContents.add(json.toString().getBytes(UTF_8)); // single line JSON
        bodyContents.add(json.toString(2).getBytes(UTF_8)); // multi line JSON

        List<String> contentTypes = new ArrayList<>();
        contentTypes.add("application/x-www-form-urlencoded; charset=UTF-8"); // as HTML FORM
        contentTypes.add("application/json; charset=UTF-8"); // as JSON content

        for (byte[] requestBodyBuf : bodyContents)
        {
            for (String contentType : contentTypes)
            {
                StringBuilder rawRequest;

                // Send complete body contents
                rawRequest = new StringBuilder();
                rawRequest.append("POST /demo?qval=first HTTP/1.1\r\n");
                rawRequest.append("Host: localhost\r\n");
                rawRequest.append("Connection: close\r\n");
                rawRequest.append("Content-Type: ").append(contentType).append("\r\n");
                rawRequest.append("Content-Length: ").append(Integer.toString(requestBodyBuf.length)).append("\r\n");
                rawRequest.append("\r\n");
                rawRequest.append(new String(requestBodyBuf, 0, requestBodyBuf.length, UTF_8));

                issueRequest(baseUri, rawRequest);
            }
        }
    }

    private static void issueRequest(URI baseUri, StringBuilder rawRequest)
    {
        try (Socket socket = new Socket(baseUri.getHost(), baseUri.getPort());
             OutputStream socketOut = socket.getOutputStream();
             InputStream socketIn = socket.getInputStream())
        {
            System.out.println("----------");
            System.out.println(rawRequest);
            // Write Request
            byte requestBuf[] = rawRequest.toString().getBytes(UTF_8);
            try (ByteArrayInputStream requestIn = new ByteArrayInputStream(requestBuf))
            {
                IO.copy(requestIn, socketOut);
                socketOut.flush();
            }

            HttpTester.Response response = HttpTester.parseResponse(socketIn);
            System.out.println();
            System.out.println("Response: " + response);
            System.out.println(response.getContent());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
