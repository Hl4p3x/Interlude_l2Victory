package ru.j2dev.commons.data.xml.helpers;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import ru.j2dev.commons.data.xml.AbstractParser;

/**
 * Author: VISTALL
 * Date:  20:43/30.11.2010
 */
public class ErrorHandlerImpl implements ErrorHandler {
    private final AbstractParser<?> _parser;

    public ErrorHandlerImpl(final AbstractParser<?> parser) {
        _parser = parser;
    }

    @Override
    public void warning(final SAXParseException exception) {
        _parser.warn("File: " + _parser.getCurrentFileName() + ':' + exception.getLineNumber() + " warning: " + exception.getMessage());
    }

    @Override
    public void error(final SAXParseException exception) {
        _parser.error("File: " + _parser.getCurrentFileName() + ':' + exception.getLineNumber() + " error: " + exception.getMessage());
    }

    @Override
    public void fatalError(final SAXParseException exception) {
        _parser.error("File: " + _parser.getCurrentFileName() + ':' + exception.getLineNumber() + " fatal: " + exception.getMessage());
    }
}

