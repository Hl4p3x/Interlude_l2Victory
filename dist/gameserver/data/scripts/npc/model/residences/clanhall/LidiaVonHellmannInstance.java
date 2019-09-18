package npc.model.residences.clanhall;

import npc.model.residences.SiegeGuardInstance;
import ru.j2dev.gameserver.model.AggroList.HateInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LidiaVonHellmannInstance extends SiegeGuardInstance {
    public LidiaVonHellmannInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onDeath(final Creature killer) {
        final SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
        if (siegeEvent == null) {
            return;
        }
        siegeEvent.processStep(getMostDamagedClan());
        super.onDeath(killer);
    }

    public Clan getMostDamagedClan() {
        final ClanHallSiegeEvent siegeEvent = getEvent(ClanHallSiegeEvent.class);
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
            if (temp != null) {
                if (siegeEvent.getSiegeClan("attackers", temp.getClan()) == null) {
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
