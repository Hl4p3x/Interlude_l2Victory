package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ExEventMatchSpelledInfo extends L2GameServerPacket {
    private final List<Effect> _effects;
    private int char_obj_id;

    public ExEventMatchSpelledInfo() {
        char_obj_id = 0;
        _effects = new ArrayList<>();
    }

    public void addEffect(final int skillId, final int dat, final int duration) {
        _effects.add(new Effect(skillId, dat, duration));
    }

    public void addSpellRecivedPlayer(final Player cha) {
        if (cha != null) {
            char_obj_id = cha.getObjectId();
        }
    }

    @Override
    protected void writeImpl() {
        writeEx(0x4);
        writeD(char_obj_id);
        writeD(_effects.size());
        _effects.forEach(temp -> {
            writeD(temp.skillId);
            writeH(temp.dat);
            writeD(temp.duration);
        });
    }

    class Effect {
        final int skillId;
        final int dat;
        final int duration;

        public Effect(final int skillId, final int dat, final int duration) {
            this.skillId = skillId;
            this.dat = dat;
            this.duration = duration;
        }
    }
}
