package ru.j2dev.gameserver.phantoms.action;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.phantoms.PhantomConfig;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomPhraseHolder;

public class ChatAnswerAction extends AbstractPhantomAction {
    private Player sender;

    public ChatAnswerAction(Player sender) {
        this.sender = sender;
    }

    @Override
    public long getDelay() {
        return PhantomConfig.chatAnswerDelay;
    }

    @Override
    public void run() {
        if (Rnd.chance(PhantomConfig.chatAnswerChance) && sender != null && !actor.getMemory().isIgnoredChatNick(sender.getName())) {
            String phrase = PhantomPhraseHolder.getInstance().getRandomPhrase(ChatType.TELL);
            if (phrase != null) {
                sender.sendPacket(new Say2(actor.getObjectId(), ChatType.TELL, actor.getName(), phrase));
                actor.getMemory().addIgnoredChatNick(sender.getName());
            }
        }
    }
}
