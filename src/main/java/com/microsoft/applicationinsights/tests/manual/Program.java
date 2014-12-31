package com.microsoft.applicationinsights.tests.manual;

import com.microsoft.applicationinsights.channel.TelemetryClient;
import com.microsoft.applicationinsights.datacontracts.*;
import com.microsoft.applicationinsights.extensibility.TelemetryConfiguration;
import com.microsoft.applicationinsights.util.DefaultTelemetryClient;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ALL")
class Program
{
    public static void main(String[] args) throws IOException
    {
        //This test demonstrate how to track PageViews. It can be used to instrument Eclipse plugin
        TelemetryConfiguration.getActive().setInstrumentationKey("02ee4ff3-dc13-48e6-9cbb-b1f0dbca1251");
        DefaultTelemetryClient client = new DefaultTelemetryClient();

        client.getContext().getUser().setAcquisitionDate(new Date());
        client.getContext().getUser().setId(UUID.randomUUID().toString());
        client.getContext().getSession().setId(UUID.randomUUID().toString());
        boolean isFirstSessionForThisUser = true;
        client.getContext().getSession().setIsFirst(isFirstSessionForThisUser);
        boolean isNewSessionForUser = true;
        client.getContext().getSession().setIsNewSession(isNewSessionForUser);

        client.trackPageView("Check in window");

        client.trackPageView("Check out window");

        client.trackEvent("custom event");

        try {
            Thread.sleep(10000);
        }
        catch(Exception e){

        }

        //Test other data items
        TelemetryClient appInsights = new DefaultTelemetryClient();
        appInsights.getContext().getProperties().put("programmatic", "works");

        Map<String, Double> metrics = new HashMap<String, Double>();
        metrics.put("Answers", (double)15);

        appInsights.trackEvent("A test event", null, metrics);

        appInsights.trackTrace("Things seem to be going well");

        MetricTelemetry mt = new MetricTelemetry("Test time", 17.0);
        mt.setMax(20.0);
        mt.setMin(10.0);
        mt.setCount(100);
        mt.setStandardDeviation(2.43);
        appInsights.trackMetric(mt);

        HttpRequestTelemetry rt = new HttpRequestTelemetry("ping", new Date(), 4711, "200", true);
        rt.setHttpMethod("GET");
        rt.setUrl("http://tempuri.org/ping");
        appInsights.track(rt);

        try
        {
            throwException("This is only a test!");
        }
        catch (Exception exc)
        {
            appInsights.trackException(exc);
        }

//        RemoteDependencyTelemetry rdt = new RemoteDependencyTelemetry("MongoDB");
//        rdt.setCount(1);
//        rdt.setValue(0.345);
//        rdt.setDependencyKind(DependencyKind.Other);
//        rdt.setAsync(false);
//
//        appInsights.track(rdt);

        System.out.println("Press Enter to terminate...");
        System.in.read();
    }

    private static void throwException(String msg) throws Exception
    {
        throw new Exception(msg);
    }
}
