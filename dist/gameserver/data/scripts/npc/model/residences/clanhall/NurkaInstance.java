package npc.model.residences.clanhall;

import npc.model.residences.SiegeGuardInstance;
import ru.j2dev.gameserver.model.AggroList.HateInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NurkaInstance extends SiegeGuardInstance {
    public static final Skill SKILL = SkillTable.getInstance().getInfo(5456, 1);

    public NurkaInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        if (attacker.getLevel() > getLevel() + 8 && attacker.getEffectList().getEffectsCountForSkill(NurkaInstance.SKILL.getId()) == 0) {
            doCast(NurkaInstance.SKILL, attacker, false);
            return;
        }
        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }

    @Override
    public void onDeath(final Creature killer) {
        final SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
        if (siegeEvent == null) {
            return;
        }
        siegeEvent.processStep(getMostDamagedClan());
        super.onDeath(killer);
        deleteMe();
    }

    public Clan getMostDamagedClan() {
        Player temp = null;
        final Map<Player, Integer> damageMap = new HashMap<>();
        for (final HateInfo info : getAggroList().getPlayableMap().values()) {
            final Playable killer = (Playable) info.attacker;
            final int damage = info.damage;
            if (killer.isPet() || killer.isSummon()) {
                temp = killer.getPlayer();
            } else if (killer.isPlayer()) {
                temp = (Player) killer;
            }
            if (temp != null && temp.getClan() != null) {
                if (temp.getClan().getHasHideout() > 0) {
                    continue;
                }
                if (!damageMap.containsKey(temp)) {
                    damageMap.put(temp, damage);
                } else {
                    final int dmg = damageMap.get(temp) + damage;
                    damageMap.put(temp, dmg);
                }
            }
        }
        int mostDamage = 0;
        Player player = null;
        for (final Entry<Player, Integer> entry : damageMap.entrySet()) {
            final int damage2 = entry.getValue();
            final Player t = entry.getKey();
            if (damage2 > mostDamage) {
                mostDamage = damage2;
                player = t;
            }
        }
        return (player == null) ? null : player.getClan();
    }

    @Override
    public boolean isEffectImmune() {
        return true;
    }
}
