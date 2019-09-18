package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.residences.clanhall.CTBBossInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class CTBTeamObject implements SpawnableObject {
    private final NpcTemplate _mobTemplate;
    private final NpcTemplate _flagTemplate;
    private final Location _flagLoc;
    private CTBSiegeClanObject _siegeClan;
    private NpcInstance _flag;
    private CTBBossInstance _mob;

    public CTBTeamObject(final int mobTemplate, final int flagTemplate, final Location flagLoc) {
        _mobTemplate = NpcTemplateHolder.getInstance().getTemplate(mobTemplate);
        _flagTemplate = NpcTemplateHolder.getInstance().getTemplate(flagTemplate);
        _flagLoc = flagLoc;
    }

    @Override
    public void spawnObject(final GlobalEvent event) {
        if (_flag == null) {
            (_flag = new NpcInstance(IdFactory.getInstance().getNextId(), _flagTemplate)).setCurrentHpMp(_flag.getMaxHp(), _flag.getMaxMp());
            _flag.setHasChatWindow(false);
            _flag.spawnMe(_flagLoc);
        } else {
            if (_mob != null) {
                throw new IllegalArgumentException("Cant spawn twice");
            }
            final NpcTemplate template = (_siegeClan == null || _siegeClan.getParam() == 0L) ? _mobTemplate : NpcTemplateHolder.getInstance().getTemplate((int) _siegeClan.getParam());
            (_mob = (CTBBossInstance) template.getNewInstance()).setCurrentHpMp(_mob.getMaxHp(), _mob.getMaxMp());
            _mob.setMatchTeamObject(this);
            _mob.addEvent(event);
            final int x = (int) (_flagLoc.x + 300.0 * Math.cos(_mob.headingToRadians(_flag.getHeading() - 32768)));
            final int y = (int) (_flagLoc.y + 300.0 * Math.sin(_mob.headingToRadians(_flag.getHeading() - 32768)));
            final Location loc = new Location(x, y, _flag.getZ(), _flag.getHeading());
            _mob.setSpawnedLoc(loc);
            _mob.spawnMe(loc);
        }
    }

    @Override
    public void despawnObject(final GlobalEvent event) {
        if (_mob != null) {
            _mob.deleteMe();
            _mob = null;
        }
        if (_flag != null) {
            _flag.deleteMe();
            _flag = null;
        }
        _siegeClan = null;
    }

    @Override
    public void refreshObject(final GlobalEvent event) {
    }

    public CTBSiegeClanObject getSiegeClan() {
        return _siegeClan;
    }

    public void setSiegeClan(final CTBSiegeClanObject siegeClan) {
        _siegeClan = siegeClan;
    }

    public boolean isParticle() {
        return _flag != null && _mob != null;
    }

    public NpcInstance getFlag() {
        return _flag;
    }
}
