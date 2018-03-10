package ru.javaops.masterjava.xml.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StaxStreamProcessor implements AutoCloseable {
    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private final XMLStreamReader reader;

    public StaxStreamProcessor(InputStream is) throws XMLStreamException {
        reader = FACTORY.createXMLStreamReader(is);
    }

    public XMLStreamReader getReader() {
        return reader;
    }

    public boolean doUntil(int stopEvent, String value) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == stopEvent) {
                if (value.equals(getValue(event))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean doUntilFindTag(String tag, String ... tagToStop) throws XMLStreamException {
        Set<String> tags = new HashSet<>(Arrays.asList(tagToStop));
        while (reader.hasNext()) {
            int event = reader.next();
            if ( (event == XMLEvent.START_ELEMENT || event == XMLEvent.END_ELEMENT) && tags.contains(reader.getLocalName())) {
                return false;
            }
            if (event == XMLEvent.START_ELEMENT && reader.getLocalName().equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public String getAttributeValue(String attribute) {
        int attributeCount = reader.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            if (attribute.equals(reader.getAttributeLocalName(i))) {
                return reader.getAttributeValue(i);
            }
        }
        return null;
    }

    public String getValue(int event) throws XMLStreamException {
        return (event == XMLEvent.CHARACTERS) ? reader.getText() : reader.getLocalName();
    }

    public String getElementValue(String element) throws XMLStreamException {
        return doUntil(XMLEvent.START_ELEMENT, element) ? reader.getElementText() : null;
    }

    public String getText() throws XMLStreamException {
        return reader.getElementText();
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                // empty
            }
        }
    }
}
