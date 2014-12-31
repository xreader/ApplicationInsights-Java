package com.microsoft.applicationinsights.datacontracts;

import com.microsoft.applicationinsights.implementation.schemav2.PageViewData;
import com.microsoft.applicationinsights.util.LocalStringsUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Telemetry used to track PageViews.
 */
public class PageViewTelemetry extends BaseTelemetry<PageViewData> {
    private final PageViewData data;

    /**
     * Default constructor.
     */
    public PageViewTelemetry() {
        super();
        this.data = new PageViewData();
        initialize(this.data.getProperties());
    }

    /**
     * Construct PageView telemetry data item and sets the page name
     * @param name page name
     */
    public PageViewTelemetry(String name) {
        this();
        this.setName(name);
    }

    public void setName(String name) {
        this.data.setName(name);
    }

    public String getName() {
        return this.data.getName();
    }

    public void setUrl(String url) {
        this.data.setUrl(url);
    }

    public String getUrl() {
        return this.data.getUrl();
    }

    public void setDuration(int duration) {
        this.data.setDuration(Integer.toString(duration));
    }

    public int getDuration() {
        throw new NotImplementedException();
    }

    @Override
    protected void additionalSanitize() {
        this.data.setName(LocalStringsUtils.sanitize(this.data.getName(), LocalStringsUtils.MaxNameLength));
        this.data.setUrl(LocalStringsUtils.sanitize(this.data.getUrl(), LocalStringsUtils.MaxUrlLength));
    }

    @Override
    protected PageViewData getData() {
        return data;
    }
}
