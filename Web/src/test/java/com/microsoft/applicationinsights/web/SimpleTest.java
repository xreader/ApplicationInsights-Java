package com.microsoft.applicationinsights.web;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.*;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Created by sergkanz on 1/12/2015.
 */
public class SimpleTest {

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
            server.join();

            //Do something
        }
        finally {
            server.stop();
            server.destroy();
        }
    }

}
