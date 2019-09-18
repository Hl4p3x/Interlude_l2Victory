package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Attribute;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.entity.residence.ResidenceType;
import ru.j2dev.gameserver.stats.StatTemplate;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.conditions.*;
import ru.j2dev.gameserver.stats.funcs.EFunction;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;
import ru.j2dev.gameserver.stats.triggers.TriggerInfo;
import ru.j2dev.gameserver.stats.triggers.TriggerType;
import ru.j2dev.gameserver.templates.item.ArmorTemplate.ArmorType;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;

import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public abstract class StatParser<H extends AbstractHolder> extends AbstractDirParser<H> {
    protected StatParser(final H holder) {
        super(holder);
    }

    protected Condition parseFirstCond(final Element sub) {
        final List<Element> e = sub.getChildren();
        if (e.isEmpty()) {
            return null;
        }
        final Element element = e.get(0);
        return parseCond(element);
    }

    protected Condition parseCond(final Element element) {
        final String name = element.getName();
        if ("and".equalsIgnoreCase(name)) {
            return parseLogicAnd(element);
        }
        if ("or".equalsIgnoreCase(name)) {
            return parseLogicOr(element);
        }
        if ("not".equalsIgnoreCase(name)) {
            return parseLogicNot(element);
        }
        if ("target".equalsIgnoreCase(name)) {
            return parseTargetCondition(element);
        }
        if ("player".equalsIgnoreCase(name)) {
            return parsePlayerCondition(element);
        }
        if ("using".equalsIgnoreCase(name)) {
            return parseUsingCondition(element);
        }
        if ("zone".equalsIgnoreCase(name)) {
            return parseZoneCondition(element);
        }
        return null;
    }

    protected Condition parseLogicAnd(final Element n) {
        final ConditionLogicAnd cond = new ConditionLogicAnd();
        n.getChildren().stream().map(this::parseCond).forEach(cond::add);
        if (cond._conditions == null || cond._conditions.length == 0) {
            error("Empty <and> condition in " + getCurrentFileName());
        }
        return cond;
    }

    protected Condition parseLogicOr(final Element n) {
        final ConditionLogicOr cond = new ConditionLogicOr();
        n.getChildren().stream().map(this::parseCond).forEach(cond::add);
        if (cond._conditions == null || cond._conditions.length == 0) {
            error("Empty <or> condition in " + getCurrentFileName());
        }
        return cond;
    }

    protected Condition parseLogicNot(final Element n) {

        for (final Element element : n.getChildren()) {
            return new ConditionLogicNot(parseCond(element));
        }
        error("Empty <not> condition in " + getCurrentFileName());
        return null;
    }

    protected Condition parseTargetCondition(final Element element) {
        Condition cond = null;
        for (final Attribute attribute : element.getAttributes()) {
            final String name = attribute.getName();
            final String value = attribute.getValue();
            if ("pvp".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionTargetPlayable(Boolean.valueOf(value)));
            }
        }
        return cond;
    }

    protected Condition parseZoneCondition(final Element element) {
        Condition cond = null;
        for (final Attribute attribute : element.getAttributes()) {
            final String name = attribute.getName();
            final String value = attribute.getValue();
            if ("type".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionZoneType(value));
            }
        }
        return cond;
    }

    protected Condition parsePlayerCondition(final Element element) {
        Condition cond = null;
        for (final Attribute attribute : element.getAttributes()) {
            final String name = attribute.getName();
            final String value = attribute.getValue();
            if ("residence".equalsIgnoreCase(name)) {
                final String[] st = value.split(";");
                cond = joinAnd(cond, new ConditionPlayerResidence(Integer.parseInt(st[1]), ResidenceType.valueOf(st[0])));
            } else if ("classId".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerClassId(value.split(",")));
            } else if ("olympiad".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerOlympiad(Boolean.valueOf(value)));
            } else if ("min_pledge_rank".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionClanPlayerMinPledgeRank(value));
            } else if ("is_hero".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerIsHero(Boolean.parseBoolean(value)));
            } else if ("on_pvp_event".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerInTeam(Boolean.parseBoolean(value)));
            } else if ("class_is_mage".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerClassIsMage(Boolean.parseBoolean(value)));
            } else if ("instance_zone".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerInstanceZone(Integer.parseInt(value)));
            } else if ("minLevel".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerMinLevel(Integer.parseInt(value)));
            } else if ("maxLevel".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerMaxLevel(Integer.parseInt(value)));
            } else if ("race".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerRace(value));
            } else if ("gender".equalsIgnoreCase(name)) {
                cond = joinAnd(cond, new ConditionPlayerGender(value));
            } else {
                if (!"damage".equalsIgnoreCase(name)) {
                    continue;
                }
                final String[] st = value.split(";");
                cond = joinAnd(cond, new ConditionPlayerMinMaxDamage(Double.parseDouble(st[0]), Double.parseDouble(st[1])));
            }
        }
        return cond;
    }

    protected Condition parseUsingCondition(final Element element) {
        Condition cond = null;
        for (final Attribute attribute : element.getAttributes()) {
            final String name = attribute.getName();
            final String value = attribute.getValue();
            if ("slotitem".equalsIgnoreCase(name)) {
                final StringTokenizer st = new StringTokenizer(value, ";");
                final int id = Integer.parseInt(st.nextToken().trim());
                final int slot = Integer.parseInt(st.nextToken().trim());
                int enchant = 0;
                if (st.hasMoreTokens()) {
                    enchant = Integer.parseInt(st.nextToken().trim());
                }
                cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
            } else if ("kind".equalsIgnoreCase(name) || "weapon".equalsIgnoreCase(name)) {
                long mask = 0L;
                final StringTokenizer st2 = new StringTokenizer(value, ",");
                tokens:
                while (st2.hasMoreTokens()) {
                    final String item = st2.nextToken().trim();
                    for (final WeaponType wt : WeaponType.VALUES) {
                        if (wt.toString().equalsIgnoreCase(item)) {
                            mask |= wt.mask();
                            continue tokens;
                        }
                    }
                    for (final ArmorType at : ArmorType.VALUES) {
                        if (at.toString().equalsIgnoreCase(item)) {
                            mask |= at.mask();
                            continue tokens;
                        }
                    }
                    error("Invalid item kind: \"" + item + "\" in " + getCurrentFileName());
                }
                if (mask == 0L) {
                    continue;
                }
                cond = joinAnd(cond, new ConditionUsingItemType(mask));
            } else {
                if (!"skill".equalsIgnoreCase(name)) {
                    continue;
                }
                cond = joinAnd(cond, new ConditionUsingSkill(Integer.parseInt(value)));
            }
        }
        return cond;
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

    protected void parseFor(final Element forElement, final StatTemplate template) {
        forElement.getChildren().forEach(element -> {
            final String elementName = element.getName();
            final EFunction func = EFunction.VALUES_BY_LOWER_NAME.get(elementName.toLowerCase());
            if (null == func) {
                throw new RuntimeException("Unknown function specified '" + elementName + "'");
            }
            attachFunc(element, template, func);
        });
    }

    protected void parseTriggers(final Element f, final StatTemplate triggerable) {
        f.getChildren().forEach(element -> {
            final int id = parseNumber(element.getAttributeValue("id")).intValue();
            final int level = parseNumber(element.getAttributeValue("level")).intValue();
            final TriggerType t = TriggerType.valueOf(element.getAttributeValue("type"));
            final double chance = parseNumber(element.getAttributeValue("chance")).doubleValue();
            final TriggerInfo trigger = new TriggerInfo(id, level, t, chance);
            triggerable.addTrigger(trigger);
            element.getChildren().stream().map(this::parseFirstCond).filter(Objects::nonNull).forEach(trigger::addCondition);
        });
    }

    protected void attachFunc(final Element n, final StatTemplate template, final String name) {
        final Stats stat = Stats.valueOfXml(n.getAttributeValue("stat"));
        final String order = n.getAttributeValue("order");
        final int ord = parseNumber(order).intValue();
        final Condition applyCond = parseFirstCond(n);
        double val = 0.0;
        if (n.getAttributeValue("value") != null) {
            val = parseNumber(n.getAttributeValue("value")).doubleValue();
        }
        template.attachFunc(new FuncTemplate(applyCond, name, stat, ord, val));
    }

    protected void attachFunc(final Element n, final StatTemplate template, final EFunction func) {
        final Stats stat = Stats.valueOfXml(n.getAttributeValue("stat"));
        final String order = n.getAttributeValue("order");
        final int ord = parseNumber(order).intValue();
        final Condition applyCond = parseFirstCond(n);
        double val = 0.0;
        if (n.getAttributeValue("value") != null) {
            val = parseNumber(n.getAttributeValue("value")).doubleValue();
        }
        template.attachFunc(new FuncTemplate(applyCond, func, stat, ord, val));
    }

    protected Number parseNumber(String value) {
        if (value.charAt(0) == '#') {
            value = getTableValue(value).toString();
        }
        try {
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

    protected abstract Object getTableValue(final String p0);
}
