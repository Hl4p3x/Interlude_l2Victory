package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NewCharacterSuccess;
import ru.j2dev.gameserver.tables.CharTemplateTable;

public class RequestNewCharacter extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final NewCharacterSuccess ct = new NewCharacterSuccess();
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.fighter, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.mage, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.darkMage, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.orcMage, false));
        ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter, false));
        sendPacket(ct);
    }
}
