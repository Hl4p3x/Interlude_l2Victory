package ru.j2dev.gameserver.data.xml.parser;

import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.TeleportLocation;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.reward.RewardData;
import ru.j2dev.gameserver.model.reward.RewardGroup;
import ru.j2dev.gameserver.model.reward.RewardList;
import ru.j2dev.gameserver.model.reward.RewardType;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.AbsorbInfo;
import ru.j2dev.gameserver.templates.npc.AbsorbInfo.AbsorbType;
import ru.j2dev.gameserver.templates.npc.Faction;
import ru.j2dev.gameserver.templates.npc.MinionData;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class NpcTemplateParser extends AbstractDirParser<NpcTemplateHolder> {

    private NpcTemplateParser() {
        super(NpcTemplateHolder.getInstance());
    }

    public static NpcTemplateParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/stats/npc/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @Override
    protected void readData(final NpcTemplateHolder holder, final Element rootElement) {
        rootElement.getChildren().forEach(npcElement -> {
            final int npcId = Integer.parseInt(npcElement.getAttributeValue("id"));
            final int templateId = (npcElement.getAttributeValue("template_id") == null) ? 0 : Integer.parseInt(npcElement.getAttributeValue("id"));
            final String name = npcElement.getAttributeValue("name");
            final String title = npcElement.getAttributeValue("title");
            final StatsSet set = new StatsSet();
            set.set("npcId", npcId);
            set.set("displayId", templateId);
            set.set("name", name);
            set.set("title", title);
            set.set("baseCpReg", 0);
            set.set("baseCpMax", 0);
            npcElement.getChildren().forEach(firstElement -> {
                if ("set".equalsIgnoreCase(firstElement.getName())) {
                    set.set(firstElement.getAttributeValue("name"), firstElement.getAttributeValue("value"));
                } else if ("equip".equalsIgnoreCase(firstElement.getName())) {
                    firstElement.getChildren().forEach(eElement -> {
                        final int itemId = Integer.parseInt(eElement.getAttributeValue("item_id"));
                        if (ItemTemplateHolder.getInstance().getTemplate(itemId) == null) {
                            LOGGER.error("Undefined item " + itemId + " used in slot " + eElement.getName() + " of npc " + npcId);
                        }
                        set.set(eElement.getName(), String.valueOf(itemId));
                    });
                } else if ("ai_params".equalsIgnoreCase(firstElement.getName())) {
                    final StatsSet ai = new StatsSet();
                    firstElement.getChildren().forEach(eElement2 -> ai.set(eElement2.getAttributeValue("name"), eElement2.getAttributeValue("value")));
                    set.set("aiParams", ai);
                } else {
                    if (!"attributes".equalsIgnoreCase(firstElement.getName())) {
                        return;
                    }
                    final int[] attributeAttack = new int[6];
                    final int[] attributeDefence = new int[6];
                    for (final Element eElement3 : firstElement.getChildren()) {
                        if ("defence".equalsIgnoreCase(eElement3.getName())) {
                            final ru.j2dev.gameserver.model.base.Element element = ru.j2dev.gameserver.model.base.Element.getElementByName(eElement3.getAttributeValue("attribute"));
                            attributeDefence[element.getId()] = Integer.parseInt(eElement3.getAttributeValue("value"));
                        } else {
                            if (!"attack".equalsIgnoreCase(eElement3.getName())) {
                                return;
                            }
                            final ru.j2dev.gameserver.model.base.Element element = ru.j2dev.gameserver.model.base.Element.getElementByName(eElement3.getAttributeValue("attribute"));
                            attributeAttack[element.getId()] = Integer.parseInt(eElement3.getAttributeValue("value"));
                        }
                    }
                    set.set("baseAttributeAttack", attributeAttack);
                    set.set("baseAttributeDefence", attributeDefence);
                }
            });
            final NpcTemplate template = new NpcTemplate(set);
            final List<Location> teleportLocations = new LinkedList<>();
            npcElement.getChildren().forEach(secondElement -> {
                final String nodeName = secondElement.getName();
                if ("faction".equalsIgnoreCase(nodeName)) {
                    final String factionId = secondElement.getAttributeValue("name");
                    final Faction faction = new Faction(factionId);
                    final int factionRange = Integer.parseInt(secondElement.getAttributeValue("range"));
                    faction.setRange(factionRange);
                    secondElement.getChildren().stream().mapToInt(nextElement -> Integer.parseInt(nextElement.getAttributeValue("npc_id"))).forEach(faction::addIgnoreNpcId);
                    template.setFaction(faction);
                } else if ("rewardlist".equalsIgnoreCase(nodeName)) {
                    final RewardType type = RewardType.valueOf(secondElement.getAttributeValue("type"));
                    final boolean autoLoot = secondElement.getAttributeValue("auto_loot") != null && Boolean.parseBoolean(secondElement.getAttributeValue("auto_loot"));
                    final RewardList rewardList = new RewardList(type, autoLoot);
                    for (final Element nextElement : secondElement.getChildren()) {
                        final String nextName = nextElement.getName();
                        if ("group".equalsIgnoreCase(nextName)) {
                            final double enterChance = (nextElement.getAttributeValue("chance") == null) ? 1000000.0 : (Double.parseDouble(nextElement.getAttributeValue("chance")) * 10000.0);
                            final RewardGroup group = (type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED) ? null : new RewardGroup(enterChance);
                            nextElement.getChildren().stream().map(rewardElement -> parseReward(npcId, rewardElement, type)).filter(Objects::nonNull).forEach(data -> {
                                if (type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED) {
                                    warn("Can't load rewardlist from group: " + npcId + "; type: " + type);
                                } else if (group != null) {
                                    group.addData(data);
                                }
                            });
                            if (group == null) {
                                return;
                            }
                            rewardList.add(group);
                        } else {
                            if (!"reward".equalsIgnoreCase(nextName)) {
                                return;
                            }
                            if (type != RewardType.SWEEP && type != RewardType.NOT_RATED_NOT_GROUPED) {
                                warn("Reward can't be without group(and not grouped): " + npcId + "; type: " + type);
                            } else {
                                final RewardData data2 = parseReward(npcId, nextElement, type);
                                if (data2 == null) {
                                    return;
                                }
                                final RewardGroup g = new RewardGroup(1000000.0);
                                g.addData(data2);
                                rewardList.add(g);
                            }
                        }
                    }
                    if ((type == RewardType.RATED_GROUPED || type == RewardType.NOT_RATED_GROUPED) && !rewardList.validate()) {
                        warn("Problems with rewardlist for npc: " + npcId + "; type: " + type);
                    }
                    template.putRewardList(type, rewardList);
                } else if ("skills".equalsIgnoreCase(nodeName)) {
                    for (final Element nextElement2 : secondElement.getChildren()) {
                        final int id = Integer.parseInt(nextElement2.getAttributeValue("id"));
                        final int level = Integer.parseInt(nextElement2.getAttributeValue("level"));
                        if (SkillTable.getInstance().getInfo(id, level) == null) {
                            LOGGER.error("Undefined id " + id + " and level " + level + " of npc " + npcId);
                        }
                        if (id == 4416) {
                            template.setRace(level);
                        }
                        final Skill skill = SkillTable.getInstance().getInfo(id, level);
                        if (skill == null) {
                            return;
                        }
                        template.addSkill(skill);
                    }
                } else if ("minions".equalsIgnoreCase(nodeName)) {
                    secondElement.getChildren().forEach(nextElement2 -> {
                        final int id = Integer.parseInt(nextElement2.getAttributeValue("npc_id"));
                        final int count = Integer.parseInt(nextElement2.getAttributeValue("count"));
                        template.addMinion(new MinionData(id, count));
                    });
                } else if ("teach_classes".equalsIgnoreCase(nodeName)) {
                    secondElement.getChildren().stream().mapToInt(nextElement2 -> Integer.parseInt(nextElement2.getAttributeValue("id"))).mapToObj(id -> ClassId.VALUES[id]).forEach(template::addTeachInfo);
                } else if ("absorblist".equalsIgnoreCase(nodeName)) {
                    secondElement.getChildren().forEach(nextElement2 -> {
                        final int chance = Integer.parseInt(nextElement2.getAttributeValue("chance"));
                        final int cursedChance = (nextElement2.getAttributeValue("cursed_chance") == null) ? 0 : Integer.parseInt(nextElement2.getAttributeValue("cursed_chance"));
                        final int minLevel = Integer.parseInt(nextElement2.getAttributeValue("min_level"));
                        final int maxLevel = Integer.parseInt(nextElement2.getAttributeValue("max_level"));
                        final boolean skill2 = nextElement2.getAttributeValue("skill") != null && Boolean.parseBoolean(nextElement2.getAttributeValue("skill"));
                        final AbsorbType absorbType = AbsorbType.valueOf(nextElement2.getAttributeValue("type"));
                        template.addAbsorbInfo(new AbsorbInfo(skill2, absorbType, chance, cursedChance, minLevel, maxLevel));
                    });
                } else {
                    if (!"teleportlist".equalsIgnoreCase(nodeName)) {
                        return;
                    }
                    secondElement.getChildren().forEach(subListElement -> {
                        final int id = Integer.parseInt(subListElement.getAttributeValue("id"));
                        final List<TeleportLocation> list2 = new ArrayList<>();
                        subListElement.getChildren().forEach(targetElement -> {
                            final int itemId2 = Integer.parseInt(targetElement.getAttributeValue("item_id", "57"));
                            final long price = Integer.parseInt(targetElement.getAttributeValue("price"));
                            final int minLevel2 = Integer.parseInt(targetElement.getAttributeValue("min_level", "0"));
                            final int maxLevel2 = Integer.parseInt(targetElement.getAttributeValue("max_level", "0"));
                            final String nameCustomStringAddr = targetElement.getAttributeValue("name").trim();
                            final int castleId = Integer.parseInt(targetElement.getAttributeValue("castle_id", "0"));
                            final TeleportLocation loc = new TeleportLocation(itemId2, price, minLevel2, maxLevel2, nameCustomStringAddr, castleId);
                            loc.set(Location.parseLoc(targetElement.getAttributeValue("loc")));
                            if (minLevel2 > 0 || maxLevel2 > 0) {
                                teleportLocations.stream().filter(minMaxCheckLoc -> minMaxCheckLoc.x == loc.x && minMaxCheckLoc.y == loc.y && minMaxCheckLoc.z == loc.z).map(minMaxCheckLoc -> "Teleport location may intersect for " + targetElement.getName()).forEach(LOGGER::warn);
                            }
                            teleportLocations.add(loc);
                            list2.add(loc);
                        });
                        template.addTeleportList(id, list2.toArray(new TeleportLocation[0]));
                    });
                }
            });
            holder.addTemplate(template);
        });
    }

    private RewardData parseReward(final int npcId, final Element rewardElement, final RewardType rewardType) {
        final int itemId = Integer.parseInt(rewardElement.getAttributeValue("item_id"));
        if (rewardType == RewardType.SWEEP) {
            if (ArrayUtils.contains(Config.NO_DROP_ITEMS_FOR_SWEEP, itemId)) {
                return null;
            }
        } else if (ArrayUtils.contains(Config.NO_DROP_ITEMS, itemId)) {
            return null;
        }
        final int min = Integer.parseInt(rewardElement.getAttributeValue("min"));
        final int max = Integer.parseInt(rewardElement.getAttributeValue("max"));
        if (max < min) {
            warn("Maximum count : " + max + " smaller than minimum count : " + min + " for npcId : " + npcId);
        }
        final int chance = (int) (Double.parseDouble(rewardElement.getAttributeValue("chance")) * 10000.0);
        final RewardData data = new RewardData(itemId);
        if (data.getItem().isHerb()) {
            data.setChance(chance * Config.RATE_DROP_HERBS);
        } else {
            data.setChance(chance);
        }
        data.setMinDrop(min);
        data.setMaxDrop(max);
        return data;
    }

    private static class LazyHolder {
        protected static final NpcTemplateParser INSTANCE = new NpcTemplateParser();
    }
}
