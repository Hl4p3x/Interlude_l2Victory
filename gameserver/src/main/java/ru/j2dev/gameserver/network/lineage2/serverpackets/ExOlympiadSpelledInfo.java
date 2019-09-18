package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

import java.util.ArrayList;

public class ExOlympiadSpelledInfo extends L2GameServerPacket {
    private final ArrayList<Effect> _effects;
    private int char_obj_id;

    public ExOlympiadSpelledInfo() {
        char_obj_id = 0;
        _effects = new ArrayList<>();
    }

    public void addEffect(final int skillId, final int level, final int duration) {
        _effects.add(new Effect(skillId, level, duration));
    }

    public void addSpellRecivedPlayer(final Player cha) {
        if (cha != null) {
            char_obj_id = cha.getObjectId();
        }
    }

    @Override
    protected final void writeImpl() {
        if (char_obj_id == 0) {
            return;
        }
        writeEx(0x2a);
        writeD(char_obj_id);
        writeD(_effects.size());
        _effects.forEach(temp -> {
            writeD(temp.skillId);
            writeH(temp.level);
            writeD(temp.duration);
        });
    }

    class Effect {
        final int skillId;
        final int level;
        final int duration;

        public Effect(final int skillId, final int level, final int duration) {
            this.skillId = skillId;
            this.level = level;
            this.duration = duration;
        }
    }
}
