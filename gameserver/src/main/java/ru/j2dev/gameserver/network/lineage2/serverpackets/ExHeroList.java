package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager.HeroRecord;

import java.util.ArrayList;
import java.util.Collection;

public class ExHeroList extends L2GameServerPacket {
    private final Collection<HeroRecord> heroes;

    public ExHeroList() {
        heroes = new ArrayList<>();
        HeroManager.getInstance().getCurrentHeroes().stream().filter(hr -> hr != null && hr.active && hr.played).forEach(heroes::add);
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x23);
        writeD(heroes.size());
        heroes.forEach(hero -> {
            writeS(hero.name);
            writeD(hero.class_id);
            writeS(hero.clan_name);
            writeD(hero.clan_crest);
            writeS(hero.ally_name);
            writeD(hero.ally_crest);
            writeD(hero.count);
        });
    }
}
