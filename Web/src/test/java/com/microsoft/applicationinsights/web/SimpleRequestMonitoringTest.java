package com.microsoft.applicationinsights.web;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.telemetry.HttpRequestTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import com.microsoft.applicationinsights.web.utils.Callback200Servlet;
import com.microsoft.applicationinsights.web.utils.MockTelemetryChannel;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static com.microsoft.applicationinsights.web.utils.HttpHelper.sendGetRequestAndWait;
import static org.junit.Assert.assertTrue;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by sergkanz on 1/12/2015.
 */
public class SimpleRequestMonitoringTest {
    @Test
    public void testDefaultPageReturns200() throws Exception {
        //Set channel
        MockTelemetryChannel channel = new MockTelemetryChannel();
        TelemetryConfiguration.getActive().setChannel(channel);

        Server server = new Server(1234);
        try {
            //Initialize the server
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            context.addServlet(Callback200Servlet.class, "/");
            context.addFilter(RequestTrackingFilter.class, "/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
            server.setHandler(context);
            server.start();

            sendGetRequestAndWait("http://localhost:1234");

            List<Telemetry> items = channel.getTelemetryItems();
            assertEquals(1, items.size());
            HttpRequestTelemetry requestTelemetry = (HttpRequestTelemetry)items.get(0);

            assertEquals("200", requestTelemetry.getResponseCode());
            assertEquals("GET /", requestTelemetry.getName());
            assertEquals("GET", requestTelemetry.getHttpMethod());
            assertEquals("http://localhost:1234/", requestTelemetry.getUrl().toString());
        }
        finally {
            server.stop();
            server.destroy();
        }
    }

}
