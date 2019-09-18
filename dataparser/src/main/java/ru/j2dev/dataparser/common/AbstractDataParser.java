package ru.j2dev.dataparser.common;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.data.xml.AbstractParser;
import ru.j2dev.dataparser.Parser;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Camelion
 * @date : 22.08.12 1:37
 */
public abstract class AbstractDataParser<H extends AbstractHolder> extends AbstractParser<H> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataParser.class);
    private static final Pattern commentReplacePattern = Pattern.compile("(/\\*[^\\*]*[^/]*/|//[^\\n]*)", Pattern.DOTALL | Pattern.MULTILINE);

    protected AbstractDataParser(H holder) {
        super(holder);
    }

    @Override
    protected void parse() {
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader br = Files.newBufferedReader(Paths.get(getFileName()), Charset.forName("UTF-16"));
            String line;
            // Считываем файл до конца
            while ((line = br.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                if (line.contains("//")) {// обрезаем комментарии
                    int index = line.indexOf("//");
                    String replacement = line.substring(index);
                    line = line.replace(replacement, "").trim();
                }
                line = line.trim();
                if (!line.isEmpty())
                    buffer.append(line).append("\n");
            }
            Matcher matcher = commentReplacePattern.matcher(buffer);
            while (matcher.find()) {
                buffer = new StringBuilder(matcher.replaceAll("").trim());
            }
            StringBuilder lost = Parser.parseClass(buffer, _holder.getClass(), _holder);
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void readData(H holder, Element rootElement) {
    }

    protected abstract String getFileName();
}