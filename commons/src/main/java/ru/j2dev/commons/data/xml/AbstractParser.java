package ru.j2dev.commons.data.xml;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import ru.j2dev.commons.data.xml.helpers.ErrorHandlerImpl;
import ru.j2dev.commons.logging.LoggerObject;

import java.io.InputStream;

/**
 * Author: VISTALL
 * Date:  18:35/30.11.2010
 */
public abstract class AbstractParser<H extends AbstractHolder> extends LoggerObject {
    protected final H _holder;

    protected String _currentFile;
    protected SAXBuilder _reader;

    protected AbstractParser(final H holder) {
        _holder = holder;
        _reader = new SAXBuilder();
        _reader.setXMLReaderFactory(XMLReaders.NONVALIDATING);
        _reader.setErrorHandler(new ErrorHandlerImpl(this));
    }

    protected void parseDocument(final InputStream f, final String name) throws Exception {
        _currentFile = name;

        final Document document = _reader.build(f);

        readData(_holder, document.getRootElement());
    }

    protected void afterParsing() {
    }

    protected abstract void readData(H holder, Element rootElement) throws Exception;

    protected abstract void parse();

    public String getCurrentFileName() {
        return _currentFile;
    }

    public void load() {
        _holder.beforeParsing();
        parse();
        afterParsing();
        _holder.process();
        _holder.afterParsing();
        _holder.log();
    }

    public void reload() {
        info("reload start...");
        _holder.clear();
        load();
    }
}
