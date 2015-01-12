package com.microsoft.applicationinsights.web;

import javax.servlet.*;
import java.io.IOException;
import java.util.Date;

public class httpfilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        ServletRequest request = req;

        //Get the IP address of client machine.
        String ipAddress = request.getRemoteAddr();

        //Log the IP address and current timestamp.
        System.out.println("IP "+ipAddress + ", Time "
                + new Date().toString());

        chain.doFilter(req, res);
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
