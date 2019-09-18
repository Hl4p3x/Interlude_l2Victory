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
 * Created with IntelliJ IDEA. User: camelion Date: 12/19/12 Time: 6:04 PM To
 * change this template use File | Settings | File Templates.
 */
public class SkillPchLinker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkillPchLinker.class);

    private static final Pattern pattern = Pattern.compile("\\[(.*)]\\s*?=\\s*(\\d+)", Pattern.DOTALL);
    private static final String SKILL_PCH_FILE_NAME = "data/pts_scripts/skill_pch.txt";
    private static final SkillPchLinker ourInstance = new SkillPchLinker();

    private SkillPchLinker() {
    }

    public static SkillPchLinker getInstance() {
        return ourInstance;
    }

    public void load() {
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(SKILL_PCH_FILE_NAME), Charset.forName("UTF-16"));
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
                    final int[] array = new int[2];
                    final int skill_id = Integer.parseInt(matcher.group(2)) / 65536;
                    final int skill_level = Integer.parseInt(matcher.group(2)) - 65536 * skill_id;
                    array[0] = skill_id;
                    array[1] = skill_level;
                    LinkerFactory.addLink(link, matcher.group(2));
                    LinkerFactory.addLinkSkillPch(link, array);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }
}