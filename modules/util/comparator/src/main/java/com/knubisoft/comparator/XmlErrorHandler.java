package com.knubisoft.comparator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class XmlErrorHandler implements ErrorHandler {

    @Override
    public void warning(final SAXParseException e) {
    }

    @Override
    public void error(final SAXParseException e) {
    }

    @Override
    public void fatalError(final SAXParseException e) {
    }
}
