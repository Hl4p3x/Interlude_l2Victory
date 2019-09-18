package ru.j2dev.dataparser.parser;

import ru.j2dev.commons.data.xml.AbstractParser;
import ru.j2dev.dataparser.holder.FStringHolder;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Camelion
 * @date : 27.08.12 13:29
 */
public class FStringParser extends AbstractParser<FStringHolder> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FStringParser.class);
    private static final String FILE_NAME = "data/pts_scripts/fstring.txt";
    private static final Pattern fStringPattern = Pattern.compile("(\\d+)\\s+\\[(.*)]");
    private static final FStringParser ourInstance = new FStringParser();

    private FStringParser() {
        super(FStringHolder.getInstance());
    }

    public static FStringParser getInstance() {
        return ourInstance;
    }

    @Override
    protected void parse() {
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(FILE_NAME), Charset.forName("UTF-16"));
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
                if (line.isEmpty())
                    continue;
                Matcher matcher = fStringPattern.matcher(line);
                if (matcher.find()) {
                    int id = Integer.valueOf(matcher.group(1));
                    String value = matcher.group(2);
                    _holder.addFString(id, value);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void readData(FStringHolder holder, Element rootElement) {
        // TODO Auto-generated method stub
    }
}