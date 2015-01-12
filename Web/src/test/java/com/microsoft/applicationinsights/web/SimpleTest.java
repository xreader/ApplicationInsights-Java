package com.microsoft.applicationinsights.web;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.telemetry.HttpRequestTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import javax.servlet.DispatcherType;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sergkanz on 1/12/2015.
 */
public class SimpleTest {

    class MockTelemetryChannel implements TelemetryChannel {

        List<Telemetry> telemetryItems = new ArrayList<Telemetry>();

        public List<Telemetry> getTelemetryItems() {
            return telemetryItems;
        }

        @Override
        public boolean isDeveloperMode() {
            return true;
        }

        @Override
        public void setDeveloperMode(boolean value) {

        }

        @Override
        public void send(Telemetry item) {
            telemetryItems.add(item);
        }

        @Override
        public void stop(long timeout, TimeUnit timeUnit) {

        }
    }

    // HTTP GET request
    private void sendGetRequest(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.4; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");
        int responseCode = con.getResponseCode();


        try {
            InputStream inputStream = con.getInputStream();
            while (inputStream.read() > 0) {
            }
            inputStream.close();
        }
        catch (Exception exc) {
        }

        try {
            InputStream errorStream = con.getErrorStream();
            while (errorStream .read() > 0) {}
            errorStream .close();
        }
        catch (Exception exc) {
        }

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
            MockTelemetryChannel channel = new MockTelemetryChannel();
            TelemetryConfiguration.getActive().setChannel(channel);

            server.start();
            sendGetRequest("http://localhost:1234");

            //wait a little to allow server to complete request
            Thread.sleep(1000);

            List<Telemetry> items = channel.getTelemetryItems();
            assertEquals(1, items.size());
            HttpRequestTelemetry requestTelemetry = (HttpRequestTelemetry)items.get(0);

            assertEquals("404", requestTelemetry.getResponseCode());

        }
        finally {
            server.stop();
            server.destroy();
        }
    }

}
