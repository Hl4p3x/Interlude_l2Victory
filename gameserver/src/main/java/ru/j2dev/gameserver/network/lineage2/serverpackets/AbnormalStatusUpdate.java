package ru.j2dev.gameserver.network.lineage2.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class AbnormalStatusUpdate extends L2GameServerPacket {
    public static final int INFINITIVE_EFFECT = -1;
    private final List<Effect> _effects;

    public AbnormalStatusUpdate() {
        _effects = new ArrayList<>();
    }

    public void addEffect(final int skillId, final int dat, final int duration) {
        _effects.add(new Effect(skillId, dat, duration));
    }

    @Override
    protected final void writeImpl() {
        writeC(0x7f);
        writeH(_effects.size());
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
