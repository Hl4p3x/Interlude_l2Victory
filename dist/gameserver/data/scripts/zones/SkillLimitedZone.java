package zones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.ArrayList;

/**
 * Created by JunkyFunky
 * on 07.07.2018 11:09
 * group j2dev
 */
public class SkillLimitedZone implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkillLimitedZone.class);
    private static final String PLAYER_SKILLID = "limitedSkill";

    private static void init() {
        int count = 0;
        for (final Zone zone : ReflectionUtils.getZonesByType(Zone.ZoneType.dummy)) {
            final boolean skillIdSet = zone.getParams().isSet(PLAYER_SKILLID);
            if (!skillIdSet) {
                continue;
            }
            final String skillId = zone.getParams().getString(PLAYER_SKILLID);
            zone.addListener(new SkillLimitedZoneListener(skillId));
            count++;
        }
        LOGGER.info("SkillIdLimitedZone: Loaded " + count + " skills(id-lvl) limit zone(s).");
    }

    @Override
    public void onInit() {
        init();
    }

    private static class SkillLimitedZoneListener implements OnZoneEnterLeaveListener {
        private final String skillString;
        private final ArrayList<Skill> skills;

        private SkillLimitedZoneListener(final String id) {
            skillString = id;
            skills = new ArrayList<>();
            String[] skillArray = skillString.split(";");
            for (String skill : skillArray) {
                skills.add(new Skill(skill));
            }
        }

        @Override
        public void onZoneEnter(final Zone zone, final Creature actor) {
            if (actor != null && actor.isPlayer()) {
                final Player player = actor.getPlayer();
                if (player.isGM()) {
                    return;
                }
                skills.forEach(sk -> {
                    if (player.getAllSkills().stream().noneMatch(skill -> skill.getId() == sk.id && skill.getLevel() >= sk.level)) {
                        player.teleToClosestTown();
                        player.sendMessage(player.isLangRus() ? "Вы не можете находиться в этой зоне" : "Your don't allowed is in zone");
                        ThreadPoolManager.getInstance().schedule(() -> {
                            final Summon summon = player.getPet();
                            if (summon != null) {
                                summon.teleportToOwner();
                            }
                        }, 3000);
                    }
                });
            }
        }

        private class Skill {
            public Skill(final String skill) {
                String[] skillArray = skill.split("-");
                id = Integer.parseInt(skillArray[0]);
                level = Integer.parseInt(skillArray[1]);
            }

            public int id;
            public int level;

        }
        @Override
        public void onZoneLeave(final Zone zone, final Creature actor) {
        }
    }
}
