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
        //This test demonstrate how to use AI SDK to instrument Eclipse plugin
        //Set your key here. It may differ for your development and production versions:
        TelemetryConfiguration.getActive().setInstrumentationKey("02ee4ff3-dc13-48e6-9cbb-b1f0dbca1251");

        DefaultTelemetryClient client = new DefaultTelemetryClient();

        //Anonymous user tracking. Will be used to calculate number of unique installations
        client.getContext().getUser().setAcquisitionDate(new Date());
        client.getContext().getUser().setId(UUID.randomUUID().toString());

        //Session parameters. Will show number of sessions - how often plugin being used.
        //Make sure that IsFirst and IsNewSession are properly populated
        //Do not forget to reset session every 30 minutes or so...
        client.getContext().getSession().setId(UUID.randomUUID().toString());
        boolean isFirstSessionForThisUser = true;
        client.getContext().getSession().setIsFirst(isFirstSessionForThisUser);
        boolean isNewSessionForUser = true;
        client.getContext().getSession().setIsNewSession(isNewSessionForUser);

        //Some environment settings. We will probably have default implementation for those
        client.getContext().getDevice().setOperatingSystem("Wiondows 10");
        client.getContext().getDevice().setId("computer name hash");

        //Version of Eclipse should go here. User context is always about end user
        client.getContext().getUser().setUserAgent("Eclipse Luna (4.4.1)");

        //It is a version of your applicaiton - eclipse plugin ion your case
        client.getContext().getComponent().setVersion("11.0.0.2012.0");

        //You can report screen resolution if it's important for you
        client.getContext().getDevice().setScreenResolution("1280X435");
        client.getContext().getProperties().put("NumebrOfMonitors", Integer.toString(2));

        //Now you can report page view when you show window
        PageViewTelemetry pv = new PageViewTelemetry("check in window");
        //if you have some additional instance information - for instance top and left coordinates of window
        // or project name (if legal will allow)
        pv.setUrl("eclipse://check-in-window/secondMonitor/120/234");
        client.track(pv);

        //You can also report events like getting the latest version or automatic checkout
        EventTelemetry et = new EventTelemetry("File was automatically checked-out");
        et.getProperties().put("reason", "file editing");
        client.track(et);

        //If exception happened
        client.trackException(new Exception("something very bad happened"));


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
