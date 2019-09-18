package zones;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class AutoBuffZone implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoBuffZone.class);

    private static boolean checkPlayerForZoneBuff(final Player player) {
        return player != null && !player.isDead() && !player.isCursedWeaponEquipped() && !player.isFakeDeath() && !player.isFlying() && !player.isInDuel() && !player.isOlyParticipant() && !player.isInStoreMode() && !player.isSitting() && player.getEvent(SiegeEvent.class) == null;
    }

    private static List<Pair<Skill, Integer>> parseZoneBuffs(final String zoneBuffsText) {
        final ArrayList<Pair<Skill, Integer>> result = new ArrayList<>();
        final StringTokenizer zoneBuffTok = new StringTokenizer(zoneBuffsText, ",;");
        while (zoneBuffTok.hasMoreTokens()) {
            final String zoneBuffText = zoneBuffTok.nextToken().trim();
            if (zoneBuffText.isEmpty()) {
                continue;
            }
            final int durationModDelimIdx = zoneBuffText.indexOf(47);
            String skillIdLvlText = zoneBuffText;
            Integer durationMod = null;
            if (durationModDelimIdx > 0) {
                durationMod = Integer.parseInt(zoneBuffText.substring(durationModDelimIdx + 1).trim());
                skillIdLvlText = zoneBuffText.substring(0, durationModDelimIdx).trim();
            }
            final int idLvlDelimIdx = skillIdLvlText.indexOf(58);
            if (idLvlDelimIdx < 0) {
                throw new IllegalArgumentException("Can't parse \"" + zoneBuffText + "\"");
            }
            final int skillId = Integer.parseInt(skillIdLvlText.substring(0, idLvlDelimIdx).trim());
            final int skillLevel = Integer.parseInt(skillIdLvlText.substring(idLvlDelimIdx + 1).trim());
            final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
            if (skill == null) {
                throw new IllegalArgumentException("Unknown skill \"" + zoneBuffText + "\"");
            }
            result.add(Pair.of(skill, durationMod));
        }
        result.trimToSize();
        return Collections.unmodifiableList(result);
    }

    private static void init() {
        int count = 0;
        for (final Zone zone : ReflectionUtils.getZones()) {
            final String zoneBuffsText = zone.getParams().getString("zoneBuffs", null);
            if (zoneBuffsText != null) {
                if (zoneBuffsText.isEmpty()) {
                    continue;
                }
                final List<Pair<Skill, Integer>> zoneBuffs = parseZoneBuffs(zoneBuffsText);
                if (zoneBuffs.isEmpty()) {
                    continue;
                }
                zone.addListener(new AutoBuffZoneListener(zoneBuffs));
                count++;
            }
        }
        LOGGER.info("AutoBuffZone: Loaded " + count + " auto buff zone(s).");
    }

    @Override
    public void onInit() {
        init();
    }

    private static class AutoBuffZoneListener implements OnZoneEnterLeaveListener {
        private final List<Pair<Skill, Integer>> _zoneBuffs;

        private AutoBuffZoneListener(final List<Pair<Skill, Integer>> zoneBuffs) {
            _zoneBuffs = zoneBuffs;
        }

        @Override
        public void onZoneEnter(final Zone zone, final Creature actor) {
            if (actor == null || !actor.isPlayer()) {
                return;
            }
            final Player target = actor.getPlayer();
            if (!checkPlayerForZoneBuff(target)) {
                return;
            }
            ThreadPoolManager.getInstance().execute(() -> _zoneBuffs.forEach(zoneBuff -> {
                final Skill skill = zoneBuff.getLeft();
                final Integer durationMod = zoneBuff.getRight();
                if (target.getEffectList().containEffectFromSkills(skill.getId())) {
                    return;
                }
                if (durationMod != null) {
                    skill.getEffects(target, target, false, false, durationMod * 1000L, 1.0, false);
                } else {
                    skill.getEffects(target, target, false, false);
                }
            }));
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature actor) {
        }
    }
}
