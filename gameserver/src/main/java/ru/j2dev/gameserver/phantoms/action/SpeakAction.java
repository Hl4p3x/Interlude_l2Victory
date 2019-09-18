package ru.j2dev.gameserver.phantoms.action;

import ru.j2dev.gameserver.phantoms.PhantomConfig;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomPhraseHolder;
import ru.j2dev.commons.math.random.RndSelector;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.utils.MapUtils;

import java.util.List;

public class SpeakAction extends AbstractPhantomAction {

    private static void shout(final Player activeChar, final Say2 cs) {
        final int rx = MapUtils.regionX(activeChar);
        final int ry = MapUtils.regionY(activeChar);
        final int offset = Config.SHOUT_OFFSET;
        GameObjectsStorage.getPlayers().stream().filter(player -> player != activeChar && activeChar.getReflection() == player.getReflection() && !player.isBlockAll()).filter(player -> !player.isInBlockList(activeChar)).forEach(player -> {
            final int tx = MapUtils.regionX(player);
            final int ty = MapUtils.regionY(player);
            if ((tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) && !activeChar.isInRangeZ(player, (long) Config.CHAT_RANGE)) {
                return;
            }
            player.sendPacket(cs);
        });
    }

    @Override
    public long getDelay() {
        return PhantomConfig.chatSpeakDelay;
    }

    @Override
    public void run() {
        final RndSelector<ChatType> rndSelector = new RndSelector<>();
        rndSelector.add(ChatType.ALL, PhantomConfig.chanceSpeakAll);
        rndSelector.add(ChatType.SHOUT, PhantomConfig.chanceSpeakShout);
        rndSelector.add(ChatType.TRADE, PhantomConfig.chanceSpeakTrade);
        final ChatType chatType = rndSelector.select();
        final String phrase = PhantomPhraseHolder.getInstance().getRandomPhrase(chatType);
        if (phrase != null) {
            final Say2 cs = new Say2(actor.getObjectId(), chatType, actor.getName(), phrase);
            switch (chatType) {
                case ALL: {
                    List<Player> list = World.getAroundPlayers(actor);
                    if (list != null) {
                        list.stream().filter(player -> player != actor && player.getReflection() == actor.getReflection() && !player.isBlockAll()).filter(player -> !player.isInBlockList(actor)).forEach(player -> player.sendPacket(cs));
                    }
                    break;
                }
                case SHOUT:
                case TRADE: {
                    shout(actor, cs);
                    break;
                }
            }
        }
    }
}
