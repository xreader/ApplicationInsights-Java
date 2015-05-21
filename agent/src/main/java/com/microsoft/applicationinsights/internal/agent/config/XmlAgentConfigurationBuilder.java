package com.microsoft.applicationinsights.internal.agent.config;

import com.microsoft.applicationinsights.internal.agent.ClassInstrumentationData;
import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gupele on 5/19/2015.
 */
final class XmlAgentConfigurationBuilder implements AgentConfigurationBuilder {
    private final static String AGENT_XML_CONFIGURATION_NAME = "AI-Agent.xml";

    private final static String MAIN_TAG = "ApplicationInsightsAgent";

    private final static String DISABLED_ATTRIBUTE = "disabled";
    private final static String NAME_ATTRIBUTE = "name";
    private final static String SIGNATURE_ATTRIBUTE = "signature";

    @Override
    public AgentConfiguration parseConfigurationFile(String baseFolder) {
        XmlAgentConfiguration agentConfiguration = new XmlAgentConfiguration();

        String configurationFileName = baseFolder + AGENT_XML_CONFIGURATION_NAME;

        File configurationFile = new File(configurationFileName);
        if (!configurationFile.exists()) {
            return agentConfiguration;
        }

        try {
            Element topElementTag = getTopTag(configurationFile);
            if (topElementTag == null) {
                return agentConfiguration;
            }

            Element instrumentationTag = getInstrumentationTag(topElementTag);
            if (instrumentationTag == null) {
                return agentConfiguration;
            }

            setBuiltInInstrumentation(agentConfiguration, instrumentationTag);

            NodeList addClasses = getAllClassesToInstrument(instrumentationTag);
            if (addClasses == null) {
                return agentConfiguration;
            }

            HashMap<String, ClassInstrumentationData> classesToInstrument = new HashMap<String, ClassInstrumentationData>();
            for (int index = 0; index < addClasses.getLength(); ++index) {
                Element classElement = getClassDataElement(addClasses.item(index));
                if (classElement == null) {
                    continue;
                }

                ClassInstrumentationData data = getClassInstrumentationData(classElement, classesToInstrument);

                addMethods(data, classElement);
            }

            agentConfiguration.setRequestedClassesToInstrument(classesToInstrument);
            return agentConfiguration;
        } catch (Throwable e) {
            System.out.println("Exception while parsing Agent configuration file: '%s'" + e.getMessage());
            return null;
        }
    }

    private void setBuiltInInstrumentation(XmlAgentConfiguration agentConfiguration, Element instrumentationTags) {
        NodeList nodes = instrumentationTags.getElementsByTagName("BuiltIn");
        Element element = getFirst(nodes);
        if (element == null) {
            return;
        }

        try {
            boolean value = Boolean.valueOf(element.getAttribute(DISABLED_ATTRIBUTE));
            agentConfiguration.setBuiltInDisabled(value);
        } catch (Throwable t) {
        }
    }

    private Element getClassDataElement(Node item) {
        if (item.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        Element eClassNode = (Element)item;
        boolean isDisabled = Boolean.valueOf(eClassNode.getAttribute(DISABLED_ATTRIBUTE));
        if (isDisabled) {
            return null;
        }

        return eClassNode;
    }

    private ClassInstrumentationData getClassInstrumentationData(Element classElement, HashMap<String, ClassInstrumentationData> classesToInstrument) {
        String className = classElement.getAttribute(NAME_ATTRIBUTE);
        if (className == null || className.length() == 0) {
            return null;
        }

        className = className.replace(".", "/");
        ClassInstrumentationData data = classesToInstrument.get(className);

        InstrumentedClassType type = InstrumentedClassType.UNDEFINED;
        try {
            type = Enum.valueOf(InstrumentedClassType.class, classElement.getAttribute("type"));
        } catch (Throwable t) {
        }

        if (data == null) {
            data = new ClassInstrumentationData(type);
            classesToInstrument.put(className, data);
        }

        return data;
    }

    private Element getInstrumentationTag(Element topElementTag) {
        NodeList customTags = topElementTag.getElementsByTagName("Instrumentation");
        return getFirst(customTags);
    }

    private NodeList getAllClassesToInstrument(Element tag) {
        NodeList addClasses = tag.getElementsByTagName("Class");
        if (addClasses == null) {
            return null;
        }
        return addClasses;
    }

    private void addMethods(ClassInstrumentationData data, Element eClassNode) {
        NodeList methodNodes = eClassNode.getElementsByTagName("Method");
        if (methodNodes == null || methodNodes.getLength() == 0) {
            return;
        }

        for (int methodIndex = 0; methodIndex < methodNodes.getLength(); ++methodIndex) {
            Node methodNode = methodNodes.item(methodIndex);
            if (methodNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element methodElement = (Element)methodNode;
            boolean isDisabled = Boolean.valueOf(methodElement.getAttribute(DISABLED_ATTRIBUTE));
            if (isDisabled) {
                continue;
            }

            String methodName = methodElement.getAttribute(NAME_ATTRIBUTE);
            if (methodName == null || methodName.length() == 0) {
                continue;
            }

            data.addMethod(methodName, methodElement.getAttribute(SIGNATURE_ATTRIBUTE));
        }
    }

    public Element getTopTag(File configurationFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(configurationFile);
        doc.getDocumentElement().normalize();

        NodeList topTags = doc.getElementsByTagName(MAIN_TAG);
        if (topTags == null || topTags.getLength() == 0) {
            return null;
        }

        Node topNodeTag = topTags.item(0);
        if (topNodeTag.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        Element topElementTag = (Element)topNodeTag;
        return topElementTag;
    }

    private Element getFirst(NodeList nodes) {
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }

        Node node = nodes.item(0);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        return (Element)node;
    }
}
