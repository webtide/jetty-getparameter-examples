package org.eclipse.jetty.demo;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.StringUtil;
import org.json.JSONObject;

public class DemoServlet extends HttpServlet
{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/plain");

        PrintWriter out = resp.getWriter();

        // Interested in Query value
        String qval = req.getParameter("q");
        out.println("query[qval] = " + qval);

        // Dump all Request parameters found
        int i = 0;
        Map<String, String[]> paramMap = req.getParameterMap();
        for (String name : paramMap.keySet())
        {
            String[] values = paramMap.get(name);
            out.printf("parameter.map[%d].name = %s%n", i, name);
            out.printf("             [%d].value = %s%n", i, String.join(", ", values));
            i++;
        }

        // Grab request body content
        InputStream in = req.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, UTF_8);
        String bodyContent = IO.toString(reader);

        if (StringUtil.isNotBlank(bodyContent))
        {
            // If we have request body content, parse it as JSON
            JSONObject json = new JSONObject(bodyContent);
            out.println("request.body (parsed) = " + json);
        }
        else
        {
            out.println("request.body = <empty>");
        }
    }
}
