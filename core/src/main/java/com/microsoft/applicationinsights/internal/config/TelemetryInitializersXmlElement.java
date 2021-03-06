/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.internal.config;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by gupele on 3/15/2015.
 */
@XmlRootElement(name="TelemetryInitializers")
public class TelemetryInitializersXmlElement {

    private ArrayList<AddTypeXmlElement> adds;
    private ArrayList<AddTypeXmlElement> removes;

    public ArrayList<AddTypeXmlElement> getAdds() {
        return adds;
    }

    @XmlElement(name="Add")
    public void setAdds(ArrayList<AddTypeXmlElement> adds) {
        this.adds = adds;
    }

    public ArrayList<AddTypeXmlElement> getRemoves() {
        return removes;
    }

    @XmlElement(name="Remove")
    public void setRemoves(ArrayList<AddTypeXmlElement> removes) {
        this.removes = removes;
    }
}
