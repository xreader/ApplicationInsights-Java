package com.microsoft.applicationinsights.web;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.*;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumSet;

/**
 * Created by sergkanz on 1/12/2015.
 */
public class SimpleTest {

    // HTTP GET request
    private void sendGetRequest(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.4; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");
        int responseCode = con.getResponseCode();

        System.out.println("Sent GET request to: " + url);
        System.out.println("Response Code: " + responseCode);
    }


    @Test
    public void runEmbeddedJetty() throws Exception {
        Server server = new Server(1234);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(org.eclipse.jetty.servlet.DefaultServlet.class, "/");
        context.addFilter(httpfilter.class, "/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));

        server.setHandler(context);

        try {
            server.start();
            sendGetRequest("http://localhost:1234");

            //We need to validate here that http filter generated the right event
        }
        finally {
            server.stop();
            server.destroy();
        }
    }

}
