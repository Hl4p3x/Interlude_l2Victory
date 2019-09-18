package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Effect.EEffectSlot;
import ru.j2dev.gameserver.model.Playable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PartySpelled extends L2GameServerPacket {
    private final int _type;
    private final int _objId;
    private final List<Effect> _effects;

    public PartySpelled(final Playable activeChar, final boolean full) {
        _objId = activeChar.getObjectId();
        _type = (activeChar.isPet() ? 1 : (activeChar.isSummon() ? 2 : 0));
        _effects = new ArrayList<>();
        if (full) {
            final ru.j2dev.gameserver.model.Effect[] effects = activeChar.getEffectList().getAllFirstEffects();
            Arrays.stream(EEffectSlot.VALUES).forEach(ees -> Arrays.stream(effects).filter(effect -> effect != null && effect.isInUse() && effect.getEffectSlot() == ees).forEach(effect -> effect.addPartySpelledIcon(this)));
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xee);
        writeD(_type);
        writeD(_objId);
        writeD(_effects.size());
        _effects.forEach(temp -> {
            writeD(temp._skillId);
            writeH(temp._level);
            writeD(temp._duration);
        });
    }

    public void addPartySpelledEffect(final int skillId, final int level, final int duration) {
        _effects.add(new Effect(skillId, level, duration));
    }

    static class Effect {
        final int _skillId;
        final int _level;
        final int _duration;

        public Effect(final int skillId, final int level, final int duration) {
            _skillId = skillId;
            _level = level;
            _duration = duration;
        }
    }
}
