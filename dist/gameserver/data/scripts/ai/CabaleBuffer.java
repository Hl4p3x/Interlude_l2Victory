package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.List;

public class CabaleBuffer extends DefaultAI {

    private static final int PREACHER_FIGHTER_SKILL_ID = 4361;
    private static final int PREACHER_MAGE_SKILL_ID = 4362;
    private static final int ORATOR_FIGHTER_SKILL_ID = 4364;
    private static final int ORATOR_MAGE_SKILL_ID = 4365;
    private static final long castDelay = 60 * 1000L;
    private static final long buffDelay = 1000L;
    /**
     * Messages of NPCs *
     */
    private static final int[] preacherText = {1000303, 1000415, 1000416, 1000417};
    private static final int[] oratorText = {1000305, 1000421, 1000422, 1000423};
    private long _castVar;
    private long _buffVar;

    public CabaleBuffer(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }

        final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();

        if (winningCabal == SevenSigns.CABAL_NULL) {
            return true;
        }

        int losingCabal = SevenSigns.CABAL_NULL;

        if (winningCabal == SevenSigns.CABAL_DAWN) {
            losingCabal = SevenSigns.CABAL_DUSK;
        } else if (winningCabal == SevenSigns.CABAL_DUSK) {
            losingCabal = SevenSigns.CABAL_DAWN;
        }

        if (_castVar + castDelay < System.currentTimeMillis()) {
            _castVar = System.currentTimeMillis();
            actor.MakeFString(SevenSigns.ORATOR_NPC_IDS.contains(actor.getNpcId()) ? oratorText[Rnd.get(oratorText.length)] : preacherText[Rnd.get(preacherText.length)]);
        }
        /*
          For each known player in range, cast either the positive or negative
          buff. <BR>
          The stats affected depend on the player type, either a fighter or a
          mystic. <BR>
          Curse of Destruction (Loser) - Fighters: -25% Accuracy, -25% Effect
          Resistance - Mystics: -25% Casting Speed, -25% Effect Resistance
          Blessing of Prophecy (Winner) - Fighters: +25% Max Load, +25% Effect
          Resistance - Mystics: +25% Magic Cancel Resist, +25% Effect
          Resistance
         */
        if (_buffVar + buffDelay < System.currentTimeMillis()) {
            _buffVar = System.currentTimeMillis();
            for (final Player player : World.getAroundPlayers(actor, 300, 200)) {
                if (player == null) {
                    continue;
                }
                final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
                final int i0 = Rnd.get(100);
                final int i1 = Rnd.get(10000);
                if (playerCabal == winningCabal && SevenSigns.ORATOR_NPC_IDS.contains(actor.getNpcId())) {
                    if (player.isMageClass()) {
                        final List<Effect> effects = player.getEffectList().getEffectsBySkillId(ORATOR_MAGE_SKILL_ID);
                        if (effects == null || effects.size() <= 0) {
                            if (i1 < 1) {
                                actor.MakeFString(1000306);
                            }

                            final Skill skill = getSkillInfo(ORATOR_MAGE_SKILL_ID, 1);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        } else if (i0 < 5) {
                            if (i1 < 500) {
                                actor.MakeFString(1000424, player.getName());
                            }

                            final Skill skill = getSkillInfo(ORATOR_MAGE_SKILL_ID, 2);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        }
                    } else {
                        final List<Effect> effects = player.getEffectList().getEffectsBySkillId(ORATOR_FIGHTER_SKILL_ID);
                        if (effects == null || effects.size() <= 0) {
                            if (i1 < 1) {
                                actor.MakeFString(1000426);
                            }

                            final Skill skill = getSkillInfo(ORATOR_FIGHTER_SKILL_ID, 1);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        } else if (i0 < 5) {
                            if (i1 < 500) {
                                actor.MakeFString(1000425, player.getName());
                            }

                            final Skill skill = getSkillInfo(ORATOR_FIGHTER_SKILL_ID, 2);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        }
                    }
                } else if (playerCabal == losingCabal && SevenSigns.PREACHER_NPC_IDS.contains(actor.getNpcId())) {
                    if (player.isMageClass()) {
                        final List<Effect> effects = player.getEffectList().getEffectsBySkillId(PREACHER_MAGE_SKILL_ID);
                        if (effects == null || effects.size() <= 0) {
                            if (i1 < 1) {
                                actor.MakeFString(1000420);
                            }

                            final Skill skill = getSkillInfo(PREACHER_MAGE_SKILL_ID, 1);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        } else if (i0 < 5) {
                            if (i1 < 500) {
                                actor.MakeFString(1000304);
                            }

                            final Skill skill = getSkillInfo(PREACHER_MAGE_SKILL_ID, 2);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        }
                    } else {
                        final List<Effect> effects = player.getEffectList().getEffectsBySkillId(PREACHER_FIGHTER_SKILL_ID);
                        if (effects == null || effects.size() <= 0) {
                            if (i1 < 1) {
                                actor.MakeFString(1000418, player.getName());
                            }

                            final Skill skill = getSkillInfo(PREACHER_FIGHTER_SKILL_ID, 1);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        } else if (i0 < 5) {
                            if (i1 < 500) {
                                actor.MakeFString(1000419, player.getName());
                            }

                            final Skill skill = getSkillInfo(PREACHER_FIGHTER_SKILL_ID, 2);
                            if (skill != null) {
                                actor.altUseSkill(skill, player);
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
