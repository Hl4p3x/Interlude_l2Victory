package ai.custom;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Mystic;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.scripts.Functions;

import java.util.List;

public class SSQAnakim extends Mystic {
    private static final String PLAYER_NAME = "%playerName%";
    private static final String[] chat = {"For the eternity of Einhasad!!!", "Dear Shillien's offspring! You are not capable of confronting us!", "I'll show you the real power of Einhasad!", "Dear Military Force of Light! Go destroy the offspring of Shillien!!!"};
    private static final String[] pms = {"My power's weakening.. Hurry and turn on the sealing device!!!", "All 4 sealing devices must be turned on!!!", "Lilith's attack is getting stronger! Go ahead and turn it on!", "%playerName%, hold on. We're almost done!"};

    private long _lastChatTime;
    private long _lastPMTime;
    private long _lastSkillTime;

    public SSQAnakim(final NpcInstance actor) {
        super(actor);
        _lastChatTime = 0L;
        _lastPMTime = 0L;
        _lastSkillTime = 0L;
        actor.setHasChatWindow(false);
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
    }

    @Override
    protected boolean thinkActive() {
        if (_lastChatTime < System.currentTimeMillis()) {
            Functions.npcSay(getActor(), SSQAnakim.chat[Rnd.get(SSQAnakim.chat.length)]);
            _lastChatTime = System.currentTimeMillis() + 12000L;
        }
        if (_lastPMTime < System.currentTimeMillis()) {
            final Player player = getPlayer();
            if (player != null) {
                String text = SSQAnakim.pms[Rnd.get(SSQAnakim.pms.length)];
                if (text.contains("%playerName%")) {
                    text = text.replace("%playerName%", player.getName());
                }
                Functions.npcSayToPlayer(getActor(), player, text);
            }
            _lastPMTime = System.currentTimeMillis() + 20000L;
        }
        if (_lastSkillTime < System.currentTimeMillis()) {
            if (getLilith() != null) {
                getActor().broadcastPacket(new MagicSkillUse(getActor(), getLilith(), 6191, 1, 5000, 10L));
            }
            _lastSkillTime = System.currentTimeMillis() + 6500L;
        }
        return true;
    }

    private NpcInstance getLilith() {
        final List<NpcInstance> around = getActor().getAroundNpc(1000, 300);
        if (around != null && !around.isEmpty()) {
            for (final NpcInstance npc : around) {
                if (npc.getNpcId() == 32715) {
                    return npc;
                }
            }
        }
        return null;
    }

    private Player getPlayer() {
        final Reflection reflection = getActor().getReflection();
        if (reflection == null) {
            return null;
        }
        final List<Player> pl = reflection.getPlayers();
        if (pl.isEmpty()) {
            return null;
        }
        return pl.get(0);
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
    }

    @Override
    protected void onEvtAggression(final Creature attacker, final int aggro) {
    }
}
