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
 * Created with IntelliJ IDEA. User: camelion Date: 13/01/13 Time: 12:03 PM To
 * change this template use File | Settings | File Templates.
 */
public class CategoryPchLinker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryPchLinker.class);

    private static final Pattern pattern = Pattern.compile("\\[(.*)]\\s*?=\\s*(\\d+)", Pattern.DOTALL);
    private static final String CATEGORY_PCH_FILE_NAME = "data/pts_scripts/category_pch.txt";
    private static final CategoryPchLinker ourInstance = new CategoryPchLinker();

    private CategoryPchLinker() {
    }

    public static CategoryPchLinker getInstance() {
        return ourInstance;
    }

    public void load() {
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(CATEGORY_PCH_FILE_NAME), Charset.forName("UTF-16"));
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
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String link = "@" + matcher.group(1);
                    LinkerFactory.addLink(link, matcher.group(2));
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }
}