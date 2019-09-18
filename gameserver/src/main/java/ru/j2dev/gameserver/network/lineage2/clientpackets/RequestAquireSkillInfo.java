package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.SkillAcquireHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SkillLearn;
import ru.j2dev.gameserver.model.base.AcquireType;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.AcquireSkillInfo;
import ru.j2dev.gameserver.tables.SkillTable;

public class RequestAquireSkillInfo extends L2GameClientPacket {
    private int _id;
    private int _level;
    private AcquireType _type;

    @Override
    protected void readImpl() {
        _id = readD();
        _level = readD();
        _type = ArrayUtils.valid(AcquireType.VALUES, readD());
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null || player.getTransformation() != 0 || SkillTable.getInstance().getInfo(_id, _level) == null || _type == null) {
            return;
        }
        final NpcInstance trainer = player.getLastNpc();
        if (trainer == null || !trainer.isInActingRange(player)) {
            return;
        }
        final int clsId = player.getVarInt("AcquireSkillClassId", player.getClassId().getId());
        final ClassId classId = (clsId >= 0 && clsId < ClassId.VALUES.length) ? ClassId.VALUES[clsId] : null;
        final SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, classId, _id, _level, _type);
        if (skillLearn == null) {
            return;
        }
        if (Config.ALT_DISABLE_SPELLBOOKS && _type == AcquireType.NORMAL) {
            sendPacket(new AcquireSkillInfo(_type, skillLearn, 0, 0));
        } else {
            sendPacket(new AcquireSkillInfo(_type, skillLearn, skillLearn.getItemId(), (int) skillLearn.getItemCount()));
        }
    }
}
