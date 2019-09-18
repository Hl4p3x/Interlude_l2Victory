package npc.model;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.ChestInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class TreasureChestInstance extends ChestInstance {

    private static final int TREASURE_BOMB_ID = 4143;

    public TreasureChestInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void tryOpen(final Player opener, final Skill skill) {
        if (isCommonTreasureChest()) {
            getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, 100);
            return;
        }
        final double chance = calcChance(opener, skill);
        if (Rnd.chance(chance)) {
            getAggroList().addDamageHate(opener, 10000, 0);
            doDie(opener);
        } else {
            fakeOpen(opener);
        }
        opener.sendPacket(new PlaySound("ItemSound2.broken_key"));
    }

    public double calcChance(final Player opener, final Skill skill) {
        final int skill_name_id = skill.getId();
        final int skill_name_level = skill.getLevel();
        int success_rate = 1;
        int chance = 1;
        switch (skill_name_id) {
            case 27:
                //TODO переделать на TIntIntMap
                switch (skill_name_level) {
                    case 1:
                        success_rate = 98;
                        break;
                    case 2:
                        success_rate = 84;
                        break;
                    case 3:
                        success_rate = 99;
                        break;
                    case 4:
                        success_rate = 84;
                        break;
                    case 5:
                        success_rate = 88;
                        break;
                    case 6:
                        success_rate = 90;
                        break;
                    case 7:
                        success_rate = 89;
                        break;
                    case 8:
                        success_rate = 88;
                        break;
                    case 9:
                        success_rate = 86;
                        break;
                    case 10:
                        success_rate = 90;
                        break;
                    case 11:
                        success_rate = 87;
                        break;
                    case 12:
                        success_rate = 89;
                        break;
                    case 13:
                        success_rate = 89;
                        break;
                    case 14:
                        success_rate = 89;
                        break;
                    case 15:
                        success_rate = 89;
                        break;
                }
                chance = success_rate - (getLevel() - skill_name_level * 4 - 16) * 6;
                if (chance > success_rate) {
                    chance = success_rate;
                }
                break;
            case 2065:
                chance = (int) (60.0 - (getLevel() - (skill_name_level - 1) * 10) * 1.5);
                if (chance > 60) {
                    chance = 60;
                }
                break;
            case 2229:
                switch (skill_name_level) {
                    case 1: {
                        final int level_mod = getLevel() - 19;
                        if (level_mod <= 0) {
                            chance = 100;
                        } else {
                            chance = (int) ((2.0E-4 * (level_mod * level_mod) - 0.0264 * level_mod + 0.7695) * 100.0);
                        }
                        break;
                    }
                    case 2: {
                        final int level_mod = getLevel() - 29;
                        if (level_mod <= 0) {
                            chance = 100;
                        } else {
                            chance = (int) ((3.0E-4 * level_mod * level_mod - 0.0279 * level_mod + 0.7568) * 100.0);
                        }
                        break;
                    }
                    case 3: {
                        final int level_mod = getLevel() - 39;
                        if (level_mod <= 0) {
                            chance = 100;
                        } else {
                            chance = (int) ((3.0E-4 * level_mod * level_mod - 0.0269 * level_mod + 0.7334) * 100.0);
                        }
                        break;
                    }
                    case 4: {
                        final int level_mod = getLevel() - 49;
                        if (level_mod <= 0) {
                            chance = 100;
                        } else {
                            chance = (int) ((3.0E-4 * level_mod * level_mod - 0.0284 * level_mod + 0.8034) * 100.0);
                        }
                        break;
                    }
                    case 5: {
                        final int level_mod = getLevel() - 59;
                        if (level_mod <= 0) {
                            chance = 100;
                        } else {
                            chance = (int) ((5.0E-4 * level_mod * level_mod - 0.0356 * level_mod + 0.9065) * 100.0);
                        }
                        break;
                    }
                    case 6: {
                        final int level_mod = getLevel() - 69;
                        if (level_mod <= 0) {
                            chance = 100;
                        } else {
                            chance = (int) ((9.0E-4 * level_mod * level_mod - 0.0373 * level_mod + 0.8572) * 100.0);
                        }
                        break;
                    }
                    case 7: {
                        final int level_mod = getLevel() - 79;
                        if (level_mod <= 0) {
                            chance = 100;
                        } else {
                            chance = (int) ((0.0043 * level_mod * level_mod - 0.0671 * level_mod + 0.9593) * 100.0);
                        }
                        break;
                    }
                    case 8:
                        chance = 100;
                        break;
                }
                break;
        }
        return chance;
    }

    private void fakeOpen(final Creature opener) {
        final Skill bomb = SkillTable.getInstance().getInfo(TREASURE_BOMB_ID, getBombLvl());
        if (bomb != null) {
            doCast(bomb, opener, false);
        }
        onDecay();
    }

    private int getBombLvl() {
        final int npcLvl = getLevel();
        int lvl = 1;
        if (npcLvl >= 78) {
            lvl = 10;
        } else if (npcLvl >= 72) {
            lvl = 9;
        } else if (npcLvl >= 66) {
            lvl = 8;
        } else if (npcLvl >= 60) {
            lvl = 7;
        } else if (npcLvl >= 54) {
            lvl = 6;
        } else if (npcLvl >= 48) {
            lvl = 5;
        } else if (npcLvl >= 42) {
            lvl = 4;
        } else if (npcLvl >= 36) {
            lvl = 3;
        } else if (npcLvl >= 30) {
            lvl = 2;
        }
        return lvl;
    }

    private boolean isCommonTreasureChest() {
        final int npcId = getNpcId();
        return npcId >= 21801 && npcId <= 21822;
    }

    @Override
    public void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        if (!isCommonTreasureChest()) {
            fakeOpen(attacker);
        }
        super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
    }
}
