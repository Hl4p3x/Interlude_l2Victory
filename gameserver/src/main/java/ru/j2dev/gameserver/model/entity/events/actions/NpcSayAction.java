package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcSay;
import ru.j2dev.gameserver.utils.MapUtils;
import ru.j2dev.gameserver.utils.PtsUtils;

public class NpcSayAction implements EventAction {
    private final int _npcId;
    private final int _range;
    private final ChatType _chatType;
    private final int _fstring;

    public NpcSayAction(final int npcId, final int range, final ChatType type, final int string) {
        _npcId = npcId;
        _range = range;
        _chatType = type;
        _fstring = string;
    }

    @Override
    public void call(final GlobalEvent event) {
        final NpcInstance npc = GameObjectsStorage.getByNpcId(_npcId);
        if (npc == null) {
            return;
        }
        if (_range <= 0) {
            final int rx = MapUtils.regionX(npc);
            final int ry = MapUtils.regionY(npc);
            final int offset = Config.SHOUT_OFFSET;
            GameObjectsStorage.getPlayers().stream().filter(player -> npc.getReflection() == player.getReflection()).forEach(player -> {
                final int tx = MapUtils.regionX(player);
                final int ty = MapUtils.regionY(player);
                if (tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) {
                    return;
                }
                packet(npc, player);
            });
        } else {
            World.getAroundPlayers(npc, _range, Math.max(_range / 2, 200)).stream().filter(player2 -> npc.getReflection() == player2.getReflection()).forEach(player2 -> packet(npc, player2));
        }
    }

    private void packet(final NpcInstance npc, final Player player) {
        player.sendPacket(new NpcSay(npc, _chatType, PtsUtils.MakeFString(player, _fstring)));
    }
}
