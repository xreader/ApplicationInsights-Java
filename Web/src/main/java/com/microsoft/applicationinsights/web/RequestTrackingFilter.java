package com.microsoft.applicationinsights.web;

import com.microsoft.applicationinsights.DefaultTelemetryClient;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.HttpRequestTelemetry;
import org.apache.commons.lang3.time.StopWatch;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class RequestTrackingFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        Date startTime = new Date();
        TelemetryClient client = new DefaultTelemetryClient();

        System.out.println("trackHttpRequest started");

        chain.doFilter(req, res);

        Date endTime = new Date();
        HttpServletRequest request = (HttpServletRequest) req;

        //Get the IP address of client machine.
        String ipAddress = request.getRemoteAddr();
        //Log the IP address and current timestamp.
        System.out.println("IP "+ipAddress + ", Time "
                + new Date().toString());

        HttpServletResponse response = (HttpServletResponse)res;

        HttpRequestTelemetry telemetry = new HttpRequestTelemetry();
        telemetry.setDuration(endTime.getTime() - startTime.getTime());

        String method = request.getMethod();
        String rURI = request.getRequestURI();
        String scheme = request.getScheme();
        String host = request.getHeader("Host");
        String query = request.getQueryString();

        telemetry.setHttpMethod(method);
        telemetry.setName(String.format("%s %s", method, rURI));
        if (query != null && query.length() > 0) {
            telemetry.setUrl(String.format("%s://%s%s?%s", scheme, host, rURI, query));
        }
        else {
            telemetry.setUrl(String.format("%s://%s%s", scheme, host, rURI));
        }

        telemetry.setResponseCode(Integer.toString(response.getStatus()));
        telemetry.setSuccess(200 == response.getStatus());
        client.track(telemetry);

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
