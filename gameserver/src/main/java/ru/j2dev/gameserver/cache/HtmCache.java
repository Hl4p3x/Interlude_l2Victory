package ru.j2dev.gameserver.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.Language;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Кэширование html диалогов.
 *
 * @author G1ta0
 * @reworked VISTALL
 * @reworked JunkyFunky
 * В кеше список вот так
 * admin/admhelp.htm
 * admin/admin.htm
 * admin/admserver.htm
 * admin/banmenu.htm
 * admin/charmanage.htm
 */
public class HtmCache {
    public static final int DISABLED = 0; // кеширование отключено (только для тестирования)
    public static final int LAZY = 1; // диалоги кешируются по мере обращения
    public static final int ENABLED = 2; // все диалоги кешируются при загрузке сервера
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmCache.class);
    private final FileFilter _htmFilter = new HtmFilter();
    private static final HtmlCompressor HTML_COMPRESSOR = createHtmlCompressor();

    private final Cache<String, String>[] caffeine_cache = new Cache[Language.VALUES.length];

    private HtmCache() {
        Arrays.setAll(caffeine_cache, HtmCache::buildCache);
    }

    public static HtmCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static Cache<? extends String, ? extends String> buildCache(int index) {
        return Caffeine.newBuilder().maximumSize(100000).build();
    }

    private static HtmlCompressor createHtmlCompressor() {
        final HtmlCompressor htmlCompressor = new HtmlCompressor();

        htmlCompressor.setEnabled(true);                   //if false all compression is off (default is true)
        htmlCompressor.setRemoveComments(true);            //if false keeps HTML comments (default is true)
        htmlCompressor.setRemoveMultiSpaces(true);         //if false keeps multiple whitespace characters (default is true)
        htmlCompressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
        htmlCompressor.setRemoveQuotes(true);              //removes unnecessary tag attribute quotes
        htmlCompressor.setSimpleDoctype(false);             //simplify existing doctype
        htmlCompressor.setRemoveScriptAttributes(false);    //remove optional attributes from script tags
        htmlCompressor.setRemoveStyleAttributes(false);     //remove optional attributes from style tags
        htmlCompressor.setRemoveLinkAttributes(true);      //remove optional attributes from link tags
        htmlCompressor.setRemoveFormAttributes(true);      //remove optional attributes from form tags
        htmlCompressor.setRemoveInputAttributes(true);     //remove optional attributes from input tags
        htmlCompressor.setSimpleBooleanAttributes(true);   //remove values from boolean tag attributes
        htmlCompressor.setRemoveJavaScriptProtocol(false);  //remove "javascript:" from inline event handlers
        htmlCompressor.setRemoveHttpProtocol(false);        //replace "http://" with "//" inside tag attributes
        htmlCompressor.setRemoveHttpsProtocol(false);       //replace "https://" with "//" inside tag attributes
        htmlCompressor.setPreserveLineBreaks(false);        //preserves original line breaks
        htmlCompressor.setRemoveSurroundingSpaces("all"); //remove spaces around provided tags

        htmlCompressor.setCompressCss(false);               //compress inline css
        htmlCompressor.setCompressJavaScript(false);        //compress inline javascript

        htmlCompressor.setGenerateStatistics(true);

        return htmlCompressor;
    }

    public void reload() {
        clear();
        switch (Config.HTM_CACHE_MODE) {
            case ENABLED: {
                LOGGER.info("HtmCache: Caffeine cache mode.");
                for (Language lang : Language.VALUES) {
                    final File root = new File(Config.DATAPACK_ROOT, "data/html/" + lang.getShortName());
                    if (!root.exists()) {
                        LOGGER.info("HtmCache: Not find html dir for lang: " + lang);
                    } else {
                        load(lang, root, root.getAbsolutePath() + "/");
                    }
                }
                for (int i = 0; i < caffeine_cache.length; i++) {
                    final Cache<String, String> c = caffeine_cache[i];
                    LOGGER.info(String.format("HtmCache: parsing %d documents; lang: %s.", c.estimatedSize(), Language.VALUES[i]));
                }
                break;
            }
            case LAZY: {
                LOGGER.info("HtmCache: lazy cache mode.");
                break;
            }
            case DISABLED: {
                LOGGER.info("HtmCache: disabled.");
                break;
            }
        }
    }

    private void load(final Language lang, final File f, final String rootPath) {
        if (!f.exists()) {
            LOGGER.info("HtmCache: dir not exists: " + f);
            return;
        }

        final Collection<File> files = FileUtils.listFiles(f, FileFilterUtils.suffixFileFilter(".htm"), FileFilterUtils.directoryFileFilter());
        files.forEach(file -> {
            if (file.isDirectory() && !file.isHidden()) {
                load(lang, file, rootPath);
            } else {
                if (_htmFilter.accept(file)) {
                    try {
                        putContent(lang, file, rootPath);
                    } catch (final IOException e) {
                        LOGGER.error("HtmCache: file error: {}", e, e);
                    }
                }
            }
        });
    }

    private void putContent(final Language lang, final File f, final String rootPath) throws IOException {
        String content = FileUtils.readFileToString(f, "UTF-8");
        final String path = f.getAbsolutePath().substring(rootPath.length()).replace("\\", "/");
        content = HtmlUtils.bbParse(content);
        if(Config.HTM_CACHE_COMPRESS) {
            content = HtmlUtils.compress(lang, HTML_COMPRESSOR, content);
        }
        caffeine_cache[lang.ordinal()].put(path.toLowerCase(), content);
    }


    /**
     * Получить существующий html.
     *
     * @param fileName путь до html относительно data/html/LANG
     * @param player
     * @return null если диалога не существует
     */
    public String getNotNull(final String fileName, final Player player) {
        final Language lang = (player == null) ? Language.ENGLISH : player.getLanguage();
        String content = getCache(fileName, lang);
        if (StringUtils.isEmpty(content)) {
            content = "<html><body>My html is missing:<br>" + fileName + " for lang: "+lang+"</body></html>";
            LOGGER.debug("Following HTM "+fileName+" is missing for lang: " +lang+".");
        }
        return content;
    }


    /**
     * Получить существующий html.
     *
     * @param fileName путь до html относительно data/html/LANG
     * @param player
     * @return null если диалога не существует
     */
    public String getNullable(final String fileName, final Player player) {
        final Language lang = (player == null) ? Language.ENGLISH : player.getLanguage();
        final String cache = getCache(fileName, lang);
        if (StringUtils.isEmpty(cache)) {
            return null;
        }
        return cache;
    }

    private String getCache(final String file, final Language lang) {
        if (file == null) {
            return null;
        }
        final String fileLower = file.toLowerCase();
        String cache = get(lang, fileLower);
        if (cache == null) {
            switch (Config.HTM_CACHE_MODE) {
                case LAZY: {
                    cache = loadLazy(lang, file);
                    if (cache == null && lang != Language.ENGLISH) {
                        cache = loadLazy(Language.ENGLISH, file);
                        break;
                    }
                    break;
                }
                case DISABLED: {
                    cache = loadDisabled(lang, file);
                    if (cache == null && lang != Language.ENGLISH) {
                        cache = loadDisabled(Language.ENGLISH, file);
                        break;
                    }
                    break;
                }
            }
        }
        return cache;
    }

    private String loadDisabled(final Language lang, final String file) {
        String cache = null;
        final File f = new File(Config.DATAPACK_ROOT, "data/html/" + lang.getShortName() + "/" + file);
        if (f.exists()) {
            try {
                cache = FileUtils.readFileToString(f, "UTF-8");
                cache = HtmlUtils.bbParse(cache);
            } catch (IOException e) {
                LOGGER.info("HtmCache: File error: " + file + " lang: " + lang);
            }
        }
        return cache;
    }

    private String loadLazy(final Language lang, final String file) {
        String cache = null;
        final File f = new File(Config.DATAPACK_ROOT, "data/html/" + lang.getShortName() + "/" + file);
        if (f.exists()) {
            try {
                cache = FileUtils.readFileToString(f, "UTF-8");
                cache = HtmlUtils.bbParse(cache);
                caffeine_cache[lang.ordinal()].put(file, cache);
            } catch (IOException e) {
                LOGGER.info("HtmCache: File error: " + file + " lang: " + lang);
            }
        }
        return cache;
    }

    private String get(final Language lang, final String f) {
        String element = caffeine_cache[lang.ordinal()].getIfPresent(f);
        if (element == null) {
            element = caffeine_cache[Language.ENGLISH.ordinal()].getIfPresent(f);
        }
        return element;
    }

    public void clear() {
        Stream.of(caffeine_cache).forEach(Cache::invalidateAll);
    }

    private static class LazyHolder {
        private static final HtmCache INSTANCE = new HtmCache();
    }

    protected class HtmFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isFile() && (file.getName().endsWith(".htm") || file.getName().endsWith(".html")) && !file.isHidden();
        }
    }
}
