package ru.j2dev.gameserver.model.instances.residences.clanhall;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallTeamBattleEvent;
import ru.j2dev.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.CTBTeamObject;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public abstract class CTBBossInstance extends MonsterInstance {
    public static final Skill SKILL = SkillTable.getInstance().getInfo(5456, 1);

    private CTBTeamObject _matchTeamObject;

    public CTBBossInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        setHasChatWindow(false);
    }

    @Override
    public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        if (attacker.getLevel() > getLevel() + 8 && attacker.getEffectList().getEffectsCountForSkill(SKILL.getId()) == 0) {
            doCast(SKILL, attacker, false);
            return;
        }
        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        final CTBSiegeClanObject clan = _matchTeamObject.getSiegeClan();
        if (clan != null && attacker.isPlayable()) {
            final Player player = attacker.getPlayer();
            return player.getClan() != clan.getClan();
        }
        return true;
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return isAttackable(attacker);
    }

    @Override
    public void onDeath(final Creature killer) {
        final ClanHallTeamBattleEvent event = getEvent(ClanHallTeamBattleEvent.class);
        event.processStep(_matchTeamObject);
        super.onDeath(killer);
    }

    @Override
    public String getTitle() {
        final CTBSiegeClanObject clan = _matchTeamObject.getSiegeClan();
        return (clan == null) ? "" : clan.getClan().getName();
    }

    public void setMatchTeamObject(final CTBTeamObject matchTeamObject) {
        _matchTeamObject = matchTeamObject;
    }
}
