package com.microsoft.applicationinsights.web;

import com.microsoft.applicationinsights.DefaultTelemetryClient;
import com.microsoft.applicationinsights.TelemetryClient;
import org.apache.commons.lang3.time.StopWatch;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class RequestTrackingFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

//        StopWatch sw = new StopWatch();
//        sw.start();

        TelemetryClient client = new DefaultTelemetryClient();

        ServletRequest request = req;

        //Get the IP address of client machine.
        String ipAddress = request.getRemoteAddr();

        //Log the IP address and current timestamp.
        System.out.println("IP "+ipAddress + ", Time "
                + new Date().toString());

        System.out.println("trackHttpRequest started");

        chain.doFilter(req, res);

        client.trackHttpRequest("Some request", new Date(), 4, String.valueOf(((HttpServletResponse)res).getStatus()), true);
        System.out.println("trackHttpRequest Completed");
    }
    public void init(FilterConfig config) throws ServletException {

        //Get init parameter
        String testParam = config.getInitParameter("test-param");

        //Print the init parameter
        System.out.println("Test Param: " + testParam);
    }
    public void destroy() {
        //add code to release any resource
    }
}
