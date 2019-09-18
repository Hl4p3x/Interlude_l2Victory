package ru.j2dev.gameserver.model.chat.chatfilter.matcher;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

import java.util.Arrays;
import java.util.regex.Pattern;

public class MatchWords implements ChatFilterMatcher {
    public final Pattern[] _patterns;

    public MatchWords(final String[] words) {
        _patterns = new Pattern[words.length];
        for (int i = 0; i < words.length; ++i) {
            _patterns[i] = Pattern.compile(words[i], Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }

    @Override
    public boolean isMatch(final Player player, final ChatType type, final String msg, final Player recipient) {
        return Arrays.stream(_patterns).anyMatch(p -> p.matcher(msg).find());
    }
}
