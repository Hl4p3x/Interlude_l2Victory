package ru.j2dev.gameserver.skills;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.stats.StatTemplate;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.conditions.*;
import ru.j2dev.gameserver.stats.conditions.ConditionGameTime.CheckGameTime;
import ru.j2dev.gameserver.stats.conditions.ConditionPlayerRiding.CheckPlayerRiding;
import ru.j2dev.gameserver.stats.conditions.ConditionPlayerState.CheckPlayerState;
import ru.j2dev.gameserver.stats.funcs.EFunction;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;
import ru.j2dev.gameserver.stats.triggers.TriggerInfo;
import ru.j2dev.gameserver.stats.triggers.TriggerType;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.ArmorTemplate.ArmorType;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.utils.PositionUtils.TargetDirection;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.StringTokenizer;

@Deprecated
abstract class DocumentBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentBase.class);

    private final File file;

    DocumentBase(final File file) {
        this.file = file;
    }

    Document parse() {
        Document doc;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            doc = factory.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            LOGGER.error("Error loading file " + file, e);
            return null;
        }
        try {
            parseDocument(doc);
        } catch (Exception e) {
            LOGGER.error("Error in file " + file, e);
            return null;
        }
        return doc;
    }

    protected abstract void parseDocument(final Document p0);

    protected abstract Object getTableValue(final String p0);

    protected abstract Object getTableValue(final String p0, final int p1);

    protected void parseTemplate(Node n, final StatTemplate template) {
        n = n.getFirstChild();
        if (n == null) {
            return;
        }
        while (n != null) {
            if (n.getNodeType() != 3) {
                final String nodeName = n.getNodeName();
                if (EFunction.VALUES_BY_LOWER_NAME.containsKey(nodeName.toLowerCase())) {
                    attachFunc(n, template, EFunction.VALUES_BY_LOWER_NAME.get(nodeName.toLowerCase()));
                } else if ("effect".equalsIgnoreCase(nodeName)) {
                    if (template instanceof EffectTemplate) {
                        throw new RuntimeException("Nested effects");
                    }
                    attachEffect(n, template);
                } else {
                    if (!(template instanceof EffectTemplate)) {
                        throw new RuntimeException("Unknown template " + nodeName);
                    }
                    if ("def".equalsIgnoreCase(nodeName)) {
                        final EffectTemplate effectTemplate = (EffectTemplate) template;
                        final StatsSet effectTemplateParamsSet = effectTemplate.getParam();
                        final Skill skill = (Skill) effectTemplateParamsSet.getObject("object");
                        parseBeanSet(n, effectTemplateParamsSet, skill.getLevel());
                    } else {
                        final Condition cond = parseCondition(n);
                        if (cond != null) {
                            ((EffectTemplate) template).attachCond(cond);
                        }
                    }
                }
            }
            n = n.getNextSibling();
        }
    }

    protected void parseTrigger(Node n, final StatTemplate template) {
        NamedNodeMap map;
        int id;
        int level;
        TriggerType t;
        double chance;
        TriggerInfo trigger;
        Node n2;
        Condition condition;
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("trigger".equalsIgnoreCase(n.getNodeName())) {
                map = n.getAttributes();
                id = parseNumber(map.getNamedItem("id").getNodeValue()).intValue();
                level = parseNumber(map.getNamedItem("level").getNodeValue()).intValue();
                t = TriggerType.valueOf(map.getNamedItem("type").getNodeValue());
                chance = parseNumber(map.getNamedItem("chance").getNodeValue()).doubleValue();
                trigger = new TriggerInfo(id, level, t, chance);
                template.addTrigger(trigger);
                for (n2 = n.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
                    condition = parseCondition(n.getFirstChild());
                    if (condition != null) {
                        trigger.addCondition(condition);
                    }
                }
            }
        }
    }

    protected void attachFunc(final Node n, final StatTemplate template, final String name) {
        final Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
        final String order = n.getAttributes().getNamedItem("order").getNodeValue();
        final int ord = parseNumber(order).intValue();
        final Condition applyCond = parseCondition(n.getFirstChild());
        double val = 0.0;
        if (n.getAttributes().getNamedItem("val") != null) {
            val = parseNumber(n.getAttributes().getNamedItem("val").getNodeValue()).doubleValue();
        }
        template.attachFunc(new FuncTemplate(applyCond, name, stat, ord, val));
    }

    protected void attachFunc(final Node n, final StatTemplate template, final EFunction func) {
        final Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
        final String order = n.getAttributes().getNamedItem("order").getNodeValue();
        final int ord = parseNumber(order).intValue();
        final Condition applyCond = parseCondition(n.getFirstChild());
        double val = 0.0;
        if (n.getAttributes().getNamedItem("val") != null) {
            val = parseNumber(n.getAttributes().getNamedItem("val").getNodeValue()).doubleValue();
        }
        template.attachFunc(new FuncTemplate(applyCond, func, stat, ord, val));
    }

    protected void attachEffect(final Node n, final Object template) {
        final NamedNodeMap attrs = n.getAttributes();
        final StatsSet set = new StatsSet();
        set.set("name", attrs.getNamedItem("name").getNodeValue());
        set.set("object", template);
        if (attrs.getNamedItem("count") != null) {
            set.set("count", parseNumber(attrs.getNamedItem("count").getNodeValue()).intValue());
        }
        if (attrs.getNamedItem("time") != null) {
            set.set("time", parseNumber(attrs.getNamedItem("time").getNodeValue()).intValue());
        }
        set.set("value", (attrs.getNamedItem("val") != null) ? parseNumber(attrs.getNamedItem("val").getNodeValue()).doubleValue() : 0.0);
        set.set("abnormal", AbnormalEffect.NULL);
        set.set("abnormal2", AbnormalEffect.NULL);
        set.set("abnormal3", AbnormalEffect.NULL);
        if (attrs.getNamedItem("abnormal") != null) {
            final AbnormalEffect ae = AbnormalEffect.getByName(attrs.getNamedItem("abnormal").getNodeValue());
            if (ae.isSpecial()) {
                set.set("abnormal2", ae);
            }
            if (ae.isEvent()) {
                set.set("abnormal3", ae);
            } else {
                set.set("abnormal", ae);
            }
        }
        if (attrs.getNamedItem("stackType") != null) {
            set.set("stackType", attrs.getNamedItem("stackType").getNodeValue());
        }
        if (attrs.getNamedItem("stackType2") != null) {
            set.set("stackType2", attrs.getNamedItem("stackType2").getNodeValue());
        }
        if (attrs.getNamedItem("stackOrder") != null) {
            set.set("stackOrder", parseNumber(attrs.getNamedItem("stackOrder").getNodeValue()).intValue());
        }
        if (attrs.getNamedItem("applyOnCaster") != null) {
            set.set("applyOnCaster", Boolean.valueOf(attrs.getNamedItem("applyOnCaster").getNodeValue()));
        }
        if (attrs.getNamedItem("applyOnSummon") != null) {
            set.set("applyOnSummon", Boolean.valueOf(attrs.getNamedItem("applyOnSummon").getNodeValue()));
        }
        if (attrs.getNamedItem("displayId") != null) {
            set.set("displayId", parseNumber(attrs.getNamedItem("displayId").getNodeValue()).intValue());
        }
        if (attrs.getNamedItem("displayLevel") != null) {
            set.set("displayLevel", parseNumber(attrs.getNamedItem("displayLevel").getNodeValue()).intValue());
        }
        if (attrs.getNamedItem("chance") != null) {
            set.set("chance", parseNumber(attrs.getNamedItem("chance").getNodeValue()).intValue());
        }
        if (attrs.getNamedItem("cancelOnAction") != null) {
            set.set("cancelOnAction", Boolean.valueOf(attrs.getNamedItem("cancelOnAction").getNodeValue()));
        }
        if (attrs.getNamedItem("isOffensive") != null) {
            set.set("isOffensive", Boolean.valueOf(attrs.getNamedItem("isOffensive").getNodeValue()));
        }
        if (attrs.getNamedItem("isReflectable") != null) {
            set.set("isReflectable", Boolean.valueOf(attrs.getNamedItem("isReflectable").getNodeValue()));
        }
        final EffectTemplate lt = new EffectTemplate(set);
        parseTemplate(n, lt);
        for (Node n2 = n.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
            if ("triggers".equalsIgnoreCase(n2.getNodeName())) {
                parseTrigger(n2, lt);
            }
        }
        if (template instanceof Skill) {
            ((Skill) template).attach(lt);
        }
    }

    protected Condition parseCondition(Node n) {
        while (n != null && n.getNodeType() != 1) {
            n = n.getNextSibling();
        }
        if (n == null) {
            return null;
        }
        if ("and".equalsIgnoreCase(n.getNodeName())) {
            return parseLogicAnd(n);
        }
        if ("or".equalsIgnoreCase(n.getNodeName())) {
            return parseLogicOr(n);
        }
        if ("not".equalsIgnoreCase(n.getNodeName())) {
            return parseLogicNot(n);
        }
        if ("player".equalsIgnoreCase(n.getNodeName())) {
            return parsePlayerCondition(n);
        }
        if ("target".equalsIgnoreCase(n.getNodeName())) {
            return parseTargetCondition(n);
        }
        if ("has".equalsIgnoreCase(n.getNodeName())) {
            return parseHasCondition(n);
        }
        if ("using".equalsIgnoreCase(n.getNodeName())) {
            return parseUsingCondition(n);
        }
        if ("game".equalsIgnoreCase(n.getNodeName())) {
            return parseGameCondition(n);
        }
        if ("zone".equalsIgnoreCase(n.getNodeName())) {
            return parseZoneCondition(n);
        }
        return null;
    }

    protected Condition parseLogicAnd(Node n) {
        final ConditionLogicAnd cond = new ConditionLogicAnd();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1) {
                cond.add(parseCondition(n));
            }
        }
        if (cond._conditions == null || cond._conditions.length == 0) {
            LOGGER.error("Empty <and> condition in " + file);
        }
        return cond;
    }

    protected Condition parseLogicOr(Node n) {
        final ConditionLogicOr cond = new ConditionLogicOr();
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1) {
                cond.add(parseCondition(n));
            }
        }
        if (cond._conditions == null || cond._conditions.length == 0) {
            LOGGER.error("Empty <or> condition in " + file);
        }
        return cond;
    }

    protected Condition parseLogicNot(Node n) {
        for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1) {
                return new ConditionLogicNot(parseCondition(n));
            }
        }
        LOGGER.error("Empty <not> condition in " + file);
        return null;
    }

    protected Condition parsePlayerCondition(final Node n) {
        Condition cond = null;
        final NamedNodeMap attrs = n.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            final Node a = attrs.item(i);
            final String nodeName = a.getNodeName();
            if ("race".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionPlayerRace(a.getNodeValue()));
            } else if ("minLevel".equalsIgnoreCase(nodeName)) {
                final int lvl = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerMinLevel(lvl));
            } else if ("summon_siege_golem".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionPlayerSummonSiegeGolem());
            } else if ("maxLevel".equalsIgnoreCase(nodeName)) {
                final int lvl = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerMaxLevel(lvl));
            } else if ("maxPK".equalsIgnoreCase(nodeName)) {
                final int pk = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerMaxPK(pk));
            } else if ("resting".equalsIgnoreCase(nodeName)) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RESTING, val));
            } else if ("moving".equalsIgnoreCase(nodeName)) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.MOVING, val));
            } else if ("running".equalsIgnoreCase(nodeName)) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RUNNING, val));
            } else if ("standing".equalsIgnoreCase(nodeName)) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.STANDING, val));
            } else if ("flying".equalsIgnoreCase(a.getNodeName())) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.FLYING, val));
            } else if ("flyingTransform".equalsIgnoreCase(a.getNodeName())) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.FLYING_TRANSFORM, val));
            } else if ("olympiad".equalsIgnoreCase(a.getNodeName())) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerOlympiad(val));
            } else if ("on_pvp_event".equalsIgnoreCase(a.getNodeName())) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerInTeam(val));
            } else if ("is_hero".equalsIgnoreCase(nodeName)) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerIsHero(val));
            } else if ("on_pvp_event".equalsIgnoreCase(nodeName)) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerInTeam(val));
            } else if ("class_is_mage".equalsIgnoreCase(nodeName)) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerClassIsMage(val));
            } else if ("min_pledge_rank".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionClanPlayerMinPledgeRank(a.getNodeValue()));
            } else if ("percentHP".equalsIgnoreCase(nodeName)) {
                final int percentHP = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerPercentHp(percentHP));
            } else if ("percentMP".equalsIgnoreCase(nodeName)) {
                final int percentMP = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerPercentMp(percentMP));
            } else if ("percentCP".equalsIgnoreCase(nodeName)) {
                final int percentCP = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerPercentCp(percentCP));
            } else if ("chargesMin".equalsIgnoreCase(nodeName)) {
                final int val2 = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerChargesMin(val2));
            } else if ("chargesMax".equalsIgnoreCase(nodeName)) {
                final int val2 = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerChargesMax(val2));
            } else if ("cubic".equalsIgnoreCase(nodeName)) {
                final int cubicId = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerCubic(cubicId));
            } else if ("instance_zone".equalsIgnoreCase(nodeName)) {
                final int id = parseNumber(a.getNodeValue()).intValue();
                cond = joinAnd(cond, new ConditionPlayerInstanceZone(id));
            } else if ("riding".equalsIgnoreCase(nodeName)) {
                final String riding = a.getNodeValue();
                if ("strider".equalsIgnoreCase(riding)) {
                    cond = joinAnd(cond, new ConditionPlayerRiding(CheckPlayerRiding.STRIDER));
                } else if ("wyvern".equalsIgnoreCase(riding)) {
                    cond = joinAnd(cond, new ConditionPlayerRiding(CheckPlayerRiding.WYVERN));
                } else if ("none".equalsIgnoreCase(riding)) {
                    cond = joinAnd(cond, new ConditionPlayerRiding(CheckPlayerRiding.NONE));
                }
            } else if ("classId".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionPlayerClassId(a.getNodeValue().split(",")));
            } else if ("gender".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionPlayerGender(a.getNodeValue()));
            } else if ("hasBuffId".equalsIgnoreCase(nodeName)) {
                final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
                final int id2 = Integer.parseInt(st.nextToken().trim());
                int level = -1;
                if (st.hasMoreTokens()) {
                    level = Integer.parseInt(st.nextToken().trim());
                }
                cond = joinAnd(cond, new ConditionPlayerHasBuffId(id2, level));
            } else if ("hasBuff".equalsIgnoreCase(nodeName)) {
                final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
                final EffectType et = Enum.valueOf(EffectType.class, st.nextToken().trim());
                int level = -1;
                if (st.hasMoreTokens()) {
                    level = Integer.parseInt(st.nextToken().trim());
                }
                cond = joinAnd(cond, new ConditionPlayerHasBuff(et, level));
            } else if ("damage".equalsIgnoreCase(nodeName)) {
                final String[] st2 = a.getNodeValue().split(";");
                cond = joinAnd(cond, new ConditionPlayerMinMaxDamage(Double.parseDouble(st2[0]), Double.parseDouble(st2[1])));
            } else if ("skillMinSeed".equalsIgnoreCase(nodeName)) {
                final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
                final int skillId = Integer.parseInt(st.nextToken().trim());
                final int skillMinSeed = Integer.parseInt(st.nextToken().trim());
                cond = joinAnd(cond, new ConditionPlayerSkillMinSeed(skillId, skillMinSeed));
            }
        }
        if (cond == null) {
            LOGGER.error("Unrecognized <player> condition in " + file);
        }
        return cond;
    }

    protected Condition parseTargetCondition(final Node n) {
        Condition cond = null;
        final NamedNodeMap attrs = n.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            final Node a = attrs.item(i);
            final String nodeName = a.getNodeName();
            final String nodeValue = a.getNodeValue();
            if ("aggro".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetAggro(Boolean.valueOf(nodeValue)));
            } else if ("pvp".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetPlayable(Boolean.valueOf(nodeValue)));
            } else if ("player".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetPlayer(Boolean.valueOf(nodeValue)));
            } else if ("exclude_caster".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetPlayerNotMe(Boolean.valueOf(nodeValue)));
            } else if ("summon".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetSummon(Boolean.valueOf(nodeValue)));
            } else if ("mob".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetMob(Boolean.valueOf(nodeValue)));
            } else if ("targetInTheSameParty".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetInTheSameParty(Boolean.valueOf(nodeValue)));
            } else if ("mobId".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetMobId(Integer.parseInt(nodeValue)));
            } else if ("race".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetRace(nodeValue));
            } else if ("npc_class".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetNpcClass(nodeValue));
            } else if ("playerRace".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetPlayerRace(nodeValue));
            } else if ("forbiddenClassIds".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetForbiddenClassId(nodeValue.split(";")));
            } else if ("playerSameClan".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetClan(nodeValue));
            } else if ("castledoor".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetCastleDoor(Boolean.valueOf(nodeValue)));
            } else if ("direction".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetDirection(TargetDirection.valueOf(nodeValue.toUpperCase())));
            } else if ("percentHP".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetPercentHp(parseNumber(a.getNodeValue()).intValue()));
            } else if ("percentMP".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetPercentMp(parseNumber(a.getNodeValue()).intValue()));
            } else if ("percentCP".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetPercentCp(parseNumber(a.getNodeValue()).intValue()));
            } else if ("hasBuffId".equalsIgnoreCase(nodeName)) {
                final StringTokenizer st = new StringTokenizer(nodeValue, ";");
                final int id = Integer.parseInt(st.nextToken().trim());
                int level = -1;
                if (st.hasMoreTokens()) {
                    level = Integer.parseInt(st.nextToken().trim());
                }
                cond = joinAnd(cond, new ConditionTargetHasBuffId(id, level));
            } else if ("hasBuff".equalsIgnoreCase(nodeName)) {
                final StringTokenizer st = new StringTokenizer(nodeValue, ";");
                final EffectType et = Enum.valueOf(EffectType.class, st.nextToken().trim());
                int level = -1;
                if (st.hasMoreTokens()) {
                    level = Integer.parseInt(st.nextToken().trim());
                }
                cond = joinAnd(cond, new ConditionTargetHasBuff(et, level));
            } else if ("hasForbiddenSkill".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionTargetHasForbiddenSkill(parseNumber(a.getNodeValue()).intValue()));
            }
        }
        if (cond == null) {
            LOGGER.error("Unrecognized <target> condition in " + file);
        }
        return cond;
    }

    protected Condition parseUsingCondition(final Node n) {
        Condition cond = null;
        final NamedNodeMap attrs = n.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            final Node a = attrs.item(i);
            final String nodeName = a.getNodeName();
            final String nodeValue = a.getNodeValue();
            if ("kind".equalsIgnoreCase(nodeName) || "weapon".equalsIgnoreCase(nodeName)) {
                long mask = 0L;
                final StringTokenizer st = new StringTokenizer(nodeValue, ",");
                Label_0089:
                while (st.hasMoreTokens()) {
                    final String item = st.nextToken().trim();
                    for (final WeaponType wt : WeaponType.VALUES) {
                        if (wt.toString().equalsIgnoreCase(item)) {
                            mask |= wt.mask();
                            continue Label_0089;
                        }
                    }
                    for (final ArmorType at : ArmorType.VALUES) {
                        if (at.toString().equalsIgnoreCase(item)) {
                            mask |= at.mask();
                            continue Label_0089;
                        }
                    }
                    LOGGER.error("Invalid item kind: \"" + item + "\" in " + file);
                }
                if (mask != 0L) {
                    cond = joinAnd(cond, new ConditionUsingItemType(mask));
                }
            } else if ("armor".equalsIgnoreCase(nodeName)) {
                final ArmorType armor = ArmorType.valueOf(nodeValue.toUpperCase());
                cond = joinAnd(cond, new ConditionUsingArmor(armor));
            } else if ("skill".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionUsingSkill(Integer.parseInt(nodeValue)));
            } else if ("blowskill".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionUsingBlowSkill(Boolean.parseBoolean(nodeValue)));
            } else if ("slotitem".equalsIgnoreCase(nodeName)) {
                final StringTokenizer st2 = new StringTokenizer(nodeValue, ";");
                final int id = Integer.parseInt(st2.nextToken().trim());
                final int slot = Integer.parseInt(st2.nextToken().trim());
                int enchant = 0;
                if (st2.hasMoreTokens()) {
                    enchant = Integer.parseInt(st2.nextToken().trim());
                }
                cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
            }
        }
        if (cond == null) {
            LOGGER.error("Unrecognized <using> condition in " + file);
        }
        return cond;
    }

    protected Condition parseHasCondition(final Node n) {
        Condition cond = null;
        final NamedNodeMap attrs = n.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            final Node a = attrs.item(i);
            final String nodeName = a.getNodeName();
            final String nodeValue = a.getNodeValue();
            if ("skill".equalsIgnoreCase(nodeName)) {
                final StringTokenizer st = new StringTokenizer(nodeValue, ";");
                final Integer id = parseNumber(st.nextToken().trim()).intValue();
                final int level = parseNumber(st.nextToken().trim()).shortValue();
                cond = joinAnd(cond, new ConditionHasSkill(id, level));
            } else if ("success".equalsIgnoreCase(nodeName)) {
                cond = joinAnd(cond, new ConditionFirstEffectSuccess(Boolean.valueOf(nodeValue)));
            }
        }
        if (cond == null) {
            LOGGER.error("Unrecognized <has> condition in " + file);
        }
        return cond;
    }

    protected Condition parseGameCondition(final Node n) {
        Condition cond = null;
        final NamedNodeMap attrs = n.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            final Node a = attrs.item(i);
            if ("night".equalsIgnoreCase(a.getNodeName())) {
                final boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
            }
        }
        if (cond == null) {
            LOGGER.error("Unrecognized <game> condition in " + file);
        }
        return cond;
    }

    protected Condition parseZoneCondition(final Node n) {
        Condition cond = null;
        final NamedNodeMap attrs = n.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            final Node a = attrs.item(i);
            if ("type".equalsIgnoreCase(a.getNodeName())) {
                cond = joinAnd(cond, new ConditionZoneType(a.getNodeValue()));
            } else if ("name".equalsIgnoreCase(a.getNodeName())) {
                cond = joinAnd(cond, new ConditionZoneName(a.getNodeValue()));
            }
        }
        if (cond == null) {
            LOGGER.error("Unrecognized <zone> condition in " + file);
        }
        return cond;
    }

    protected void parseBeanSet(final Node n, final StatsSet set, final int level) {
        try {
            final String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
            String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
            final char ch = (value.length() == 0) ? ' ' : value.charAt(0);
            if (value.contains("#") && ch != '#') {
                for (final String str : value.split("[;: ]+")) {
                    if (str.charAt(0) == '#') {
                        value = value.replace(str, String.valueOf(getTableValue(str, level)));
                    }
                }
            }
            if (ch == '#') {
                final Object tableVal = getTableValue(value, level);
                final Number parsedVal = parseNumber(tableVal.toString());
                set.set(name, (parsedVal == null) ? tableVal : String.valueOf(parsedVal));
            } else if ((Character.isDigit(ch) || ch == '-') && !value.contains(" ") && !value.contains(";")) {
                set.set(name, String.valueOf(parseNumber(value)));
            } else {
                set.set(name, value);
            }
        } catch (Exception e) {
            System.out.println(n.getAttributes().getNamedItem("name") + " " + set.get("skill_id"));
            e.printStackTrace();
        }
    }

    protected Number parseNumber(String value) {
        if (value.charAt(0) == '#') {
            value = getTableValue(value).toString();
        }
        try {
            if ("max".equalsIgnoreCase(value)) {
                return Double.POSITIVE_INFINITY;
            }
            if ("min".equalsIgnoreCase(value)) {
                return Double.NEGATIVE_INFINITY;
            }
            if (value.indexOf(46) == -1) {
                int radix = 10;
                if (value.length() > 2 && "0x".equalsIgnoreCase(value.substring(0, 2))) {
                    value = value.substring(2);
                    radix = 16;
                }
                return Integer.valueOf(value, radix);
            }
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Condition joinAnd(final Condition cond, final Condition c) {
        if (cond == null) {
            return c;
        }
        if (cond instanceof ConditionLogicAnd) {
            ((ConditionLogicAnd) cond).add(c);
            return cond;
        }
        final ConditionLogicAnd and = new ConditionLogicAnd();
        and.add(cond);
        and.add(c);
        return and;
    }
}
