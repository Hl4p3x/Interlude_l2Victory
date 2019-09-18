package ru.j2dev.dataparser.pch.linker;

import ru.j2dev.dataparser.pch.LinkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: camelion Date: 12/01/2013 Time: 11:27 PM To
 * change this template use File | Settings | File Templates.
 */
public class InstantzoneDataPchLinker {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantzoneDataPchLinker.class);

    private static final Pattern pattern = Pattern.compile("instantzone_begin\\s+id=(\\d+).*?name=(\\S+).*?instantzone_end", Pattern.DOTALL);
    private static final Pattern commentReplacePattern = Pattern.compile("(/\\*[^\\*]*[^/]*/|//[^\\n]*)", Pattern.DOTALL | Pattern.MULTILINE);
    private static final String INSTANTZONEDATA_FILE_NAME = "data/pts_scripts/instantzonedata.txt";
    private static final InstantzoneDataPchLinker ourInstance = new InstantzoneDataPchLinker();

    private InstantzoneDataPchLinker() {
    }

    public static InstantzoneDataPchLinker getInstance() {
        return ourInstance;
    }

    public void load() {
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(INSTANTZONEDATA_FILE_NAME), Charset.forName("UTF-16"));
            StringBuilder buffer = new StringBuilder();
            String line;
            // Считываем файл до конца
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("//") || line.isEmpty())
                    continue;
                if (line.contains("//")) {// обрезаем комментарии
                    int index = line.indexOf("//");
                    String replacement = line.substring(index);
                    line = line.replace(replacement, "").trim();
                }
                if (line.isEmpty())
                    continue;
                buffer.append(line).append("\n");
            }
            // replace comments
            Matcher matcher = commentReplacePattern.matcher(buffer);
            while (matcher.find()) {
                buffer = new StringBuilder(matcher.replaceAll("").trim());
            }
            matcher = pattern.matcher(buffer);
            while (matcher.find()) {
                String link = matcher.group(2);
                String value = matcher.group(1);
                link = "@" + link;
                LinkerFactory.addLink(link, value);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }
}