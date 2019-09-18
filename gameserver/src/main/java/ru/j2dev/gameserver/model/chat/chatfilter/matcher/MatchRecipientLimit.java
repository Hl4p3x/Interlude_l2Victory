package ru.j2dev.gameserver.model.chat.chatfilter.matcher;

import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilterMatcher;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatMsg;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

import java.util.Deque;
import java.util.Iterator;

public class MatchRecipientLimit implements ChatFilterMatcher {
    private final int _limitCount;
    private final int _limitTime;
    private final int _limitBurst;

    public MatchRecipientLimit(final int limitCount, final int limitTime, final int limitBurst) {
        _limitCount = limitCount;
        _limitTime = limitTime;
        _limitBurst = limitBurst;
    }

    @Override
    public boolean isMatch(final Player player, final ChatType type, final String msg, final Player recipient) {
        int firstMsgTime;
        final int currentTime = firstMsgTime = (int) (System.currentTimeMillis() / 1000L);
        int count = 0;
        final TIntHashSet recipients = new TIntHashSet();
        final Deque<ChatMsg> msgBucket = player.getMessageBucket();
        final Iterator<ChatMsg> itr = msgBucket.descendingIterator();
        while (itr.hasNext()) {
            final ChatMsg cm = itr.next();
            if (cm.chatType == type && cm.recipient != 0) {
                firstMsgTime = cm.time;
                recipients.add(cm.recipient);
                count = recipients.size();
                if (_limitBurst == count) {
                    break;
                }
            }
        }
        count -= (currentTime - firstMsgTime) / _limitTime * _limitCount;
        return _limitBurst <= count;
    }
}
