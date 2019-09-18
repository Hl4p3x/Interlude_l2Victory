package ru.j2dev.gameserver.data.xml.parser;

import gnu.trove.list.array.TIntArrayList;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.chat.ChatFilters;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilter;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.model.chat.chatfilter.matcher.*;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ChatFilterParser extends AbstractFileParser<ChatFilters> {

    protected ChatFilterParser() {
        super(ChatFilters.getInstance());
    }

    public static ChatFilterParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    protected List<ChatFilterMatcher> parseMatchers(final Element n) {
        final List<ChatFilterMatcher> matchers = new ArrayList<>();
        for (Element e : n.getChildren()) {
            if ("Channels".equals(e.getName())) {
                final List<ChatType> channels = new ArrayList<>();
                final StringTokenizer st = new StringTokenizer(e.getText(), ",");
                while (st.hasMoreTokens()) {
                    channels.add(ChatType.valueOf(st.nextToken()));
                }
                matchers.add(new MatchChatChannels(channels.toArray(new ChatType[0])));
            } else if ("Maps".equals(e.getName())) {
                final TIntArrayList maps = new TIntArrayList();
                final StringTokenizer st = new StringTokenizer(e.getText(), ",");
                while (st.hasMoreTokens()) {
                    final String[] map = st.nextToken().split("_");
                    maps.add(Integer.parseInt(map[0]));
                    maps.add(Integer.parseInt(map[1]));
                }
                matchers.add(new MatchMaps(maps.toArray()));
            } else if ("Words".equals(e.getName())) {
                final List<String> words = new ArrayList<>();
                final StringTokenizer st = new StringTokenizer(e.getText());
                while (st.hasMoreTokens()) {
                    words.add(st.nextToken());
                }
                matchers.add(new MatchWords(words.toArray(new String[0])));
            } else if ("ExcludePremium".equals(e.getName())) {
                matchers.add(new MatchPremiumState(Boolean.parseBoolean(e.getText())));
            } else if ("Level".equals(e.getName())) {
                matchers.add(new MatchMinLevel(Integer.parseInt(e.getText())));
            } else if ("PvP_count".equals(e.getName())) {
                matchers.add(new MatchMinPvP(Integer.parseInt(e.getText())));
            } else if ("JobLevel".equals(e.getName())) {
                matchers.add(new MatchMinJobLevel(Integer.parseInt(e.getText())));
            } else if ("OnlineTime".equals(e.getName())) {
                matchers.add(new MatchMinOnlineTime(Integer.parseInt(e.getText())));
            } else if ("LiveTime".equals(e.getName())) {
                matchers.add(new MatchMinLiveTime(Integer.parseInt(e.getText())));
            } else if (e.getName().endsWith("Limit")) {
                int limitCount = 0;
                int limitTime = 0;
                int limitBurst = 0;
                for (Element d : e.getChildren()) {
                    switch (d.getName()) {
                        case "Count":
                            limitCount = Integer.parseInt(d.getText());
                            break;
                        case "Time":
                            limitTime = Integer.parseInt(d.getText());
                            break;
                        default:
                            if (!"Burst".equals(d.getName())) {
                                continue;
                            }
                            limitBurst = Integer.parseInt(d.getText());
                            break;
                    }
                }
                if (limitCount < 1) {
                    throw new IllegalArgumentException("Limit Count < 1!");
                }
                if (limitTime < 1) {
                    throw new IllegalArgumentException("Limit Time  < 1!");
                }
                if (limitBurst < 1) {
                    throw new IllegalArgumentException("Limit Burst < 1!");
                }
                switch (e.getName()) {
                    case "Limit":
                        matchers.add(new MatchChatLimit(limitCount, limitTime, limitBurst));
                        break;
                    case "FloodLimit":
                        matchers.add(new MatchFloodLimit(limitCount, limitTime, limitBurst));
                        break;
                    default:
                        if (!"RecipientLimit".equals(e.getName())) {
                            continue;
                        }
                        matchers.add(new MatchRecipientLimit(limitCount, limitTime, limitBurst));
                        break;
                }
            } else if ("Or".equals(e.getName())) {
                final List<ChatFilterMatcher> matches = parseMatchers(e);
                matchers.add(new MatchLogicalOr(matches.toArray(new ChatFilterMatcher[0])));
            } else if ("And".equals(e.getName())) {
                final List<ChatFilterMatcher> matches = parseMatchers(e);
                matchers.add(new MatchLogicalAnd(matches.toArray(new ChatFilterMatcher[0])));
            } else if ("Not".equals(e.getName())) {
                final List<ChatFilterMatcher> matches = parseMatchers(e);
                if (matches.size() == 1) {
                    matchers.add(new MatchLogicalNot(matches.get(0)));
                } else {
                    matchers.add(new MatchLogicalNot(new MatchLogicalAnd(matches.toArray(new ChatFilterMatcher[0]))));
                }
            } else {
                if (!"Xor".equals(e.getName())) {
                    continue;
                }
                final List<ChatFilterMatcher> matches = parseMatchers(e);
                matchers.add(new MatchLogicalXor(matches.toArray(new ChatFilterMatcher[0])));
            }
        }
        return matchers;
    }

    @Override
    protected void readData(final ChatFilters holder, final Element rootElement) {
        for (Element filterElement : rootElement.getChildren()) {
            int action = 0;
            String value = null;
            for (Element e : filterElement.getChildren()) {
                switch (e.getName()) {
                    case "Action":
                        final String banStr = e.getText();
                        switch (banStr) {
                            case "BanChat":
                                action = 1;
                                break;
                            case "WarnMsg":
                                action = 2;
                                break;
                            case "ReplaceMsg":
                                action = 3;
                                break;
                            default:
                                if (!"RedirectMsg".equals(banStr)) {
                                    continue;
                                }
                                action = 4;
                                break;
                        }
                        break;
                    case "BanTime":
                        value = String.valueOf(Integer.parseInt(e.getText()));
                        break;
                    case "RedirectChannel":
                        value = ChatType.valueOf(e.getText()).toString();
                        break;
                    case "ReplaceMsg":
                        value = e.getText();
                        break;
                    default:
                        if (!"WarnMsg".equals(e.getName())) {
                            continue;
                        }
                        value = e.getText();
                        break;
                }
            }
            final List<ChatFilterMatcher> matchers = parseMatchers(filterElement);
            if (matchers.isEmpty()) {
                throw new IllegalArgumentException("No matchers defined for a filter!");
            }
            ChatFilterMatcher matcher;
            if (matchers.size() == 1) {
                matcher = matchers.get(0);
            } else {
                matcher = new MatchLogicalAnd(matchers.toArray(new ChatFilterMatcher[0]));
            }
            holder.add(new ChatFilter(matcher, action, value));
        }
    }

    @Override
    public File getXMLFile() {
        return new File(Config.CHATFILTERS_CONFIG_FILE);
    }

    private static class LazyHolder {
        private static final ChatFilterParser INSTANCE = new ChatFilterParser();
    }

}
