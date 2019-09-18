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
public class CastledataPchLinker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CastledataPchLinker.class);

    private static final Pattern pattern = Pattern.compile("(?<fort>fortress_begin\\s+\\[(?<f1>\\S+)\\]\\s+(?<f2>\\d+).*?fortress_end)" + "|(?<dominion>dominion_begin\\s+\\[(?<d1>\\S+)\\]\\s+(?<d2>\\d+).*?dominion_end)", Pattern.DOTALL);
    private static final String CASTLEDATA_FILE_NAME = "data/pts_scripts/castledata.txt";
    private static final CastledataPchLinker ourInstance = new CastledataPchLinker();

    private CastledataPchLinker() {
    }

    public static CastledataPchLinker getInstance() {
        return ourInstance;
    }

    public void load() {
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(CASTLEDATA_FILE_NAME), Charset.forName("UTF-16"));
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
            Matcher matcher = pattern.matcher(buffer);
            while (matcher.find()) {
                String link, value;
                if (matcher.group("fort") != null) {
                    link = matcher.group("f1");
                    value = matcher.group("f2");
                } else { // dominion
                    link = matcher.group("d1");
                    value = matcher.group("d2");
                }
                link = "@" + link;
                LinkerFactory.addLink(link, value);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }
}